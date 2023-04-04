package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.DBManager;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivitySettingsBinding;
import am.softlab.arfinance.datasets.SettingsTable;
import am.softlab.arfinance.models.ModelSettings;
import am.softlab.arfinance.utils.ActivityUtils;

public class SettingsActivity extends AppCompatActivity {
    private int mMaxPieSectors;

    //view binding
    private ActivitySettingsBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //resources
    private Resources res;
    private DBManager mDBManager;
    private ModelSettings mAFSettingsLocal;

    private static final String TAG = "SETTINGS_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();
        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //get DBManager
        mDBManager = ((MyApplication) getApplicationContext()).getDBManager();
        mAFSettingsLocal = new ModelSettings( ((MyApplication)getApplicationContext()).getAFSettings() );

        //loadSettings
        mMaxPieSectors = mAFSettingsLocal.getSt_max_pie_sectors();
        if (mMaxPieSectors < 3 || mMaxPieSectors > Constants.MAX_PIE_SECTORS_LIMIT)
            mMaxPieSectors = 3;
        binding.maxPieSectorsSl.setValue(mMaxPieSectors);
        binding.encryptionPassTiet.setText(mAFSettingsLocal.getSt_enc_pass());

        // Init Slider - maxPieSectorsTv
        String lbText = res.getString(R.string.max_pie_sectors) + ": ";
        if (mMaxPieSectors == Constants.MAX_PIE_SECTORS_LIMIT)
            lbText += res.getString(R.string.unlimited);
        else
            lbText += mMaxPieSectors;
        binding.maxPieSectorsTv.setText(lbText);
        //
        binding.maxPieSectorsSl.setValueTo(Constants.MAX_PIE_SECTORS_LIMIT);
        binding.maxPieSectorsSl.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                int currVal = Math.round(value);
                return (value == Constants.MAX_PIE_SECTORS_LIMIT) ? "~" : ""+currVal;
            }
        });
        //handle onChange, maxPieSectorsSl slider
        binding.maxPieSectorsSl.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                mMaxPieSectors = Math.round(value);

                String lbText = res.getString(R.string.max_pie_sectors) + ": ";
                if (mMaxPieSectors == Constants.MAX_PIE_SECTORS_LIMIT)
                    lbText += res.getString(R.string.unlimited);
                else
                    lbText += mMaxPieSectors;

                binding.maxPieSectorsTv.setText(lbText);
            }
        });

        //handle click, begin upload category
        binding.saveBtn.setOnClickListener(view -> validateData());

        //handle click, goBack
        binding.backBtn.setOnClickListener(view -> {
            ActivityUtils.hideKeyboardInView(this);
            onBackPressed();
        });
    }


    private void validateData() {
        ActivityUtils.hideKeyboardInView(this);

        mAFSettingsLocal.setUser_id(firebaseAuth.getCurrentUser().getUid());
        mAFSettingsLocal.setSt_max_pie_sectors(mMaxPieSectors);
        mAFSettingsLocal.setSt_enc_pass(binding.encryptionPassTiet.getText().toString());

        SettingsTable.addOrUpdateSetting(mDBManager.getDatabase(), mAFSettingsLocal);

        ((MyApplication)getApplicationContext()).updateAFSettings(mAFSettingsLocal);
        finish();
    }
}
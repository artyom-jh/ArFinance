package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;

import am.softlab.arfinance.BuildConfig;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    //resources
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_about);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        String version = res.getString(R.string.version) + ": " + BuildConfig.VERSION_NAME + " (build " + BuildConfig.VERSION_CODE + ")";
        binding.versionTv.setText(version);

        //handle click, goback
        binding.backBtn.setOnClickListener(v -> {
            MyApplication.hideKeyboard(this);
            onBackPressed();
        });
    }
}
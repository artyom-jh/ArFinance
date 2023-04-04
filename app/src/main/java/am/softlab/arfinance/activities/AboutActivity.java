package am.softlab.arfinance.activities;

import static am.softlab.arfinance.utils.ActivityUtils.hideKeyboardInView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import am.softlab.arfinance.BuildConfig;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_about);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String version = "Version: " + BuildConfig.VERSION_NAME + " (build " + BuildConfig.VERSION_CODE + ")";
        binding.versionTv.setText(version);

        binding.appNameTv.setOnLongClickListener(v -> {
            startActivity((new Intent(AboutActivity.this, AppLogViewerActivity.class)));
            return true;  //it has a return value, which should be true if long-click events are handled
        });

        //handle click, goback
        binding.backBtn.setOnClickListener(v -> {
            hideKeyboardInView(this);
            onBackPressed();
        });
    }
}
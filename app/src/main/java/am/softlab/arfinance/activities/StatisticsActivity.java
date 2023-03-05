package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import am.softlab.arfinance.databinding.ActivityStatisticsBinding;

public class StatisticsActivity extends AppCompatActivity {

    //view binding
    private ActivityStatisticsBinding binding;

    private static final String TAG = "STATISTICS_TAG";

    //resources
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //handle click, goback
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
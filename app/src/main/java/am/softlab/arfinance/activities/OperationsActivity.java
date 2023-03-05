package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityOperationsBinding;
import am.softlab.arfinance.databinding.ActivityProfileBinding;

public class OperationsActivity extends AppCompatActivity {

    //view binding
    private ActivityOperationsBinding binding;

    private Boolean isIncome;
    private static final String TAG = "OPERATIONS_TAG";

    //resources
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //actType get from intent started from DashboardActivity
        String actType = getIntent().getStringExtra("actType");
        isIncome = actType.equals("income");
        if (isIncome) {
            binding.titleTv.setText(res.getString(R.string.income));
        } else {
            binding.titleTv.setText(res.getString(R.string.expenses));
        }

        //handle click, goback
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
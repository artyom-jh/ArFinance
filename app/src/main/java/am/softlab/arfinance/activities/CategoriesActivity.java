package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import am.softlab.arfinance.databinding.ActivityCategoriesBinding;

public class CategoriesActivity extends AppCompatActivity {

    //view binding
    private ActivityCategoriesBinding binding;

    private String bookId;
    private static final String TAG = "OPERATIONS_TAG";

    //resources
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
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
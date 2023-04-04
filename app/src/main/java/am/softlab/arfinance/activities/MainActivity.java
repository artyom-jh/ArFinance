package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    //view binding
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ((MyApplication) getApplicationContext()).getDBManager().open();

        //handle loginBtn click, start login screen
        binding.loginBtn.setOnClickListener(
                v -> startActivity(new Intent(MainActivity.this, LoginActivity.class))
        );

        //handle skipBtn click, start continue without login screen
        binding.skipBtn.setOnClickListener(
                v -> startActivity( new Intent(MainActivity.this, DashboardActivity.class) )
        );
    }
}
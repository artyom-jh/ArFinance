package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.databinding.ActivityProfileBinding;
import am.softlab.arfinance.databinding.ActivitySchedulerBinding;

public class SchedulerActivity extends AppCompatActivity {

    //view binding
    private ActivitySchedulerBinding binding;

    //firebase auth, for loading user data using user uid
    private FirebaseAuth firebaseAuth;
    //firebase current user
    private FirebaseUser firebaseUser;

    //resources
    private Resources res;

    private static final String TAG = "SCHEDULER_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySchedulerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        //handle click, goback
        binding.backBtn.setOnClickListener(v -> {
            MyApplication.hideKeyboard(this);
            onBackPressed();
        });
    }
}
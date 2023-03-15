package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    //View binding
    private ActivityForgotPasswordBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //resources
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //init/setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go back
        binding.backBtn.setOnClickListener(v -> {
            MyApplication.hideKeyboard(this);
            onBackPressed();
        });

        //handle click, begin recovery password
        binding.submitBtn.setOnClickListener(v -> validateData());
    }

    private String email = "";
    private void validateData() {
        //get data i.e. email
        email = binding.emailEt.getText().toString().trim();

        //validate data e.g. shouldn't empty and should be valid format
        if(email.isEmpty()){
            Toast.makeText(this, res.getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, res.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }
        else{
            recoverPassword();
        }
    }

    private void recoverPassword() {
        MyApplication.hideKeyboard(this);

        //show progress
        progressDialog.setMessage(res.getString(R.string.sending_password) + " " + email);
        progressDialog.show();

        //begin sending recovery
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    //sent
                    progressDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this, res.getString(R.string.instructions_sent) + " " + email, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    //failed to send
                    progressDialog.dismiss();
                    Toast.makeText(ForgotPasswordActivity.this, res.getString(R.string.failed_to_send) + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
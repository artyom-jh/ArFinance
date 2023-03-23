package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import am.softlab.arfinance.BuildConfig;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityChangePasswordBinding;

public class ChangePasswordActivity extends AppCompatActivity {

    //view binding
    private ActivityChangePasswordBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //resources
    private Resources res;

    //progress dialog
    private ProgressDialog progressDialog;

    private static final String TAG = "CHANGE_PASSWORD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //handle click, go back
        binding.backBtn.setOnClickListener(view -> {
            MyApplication.hideKeyboard(this);
            finish();
        });

        //handle click, change password
        binding.changeBtn.setOnClickListener(view -> validateData());
    }

    private String oldPassword = "";
    private String newPassword = "";
    private void validateData() {
        oldPassword = binding.oldPasswordEt.getText().toString().trim();
        newPassword = binding.newPasswordEt.getText().toString().trim();
        String cPassword = binding.cNewPasswordEt.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(this, res.getString(R.string.enter_old_password), Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, res.getString(R.string.enter_new_password), Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this, res.getString(R.string.confirm_new_password_msg), Toast.LENGTH_SHORT).show();
        }
        else if (!newPassword.equals(cPassword)) {
            Toast.makeText(this, res.getString(R.string.password_not_match), Toast.LENGTH_SHORT).show();
        }
        else {
            changeUserPassword();
        }
    }

    private void changeUserPassword() {
        MyApplication.hideKeyboard(this);

        //show progress
        progressDialog.setMessage(res.getString(R.string.creating_password));
        progressDialog.show();

        FirebaseUser user = firebaseAuth.getCurrentUser();

        //change user password in firebase
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), oldPassword);

            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (BuildConfig.DEBUG)
                                Log.d(TAG, "changeUserPassword: user re-authentication success");

                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                if (BuildConfig.DEBUG)
                                                    Log.d(TAG, "changeUserPassword: User password updated");

                                                progressDialog.dismiss();
                                                Toast.makeText(ChangePasswordActivity.this, res.getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                                                firebaseAuth.signOut();
                                                startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();

                            if (BuildConfig.DEBUG)
                                Log.d(TAG, "changeUserPassword: user re-authentication failed");

                            Toast.makeText(ChangePasswordActivity.this, res.getString(R.string.reauth_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            progressDialog.dismiss();
            Toast.makeText(ChangePasswordActivity.this, res.getString(R.string.no_current_user), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
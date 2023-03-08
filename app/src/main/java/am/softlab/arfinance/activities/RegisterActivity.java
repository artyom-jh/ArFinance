package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    //view binding
    private ActivityRegisterBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //resources
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);


        //handle click, go back
        binding.backBtn.setOnClickListener(view -> onBackPressed());

        //handle click, begin register
        binding.registerBtn.setOnClickListener(view -> validateData());
    }

    private String name = "", email = "", password = "";
    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cPassword = binding.cPasswordEt.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, res.getString(R.string.enter_your_name), Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, res.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, res.getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cPassword)){
            Toast.makeText(this, res.getString(R.string.confirm_password), Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(cPassword)){
            Toast.makeText(this, res.getString(R.string.password_not_match), Toast.LENGTH_SHORT).show();
        }
        else{
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //show progress
        progressDialog.setMessage(res.getString(R.string.creating_account));
        progressDialog.show();

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    //account creation success, now add in firebase realtime database
                    updateUserInfo();

                })
                .addOnFailureListener(e -> {
                    //account creating failed
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage(res.getString(R.string.saving_user_info));

        //timetamp
        long timestamp = System.currentTimeMillis();

        //get current user uid, since user is registered so we can get now
        String uid = firebaseAuth.getUid();

        //setup data to add in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", ""); // add empty, will do later
        hashMap.put("userType", "user"); // possible values are user, admin: will make admin manually in firebase realtime database by changing this value
        hashMap.put("timestamp", timestamp);

        //set data to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    //data added to db
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, res.getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                    //since user account is created so start dashboard of user
                    startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    //data failed adding to db
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
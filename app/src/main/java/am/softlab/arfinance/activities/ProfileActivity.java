package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    //view binding
    private ActivityProfileBinding binding;

    //firebase auth, for loading user data using user uid
    private FirebaseAuth firebaseAuth;
    //firebase current user
    private FirebaseUser firebaseUser;

    //progress dialog
    private ProgressDialog progressDialog;

    //resources
    private Resources res;

    private static final String TAG = "PROFILE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //reset data of user info
        binding.accountTypeTv.setText("N/A");
        binding.memberDateTv.setText("N/A");
        binding.accountStatusTv.setText("N/A");

        //setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        //init/setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        loadUserInfo();

        //handle click, start profile edit page
        binding.profileEditBtn.setOnClickListener(
                v -> startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class))
        );

        //handle click, goBack
        binding.backBtn.setOnClickListener(v -> onBackPressed());

        //handle click, verify user if not
        binding.accountStatusTv.setOnClickListener(v -> {

            if (firebaseUser.isEmailVerified()) {
                //already verified
                Toast.makeText(ProfileActivity.this, res.getString(R.string.already_verified), Toast.LENGTH_SHORT).show();
            }
            else{
                //not verified, show confirmation dialog first
                emailVerificationDialog();
            }
        });
    }

    private void emailVerificationDialog() {
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.verify_email))
                .setMessage(res.getString(R.string.sure_send_email_verification) + " " + firebaseUser.getEmail())
                .setPositiveButton("SEND", (dialog, which) -> sendEmailVerification())
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendEmailVerification() {
        //show progress
        progressDialog.setMessage(res.getString(R.string.sending_email_verification) + " " + firebaseUser.getEmail());
        progressDialog.show();

        firebaseUser.sendEmailVerification()
                .addOnSuccessListener(unused -> {
                    //successfully sent
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, res.getString(R.string.verification_instructions_sent) + " " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    //failed to send
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, res.getString(R.string.failed_to_send) +" " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user info of user " + firebaseAuth.getUid());

        //get email verification status, after verification you have to re login to get changes...
        if(firebaseUser.isEmailVerified()){
            binding.accountStatusTv.setText(res.getString(R.string.verified));
        } else{
            binding.accountStatusTv.setText(res.getString(R.string.not_verified));
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info of user here from snapshot
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String userType = ""+snapshot.child("userType").getValue();

                        //format date to dd/MM/yyy
                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        //set data to ui
                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.memberDateTv.setText(formattedDate);
                        binding.accountTypeTv.setText(userType);

                        //set image, using glide
                        Glide.with(getApplicationContext())
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileIv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }
}
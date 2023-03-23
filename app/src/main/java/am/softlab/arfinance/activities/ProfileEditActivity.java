package am.softlab.arfinance.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import am.softlab.arfinance.BuildConfig;
import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityProfileEditBinding;

public class ProfileEditActivity extends AppCompatActivity {

    //view binding
    private ActivityProfileEditBinding binding;

    //firebase auth, get/update user data using uid
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    private static final String TAG = "PROFILE_EDIT_TAG";

    private Uri imageUri = null;
    private String name ="";

    //resources
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false); //don't dismiss while clicking outside of progress dialog

        //setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        //handle click, goBack
        binding.backBtn.setOnClickListener(v -> {
            MyApplication.hideKeyboard(this);
            onBackPressed();
        });

        //handle click, pick image
        binding.profileIv.setOnClickListener(v -> showImageAttachMenu());

        //handle click, update profile
        binding.updateBtn.setOnClickListener(v -> validateData());
    }

    private void loadUserInfo() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "loadUserInfo: Loading user info of user " + firebaseAuth.getUid());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get all info of user here from snapshot
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        //set data to ui
                        binding.nameEt.setText(name);

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

    private  void validateData() {
        //get data
        name = binding.nameEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(name)) {
            //no name is entered
            Toast.makeText(this, res.getString(R.string.enter_your_name), Toast.LENGTH_SHORT).show();
        }
        else {
            //name is entered
            if (imageUri == null) {
                //need to update without image
                updateProfile("");
            }
            else {
                //need to update with image
                uploadImage();
            }
        }
    }

    private void uploadImage() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "uploadImage: Uploading profile image...");

        progressDialog.setMessage(res.getString(R.string.updating_profile_image));
        progressDialog.show();

        //image patch and name, use uid to replace previous
        String filePathAndName = "ProfileImages/" + firebaseAuth.getUid();

        //storage reference
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onSuccess: Profile image uploaded");
                        Log.d(TAG, "onSuccess: Getting url of uploaded image");
                    }
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    //noinspection StatementWithEmptyBody
                    while (!uriTask.isSuccessful());
                    String uploadedImageUrl = ""+uriTask.getResult();

                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "onSuccess: Uploaded Image URL: " + uploadedImageUrl);

                    updateProfile(uploadedImageUrl);
                })
                .addOnFailureListener(e -> {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "onFailure: Failed to upload image due to " + e.getMessage());

                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditActivity.this, res.getString(R.string.failed_to_upload_image) + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfile(String imageUrl) {
        MyApplication.hideKeyboard(this);

        if (BuildConfig.DEBUG)
            Log.d(TAG, "updateProfile: Updating user profile");

        progressDialog.setMessage(res.getString(R.string.updating_user_profile));
        progressDialog.show();

        //setup data to update in db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", ""+name);
        if (imageUri != null) {
            hashMap.put("profileImage", ""+imageUrl);
        }

        //update data to db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "onSuccess: Profile updated...");

                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditActivity.this, res.getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, "onFailure: Failed to update db due to " + e.getMessage());

                    progressDialog.dismiss();
                    Toast.makeText(ProfileEditActivity.this, res.getString(R.string.failed_to_update_db) + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> finish());
    }

    private void showImageAttachMenu() {
        //init/setup popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.profileIv);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, res.getString(R.string.camera));
        popupMenu.getMenu().add(Menu.NONE, 1, 1, res.getString(R.string.gallery));

        popupMenu.show();

        //handle menu item click
        popupMenu.setOnMenuItemClickListener(item -> {
            //get id of item clicked
            int whichItemClicked = item.getItemId();
            if (whichItemClicked == 0) {
                //camera clicked
                if ( MyApplication.checkPermission(
                        ProfileEditActivity.this,
                        new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        Constants.CAMERA_PERMISSION_CODE) )
                {
                    pickImageCamera();
                }
            }
            else if (whichItemClicked == 1) {
                //gallery clicked
                if ( MyApplication.checkPermission(ProfileEditActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Constants.WRITE_EXTERNAL_STORAGE) )
                {
                    pickImageGallery();
                }
            }
            return false;
        });
    }

    private void pickImageCamera() {
        //intent to pick image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, res.getString(R.string.new_pick));  //image title
        values.put(MediaStore.Images.Media.DESCRIPTION, res.getString(R.string.sample_image_desc));
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery() {
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of camera intent
                    //get uri of image
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "onActivityResult: Picked From Camera " + imageUri);

                        binding.profileIv.setImageURI(imageUri);
                    }
                    else {
                        Toast.makeText(ProfileEditActivity.this, res.getString(R.string.canceled), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //used to handle result of gallery intent
                    //get uri of image
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "onActivityResult: " + imageUri);

                        Intent data = result.getData();
                        imageUri = data.getData();

                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "onActivityResult: Picked From Gallery " + imageUri);

                        binding.profileIv.setImageURI(imageUri);
                    }
                    else {
                        Toast.makeText(ProfileEditActivity.this, res.getString(R.string.canceled), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // This function is called when user accept or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allIsOK = true;

        for (int i=0; i<grantResults.length; i++) {
            // Checking whether user granted the permission or not.
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allIsOK = false;
                break;
            }
        }

        if (allIsOK) {
            if (requestCode == Constants.CAMERA_PERMISSION_CODE) {
                Toast.makeText(ProfileEditActivity.this, res.getString(R.string.camera_granted), Toast.LENGTH_SHORT).show();
                pickImageCamera();
            } else if (requestCode == Constants.WRITE_EXTERNAL_STORAGE) {
                Toast.makeText(ProfileEditActivity.this, res.getString(R.string.write_external_granted), Toast.LENGTH_SHORT).show();
                pickImageGallery();
            }
        } else {
            if (requestCode == Constants.CAMERA_PERMISSION_CODE)
                Toast.makeText(ProfileEditActivity.this, res.getString(R.string.camera_denied), Toast.LENGTH_SHORT).show();
            else if (requestCode == Constants.WRITE_EXTERNAL_STORAGE)
                Toast.makeText(ProfileEditActivity.this, res.getString(R.string.write_external_denied), Toast.LENGTH_SHORT).show();
        }
    }
}
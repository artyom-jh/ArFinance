package am.softlab.arfinance.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityOperationAddBinding;
import am.softlab.arfinance.models.ModelCategory;

public class OperationAddActivity extends AppCompatActivity {

    //view binding
    private ActivityOperationAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;
    private MaterialDatePicker.Builder<Long> materialDateBuilder;
    private MaterialDatePicker<Long> materialDatePicker;

    //resources
    private Resources res;

    //arraylist to hold odf categories
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    //selected category id and category title
    private String selectedCategoryId, selectedCategoryTitle, oldSelectedCategoryId="";
    private double oldAmount = 0;
    private long operationTimestamp=0;
    //operation id get from intent started from AdapterOperation

    private boolean isIncome;
    private String operId, walletId, oldImageUrl="";
    private long currentOperationId = 0;

    private boolean imageChanged = false;

    private Uri imageUri = null;

    private static final String TAG = "OPERATION_ADD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperationAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        // init MaterialDatePicker Builder
        materialDateBuilder = MaterialDatePicker.Builder.datePicker()
                .setTitleText(res.getString(R.string.select_date));

        //operation id get from intent started from AdapterOperation
        operId = getIntent().getStringExtra("operId");
        walletId= getIntent().getStringExtra("walletId");
        isIncome = getIntent().getBooleanExtra("isIncome", false);

        if (operId == null) {       // Add mode
            if (isIncome)
                binding.titleIv.setText(res.getString(R.string.add_income));
            else
                binding.titleIv.setText(res.getString(R.string.add_expenses));

        } else {                    // Edit mode
            if (isIncome)
                binding.titleIv.setText(res.getString(R.string.edit_income));
            else
                binding.titleIv.setText(res.getString(R.string.edit_expenses));
        }

        loadCategories();

        if (operId == null) {       // Add mode
            operationTimestamp = System.currentTimeMillis();
            String formattedDate = MyApplication.formatTimestamp(operationTimestamp);
            binding.operDateTv.setText(formattedDate);
        }
        else {                      // Edit mode
            currentOperationId = Long.parseLong(operId);
            loadOperationInfo();
        }

        //handle click, go back
        binding.backBtn.setOnClickListener(view -> {
            MyApplication.hideKeyboard(this);
            onBackPressed();
        });

        //handle click, pick image
        binding.operationImageIv.setOnClickListener(v -> showImageAttachMenu());

        //handle click, begin upload category
        binding.submitBtn.setOnClickListener(view -> validateData());

        //handle click, pick date
        binding.operDateTv.setOnClickListener(view -> datePickDialog());

        //handle click, pick category
        binding.categoryTv.setOnClickListener(view -> categoryPickDialog());
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //db reference to load categories... db > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryTitleArrayList.clear(); // clear before adding data
                        categoryIdArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelCategory model = ds.getValue(ModelCategory.class);
                            if (model.getIsIncome() == isIncome) {
                                //get id and title of category
                                String categoryId = "" + model.getId(); // ds.child("id").getValue();
                                String categoryTitle = "" + model.getCategory(); // ds.child("category").getValue();

                                //add to respective arraylists
                                categoryTitleArrayList.add(categoryTitle);
                                categoryIdArrayList.add(categoryId);

                                Log.d(TAG, "onDataChange: ID: " + categoryId);
                                Log.d(TAG, "onDataChange: Category: " + categoryTitle);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }

    private void loadOperationInfo() {
        Log.d(TAG, "loadOperationInfo: Loading operation info");

        progressDialog.setMessage(res.getString(R.string.loading_operation));
        progressDialog.show();

        DatabaseReference refOperations = FirebaseDatabase.getInstance().getReference("Operations");
        refOperations.child(operId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get operation info
                        selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                        oldSelectedCategoryId = selectedCategoryId;
                        String amountStr = ""+snapshot.child("amount").getValue();
                        oldAmount = Double.parseDouble(amountStr);

                        String notes = ""+snapshot.child("notes").getValue();
                        //set to view
                        operationTimestamp = Long.parseLong(snapshot.child("operationTimestamp").getValue().toString());
                        String formattedDate = MyApplication.formatTimestamp(operationTimestamp);
                        binding.operDateTv.setText( formattedDate );
                        binding.operAmountEt.setText( amountStr );
                        binding.operNotesEt.setText(notes);

                        if (snapshot.child("imageUrl").getValue() != null)
                            oldImageUrl = ""+snapshot.child("imageUrl").getValue();
                        imageChanged = false;

                        //set image, using glide
                        if (!oldImageUrl.isEmpty())
                            Glide.with(getApplicationContext())
                                    .load(oldImageUrl)
                                    .placeholder(R.drawable.ic_add_photo_gray)
                                    .into(binding.operationImageIv);

                        Log.d(TAG, "onDataChange: Loading Operation Category Info");
                        progressDialog.setMessage(res.getString(R.string.loading_categories));

                        DatabaseReference refOperationCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refOperationCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = ""+snapshot.child("category").getValue();
                                        //set to category text view
                                        binding.categoryTv.setText(category);

                                        progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        progressDialog.dismiss();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });
    }

    private void datePickDialog() {
        Log.d(TAG, "datePickDialog: showing date pick dialog");

        long selectedDate;
        if (operId == null) {       // Add mode
            selectedDate = MaterialDatePicker.todayInUtcMilliseconds();
        } else {                    // Edit mode
            selectedDate = operationTimestamp;
        }

        //configure datePicker
        materialDatePicker = materialDateBuilder
                .setTitleText(res.getString(R.string.select_date))
                .setSelection(selectedDate)
                .build();

        // now handle the positive button click from the material design date picker
        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> {
                    // if the user clicks on the positive button that is ok button update the selected date
                    //noinspection ConstantConditions
                    operationTimestamp = materialDatePicker.getSelection();
                    String formattedDate = MyApplication.formatTimestamp(operationTimestamp);
                    binding.operDateTv.setText(formattedDate);
                });

        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        //get string array of categories from arraylist
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for(int i = 0; i < categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(
                        categoriesArray,
                        (dialogInterface, which) -> {
                            //handle item click
                            //get clicked item from list
                            selectedCategoryTitle = categoryTitleArrayList.get(which);
                            selectedCategoryId = categoryIdArrayList.get(which);
                            //set to category textview
                            binding.categoryTv.setText(selectedCategoryTitle);

                            Log.d(TAG, "onClick: Selected Category: " + selectedCategoryId + " " + selectedCategoryTitle);
                        }
                )
                .show();
    }

    private double amount=0.0;
    private void validateData() {
        //before adding validate data

        try {
            amount = Double.parseDouble(binding.operAmountEt.getText().toString().trim());
        } catch (NumberFormatException e) {
            amount = 0;
        }

        if (operationTimestamp == 0) {
            Toast.makeText(this, res.getString(R.string.pick_date), Toast.LENGTH_SHORT).show();
        }
        else if (amount == 0) {
            Toast.makeText(this, res.getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, res.getString(R.string.pick_category), Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setMessage(res.getString(R.string.updating_operation));
            progressDialog.show();

            if (!imageChanged
                    || (operId == null && imageUri == null) )
            {
                //need to update without image
                addOrUpdateOperationFirebase(oldImageUrl);
            }
            else {
                //need to update with image
                uploadImage();
            }
        }
    }

    private void addOrUpdateOperationFirebase(String uploadedImageUrl) {
        MyApplication.hideKeyboard(this);

        long timestamp = System.currentTimeMillis();

        if (operId == null) {       // Add mode
            Log.d(TAG, "addOrEditOperationFirebase: Starting adding operation info to db...");
            progressDialog.setMessage(res.getString(R.string.adding_operation));
        } else {                    // Edit mode
            Log.d(TAG, "addOrEditOperationFirebase: Starting updating operation info to db...");
            progressDialog.setMessage(res.getString(R.string.updating_operation));
            currentOperationId = timestamp;
        }

        String notes = binding.operNotesEt.getText().toString().trim();

        //setup data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("operationTimestamp", operationTimestamp);
        hashMap.put("walletId", walletId);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("notes", ""+notes);
        hashMap.put("uid", ""+firebaseAuth.getUid());
        hashMap.put("isIncome", isIncome);
        hashMap.put("amount", amount);
        hashMap.put("uid_walletId", ""+firebaseAuth.getUid() + "_" + walletId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("imageUrl", uploadedImageUrl);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");

        if (operId == null) {   // Add mode
            hashMap.put("id", ""+currentOperationId);

            //add to firebase db... Database Root > Operations > operId > operation info
            ref.child(""+timestamp)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        //operation add success
                        Log.d(TAG, "onSuccess: Operation added...");
                        MyApplication.updateWalletBalance(walletId, selectedCategoryId, amount, isIncome, Constants.ROW_ADDED);

                        progressDialog.dismiss();
                        Toast.makeText(OperationAddActivity.this, res.getString(R.string.operation_added), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        //category add failed
                        progressDialog.dismiss();
                        Toast.makeText(OperationAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> finish());
        }
        else {          // Edit mode
            hashMap.put("id", ""+operId);

            ref.child(""+operId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "onSuccess: Operation updated...");

                        MyApplication.updateWalletBalance(walletId, "",amount - oldAmount, isIncome, Constants.ROW_UPDATED);

                        if (!selectedCategoryId.equals(oldSelectedCategoryId)) {
                            MyApplication.updateCategoryUsage(oldSelectedCategoryId, -1);
                            MyApplication.updateCategoryUsage(selectedCategoryId, 1);
                        }

                        progressDialog.dismiss();
                        Toast.makeText(OperationAddActivity.this, res.getString(R.string.operation_updated), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "onFailure: failed to update due to " + e.getMessage());

                        progressDialog.dismiss();
                        Toast.makeText(OperationAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> finish());
        }
    }


    private void showImageAttachMenu() {
        boolean bool = (!oldImageUrl.isEmpty() && !imageChanged)
                || (imageChanged && imageUri != null);
        //init/setup popup menu
        PopupMenu popupMenu = new PopupMenu(this, binding.operationImageIv);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, res.getString(R.string.camera));
        popupMenu.getMenu().add(Menu.NONE, 1, 1, res.getString(R.string.gallery));
        popupMenu.getMenu().add(Menu.NONE, 2, 2, res.getString(R.string.view)).setEnabled(bool);
        popupMenu.getMenu().add(Menu.NONE, 3, 3, res.getString(R.string.clear)).setEnabled(bool);

        popupMenu.show();

        //handle menu item click
        popupMenu.setOnMenuItemClickListener(item -> {
            //get id of item clicked
            int whichItemClicked = item.getItemId();

            if (whichItemClicked == 0) {        //Camera menu
                //camera clicked
                String[] perms;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)  // Android 10(Q) - API 29
                    perms = new String[] { Manifest.permission.CAMERA };
                else
                    perms = new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };

                if ( MyApplication.checkPermission(
                        OperationAddActivity.this,
                        perms,
                        Constants.CAMERA_PERMISSION_CODE) )
                {
                    pickImageCamera();
                }
            }

            else if (whichItemClicked == 1) {   //Gallery menu
                //gallery clicked
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    pickImageGallery();
                }
                else if ( MyApplication.checkPermission(
                        OperationAddActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Constants.WRITE_EXTERNAL_STORAGE) )
                {
                    pickImageGallery();
                }
            }

            else if (whichItemClicked == 2) {   //View menu
                //todo
            }

            else if (whichItemClicked == 3) {   //Clear menu
                imageUri = null;
                imageChanged = true;
                binding.operationImageIv.setImageResource(R.drawable.ic_add_photo_gray);
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
                        Log.d(TAG, "onActivityResult: Picked From Camera " + imageUri);
                        imageChanged = true;
                        binding.operationImageIv.setImageURI(imageUri);
                    }
                    else {
                        Toast.makeText(OperationAddActivity.this, res.getString(R.string.canceled), Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG, "onActivityResult: " + imageUri);
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked From Gallery " + imageUri);

                        imageChanged = true;
                        binding.operationImageIv.setImageURI(imageUri);
                    }
                    else {
                        Toast.makeText(OperationAddActivity.this, res.getString(R.string.canceled), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(OperationAddActivity.this, res.getString(R.string.camera_granted), Toast.LENGTH_SHORT).show();
                pickImageCamera();
            } else if (requestCode == Constants.WRITE_EXTERNAL_STORAGE) {
                Toast.makeText(OperationAddActivity.this, res.getString(R.string.write_external_granted), Toast.LENGTH_SHORT).show();
                pickImageGallery();
            }
        } else {
            if (requestCode == Constants.CAMERA_PERMISSION_CODE)
                Toast.makeText(OperationAddActivity.this, res.getString(R.string.camera_denied), Toast.LENGTH_SHORT).show();
            else if (requestCode == Constants.WRITE_EXTERNAL_STORAGE)
                Toast.makeText(OperationAddActivity.this, res.getString(R.string.write_external_denied), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        if (!oldImageUrl.isEmpty() && imageChanged && imageUri == null) {    // image changed to null - delete image from server
            Log.d(TAG, "uploadImage: Deleting operation image from FirebaseStorage server...");

            String filePathAndName = "OperationImages/" + currentOperationId;

            //storage reference
            StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
            reference
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "uploadImage: Deleted from FirebaseStorage server...");
                            addOrUpdateOperationFirebase("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Failed to delete image due to " + e.getMessage());
                            addOrUpdateOperationFirebase("");
                        }
                    });
        }

        else if (imageUri != null) {
            Log.d(TAG, "uploadImage: Uploading operation image...");

            progressDialog.setMessage(res.getString(R.string.updating_operation_image));

            if (currentOperationId <= 0)
                currentOperationId = System.currentTimeMillis();

            //image patch and name, use uid to replace previous
            String filePathAndName = "OperationImages/" + currentOperationId;

            //storage reference
            StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
            reference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "onSuccess: Operation image uploaded");
                        Log.d(TAG, "onSuccess: Getting url of uploaded image");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        //noinspection StatementWithEmptyBody
                        while (!uriTask.isSuccessful()) ;
                        String uploadedImageUrl = "" + uriTask.getResult();

                        Log.d(TAG, "onSuccess: Uploaded Image URL: " + uploadedImageUrl);

                        addOrUpdateOperationFirebase(uploadedImageUrl);
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "onFailure: Failed to upload image due to " + e.getMessage());
                        addOrUpdateOperationFirebase(oldImageUrl);
                        Toast.makeText(OperationAddActivity.this, res.getString(R.string.failed_to_upload_image) + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

}
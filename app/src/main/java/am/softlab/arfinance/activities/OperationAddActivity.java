package am.softlab.arfinance.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.Surface;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.MyPair;
import am.softlab.arfinance.MyStringUtils;
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
                binding.titleIv.setText(res.getString(R.string.add_expense));

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

        //handle click, recognize amount
        binding.recognizeIb.setOnClickListener(view -> checkForAttachment());

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
                        if ((oldImageUrl != null) && !oldImageUrl.isEmpty())
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


    private void checkForAttachment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OperationAddActivity.this);

        if (imageUri != null) {
            //confirm recognize amount dialog
            builder.setTitle(res.getString(R.string.recognition))
                    .setMessage(res.getString(R.string.recognition_start))
                    .setPositiveButton(
                            res.getString(R.string.recognize),
                            (dialogInterface, i) -> {
                                recognizeAmountFromImage();
                            }
                    )
                    .setNegativeButton(
                            res.getString(R.string.cancel),
                            (dialogInterface, i) -> dialogInterface.dismiss()
                    )
                    .show();
        }
        else {
            builder.setTitle(res.getString(R.string.warning))
                    .setMessage(res.getString(R.string.select_image))
                    .setPositiveButton(res.getString(R.string.close),null)
                    .show();
        }
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
        boolean bool = ((oldImageUrl != null) && !oldImageUrl.isEmpty() && !imageChanged)
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
                if ( MyApplication.checkPermission(
                        OperationAddActivity.this,
                        new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        Constants.CAMERA_PERMISSION_CODE) )
                {
                    pickImageCamera();
                }
            }

            else if (whichItemClicked == 1) {   //Gallery menu
                //gallery clicked
                if ( MyApplication.checkPermission(OperationAddActivity.this,
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
        if ((oldImageUrl != null) && !oldImageUrl.isEmpty() && imageChanged && imageUri == null) {    // image changed to null - delete image from server
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


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }
    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(boolean isFrontFacing)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = OperationAddActivity.this.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) OperationAddActivity.this.getSystemService(CAMERA_SERVICE);
        String cameraId = cameraManager.getCameraIdList()[0];
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }
        return rotationCompensation;
    }

    private void recognizeAmountFromImage() {
        if (imageUri == null) {
            // no bitmap
            Log.d(TAG, "recognizeAmountFromImage: empty image Uri");
            Toast.makeText(OperationAddActivity.this, res.getString(R.string.select_image_short), Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "recognizeAmountFromImage: Preparing Image...");
        progressDialog.setMessage(res.getString(R.string.preparing_image));
        progressDialog.show();

        InputImage image;
        try {
            image = InputImage.fromFilePath(OperationAddActivity.this, imageUri);

            Log.d(TAG, "recognizeAmountFromImage: Recognizing Text...");
            progressDialog.setMessage(res.getString(R.string.recognizing_text));

            //init TextRecognizer
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            Task<Text> textTaskResult =
                    recognizer.process(image)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text visionText) {
                                    // Task completed successfully
                                    performTextAnalysis(visionText);
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            progressDialog.dismiss();
                                            Log.d(TAG, "recognizeAmountFromImage: Failed to recognize text due to " + e.getMessage());
                                            Toast.makeText(OperationAddActivity.this, res.getString(R.string.failed_to_recognize) + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
        }
        catch (Exception e) {
            progressDialog.dismiss();
            Log.d(TAG, "recognizeAmountFromImage: Failed to get rotation compensation " + e.getMessage());
            Toast.makeText(OperationAddActivity.this, res.getString(R.string.failed_to_get_rotation) + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performTextAnalysis(Text visionText) {
        List<Text.TextBlock> textBlock = visionText.getTextBlocks();
        if (textBlock.size() == 0) {
            Log.d(TAG, "performTextAnalysis: recognition failed, empty textBlock");
            progressDialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(OperationAddActivity.this);
            builder.setTitle(res.getString(R.string.failure))
                    .setMessage(res.getString(R.string.recognition_failed))
                    .setPositiveButton(res.getString(R.string.close),null)
                    .show();
            return;
        }
        //Log.d(TAG, "recognizedText: " + visionText.getText());

        //start text analysis
        progressDialog.setMessage(res.getString(R.string.perform_text_analysis));

        List<MyPair> amountsMyPairList = new ArrayList<>();

        //extract integers or decimals from a string
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String lineText = MyStringUtils.removeDateTimeFromString(block.getText());

                Pattern pattern = Pattern.compile(".*\\d.*");
                // find match between given string and pattern
                Matcher matcherText = pattern.matcher(lineText);
                // return true if the string matched the regex
                if (!matcherText.matches())
                    continue;

                for (Text.Element element : line.getElements()) {
                    String elementText = element.getText();
                    elementText = MyStringUtils.removeDateTimeFromString(elementText);

                    Rect frame = element.getBoundingBox();
                    int fontSize = 0;
                    if (frame != null)
                        fontSize = frame.bottom - frame.top;

                    //extract integer or decimal number using regex
                    Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
                    Matcher matcher = regex.matcher(elementText);
                    while (matcher.find()) {
                        String matchedStr;
                        double dAmount=0;

                        try {
                            matchedStr = matcher.group(1);
                            if (matchedStr != null)
                                dAmount = Double.parseDouble(matchedStr);
                        } catch (NumberFormatException e) {
                            dAmount = 0;
                        }

                        //add only unique amount <0 or >9
                        if (dAmount < 0
                                || (dAmount > 9 && dAmount <= Constants.MAX_RECOGNIZED_AMOUNT))
                        {
                            boolean found = false;
                            for (MyPair myPair: amountsMyPairList) {
                                //if amount already exists in array
                                if (myPair.getAmount() == dAmount) {
                                    //if element size is less in array - update size
                                    if (myPair.getFontSize() < fontSize)
                                        myPair.setFontSize(fontSize);

                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                                amountsMyPairList.add(new MyPair(dAmount, fontSize));
                        }
                    }
                }
            }
        }
//        //example from google - https://developers.google.com/ml-kit/vision/text-recognition/android
//        //------------------------------------------------------------------------------------------
//        String resultText = result.getText();
//        for (Text.TextBlock block : result.getTextBlocks()) {
//            String blockText = block.getText();
//            Point[] blockCornerPoints = block.getCornerPoints();
//            Rect blockFrame = block.getBoundingBox();
//            for (Text.Line line : block.getLines()) {
//                String lineText = line.getText();
//                Point[] lineCornerPoints = line.getCornerPoints();
//                Rect lineFrame = line.getBoundingBox();
//                for (Text.Element element : line.getElements()) {
//                    String elementText = element.getText();
//                    Point[] elementCornerPoints = element.getCornerPoints();
//                    Rect elementFrame = element.getBoundingBox();
//                    for (Text.Symbol symbol : element.getSymbols()) {
//                        String symbolText = symbol.getText();
//                        Point[] symbolCornerPoints = symbol.getCornerPoints();
//                        Rect symbolFrame = symbol.getBoundingBox();
//                    }
//                }
//            }
//        }

        progressDialog.dismiss();

        //show alert dialog if no amount found in recognized strings
        if (amountsMyPairList.size() <= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(OperationAddActivity.this);
            builder.setTitle(res.getString(R.string.failure))
                    .setMessage(res.getString(R.string.recognition_amount_failed))
                    .setPositiveButton(res.getString(R.string.close),null)
                    .show();
        }
        //show amount selection dialog for recognized amounts
        else {
            //sort amountsMyPairList in descending order by FontSize field
            amountsMyPairList.sort((a, b) -> Double.compare(b.getFontSize(), a.getFontSize()));

            int toIndex = 0;
            int firstFontSize = amountsMyPairList.get(0).getFontSize();
            // ignore elements with FontSize less than 50% than the size of the first element
            for (MyPair myPair: amountsMyPairList) {
                if (myPair.getFontSize() >= (firstFontSize * 0.5))
                    toIndex++;
            }

            amountsMyPairList = amountsMyPairList.subList(0, toIndex-1);
            amountPickDialog(amountsMyPairList);
        }
    }

    private void amountPickDialog(List<MyPair> amountsMyPairList) {
        Log.d(TAG, "amountPickDialog: showing amount pick dialog");

        //get string array of amounts from amountsList
        String[] amountsArray = new String[amountsMyPairList.size()];
        for(int i = 0; i < amountsMyPairList.size(); i++){
            amountsArray[i] = MyApplication.formatDouble(amountsMyPairList.get(i).getAmount());
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.pick_amount))
                .setItems(
                        amountsArray,
                        (dialogInterface, which) -> {
                            //handle item click
                            //get clicked item from list and set to amount edittext
                            binding.operAmountEt.setText(amountsArray[which]);
                            Log.d(TAG, "onClick: Selected Amount: " + amountsArray[which]);
                        }
                )
                .show();
    }
}
package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityOperationAddBinding;

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
    private String operId;

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
            loadOperationInfo();
        }

        //handle click, go back
        binding.backBtn.setOnClickListener(view -> onBackPressed());

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
        ref.orderByChild("isIncome").equalTo(isIncome)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryTitleArrayList.clear(); // clear before adding data
                        categoryIdArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            //get id and title of category
                            String categoryId = "" + ds.child("id").getValue();
                            String categoryTitle = "" + ds.child("category").getValue();

                            //add to respective arraylists
                            categoryTitleArrayList.add(categoryTitle);
                            categoryIdArrayList.add(categoryId);

                            Log.d(TAG, "onDataChange: ID: " + categoryId);
                            Log.d(TAG, "onDataChange: Category: " + categoryTitle);
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

                        Log.d(TAG, "onDataChange: Loading Operation Category Info");
                        DatabaseReference refOperationCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refOperationCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = ""+snapshot.child("category").getValue();
                                        //set to category text view
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
            addOrUpdateOperationFirebase();
        }
    }

    private void addOrUpdateOperationFirebase() {
        if (operId == null) {       // Add mode
            Log.d(TAG, "addOrEditOperationFirebase: Starting adding operation info to db...");
            progressDialog.setMessage(res.getString(R.string.adding_operation));
        } else {                    // Edit mode
            Log.d(TAG, "addOrEditOperationFirebase: Starting updating operation info to db...");
            progressDialog.setMessage(res.getString(R.string.updating_operation));
        }

        //show progress
        progressDialog.show();

        String notes = binding.operNotesEt.getText().toString().trim();
        long timestamp = System.currentTimeMillis();

        //setup data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("operationTimestamp", operationTimestamp);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("notes", ""+notes);
        hashMap.put("uid", ""+firebaseAuth.getUid());
        hashMap.put("isIncome", isIncome);
        hashMap.put("amount", amount);
        hashMap.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");

        if (operId == null) {   // Add mode
            hashMap.put("id", ""+timestamp);

            //add to firebase db... Database Root > Operations > operId > operation info
            ref.child(""+timestamp)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        //category add success
                        Log.d(TAG, "onSuccess: Operation added...");
                        MyApplication.updateCategoryAmount(selectedCategoryId, amount);
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

                        if (selectedCategoryId.equals(oldSelectedCategoryId)) {
                            MyApplication.updateCategoryAmount(selectedCategoryId, amount - oldAmount);
                        } else {
                            MyApplication.updateCategoryAmount(oldSelectedCategoryId, 0-oldAmount);
                            MyApplication.updateCategoryAmount(selectedCategoryId, amount);
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

}
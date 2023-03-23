package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import am.softlab.arfinance.BuildConfig;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityCategoryAddBinding;

public class CategoryAddActivity extends AppCompatActivity {

    //view binding
    private ActivityCategoryAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //resources
    private Resources res;

    //category id get from intent started from AdapterCategory
    private String categoryId;

    //string array of categories type
    private String[] categoryTypeArray;
    //selected category id and category title
    private int selectedCategoryTypeIndex = -1;
    private String selectedCategoryTypeTitle;

    private static final String TAG = "CATEGORY_ADD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        categoryTypeArray = new String[] {
                res.getString(R.string.income),
                res.getString(R.string.expenses)
        };

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        //category id get from intent started from AdapterCategory
        categoryId = getIntent().getStringExtra("categoryId");

        if (categoryId == null) {   // Add mode
            binding.titleIv.setText(res.getString(R.string.add_category));
        } else {                    // Edit mode
            binding.titleIv.setText(res.getString(R.string.edit_category));

            //get data
            binding.categoryNameEt.setText( getIntent().getStringExtra("categoryName") );
            binding.categoryNotesEt.setText( getIntent().getStringExtra("categoryNotes") );

            boolean isIncome = getIntent().getBooleanExtra("isIncome", true);
            if (isIncome) {
                selectedCategoryTypeIndex = 0;
                selectedCategoryTypeTitle = categoryTypeArray[0];
            } else {
                selectedCategoryTypeIndex = 1;
                selectedCategoryTypeTitle = categoryTypeArray[1];
            }
            binding.categoryTypeTv.setText(selectedCategoryTypeTitle);
        }

        //handle click, go back
        binding.backBtn.setOnClickListener(view -> {
            MyApplication.hideKeyboard(this);
            onBackPressed();
        });

        //handle click, begin upload category
        binding.submitBtn.setOnClickListener(view -> validateData());

        //handle click, pick category
        binding.categoryTypeTv.setOnClickListener(view -> categoryTypePickDialog());
    }

    private String category = "";
    private String notes = "";
    private void validateData() {
        //before adding validate data

        //get data
        category = binding.categoryNameEt.getText().toString().trim();
        notes = binding.categoryNotesEt.getText().toString().trim();

        //validate if not empty
        if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, res.getString(R.string.enter_category), Toast.LENGTH_SHORT).show();
        }
        else if (selectedCategoryTypeIndex < 0) {
            Toast.makeText(this, res.getString(R.string.pick_category_type), Toast.LENGTH_SHORT).show();
        }
        else {
            addCategoryFirebase();
        }
    }

    private void categoryTypePickDialog() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "categoryTypePickDialog: showing category pick dialog");

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.pick_category_type))
                .setItems(
                        categoryTypeArray,
                        (dialogInterface, which) -> {
                            //handle item click
                            //get clicked item from list
                            selectedCategoryTypeIndex = which;
                            selectedCategoryTypeTitle = categoryTypeArray[which];
                            //set to category textview
                            binding.categoryTypeTv.setText(selectedCategoryTypeTitle);

                            if (BuildConfig.DEBUG)
                                Log.d(TAG, "onClick: Selected Category: " + selectedCategoryTypeIndex + " " + selectedCategoryTypeTitle);
                        }
                )
                .show();
    }

    private void addCategoryFirebase() {
        MyApplication.hideKeyboard(this);

        //show progress
        progressDialog.setMessage(res.getString(R.string.adding_category));
        progressDialog.show();

        //get timestamp
        long timestamp = System.currentTimeMillis();

        //setup info to add in firebase db
        boolean isIncome = selectedCategoryTypeIndex == 0;

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("category", ""+category);
        hashMap.put("notes", ""+notes);
        hashMap.put("isIncome", isIncome);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");

        if (categoryId == null) {   // Add mode
            hashMap.put("usageCount", 0);
            hashMap.put("id", ""+timestamp);

            //add to firebase db... Database Root > Categories > categoryId > category info
            ref.child(""+timestamp)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        //category add success
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "onSuccess: Category added...");

                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, res.getString(R.string.category_added), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        //category add failed
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> finish());
        }
        else {          // Edit mode
            hashMap.put("id", ""+categoryId);

            ref.child(""+categoryId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(unused -> {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "onSuccess: Category updated...");

                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, res.getString(R.string.category_updated), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG, "onFailure: failed to update due to " + e.getMessage());

                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> finish());
        }
    }

}
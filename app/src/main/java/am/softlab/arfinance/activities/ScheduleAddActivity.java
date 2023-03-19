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

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityScheduleAddBinding;
import am.softlab.arfinance.models.ModelCategory;
import am.softlab.arfinance.models.ModelWallet;

public class ScheduleAddActivity extends AppCompatActivity {

    //view binding
    private ActivityScheduleAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;
    private MaterialDatePicker.Builder<Long> materialDateBuilder;
    private MaterialDatePicker<Long> materialDatePicker;

    //resources
    private Resources res;

    //schedule id get from intent started from AdapterSchedule
    private String scheduleId;

    //string array of categories type
    private String[] scheduleTypeArray;
    //selected category id and category title
    private int selectedScheduleTypeIndex = -1;
    private String selectedScheduleTypeTitle;

    private int selectedPeriodIndex = 0;
    private String selectedPeriodTitle="";

    private static final String TAG = "SCHEDULE_ADD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        scheduleTypeArray = new String[] {
                res.getString(R.string.income),
                res.getString(R.string.expenses)
        };

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        // init MaterialDatePicker Builder
        materialDateBuilder = MaterialDatePicker.Builder.datePicker()
                .setTitleText(res.getString(R.string.select_date));

        //category id get from intent started from AdapterCategory
        scheduleId = getIntent().getStringExtra("scheduleId");

        loadWallets();

        if (scheduleId == null) {   // Add mode
            mStartDateTime = System.currentTimeMillis();
            mOldStartDateTime = 0L;
            String formattedDate = MyApplication.formatTimestamp(mStartDateTime);
            binding.scheduleStartDateTv.setText(formattedDate);

            binding.titleIv.setText(res.getString(R.string.add_schedule));
        } else {                    // Edit mode
            binding.titleIv.setText(res.getString(R.string.edit_schedule));

            loadScheduleInfo();
        }

        //handle click, pick date
        binding.scheduleStartDateTv.setOnClickListener(view -> datePickDialog());

        //handle click, pick wallet
        binding.walletTv.setOnClickListener(view -> walletPickDialog());

        //handle click, pick category
        binding.scheduleTypeTv.setOnClickListener(view -> scheduleTypePickDialog());

        //handle click, pick category
        binding.categoryTv.setOnClickListener(view -> categoryPickDialog());

        //handle click, pick period
        binding.periodTv.setOnClickListener(view -> periodPickDialog());

        //handle click, begin upload schedule
        binding.submitBtn.setOnClickListener(view -> validateData());

        //handle click, go back
        binding.backBtn.setOnClickListener(view -> {
            MyApplication.hideKeyboard(this);
            onBackPressed();
        });
    }


    private String mScheduleName = "";
    private long mStartDateTime=0, mOldStartDateTime=0;
    private double mAmount=0.0;
    private void validateData() {
        //before adding validate data

        //get data
        mScheduleName = binding.scheduleNameEt.getText().toString().trim();

        try {
            mAmount = Double.parseDouble(binding.scheduleAmountEt.getText().toString().trim());
        } catch (NumberFormatException e) {
            mAmount = 0.0;
        }

        //validate if not empty
        if (TextUtils.isEmpty(mScheduleName)) {
            Toast.makeText(this, res.getString(R.string.enter_schedule_name), Toast.LENGTH_SHORT).show();
        }
        else if (mStartDateTime == 0) {
            Toast.makeText(this, res.getString(R.string.pick_date), Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedWalletId)) {
            Toast.makeText(this, res.getString(R.string.pick_wallet), Toast.LENGTH_SHORT).show();
        }
        else if (selectedScheduleTypeIndex < 0) {
            Toast.makeText(this, res.getString(R.string.pick_schedule_type), Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, res.getString(R.string.pick_category), Toast.LENGTH_SHORT).show();
        }
        else if (mAmount == 0) {
            Toast.makeText(this, res.getString(R.string.enter_amount), Toast.LENGTH_SHORT).show();
        }
        else if (selectedPeriodIndex <= -1) {
            Toast.makeText(this, res.getString(R.string.pick_period), Toast.LENGTH_SHORT).show();
        }
        else {
            addOrUpdateScheduleFirebase();
        }
    }


    private void datePickDialog() {
        Log.d(TAG, "datePickDialog: showing date pick dialog");

        long selectedDate;
        if (scheduleId == null) {       // Add mode
            selectedDate = MaterialDatePicker.todayInUtcMilliseconds();
        } else {                    // Edit mode
            selectedDate = mStartDateTime;
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
                    mStartDateTime = materialDatePicker.getSelection();
                    String formattedDate = MyApplication.formatTimestamp(mStartDateTime);
                    binding.scheduleStartDateTv.setText(formattedDate);
                });

        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }


    //arraylist to hold of wallets
    private ArrayList<String> walletNameArrayList, walletIdArrayList, currencySymbolArrayList;
    private String selectedWalletId="", selectedWalletName="", selectedCurrencySymbol="", oldSelectedWalletId="";
    private void loadWallets() {
        Log.d(TAG, "loadWallets: Loading wallets...");

        progressDialog.setMessage(res.getString(R.string.loading_walletss));
        progressDialog.show();

        walletNameArrayList = new ArrayList<>();
        walletIdArrayList = new ArrayList<>();
        currencySymbolArrayList = new ArrayList<>();

        if (firebaseAuth.getCurrentUser() != null) {
            //db reference to load wallets... db > Wallets
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
            ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            walletNameArrayList.clear(); // clear before adding data
                            walletIdArrayList.clear();
                            currencySymbolArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ModelWallet model = ds.getValue(ModelWallet.class);

                                walletNameArrayList.add(model.getWalletName());
                                walletIdArrayList.add(""+model.getId());
                                currencySymbolArrayList.add(model.getCurrencySymbol());

                                Log.d(TAG, "onDataChange: ID: " + model.getId());
                                Log.d(TAG, "onDataChange: Wallet: " + model.getWalletName());
                            }

                            loadCategories();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //noop
                        }
                    });
        }
    }
    private void walletPickDialog() {
        Log.d(TAG, "walletPickDialog: showing wallet pick dialog");

        if (firebaseAuth.getCurrentUser() != null) {
            //get string array of wallets from arraylist
            String[] walletsArray = new String[walletNameArrayList.size()];
            for (int i = 0; i < walletNameArrayList.size(); i++) {
                walletsArray[i] = walletNameArrayList.get(i);
            }

            //alert dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Pick Wallet")
                    .setItems(
                            walletsArray,
                            (dialogInterface, which) -> {
                                //handle item click
                                //get clicked item from list
                                selectedWalletName = walletNameArrayList.get(which);
                                selectedWalletId = walletIdArrayList.get(which);
                                selectedCurrencySymbol = currencySymbolArrayList.get(which);

                                //set to wallet textview
                                binding.walletTv.setText(selectedWalletName);

                                Log.d(TAG, "onClick: Selected Wallet: " + selectedWalletId + " " + selectedWalletName);
                            }
                    )
                    .show();
        }
        else
            Toast.makeText(this, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
    }


    //arraylist to hold odf categories
    private ArrayList<String> categoryIncomeTitleArrayList, categoryIncomeIdArrayList;
    private ArrayList<String> categoryExpenseTitleArrayList, categoryExpenseIdArrayList;

    //selected category id and category title
    private String selectedCategoryId="", selectedCategoryTitle="", oldSelectedCategoryId="";

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories...");

        progressDialog.setMessage(res.getString(R.string.loading_operations));

        categoryIncomeTitleArrayList = new ArrayList<>();
        categoryIncomeIdArrayList = new ArrayList<>();
        categoryExpenseTitleArrayList = new ArrayList<>();
        categoryExpenseIdArrayList = new ArrayList<>();

        //db reference to load categories... db > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryIncomeTitleArrayList.clear(); // clear before adding data
                        categoryIncomeIdArrayList.clear();
                        categoryExpenseTitleArrayList.clear();
                        categoryExpenseIdArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelCategory model = ds.getValue(ModelCategory.class);

                            //get id and title of category
                            String categoryId = "" + model.getId(); // ds.child("id").getValue();
                            String categoryTitle = "" + model.getCategory(); // ds.child("category").getValue();

                            //add to respective arraylists
                            if (model.getIsIncome()) {
                                categoryIncomeTitleArrayList.add(categoryTitle);
                                categoryIncomeIdArrayList.add(categoryId);
                            }
                            else {
                                categoryExpenseTitleArrayList.add(categoryTitle);
                                categoryExpenseIdArrayList.add(categoryId);
                            }

                            Log.d(TAG, "onDataChange: ID: " + categoryId);
                            Log.d(TAG, "onDataChange: Category: " + categoryTitle);
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });
    }

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");

        String[] categoriesArray;

        if (selectedScheduleTypeIndex == 0) {       //Income
            //get string array of income categories from arraylist
            categoriesArray = new String[categoryIncomeTitleArrayList.size()];
            for(int i = 0; i < categoryIncomeTitleArrayList.size(); i++){
                categoriesArray[i] = categoryIncomeTitleArrayList.get(i);
            }
        }
        else if (selectedScheduleTypeIndex == 1) {   //Expense
            //get string array of expense categories from arraylist
            categoriesArray = new String[categoryExpenseTitleArrayList.size()];
            for(int i = 0; i < categoryExpenseTitleArrayList.size(); i++){
                categoriesArray[i] = categoryExpenseTitleArrayList.get(i);
            }
        }
        else {
            Toast.makeText(this, res.getString(R.string.pick_schedule_type), Toast.LENGTH_SHORT).show();
            return;
        }

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(
                        categoriesArray,
                        (dialogInterface, which) -> {
                            //handle item click
                            //get clicked item from list
                            if (selectedScheduleTypeIndex == 0) {       //Income
                                selectedCategoryTitle = categoryIncomeTitleArrayList.get(which);
                                selectedCategoryId = categoryIncomeIdArrayList.get(which);
                            }
                            else {                                      //Expense
                                selectedCategoryTitle = categoryExpenseTitleArrayList.get(which);
                                selectedCategoryId = categoryExpenseIdArrayList.get(which);
                            }
                            //set to category textview
                            binding.categoryTv.setText(selectedCategoryTitle);

                            Log.d(TAG, "onClick: Selected Category: " + selectedCategoryId + " " + selectedCategoryTitle);
                        }
                )
                .show();
    }


    private void scheduleTypePickDialog() {
        Log.d(TAG, "scheduleTypePickDialog: showing schedule type pick dialog");

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.pick_schedule_type))
                .setItems(
                        scheduleTypeArray,
                        (dialogInterface, which) -> {
                            //handle item click
                            //get clicked item from list
                            selectedScheduleTypeIndex = which;
                            selectedScheduleTypeTitle = scheduleTypeArray[which];

                            selectedCategoryId="";
                            selectedCategoryTitle="";

                            //set to type and category textview
                            binding.scheduleTypeTv.setText(selectedScheduleTypeTitle);
                            binding.categoryTv.setText(selectedCategoryTitle);

                            Log.d(TAG, "onClick: Selected Category: " + selectedScheduleTypeIndex + " " + selectedScheduleTypeTitle);
                        }
                )
                .show();
    }

    private void periodPickDialog() {
        Log.d(TAG, "periodPickDialog: showing period pick dialog");

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.pick_schedule_type))
                .setItems(
                        Constants.periodsArray,
                        (dialogInterface, which) -> {
                            //handle item click
                            //get clicked item from list
                            selectedPeriodIndex = which;
                            selectedPeriodTitle = Constants.periodsArray[which];
                            //set to category textview
                            binding.periodTv.setText(selectedPeriodTitle);

                            Log.d(TAG, "onClick: Selected Period: " + selectedPeriodIndex + " " + selectedPeriodTitle);
                        }
                )
                .show();
    }



    private void loadScheduleInfo() {
        Log.d(TAG, "loadScheduleInfo: Loading schedule info");

        //show progress
        progressDialog.show();

        DatabaseReference refOperations = FirebaseDatabase.getInstance().getReference("Schedulers");
        refOperations.child(scheduleId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get schedule info
                        mScheduleName = ""+snapshot.child("name").getValue();

                        mStartDateTime = Long.parseLong(snapshot.child("startDateTime").getValue().toString());
                        mOldStartDateTime = mStartDateTime;
                        String formattedDate = MyApplication.formatTimestamp(mStartDateTime);

                        selectedWalletId = ""+snapshot.child("walletId").getValue();
                        selectedWalletName = MyApplication.getWalletById(selectedWalletId);
                        oldSelectedWalletId = selectedWalletId;

                        selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                        selectedCategoryTitle = MyApplication.getCategoryById(selectedCategoryId);
                        oldSelectedCategoryId = selectedCategoryId;

                        boolean isIncome = getIntent().getBooleanExtra("isIncome", true);
                        if (isIncome) {
                            selectedScheduleTypeIndex = 0;
                            selectedScheduleTypeTitle = scheduleTypeArray[0];
                        } else {
                            selectedScheduleTypeIndex = 1;
                            selectedScheduleTypeTitle = scheduleTypeArray[1];
                        }

                        String amountStr = ""+snapshot.child("amount").getValue();

                        int idx = Integer.parseInt(snapshot.child("period").getValue().toString());
                        if (idx > 0 && idx < Constants.periodsArray.length) {
                            selectedPeriodIndex = idx;
                            selectedPeriodTitle = Constants.periodsArray[idx];
                        }
                        else {
                            selectedPeriodIndex = -1;
                            selectedPeriodTitle = Constants.periodsArray[0];
                        }

                        //set to view
                        binding.scheduleNameEt.setText(mScheduleName);
                        binding.scheduleStartDateTv.setText(formattedDate);
                        binding.walletTv.setText(selectedWalletName);
                        binding.scheduleTypeTv.setText(selectedScheduleTypeTitle);
                        binding.categoryTv.setText(selectedCategoryTitle);
                        binding.scheduleAmountEt.setText( amountStr );
                        binding.periodTv.setText(selectedPeriodTitle);

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });
    }


    private void addOrUpdateScheduleFirebase() {
        MyApplication.hideKeyboard(this);

        if (scheduleId == null) {       // Add mode
            Log.d(TAG, "addOrUpdateScheduleFirebase: Starting adding schedule info to db...");
            progressDialog.setMessage(res.getString(R.string.adding_schedule));
        } else {                    // Edit mode
            Log.d(TAG, "addOrUpdateScheduleFirebase: Starting updating schedule info to db...");
            progressDialog.setMessage(res.getString(R.string.updating_schedule));
        }

        //show progress
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        boolean isIncome = (selectedScheduleTypeIndex == 0);

        //setup data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", mScheduleName);
        hashMap.put("startDateTime", mStartDateTime);
        hashMap.put("walletId", selectedWalletId);
        hashMap.put("isIncome", isIncome);
        hashMap.put("categoryId", selectedCategoryId);
        hashMap.put("amount", mAmount);
        hashMap.put("period", selectedPeriodIndex);
        hashMap.put("uid", ""+firebaseAuth.getUid());
        hashMap.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Schedulers");

        if (scheduleId == null) {   // Add mode
            hashMap.put("id", ""+timestamp);
            hashMap.put("enabled", false);
            hashMap.put("lastStartDateTime", 0L);

            //add to firebase db... Database Root > Schedulers > scheduleId > schedule info
            ref.child(""+timestamp)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        //schedule add success
                        Log.d(TAG, "onSuccess: Schedule added...");
                        MyApplication.updateWalletBalance(selectedWalletId, selectedCategoryId, 0, isIncome, Constants.ROW_ADDED);
                        progressDialog.dismiss();
                        Toast.makeText(ScheduleAddActivity.this, res.getString(R.string.schedule_added), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        //schedule add failed
                        progressDialog.dismiss();
                        Toast.makeText(ScheduleAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> finish());
        }
        else {          // Edit mode
            hashMap.put("id", scheduleId);

            //if mStartDateTime changed - reset lastStartDateTime
            if (mStartDateTime != mOldStartDateTime)
                hashMap.put("lastStartDateTime", 0L);

            ref.child(scheduleId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "onSuccess: Schedule updated...");

                        if (!selectedWalletId.equals(oldSelectedWalletId)) {
                            MyApplication.updateWalletBalance(oldSelectedWalletId, "",0, isIncome, Constants.ROW_DELETED);
                            MyApplication.updateWalletBalance(selectedWalletId, "", 0, isIncome, Constants.ROW_ADDED);
                        }

                        if (!selectedCategoryId.equals(oldSelectedCategoryId)) {
                            MyApplication.updateCategoryUsage(oldSelectedCategoryId, -1);
                            MyApplication.updateCategoryUsage(selectedCategoryId, 1);
                        }

                        progressDialog.dismiss();
                        Toast.makeText(ScheduleAddActivity.this, res.getString(R.string.schedule_updated), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "onFailure: failed to update due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(ScheduleAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> finish());
        }
    }
}
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
import java.util.List;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityWalletAddBinding;

public class WalletAddActivity extends AppCompatActivity {
    //view binding
    private ActivityWalletAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //resources
    private Resources res;

    //wallet id get from intent started from AdapterWallet
    private String walletId;

    //string array of currencies
    private String[] currencyNameArray, currencyCodeArray, currencySymbolArray;
    //selected currency id and currency name
    private int selectedCurrencyIndex = -1;
    private String selectedCurrencyName, selectedCurrencyCode, selectedCurrencySymbol;

    private static final String TAG = "CURRENCY_ADD_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        currencyNameArray = new String[Constants.CURRENCY_ARRAY_LIST.size()];
        currencyCodeArray = new String[Constants.CURRENCY_ARRAY_LIST.size()];
        currencySymbolArray = new String[Constants.CURRENCY_ARRAY_LIST.size()];
        int i = 0;
        for (List<String> obj: Constants.CURRENCY_ARRAY_LIST) {
            currencyCodeArray[i] = obj.get(0);
            currencyNameArray[i] = obj.get(1);
            currencySymbolArray[i] = obj.get(2);
            i++;
        }

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        //wallet id get from intent started from AdapterWallet
        walletId = getIntent().getStringExtra("walletId");

        if (walletId == null) {   // Add mode
            binding.titleIv.setText(res.getString(R.string.add_wallet));
        } else {                  // Edit mode
            binding.titleIv.setText(res.getString(R.string.edit_wallet));

            //get data
            binding.walletNameEt.setText( getIntent().getStringExtra("walletName") );
            binding.walletNotesEt.setText( getIntent().getStringExtra("walletNotes") );

            selectedCurrencyCode = getIntent().getStringExtra("currencyCode");
            selectedCurrencyName = getIntent().getStringExtra("currencyName");
            selectedCurrencySymbol = getIntent().getStringExtra("currencySymbol");
            selectedCurrencyIndex = 0;
            for(i = 0; i < currencyCodeArray.length; i++){
                if(selectedCurrencyCode.equals(currencyCodeArray[i])) {
                    selectedCurrencyIndex = i;
                    break;
                }
            }

            binding.currencyTv.setText(selectedCurrencyName);
        }

        //handle click, go back
        binding.backBtn.setOnClickListener(view -> onBackPressed());

        //handle click, begin upload category
        binding.submitBtn.setOnClickListener(view -> validateData());

        //handle click, pick currency
        binding.currencyTv.setOnClickListener(view -> currencyPickDialog());
    }

    private String walletName = "";
    private String notes = "";
    private void validateData() {
        //before adding validate data

        //get data
        walletName = binding.walletNameEt.getText().toString().trim();
        notes = binding.walletNotesEt.getText().toString().trim();

        //validate if not empty
        if (TextUtils.isEmpty(walletName)) {
            Toast.makeText(this, res.getString(R.string.enter_wallet), Toast.LENGTH_SHORT).show();
        }
        else if (selectedCurrencyIndex < 0) {
            Toast.makeText(this, res.getString(R.string.pick_currency), Toast.LENGTH_SHORT).show();
        }
        else {
            addWalletFirebase();
        }
    }

    private void currencyPickDialog() {
        Log.d(TAG, "currencyPickDialog: showing currency pick dialog");

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(res.getString(R.string.pick_currency))
                .setItems(
                        currencyNameArray,
                        (dialogInterface, which) -> {
                            //handle item click
                            //get clicked item from list
                            selectedCurrencyIndex = which;
                            selectedCurrencyCode = currencyCodeArray[which];
                            selectedCurrencyName = currencyNameArray[which];
                            selectedCurrencySymbol = currencySymbolArray[which];
                            //set to currency textview
                            binding.currencyTv.setText(selectedCurrencyName);

                            Log.d(TAG, "onClick: Selected Currency: " + selectedCurrencyIndex + " " + selectedCurrencyName);
                        }
                )
                .show();
    }

    private void addWalletFirebase() {
        //show progress
        progressDialog.setMessage(res.getString(R.string.adding_wallet));
        progressDialog.show();

        //get timestamp
        long timestamp = System.currentTimeMillis();

        //setup info to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("walletName", ""+walletName);
        hashMap.put("notes", ""+notes);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getCurrentUser().getUid());
        hashMap.put("currencyName", selectedCurrencyName);
        hashMap.put("currencyCode", selectedCurrencyCode);
        hashMap.put("currencySymbol", selectedCurrencySymbol);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");

        if (walletId == null) {   // Add mode
            hashMap.put("balance", (double)0);
            hashMap.put("totalIncome", (double)0);
            hashMap.put("totalExpenses", (double)0);
            hashMap.put("usageCount", 0);
            hashMap.put("id", ""+timestamp);

            //add to firebase db... Database Root > Wallets > walletId > wallet info
            ref.child(""+timestamp)
                    .setValue(hashMap)
                    .addOnSuccessListener(unused -> {
                        //category add success
                        Log.d(TAG, "onSuccess: Wallet added...");
                        progressDialog.dismiss();
                        Toast.makeText(WalletAddActivity.this, res.getString(R.string.wallet_added), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        //category add failed
                        progressDialog.dismiss();
                        Toast.makeText(WalletAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> finish());
        }
        else {          // Edit mode
            hashMap.put("id", ""+walletId);

            ref.child(""+walletId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "onSuccess: Wallet updated...");
                        progressDialog.dismiss();
                        Toast.makeText(WalletAddActivity.this, res.getString(R.string.wallet_updated), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "onFailure: failed to update due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(WalletAddActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> finish());
        }
    }
}
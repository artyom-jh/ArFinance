package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //resources
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go to register screen
        binding.noAccountTv.setOnClickListener(
                view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        //handle click, begin login
        binding.loginBtn.setOnClickListener(view -> validateData());

        //handle click, open forgot password activity
        binding.forgotTv.setOnClickListener(
                v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private String email = "", password = "";
    private void validateData() {
        // Before login, doing data validation

        //get data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        //validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, res.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, res.getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
        }
        else{
            //data is validated, begin login
            loginUser();
        }
    }

    private void loginUser() {
        MyApplication.hideKeyboard(this);

        //show progress
        progressDialog.setMessage(res.getString(R.string.logging_in));
        progressDialog.show();

        //login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    //login success, check if user is user or admin
                    checkUser();
                })
                .addOnFailureListener(e -> {
                    //login failed
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        progressDialog.setMessage(res.getString(R.string.checking_user));
        //check if user is user or admin from realtime database
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //check in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // progressDialog.dismiss(); // not here, dismiss in checkCategoriesTable
                        checkCategoriesTable();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }

    private  void checkCategoriesTable() {
        progressDialog.setMessage(res.getString(R.string.initCategoriesTables));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int size = (int) snapshot.getChildrenCount();
                        if (size == 0) {
                            // progressDialog.dismiss(); // not here, dismiss in initTablesAndStartDashboard()
                            initCategoriesTables();
                        } else {
                            checkWalletsTable();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }

    private  void initCategoriesTables() {
        int incomeLastIndex = 2;  // ATTENTION - for value see categoryTypesArray

        String uid = ""+firebaseAuth.getUid();

        // add default categories
        HashMap<String, Object> hashMapMulti = new HashMap<>();

        for (int i=0; i < Constants.categoryTypesArray.length; i++) {
            long timestamp = System.currentTimeMillis();
            boolean isIncome = (i <= incomeLastIndex);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", ""+timestamp);
            hashMap.put("category", Constants.categoryTypesArray[i]);
            hashMap.put("notes", Constants.categoryNotesArray[i]);
            hashMap.put("isIncome", isIncome);
            hashMap.put("usageCount", 0);
            hashMap.put("timestamp", timestamp);
            hashMap.put("uid", uid);

            hashMapMulti.put(""+timestamp, hashMap);

            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.updateChildren(
                hashMapMulti,
                (error, ref1) -> checkWalletsTable()
        );
    }

    private  void checkWalletsTable() {
        progressDialog.setMessage(res.getString(R.string.initWalletsTables));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
        ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int size = (int) snapshot.getChildrenCount();
                        if (size == 0) {
                            // progressDialog.dismiss(); // not here, dismiss in initTablesAndStartDashboard()
                            initWalletTablesAndStartDashboard();
                        } else {
                            progressDialog.dismiss();
                            Intent dashIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                            dashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(dashIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }

    private  void initWalletTablesAndStartDashboard() {
        // default wallets - name
        String[] walletsNameArray = new String[] {
                "Wallet01 AMD",
                "Wallet02 USD",
                "Wallet03 EUR"
        };
        String[] walletsNotesArray = new String[] {
                "Lorem ipsum dolor sit amet, consectetur elit, sed do eiusmod tempor dolore magna aliqua.",
                "Ut enim ad minim veniam, quis nostrud ullamco laboris nisi ut aliquip commodo consequat.",
                "Duis aute irure dolor in voluptate velit esse cillum dolore eu fugiat nulla pariatur."
        };
        // default wallets - index for Constants.CURRENCY_ARRAY_LIST
        int[] currencyIndexArray = new int[] {
                0,  // AMD
                10, // USD
                5,  // EUR
        };

        String uid = ""+firebaseAuth.getUid();

        // add default wallets
        HashMap<String, Object> hashMapMulti = new HashMap<>();
        for (int i=0; i < walletsNameArray.length; i++) {
            long timestamp = System.currentTimeMillis();
            List<String> currencyArray = Constants.CURRENCY_ARRAY_LIST.get(currencyIndexArray[i]);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("walletName", ""+walletsNameArray[i]);
            hashMap.put("notes", walletsNotesArray[i]);
            hashMap.put("timestamp", timestamp);
            hashMap.put("uid", uid);
            hashMap.put("currencyName", currencyArray.get(1));
            hashMap.put("currencyCode", currencyArray.get(0));
            hashMap.put("currencySymbol", currencyArray.get(2));
            hashMap.put("balance", (double)0);
            hashMap.put("totalIncome", (double)0);
            hashMap.put("totalExpenses", (double)0);
            hashMap.put("usageCount", 0);
            hashMap.put("id", ""+timestamp);

            hashMapMulti.put(""+timestamp, hashMap);

            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
        ref.updateChildren(
                hashMapMulti,
                (error, ref1) -> {
                    progressDialog.dismiss();
                    Intent dashIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                    dashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(dashIntent);
                    finish();
                }
        );

    }
}
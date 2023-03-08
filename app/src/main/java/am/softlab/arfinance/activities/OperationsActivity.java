package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.adapters.AdapterOperation;
import am.softlab.arfinance.databinding.ActivityOperationsBinding;
import am.softlab.arfinance.models.ModelOperation;

public class OperationsActivity extends AppCompatActivity {

    //view binding
    private ActivityOperationsBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //arraylist to store operation
    private ArrayList<ModelOperation> operationArrayList;
    //adapter
    private AdapterOperation adapterOperation;

    private boolean isIncome;
    private static final String TAG = "OPERATIONS_TAG";

    //resources
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //actType get from intent started from DashboardActivity
        String actType = getIntent().getStringExtra("actType");
        isIncome = actType.equals("income");
        if (isIncome) {
            binding.titleTv.setText(res.getString(R.string.income));
        } else {
            binding.titleTv.setText(res.getString(R.string.expenses));
        }

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadOperations();

        //edit text change listener, search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //noop
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //called as and when user type each letter
                try{
                    adapterOperation.getFilter().filter(charSequence);
                }catch (Exception e){
                    //noop
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //noop
            }
        });

        //handle click, start operation add screen
        binding.addOperationBtn.setOnClickListener(view -> {
            Intent intent = new Intent(OperationsActivity.this, OperationAddActivity.class);
            intent.putExtra("isIncome", isIncome);
            startActivity(intent);
        });

        //handle click, goBack
        binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void loadOperations() {
        // init arraylist
        operationArrayList = new ArrayList<>();
        //get all operations from firebase > Operations
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");
        ref.orderByChild("isIncome").equalTo(isIncome)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // clear arraylist before adding data into it
                        operationArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // get data
                            ModelOperation model = ds.getValue(ModelOperation.class);
                            //add to arraylist
                            operationArrayList.add(model);
                        }
                        //setup adapter
                        adapterOperation = new AdapterOperation(OperationsActivity.this, operationArrayList);
                        //set adapter tp recyclerview
                        binding.operationsRv.setAdapter(adapterOperation);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }

    private void checkUser() {
        // get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            //not logged in, goto main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
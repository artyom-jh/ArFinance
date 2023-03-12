package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import am.softlab.arfinance.R;
import am.softlab.arfinance.adapters.AdapterCategory;
import am.softlab.arfinance.adapters.AdapterWallet;
import am.softlab.arfinance.databinding.ActivityCategoriesBinding;
import am.softlab.arfinance.databinding.ActivityWalletsBinding;
import am.softlab.arfinance.models.ModelCategory;
import am.softlab.arfinance.models.ModelWallet;

public class WalletsActivity extends AppCompatActivity {
    //view binding
    private ActivityWalletsBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //arraylist to store wallet
    private ArrayList<ModelWallet> walletArrayList;
    //adapter
    private AdapterWallet adapterWallet;

    //resources
    private Resources res;

    private static final String TAG = "WALLETS_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadWallets();

        //edit text change listener, search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //noop
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //called as and when user type each letter
                try {
                    adapterWallet.getFilter().filter(charSequence);
                } catch (Exception e) {
                    //noop
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //noop
            }
        });

        //handle click, start category add screen
        binding.addWalletBtn.setOnClickListener(
                view -> {
                    if (firebaseAuth.getCurrentUser() != null) {
                        startActivity(new Intent(WalletsActivity.this, WalletAddActivity.class));
                    }
                    else
                        Toast.makeText(this, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
                }
        );

        //handle click, goback
        binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void loadWallets() {
        // init arraylist
        walletArrayList = new ArrayList<>();
        if (firebaseAuth.getCurrentUser() != null) {
            //get all wallets from firebase > Wallets
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
            ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // clear arraylist before adding data into it
                            walletArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // get data
                                ModelWallet model = ds.getValue(ModelWallet.class);
                                //add to arraylist
                                walletArrayList.add(model);
                            }
                            //setup adapter
                            adapterWallet = new AdapterWallet(WalletsActivity.this, walletArrayList);
                            //set adapter tp recyclerview
                            binding.walletsRv.setAdapter(adapterWallet);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //noop
                        }
                    });
        }
    }
}
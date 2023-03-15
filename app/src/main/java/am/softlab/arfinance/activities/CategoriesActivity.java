package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.adapters.AdapterCategory;
import am.softlab.arfinance.databinding.ActivityCategoriesBinding;
import am.softlab.arfinance.models.ModelCategory;

public class CategoriesActivity extends AppCompatActivity {

    //view binding
    private ActivityCategoriesBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //arraylist to store category
    private ArrayList<ModelCategory> categoryArrayList;
    //adapter
    private AdapterCategory adapterCategory;

    //resources
    private Resources res;

    //progress dialog
    private ProgressDialog progressDialog;

    private static final String TAG = "CATEGORIES_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadCategories();

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
                    adapterCategory.getFilter().filter(charSequence);
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
        binding.addCategoryBtn.setOnClickListener(
                view -> {
                    if (firebaseAuth.getCurrentUser() != null) {
                        startActivity(new Intent(CategoriesActivity.this, CategoryAddActivity.class));
                    }
                    else
                        Toast.makeText(this, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
                }
        );

        //handle click, goback
        binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void loadCategories() {
        progressDialog.setMessage(res.getString(R.string.loading_categories));
        progressDialog.show();

        // init arraylist
        categoryArrayList = new ArrayList<>();

        if (firebaseAuth.getCurrentUser() != null) {
            //get all categories from firebase > Categories
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
            ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // clear arraylist before adding data into it
                            categoryArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // get data
                                ModelCategory model = ds.getValue(ModelCategory.class);
                                //add to arraylist
                                categoryArrayList.add(model);
                            }
                            //setup adapter
                            adapterCategory = new AdapterCategory(CategoriesActivity.this, categoryArrayList);
                            //set adapter tp recyclerview
                            binding.categoriesRv.setAdapter(adapterCategory);

                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    });
        }
        else {
            categoryArrayList.add(MyApplication.getDemoIncomeCategory());
            categoryArrayList.add(MyApplication.getDemoExpensesCategory());

            //setup adapter
            adapterCategory = new AdapterCategory(CategoriesActivity.this, categoryArrayList);
            binding.categoriesRv.setAdapter(adapterCategory);

            progressDialog.dismiss();
        }
    }

}
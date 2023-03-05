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

    private static final String TAG = "CATEGORIES_TAG";

    //resources
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
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
                try{
                    adapterCategory.getFilter().filter(charSequence);
                }catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //noop
            }
        });

        //handle click, start category add screen
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CategoriesActivity.this, CategoryAddActivity.class));
            }
        });

        //handle click, goback
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadCategories() {
        // init arraylist
        categoryArrayList = new ArrayList<>();
        //get all categories from firebase > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist before adding data into it
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    // get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add to arraylist
                    categoryArrayList.add(model);
                }
                //setup adapter
                adapterCategory = new AdapterCategory(CategoriesActivity.this, categoryArrayList);
                //set adapter tp recyclerview
                binding.categoriesRv.setAdapter(adapterCategory);
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
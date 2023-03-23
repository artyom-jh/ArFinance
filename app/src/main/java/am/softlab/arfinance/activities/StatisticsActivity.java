package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import am.softlab.arfinance.BuildConfig;
import am.softlab.arfinance.Constants;
import am.softlab.arfinance.PieFragment;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityStatisticsBinding;
import am.softlab.arfinance.models.ModelCategory;

public class StatisticsActivity extends AppCompatActivity {

    //view binding
    private ActivityStatisticsBinding binding;

    private FirebaseAuth firebaseAuth;

    public ViewPagerAdapter viewPagerAdapter;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    //resources
    private Resources res;

    //progress dialog
    private ProgressDialog progressDialog;

    private static final String TAG = "STATISTICS_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //get resources
        res = this.getResources();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        //handle click, goBack
        binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);

//        List<String> tabsArrayList = Arrays.asList(
//                res.getString(R.string.income),
//                res.getString(R.string.expenses));

        //add data to view pager adapter
        viewPagerAdapter.addFragment(PieFragment.newInstance(
                Constants.PAGE_INCOME
        ), res.getString(R.string.income));
        viewPagerAdapter.addFragment(PieFragment.newInstance(
                Constants.PAGE_EXPENSES
        ), res.getString(R.string.expenses));

        //set adapter to view pager
        viewPager.setAdapter(viewPagerAdapter);

        //refresh list
        viewPagerAdapter.notifyDataSetChanged();
    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<PieFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private final Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(PieFragment fragment, String title) {
            //add fragment passed as parameter in fragmentList
            fragmentList.add(fragment);
            //add title passed as parameter in fragmentTitleList
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }


    private void loadCategories() {
        progressDialog.setMessage(res.getString(R.string.loading_categories));
        progressDialog.show();

        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        if (firebaseAuth.getCurrentUser() != null) {
            if (BuildConfig.DEBUG)
                Log.d(TAG, "loadCategories: Loading categories...");

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

                                categoryTitleArrayList.add(model.getCategory());
                                categoryIdArrayList.add("" + model.getId());

                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "onDataChange: ID: " + model.getId());
                                    Log.d(TAG, "onDataChange: Category: " + model.getCategory());
                                }
                            }

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
            progressDialog.dismiss();
        }
    }
    public String getCategoryNameById(String categoryId) {
        int i = categoryIdArrayList.indexOf(categoryId);
        if (i >=0 )
            return categoryTitleArrayList.get(i);
        return "";
    }
}
package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.PieFragment;
import am.softlab.arfinance.R;
import am.softlab.arfinance.adapters.AdapterCategory;
import am.softlab.arfinance.databinding.ActivityStatisticsBinding;
import am.softlab.arfinance.models.ModelCategory;

public class StatisticsActivity extends AppCompatActivity {

    //view binding
    private ActivityStatisticsBinding binding;

    public ViewPagerAdapter viewPagerAdapter;

    private static final String TAG = "STATISTICS_TAG";

    //resources
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        //handle click, goback
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
        private Context context;

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
}
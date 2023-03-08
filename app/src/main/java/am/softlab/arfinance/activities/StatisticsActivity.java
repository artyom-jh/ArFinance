package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.PieFragment;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityStatisticsBinding;

public class StatisticsActivity extends AppCompatActivity {

    //view binding
    private ActivityStatisticsBinding binding;

    public ViewPagerAdapter viewPagerAdapter;

    private static final String TAG = "STATISTICS_TAG";

    //resources
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

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
}
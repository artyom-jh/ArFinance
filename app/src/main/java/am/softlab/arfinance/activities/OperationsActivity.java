package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.OperationFragment;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityOperationsBinding;
import am.softlab.arfinance.models.ModelWallet;

public class OperationsActivity extends AppCompatActivity {

    //view binding
    private ActivityOperationsBinding binding;

    public OperationsActivity.ViewPagerAdapter viewPagerAdapter;

    private boolean isIncome;

    //resources
    private Resources res;

    //progress dialog
    private ProgressDialog progressDialog;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    private static final String TAG = "OPERATIONS_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOperationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        //get resources
        res = this.getResources();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        //actType get from intent started from DashboardActivity
        String actType = getIntent().getStringExtra("actType");

        isIncome = actType.equals("income");
        if (isIncome) {
            binding.operationsIv.setImageResource(R.drawable.ic_income_white);
            binding.titleTv.setText(res.getString(R.string.income));
        } else {
            binding.operationsIv.setImageResource(R.drawable.ic_expenses_white);
            binding.titleTv.setText(res.getString(R.string.expenses));
        }

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        //handle click, goBack
        binding.backBtn.setOnClickListener(v -> {
            MyApplication.hideKeyboard(this);
            onBackPressed();
        });
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        progressDialog.setMessage(res.getString(R.string.loading_operations));
        progressDialog.show();

        viewPagerAdapter = new OperationsActivity.ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);

        if (firebaseAuth.getCurrentUser() != null) {
            //get all wallets from firebase > Wallets
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
            ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // get data
                                ModelWallet model = ds.getValue(ModelWallet.class);

                                //add data to view pager adapter
                                viewPagerAdapter.addFragment(
                                        OperationFragment.newInstance(
                                                model.getId(),
                                                model.getCurrencySymbol(), isIncome),
                                        String.format("%s(%s)", model.getWalletName(), model.getCurrencySymbol())
                                );
                            }
                            //set adapter to view pager
                            viewPager.setAdapter(viewPagerAdapter);

                            //refresh list
                            viewPagerAdapter.notifyDataSetChanged();

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
            // DEMO
            ModelWallet model = MyApplication.getDemoWallet();
            viewPagerAdapter.addFragment(
                    OperationFragment.newInstance(model.getId(), model.getCurrencySymbol(), isIncome),
                    model.getWalletName()
            );

            //set adapter to view pager
            viewPager.setAdapter(viewPagerAdapter);

            //refresh list
            viewPagerAdapter.notifyDataSetChanged();

            progressDialog.dismiss();
        }
    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<OperationFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private final Context context;

        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(OperationFragment fragment, String title) {
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

    // This function is called when user accept or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(OperationsActivity.this, res.getString(R.string.write_external_granted), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(OperationsActivity.this, res.getString(R.string.write_external_denied), Toast.LENGTH_SHORT).show();
        }
    }
}
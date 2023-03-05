package am.softlab.arfinance.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    //view binding
    private ActivityDashboardBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //resources
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //todo setupViewPagerAdapter(binding.viewPager);
        //todo binding.tabLayout.setupWithViewPager(binding.viewPager);

        //handle click, logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                builder.setTitle(res.getString(R.string.signout))
                        .setMessage(res.getString(R.string.sure_signout))
                        .setPositiveButton(res.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                firebaseAuth.signOut();
                                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });

        //handle click, open profile
        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity((new Intent(DashboardActivity.this, ProfileActivity.class)));
                }
                else {
                    Toast.makeText(DashboardActivity.this, res.getString(R.string.login_first), Toast.LENGTH_SHORT).show();
                }
            }
        });


        /* ---------- CARDS ---------- */

        //handle card click, Income
        binding.incomeCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(DashboardActivity.this, OperationsActivity.class);
                    intent.putExtra("actType", "income");
                    startActivity(intent);
                }
                else {
                    Toast.makeText(DashboardActivity.this, res.getString(R.string.login_first), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //handle card click, Expenses
        binding.expensesCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(DashboardActivity.this, OperationsActivity.class);
                    intent.putExtra("actType", "expenses");
                    startActivity(intent);
                }
                else {
                    Toast.makeText(DashboardActivity.this, res.getString(R.string.login_first), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //handle card click, Statistics
        binding.statisticsCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity((new Intent(DashboardActivity.this, StatisticsActivity.class)));
                }
                else {
                    Toast.makeText(DashboardActivity.this, res.getString(R.string.login_first), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //handle card click, Categories
        binding.categoriesCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity((new Intent(DashboardActivity.this, CategoriesActivity.class)));
                }
                else {
                    Toast.makeText(DashboardActivity.this, res.getString(R.string.login_first), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkUser() {
        // get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            //not logged in
            binding.subTitleTv.setText("Not Logged In");
        }
        else{
            //logged in, get user info
            String email = firebaseUser.getEmail();
            //set in textview of toolbar
            binding.subTitleTv.setText(email);
        }
    }
}
package am.softlab.arfinance.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.DBManager;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.databinding.ActivityDashboardBinding;

public class DashboardActivity extends AppCompatActivity {

    //view binding
    private ActivityDashboardBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //resources
    private Resources res;
    private DBManager mDBManager;

    private PowerMenu powerMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        this.mDBManager = ((MyApplication) DashboardActivity.this.getApplicationContext()).getDBManager();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int screenheight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;

        powerMenu = new PowerMenu.Builder(this)
                .addItem(new PowerMenuItem(res.getString(R.string.profile), false, R.drawable.ic_person_black))
                .setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.gray01)))
                .setDividerHeight(1)

                .addItem(new PowerMenuItem(res.getString(R.string.scheduler), false, R.drawable.ic_schedule_black))
                .setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.gray01)))
                .setDividerHeight(1)

                .addItem(new PowerMenuItem(res.getString(R.string.settings), false, R.drawable.ic_settings_black))
                .setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.gray01)))
                .setDividerHeight(1)

                .addItem(new PowerMenuItem(res.getString(R.string.about), false))

                .setWidth(screenWidth*3/5)
                .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT).
                .setMenuRadius(10f) // sets the corner radius.
                .setMenuShadow(10f) // sets the shadow.
                .setTextColor(ContextCompat.getColor(this, R.color.black))
                .setTextSize(22)
                //.setTextGravity(Gravity.CENTER)
                //.setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)
                .setMenuColor(Color.WHITE)
                .setSelectedMenuColor(ContextCompat.getColor(this, R.color.black))
                .setOnMenuItemClickListener(onMenuItemClickListener)
                .build();

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //handle click, logout
        binding.logoutBtn.setOnClickListener(view -> {
            //confirm delete dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
            builder.setTitle(res.getString(R.string.signout))
                    .setMessage(res.getString(R.string.sure_signout))
                    .setPositiveButton(
                            res.getString(R.string.signout),
                            (dialogInterface, i) -> {
                                firebaseAuth.signOut();
                                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                                finish();
                            }
                    )
                    .setNegativeButton(
                            res.getString(R.string.cancel),
                            (dialogInterface, i) -> dialogInterface.dismiss()
                    )
                    .show();
        });

        //handle click, open profile
        binding.dropdownMenuBtn.setOnClickListener(v -> {
            //show dropdown menu
            powerMenu.showAsDropDown(binding.dropdownMenuBtn);
        });


        /* ---------- CARDS ---------- */

        //handle card click, Income
        binding.incomeCv.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, OperationsActivity.class);
            intent.putExtra("actType", "income");
            startActivity(intent);
        });

        //handle card click, Expenses
        binding.expensesCv.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, OperationsActivity.class);
            intent.putExtra("actType", "expenses");
            startActivity(intent);
        });

        //handle card click, Statistics
        binding.statisticsCv.setOnClickListener(v -> startActivity((new Intent(DashboardActivity.this, StatisticsActivity.class))));

        //handle card click, Categories
        binding.categoriesCv.setOnClickListener(v -> startActivity((new Intent(DashboardActivity.this, CategoriesActivity.class))));

        //handle card click, Wallets
        binding.walletsCv.setOnClickListener(v -> startActivity((new Intent(DashboardActivity.this, WalletsActivity.class))));
    }

    private final OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
            powerMenu.dismiss();

            if (position == 0) {                //Profile
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity((new Intent(DashboardActivity.this, ProfileActivity.class)));
                } else {
                    Toast.makeText(DashboardActivity.this, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
                }
            }
            else if (position == 1) {           //Scheduler
                if (MyApplication.checkPermission(
                        DashboardActivity.this,
                        new String[] {Manifest.permission.POST_NOTIFICATIONS},
                        Constants.POST_NOTIFICATIONS))
                {
                    startSchedulerActivity();
                }
            }
            else if (position == 2) {           //Settings
                startActivity((new Intent(DashboardActivity.this, SettingsActivity.class)));
            }
            else if (position == 3) {           //About
                startActivity((new Intent(DashboardActivity.this, AboutActivity.class)));
            }
        }
    };

    public void startSchedulerActivity() {
        MyApplication.createNotificationChannel();
        //MyApplication.showNotification("test");

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity((new Intent(DashboardActivity.this, SchedulerActivity.class)));
        } else {
            Toast.makeText(DashboardActivity.this, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
        }
    }

    // This function is called when user accept or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(DashboardActivity.this, res.getString(R.string.post_notification_granted), Toast.LENGTH_SHORT).show();
            startSchedulerActivity();
        } else {
            Toast.makeText(DashboardActivity.this, res.getString(R.string.post_notification_denied), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUser() {
        // get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            //not logged in
            binding.subTitleTv.setText(res.getString(R.string.not_logged_in));
        }
        else{
            //logged in, get user info
            String email = firebaseUser.getEmail();
            //set in textview of toolbar
            binding.subTitleTv.setText(email);

            //show progress
            progressDialog.setMessage(res.getString(R.string.please_wait));
            progressDialog.show();

            MyApplication.loadWalletList(progressDialog);
            // ATTENTION 1: loadCategoryList() called from loadWalletList
            // ATTENTION 2: loadSchedulers() called from loadCategoryList
            // ATTENTION 3: periodicWork() called from loadSchedulers
        }
    }
}
package am.softlab.arfinance;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import am.softlab.arfinance.models.ModelCategory;
import am.softlab.arfinance.models.ModelWallet;

public class MyApplication extends Application {

    private static Context context;
    public static Context getContext() {
        return context;
    }

    private static List<List<String>> categoryArrayList = new ArrayList<List<String>>();

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    //created a static method to convert timestamp to proper date format, so we can use
    //it everywhere in project, no need to rewrite again
    public static String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        //formatting timestamp to dd/MM/yyyy
        return DateFormat.format("dd/MM/yyyy", cal).toString();
    }

    public static String formatDouble(double number){
        DecimalFormat formatterDecimal = new DecimalFormat("#,###,##0.00");
        return formatterDecimal.format(number);
    }

    public static String formatInteger(int number){
        DecimalFormat formatterDecimal = new DecimalFormat("#,###,##0");
        return formatterDecimal.format(number);
    }

    public static void loadCategoryList() {
        //get category using categoryId
        categoryArrayList = new ArrayList<List<String>>();

        //get all categories from firebase > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist before adding data into it
                categoryArrayList.clear();

                for (DataSnapshot ds: snapshot.getChildren()){
                    // get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add to arraylist
                    List<String> currentList = new ArrayList<String>();
                    currentList.add(model.getId());
                    currentList.add(model.getCategory());
                    categoryArrayList.add(currentList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //noop
            }
        });
    }
    public static String getCategoryById(String catId) {
        if (categoryArrayList != null && categoryArrayList.size() > 1) {
            for (List<String> stringList : categoryArrayList) {
                if (stringList.get(0).equals(catId)) {
                    return stringList.get(1);
                }
            }
        }
        return "";
    }

    public static void updateWalletBalance(String walletId, double addAmount, boolean isIncome, int transactionType) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
        ref.child(walletId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //1) Get current usageCount and amounts - balance, totalIncome, totalExpenses;
                        double balance, totalIncome, totalExpenses;
                        int usageCount;

                        //totalIncome
                        String amountStr = ""+snapshot.child("totalIncome").getValue();
                        if (amountStr.equals("") || amountStr.equals("null"))
                            totalIncome = 0.0;
                        else
                            totalIncome = Double.parseDouble(amountStr);

                        //totalExpenses
                        amountStr = ""+snapshot.child("totalExpenses").getValue();
                        if (amountStr.equals("") || amountStr.equals("null"))
                            totalExpenses = 0.0;
                        else
                            totalExpenses = Double.parseDouble(amountStr);

                        //usageCount
                        amountStr = ""+snapshot.child("usageCount").getValue();
                        if (amountStr.equals("") || amountStr.equals("null"))
                            usageCount = 0;
                        else
                            usageCount = Integer.parseInt(amountStr);

                        //2) Increment usageCount and Increment/Ddecrease amounts - balance, totalIncome, totalExpenses
                        if (isIncome)
                            totalIncome += addAmount;
                        else
                            totalExpenses += addAmount;

                        balance = totalIncome - totalExpenses;

                        if (transactionType == Constants.ROW_ADDED)
                            usageCount++;
                        else if (transactionType == Constants.ROW_DELETED)
                            usageCount--;

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("balance", balance);
                        hashMap.put("totalIncome", totalIncome);
                        hashMap.put("totalExpenses", totalExpenses);
                        hashMap.put("usageCount", usageCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Wallets");
                        reference.child(walletId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }

    public static void updateCategoryUsage(String categoryId, int incrementValue) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get usage count
                        String usageCountStr = ""+snapshot.child("usageCount").getValue();
                        //in case of null replace with 0
                        if (usageCountStr.equals("") || usageCountStr.equals("null")) {
                            usageCountStr = "0";
                        }

                        //2) Increment views count
                        int newUsageCount = Integer.parseInt(usageCountStr) + incrementValue;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("usageCount", newUsageCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
                        reference.child(categoryId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }


    // Function to check and request permission
    public static boolean checkPermission(Activity activity, String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MyApplication.context, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, new String[] { permission }, requestCode);
            return false;
        }
        return true;
    }
    public static boolean checkPermission(Activity activity, String[] permissions, int requestCode) {
        boolean retVal = true;
        ArrayList<String> permsList = new ArrayList<String>();

        for( String oneItem : permissions ) {
            // Checking if permission is not granted
            if (ContextCompat.checkSelfPermission(MyApplication.context, oneItem) == PackageManager.PERMISSION_DENIED) {
                permsList.add(oneItem);
                retVal = false;
            }
        }

        if (permsList.size() > 0) {
            String[] perms = permsList.toArray(new String[permsList.size()]);
            ActivityCompat.requestPermissions(activity, perms, requestCode);
        }

        return retVal;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        view.clearFocus();
    }

    public static void hideKeyboard(Fragment fragment) {
        InputMethodManager imm = (InputMethodManager) fragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        //Find the currently focused view, so we can grab the correct window token from it.
        View view = fragment.getView().getRootView();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(fragment.getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        view.clearFocus();
    }

    // ===== DEMOS =====
    public static ModelWallet getDemoWallet() {
        return new ModelWallet(
                "1234567000001",
                "Demo Wallet",
                "Demo Notes",
                "222",
                "US Dollar",
                "USD",
                "$",
                1000,
                3000,
                2000,
                1,
                1678658900001L
        );
    }
    public static ModelCategory getDemoIncomeCategory() {
        return new ModelCategory(
                "1234567000002",
                "Demo Income Category",
                "Demo Income Notes",
                "",
                true,
                0,
                1678658900002L
        );
    }
    public static ModelCategory getDemoExpensesCategory() {
        return new ModelCategory(
                "1234567000003",
                "Demo Expenses Category",
                "Demo Expenses Notes",
                "",
                false,
                0,
                1678658900003L
        );
    }
}

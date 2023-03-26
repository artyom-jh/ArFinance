package am.softlab.arfinance;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import am.softlab.arfinance.activities.AttachmentViewActivity;
import am.softlab.arfinance.activities.DashboardActivity;
import am.softlab.arfinance.models.ModelCategory;
import am.softlab.arfinance.models.ModelSchedule;
import am.softlab.arfinance.models.ModelWallet;

public class MyApplication extends Application {

    private static Context mContext;
    public static Context getContext() {
        return mContext;
    }

    private static List<ModelCategory> categoryArrayList = new ArrayList<ModelCategory>();
    private static List<List<String>> walletArrayList = new ArrayList<List<String>>();
    private static ArrayList<ModelSchedule> scheduleArrayList = new ArrayList<ModelSchedule>();

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.mContext = getApplicationContext();
    }

    public static List<ModelCategory> getCategoryArrayList() {
        return categoryArrayList;
    }

    //created a static method to convert timestamp to proper date format, so we can use
    //it everywhere in project, no need to rewrite again
    public static String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        //formatting timestamp to dd/MM/yyyy
        return DateFormat.format("dd/MM/yyyy", cal).toString();
    }
    public static String formatTimestamp2(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        //formatting timestamp to dd/MM/yyyy
        return DateFormat.format("yyyy_MM_dd", cal).toString();
    }
    public static String formatTimestampShort(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        //formatting timestamp to dd/MM/yyyy
        return DateFormat.format("dd/MM/yy", cal).toString();
    }

    public static String formatDouble(double number){
        DecimalFormat formatterDecimal = new DecimalFormat("#,###,##0.00");
        return formatterDecimal.format(number);
    }

    public static String formatInteger(int number){
        DecimalFormat formatterDecimal = new DecimalFormat("#,###,##0");
        return formatterDecimal.format(number);
    }

    public static void startAttachmentViewActivity(Context context, String id, String category, long operationTimestamp, String imageUrl, Uri imageUri) {
        Intent intent = new Intent(context, AttachmentViewActivity.class);
        intent.putExtra("operId", id);
        intent.putExtra("categoryName", category);
        intent.putExtra("operationTimestamp", operationTimestamp);
        intent.putExtra("imageUrl", imageUrl);
        if (imageUri == null)
            intent.putExtra("imageUri", "");
        else
            intent.putExtra("imageUri", imageUri.toString());
        context.startActivity(intent);
    }

    public static void loadCategoryList(ProgressDialog progressDialog) {
        // clear arraylist before adding data into it
        categoryArrayList.clear();

        if (progressDialog != null)
            progressDialog.setMessage(mContext.getResources().getString(R.string.loading_categories));

        //get all categories from firebase > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    // get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add to arraylist
                    categoryArrayList.add(model);
                }

                loadSchedulers(progressDialog, true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog != null)
                    progressDialog.dismiss();
            }
        });
    }
    public static String getCategoryById(String catId) {
        if (categoryArrayList != null && categoryArrayList.size() > 0 && catId != null) {
            for (ModelCategory model : categoryArrayList) {
                if (model.getId() != null && model.getId().equals(catId)) {
                    return model.getCategory();
                }
            }
        }
        return "";
    }

    public static void loadWalletList(ProgressDialog progressDialog) {
        //get wallet using walletId
        walletArrayList = new ArrayList<List<String>>();

        if (progressDialog != null)
            progressDialog.setMessage(mContext.getResources().getString(R.string.loading_wallets));

        //get all wallets from firebase > Wallets
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist before adding data into it
                walletArrayList.clear();

                for (DataSnapshot ds: snapshot.getChildren()){
                    // get data
                    ModelWallet model = ds.getValue(ModelWallet.class);
                    //add to arraylist
                    List<String> currentList = new ArrayList<String>();
                    currentList.add(model.getId());
                    currentList.add(model.getWalletName());
                    currentList.add(model.getCurrencySymbol());
                    walletArrayList.add(currentList);
                }

                loadCategoryList(progressDialog);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog != null)
                    progressDialog.dismiss();
            }
        });
    }
    public static String getWalletById(String walletId) {
        if (walletArrayList != null && walletArrayList.size() > 1) {
            for (List<String> stringList : walletArrayList) {
                if (stringList.get(0).equals(walletId)) {
                    return stringList.get(1);
                }
            }
        }
        return "";
    }

    public static String getWalletSymbolById(String walletId) {
        if (walletArrayList != null && walletArrayList.size() > 1) {
            for (List<String> stringList : walletArrayList) {
                if (stringList.get(0).equals(walletId)) {
                    return stringList.get(2);
                }
            }
        }
        return "";
    }

    public static void loadSchedulers(ProgressDialog progressDialog, boolean runPeriodicWork) {
        if (progressDialog != null)
            progressDialog.setMessage(mContext.getResources().getString(R.string.loading_schedulers));

        //get schedullers from firebase > Schedulers
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Schedulers");
        ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // clear arraylist before adding data into it
                        scheduleArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelSchedule model = ds.getValue(ModelSchedule.class);
                            if (model.getEnabled())
                                scheduleArrayList.add(model);
                        }

                        if (runPeriodicWork)
                            periodicWork(progressDialog);
                        else {
                            if (progressDialog != null)
                                progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (progressDialog != null)
                            progressDialog.dismiss();
                    }
                });
    }
    public static ArrayList<ModelSchedule> getScheduleArrayList() {
        return scheduleArrayList;
    }

    public static String getPeriodById(int periodId) {
        switch(periodId) {
            case Constants.PERIOD_DAILY: return Constants.periodsArray[Constants.PERIOD_DAILY];
            case Constants.PERIOD_WEEKLY: return Constants.periodsArray[Constants.PERIOD_WEEKLY];
            case Constants.PERIOD_MONTHLY: return Constants.periodsArray[Constants.PERIOD_MONTHLY];
            case Constants.PERIOD_YEARLY: return Constants.periodsArray[Constants.PERIOD_YEARLY];
            default: return Constants.periodsArray[Constants.PERIOD_UNKNOWN];
        }
    }

    public static void updateLastRunAndBalance(
            String scheduleId,
            long lastStartDateTime,
            String walletId,
            String categoryId,
            double addAmount,
            boolean isIncome,
            int transactionType )
    {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lastStartDateTime", lastStartDateTime);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Schedulers");
        reference.child(scheduleId)
                .updateChildren(hashMap)
                .addOnCompleteListener(task -> updateWalletBalance(walletId, categoryId, addAmount, isIncome, transactionType));
    }

    public static void updateWalletBalance(String walletId, String categoryId, double addAmount, boolean isIncome, int transactionType) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
        ref.child(walletId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //1) Get current usageCount and amounts - balance, totalIncome, totalExpenses;
                        double balance, totalIncome, totalExpenses;
                        int usageCount;

                        //totalIncome
                        String amountStr = "";
                        if (snapshot.child("totalIncome") != null)
                            amountStr = ""+snapshot.child("totalIncome").getValue();

                        if (amountStr.equals("") || amountStr.equals("null"))
                            totalIncome = 0.0;
                        else
                            totalIncome = Double.parseDouble(amountStr);

                        //totalExpenses
                        amountStr = "";
                        if (snapshot.child("totalExpenses") != null)
                            amountStr = ""+snapshot.child("totalExpenses").getValue();

                        if (amountStr.equals("") || amountStr.equals("null"))
                            totalExpenses = 0.0;
                        else
                            totalExpenses = Double.parseDouble(amountStr);

                        //usageCount
                        amountStr = "";
                        if (snapshot.child("usageCount") != null)
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
                                .updateChildren(hashMap)
                                .addOnCompleteListener(task -> {
                                    if (!categoryId.isEmpty() && (transactionType == Constants.ROW_ADDED)) {
                                        updateCategoryUsage(categoryId, 1);
                                    }
                                    else if (!categoryId.isEmpty() && (transactionType == Constants.ROW_DELETED)) {
                                        updateCategoryUsage(categoryId, -1);
                                    }
                                });
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
                        String usageCountStr = "";
                        if (snapshot.child("usageCount") != null)
                            usageCountStr = ""+snapshot.child("usageCount").getValue();

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
        // Android 10(Q) - API 29
        // Android 10(R) - API 30 and later - dont check EXTERNAL_STORAGE
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) || permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) )
        {
            return true;
        }
        // Checking if permission is not granted
        else if (ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(activity, new String[] { permission }, requestCode);
            return false;
        }

        return true;
    }

    public static boolean checkPermission(Activity activity, String[] permissions, int requestCode) {
        boolean retVal = true;
        ArrayList<String> permsList = new ArrayList<String>();

        for( String oneItem : permissions ) {
            // Android 10(Q) - API 29
            // Android 10(R) - API 30 and later - dont check EXTERNAL_STORAGE
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    && (oneItem.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) || oneItem.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) )
            {
                continue;
            }

            // Checking if permission is not granted
            if (ContextCompat.checkSelfPermission(mContext, oneItem) == PackageManager.PERMISSION_DENIED) {
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


    public static void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mContext.getResources().getString(R.string.notification_channel_name);
            String description = mContext.getResources().getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;  //.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(String msg) {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent notifyIntent = new Intent(mContext, DashboardActivity.class);
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                mContext, 0, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, Constants.CHANNEL_ID);
        builder.setContentIntent(notifyPendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.logo));
        builder.setSmallIcon(R.drawable.ic_notification_icon);
        builder.setContentTitle(mContext.getResources().getString(R.string.notification_channel_name));
        builder.setContentText(msg);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setSubText("");

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        notificationManager.notify(Constants.POST_NOTIFICATIONS, builder.build());
    }


    // !!! oneTimeWork example !!!
//    private  void oneTimeWork() {
//        WorkRequest workRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
//                .setConstraints(new Constraints.Builder()
//                        .setRequiredNetworkType(NetworkType.CONNECTED)
//                        .build())
//                .build();
//
//        WorkManager.getInstance(this)
//                .enqueue(workRequest);
//    }

    public static void periodicWork(ProgressDialog progressDialog) {
        WorkManager workManager = WorkManager.getInstance(mContext);
        //workManager.cancelUniqueWork(Constants.WORK_ID);
        workManager.cancelAllWorkByTag(Constants.WORK_ID);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                //.setRequiresCharging(true)        // dont work on virtual android !!!
                .build();

        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                MyWorker.class,
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS
            ).setConstraints(constraints)
                .build();

        workManager.enqueueUniquePeriodicWork(
                Constants.WORK_ID,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                periodicWorkRequest);

        if (progressDialog != null)
            progressDialog.dismiss();
    }


    public static void downloadImage(Context context, String categoryName, long operationTimestamp, String imageUrl, Uri imageUri) {
        if (BuildConfig.DEBUG)
            Log.d(TAG_DOWNLOAD, "downloadImage: downloading image...");

        String nameWithExtension = categoryName + "_" + formatTimestamp2(operationTimestamp) + ".jpg";

        if (BuildConfig.DEBUG)
            Log.d(TAG_DOWNLOAD, "downloadImage: NAME: " + nameWithExtension);

        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getResources().getString(R.string.please_wait));
        progressDialog.setMessage("Downloading " + nameWithExtension + "...");  //e.g. Downloading ABC.jpg
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //download from firebase storage using url
        if (imageUri != null) {
            try {
                InputStream iStream =  context.getContentResolver().openInputStream(imageUri);
                byte[] bytes = getBytes(iStream);
                saveDownloadedImage(context, progressDialog, bytes, nameWithExtension);
            }
            catch (Exception e) {
                //noop
            }
        }
        else {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            storageReference.getBytes(Constants.MAX_BYTES_UPLOAD)
                    .addOnSuccessListener(bytes -> {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG_DOWNLOAD, "onSuccess: Image Downloaded");
                            Log.d(TAG_DOWNLOAD, "onSuccess: Saving image...");
                        }
                        saveDownloadedImage(context, progressDialog, bytes, nameWithExtension);
                    })
                    .addOnFailureListener(e -> {
                        if (BuildConfig.DEBUG)
                            Log.d(TAG_DOWNLOAD, "onFailure: Failed to download due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to download due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private static void saveDownloadedImage(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension) {
        if (BuildConfig.DEBUG)
            Log.d(TAG_DOWNLOAD, "saveDownloadedImage: Saving downloaded image");

        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            if (BuildConfig.DEBUG)
                Log.d(TAG_DOWNLOAD, "saveDownloadedImage: Saved to Download Folder");

            progressDialog.dismiss();
        }
        catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.d(TAG_DOWNLOAD, "saveDownloadedImage: Failed saving to Download Folder due to " + e.getMessage());

            Toast.makeText(context, "Failed saving to Download Folder due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }


    public static Drawable getCountryFlag(String currencyCode) {
        switch(currencyCode) {
            case "AMD": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_armenia, null);
            case "AUD": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_australia, null);
            case "GBP": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_uk, null);
            case "CAD": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_canada, null);
            case "CNY": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_china, null);
            case "EUR": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_eu, null);
            case "JPY": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_japan, null);
            case "RUB": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_russia, null);
            case "KRW": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_south_korea, null);
            case "CHF": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_switzerland, null);
            case "USD": return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_us, null);
            default: return ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.flag_empty, null);
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        return getBitmapFromVectorDrawable(context, drawable);
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
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

    public static ModelSchedule getDemoSchedule() {
        return new ModelSchedule(
                "1234567000044",
                "Demo Schedule",
                "",
                false,
                1678658900002L,
                "1234567000001",
                true,
                "1234567000003",
                1000,
                Constants.PERIOD_MONTHLY,
                0L,
                1678658900002L
        );
    }
}

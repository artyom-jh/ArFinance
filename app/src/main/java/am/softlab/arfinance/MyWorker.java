package am.softlab.arfinance;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import am.softlab.arfinance.models.ModelSchedule;

public class MyWorker extends Worker {

    private Context context;

    private final FirebaseAuth firebaseAuth;

    //resources
    private final Resources res;

    private static final String TAG = "MYWORKER_TAG";

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

        //get resources
        res = this.context.getResources();

        firebaseAuth = FirebaseAuth.getInstance();

        MyApplication.createNotificationChannel();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (BuildConfig.DEBUG)
            Log.d(TAG, "doWork: success function called");

        if (MyApplication.getScheduleArrayList().size() <= 0)
            return Result.success();

        HashMap<String, Object> hashMapMulti = new HashMap<>();

        for (ModelSchedule model : MyApplication.getScheduleArrayList()) {
            long currentTimestamp = System.currentTimeMillis();
            long startDateTime = model.getStartDateTime();
            long lastStartDateTime = model.getLastStartDateTime();

            int period = model.getPeriod();
            long diffMillis;
            switch(period) {
                case Constants.PERIOD_DAILY: diffMillis = Constants.MILLIS_IN_DAY; break;
                case Constants.PERIOD_WEEKLY: diffMillis = Constants.MILLIS_IN_WEEK; break;
                case Constants.PERIOD_MONTHLY: diffMillis = Constants.MILLIS_IN_MONTH; break;
                case Constants.PERIOD_YEARLY: diffMillis = Constants.MILLIS_IN_YEAR; break;
                default: diffMillis = 0L; break;
            }

            if (diffMillis <= 0 || startDateTime > currentTimestamp || lastStartDateTime >= currentTimestamp)
                continue;

            while ( (lastStartDateTime == 0 && startDateTime <= currentTimestamp)
                    || (lastStartDateTime+diffMillis <= currentTimestamp) )
            {
                if (lastStartDateTime == 0 && startDateTime <= currentTimestamp)
                    lastStartDateTime = startDateTime;
                else
                    lastStartDateTime += diffMillis;

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("id", "" + currentTimestamp);
                hashMap.put("operationTimestamp", lastStartDateTime);
                hashMap.put("walletId", model.getWalletId());
                hashMap.put("categoryId", model.getCategoryId());
                hashMap.put("notes", "Scheduled work - " + model.getName());
                hashMap.put("uid", "" + firebaseAuth.getUid());
                hashMap.put("isIncome", model.getIsIncome());
                hashMap.put("amount", model.getAmount());
                hashMap.put("uid_walletId", "" + firebaseAuth.getUid() + "_" + model.getWalletId());
                hashMap.put("imageUrl", "");
                hashMap.put("timestamp", currentTimestamp);

                hashMapMulti.put("" + currentTimestamp, hashMap);

                try {
                    TimeUnit.MILLISECONDS.sleep(3);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }

                MyApplication.updateLastRunAndBalance(
                        model.getId(),
                        lastStartDateTime,
                        model.getWalletId(),
                        model.getCategoryId(),
                        model.getAmount(),
                        model.getIsIncome(),
                        Constants.ROW_ADDED
                );
            }
        }

        if (hashMapMulti.size() > 0) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");
            ref.updateChildren(
                    hashMapMulti,
                    (error, ref1) -> {
                        MyApplication.showNotification(res.getString(R.string.tasks_completed));
                    }
            );
        }

        return Result.success();
    }

}

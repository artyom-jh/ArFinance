package am.softlab.arfinance;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker {

    private Context context;

    //resources
    private final Resources res;

    private static final String TAG = "MYWORKER_TAG";

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

        //get resources
        res = this.context.getResources();

        MyApplication.createNotificationChannel();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: success function called");

        //showNotification("Test");

        return Result.success();
    }

}

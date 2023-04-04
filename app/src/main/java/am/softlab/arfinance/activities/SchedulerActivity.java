package am.softlab.arfinance.activities;

import static am.softlab.arfinance.utils.ActivityUtils.hideKeyboardInView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.adapters.AdapterSchedule;
import am.softlab.arfinance.databinding.ActivitySchedulerBinding;
import am.softlab.arfinance.models.ModelSchedule;

public class SchedulerActivity extends AppCompatActivity {

    //view binding
    private ActivitySchedulerBinding binding;

    //firebase auth, for loading user data using user uid
    private FirebaseAuth firebaseAuth;
    //firebase current user
    private FirebaseUser firebaseUser;

    //arraylist to store schedule
    private ArrayList<ModelSchedule> scheduleArrayList;
    //adapter
    private AdapterSchedule adapterSchedule;

    //progress dialog
    private ProgressDialog progressDialog;

    //resources
    private Resources res;

    private static final String TAG = "SCHEDULER_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySchedulerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get resources
        res = this.getResources();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        //setup firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        loadSchedulers();

        //handle click, start schedule add screen
        binding.addScheduleBtn.setOnClickListener(
                view -> {
                    if (firebaseUser != null) {
                        startActivity(new Intent(SchedulerActivity.this, ScheduleAddActivity.class));
                    }
                    else
                        Toast.makeText(this, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
                }
        );

        //handle click, goback
        binding.backBtn.setOnClickListener(v -> {
            hideKeyboardInView(this);
            onBackPressed();
        });
    }

    private void loadSchedulers() {
        progressDialog.setMessage(res.getString(R.string.loading_schedulers));
        progressDialog.show();

        // init arraylist
        scheduleArrayList = new ArrayList<>();

        if (firebaseAuth.getCurrentUser() != null) {
            //get all schedulers from firebase > Schedulers
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Schedulers");
            ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // clear arraylist before adding data into it
                            scheduleArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // get data
                                ModelSchedule model = ds.getValue(ModelSchedule.class);
                                //add to arraylist
                                scheduleArrayList.add(model);
                            }
                            //setup adapter
                            adapterSchedule = new AdapterSchedule(SchedulerActivity.this, scheduleArrayList);
                            //set adapter tp recyclerview
                            binding.schedulesRv.setAdapter(adapterSchedule);

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
            scheduleArrayList.add(MyApplication.getDemoSchedule());

            //setup adapter
            adapterSchedule = new AdapterSchedule(SchedulerActivity.this, scheduleArrayList);
            binding.schedulesRv.setAdapter(adapterSchedule);

            progressDialog.dismiss();
        }
    }
}
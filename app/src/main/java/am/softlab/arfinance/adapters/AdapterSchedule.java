package am.softlab.arfinance.adapters;

import static am.softlab.arfinance.utils.DateTimeUtils.formatTimestamp;
import static am.softlab.arfinance.utils.NumberUtils.formatDouble;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.activities.ScheduleAddActivity;
import am.softlab.arfinance.databinding.RowScheduleBinding;
import am.softlab.arfinance.models.ModelSchedule;

public class AdapterSchedule extends RecyclerView.Adapter<AdapterSchedule.HolderSchedule> {
    private final Context context;
    public ArrayList<ModelSchedule> scheduleArrayList;

    //view binding
    private RowScheduleBinding binding;
    private final ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    //firebase current user
    private FirebaseUser firebaseUser;


    //resources
    private final Resources res;

    public AdapterSchedule(Context context, ArrayList<ModelSchedule> scheduleArrayList) {
        this.context = context;
        this.scheduleArrayList = scheduleArrayList;

        //get resources
        res = this.context.getResources();

        progressDialog = new ProgressDialog(this.context);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public AdapterSchedule.HolderSchedule onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind row_schedule.xml
        binding = RowScheduleBinding.inflate(LayoutInflater.from(context), parent, false);

        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        return new AdapterSchedule.HolderSchedule(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSchedule.HolderSchedule holder, int position) {
        // get data
        ModelSchedule model = scheduleArrayList.get(position);
        String id = model.getId();
        String wallet = MyApplication.getWalletById(model.getWalletId());
        String currencySymbol = MyApplication.getWalletSymbolById(model.getWalletId());
        String category = MyApplication.getCategoryById(model.getCategoryId());
        double amount = model.getAmount();
        long scheduleTimestamp = model.getStartDateTime();

        //format date, already made in function in MyApplication class
        String dateStr = formatTimestamp(scheduleTimestamp);
        String amountStr = formatDouble(amount) + " " + currencySymbol;
        String periodStr = MyApplication.getPeriodById(model.getPeriod());

        //set data
        holder.scheduleNameTv.setText(model.getName());
        holder.scheduleStartDateTv.setText(dateStr);
        holder.scheduleWalletTv.setText(wallet);

        if (model.getIsIncome()) {
            binding.scheduleTypeTv.setText(res.getString(R.string.income));
        } else {
            binding.scheduleTypeTv.setText(res.getString(R.string.expenses));
        }

        holder.scheduleCategoryTv.setText(category);
        holder.scheduleAmountTv.setText(amountStr);
        holder.schedulePeriodTv.setText(periodStr);
        holder.scheduleEnabledSw.setChecked(model.getEnabled());

        // handle click, delete schedule
        holder.deleteBtn.setOnClickListener(view -> {
            if (!binding.scheduleEnabledSw.isChecked()) {
                if (firebaseAuth.getCurrentUser() != null) {
                    if (firebaseUser.isEmailVerified()) {
                        //confirm delete dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(res.getString(R.string.delete))
                                .setMessage(res.getString(R.string.sure_delete_schedule))
                                .setPositiveButton(res.getString(R.string.delete), (dialogInterface, i) -> {
                                    //begin delete
                                    Toast.makeText(context, res.getString(R.string.deleting), Toast.LENGTH_SHORT).show();
                                    deleteSchedule(model);
                                })
                                .setNegativeButton(res.getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                                .show();
                    } else
                        Toast.makeText(context, res.getString(R.string.not_verified_detailed), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(context, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, res.getString(R.string.deny_change_schedule), Toast.LENGTH_SHORT).show();
        });

        //handle item click
        holder.itemView.setOnClickListener(v -> {
            if (!binding.scheduleEnabledSw.isChecked()) {
                if (firebaseAuth.getCurrentUser() != null) {
                    if (firebaseUser.isEmailVerified()) {
                        Intent intent = new Intent(context, ScheduleAddActivity.class);
                        intent.putExtra("scheduleId", id);
                        context.startActivity(intent);
                    } else
                        Toast.makeText(context, res.getString(R.string.not_verified_detailed), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(context, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, res.getString(R.string.deny_change_schedule), Toast.LENGTH_SHORT).show();
        });

        holder.scheduleEnabledSw.setOnClickListener(view -> {
            if (firebaseAuth.getCurrentUser() != null) {
                if (firebaseUser.isEmailVerified()) {
                    final Switch btn = (Switch) view;
                    final boolean switchChecked = btn.isChecked();

                    btn.setChecked(!switchChecked);

                    final String positiveMsg = switchChecked ? res.getString(R.string.start) : res.getString(R.string.stop);
                    final String titleMsg = switchChecked ? res.getString(R.string.enable_schedule) : res.getString(R.string.disable_schedule);
                    final String bodyMsg = switchChecked ? res.getString(R.string.sure_start_schedule) : res.getString(R.string.sure_stop_schedule);

                    //confirm delete dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(titleMsg)
                            .setMessage(bodyMsg)
                            .setPositiveButton(positiveMsg,
                                    (dialogInterface, i) -> setScheduleEnabled(holder, id, btn, switchChecked))
                            .setNegativeButton(res.getString(R.string.cancel), null)
                            .show();
                }
                else
                    Toast.makeText(context, res.getString(R.string.not_verified_detailed), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
        });
    }

    public void setScheduleEnabled(@NonNull AdapterSchedule.HolderSchedule holder, String scheduleId, Switch btn, boolean switchChecked) {
        if (switchChecked)
            progressDialog.setMessage(res.getString(R.string.starting));
        else
            progressDialog.setMessage(res.getString(R.string.stopping));
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("enabled", switchChecked);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Schedulers");
        reference.child(scheduleId)
                .updateChildren(hashMap)
                .addOnSuccessListener(unused -> {
                    btn.setChecked(switchChecked);

                    MyApplication.loadSchedulers(null, false);

                    progressDialog.dismiss();
                    final String toastMsg = switchChecked ? res.getString(R.string.started) : res.getString(R.string.stopped);
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> progressDialog.dismiss());
    }

    private String deleteWalletId, deleteCategoryId;
    private void deleteSchedule(ModelSchedule model) {
        //get id of schedule to delete
        String id = model.getId();
        deleteWalletId = model.getWalletId();
        deleteCategoryId = model.getCategoryId();

        //Firebase DB > Schedulers > scheduleId >
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Schedulers");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // deleted successfully

                    MyApplication.loadSchedulers(null, false);

                    MyApplication.updateWalletBalance(deleteWalletId, deleteCategoryId, 0, model.getIsIncome(), Constants.ROW_DELETED);
                    // ATTENTION 1: updateCategoryUsage() called from updateWalletBalance

                    Toast.makeText(context, res.getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // failed to delete
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return scheduleArrayList.size();
    }

    // View holder class to hold UI views for row_schedule.xml
    class HolderSchedule extends RecyclerView.ViewHolder{
        //ui views of row_schedule.xml
        TextView scheduleNameTv, scheduleStartDateTv, scheduleWalletTv, scheduleTypeTv, scheduleCategoryTv, scheduleAmountTv, schedulePeriodTv;
        Switch scheduleEnabledSw;
        ImageButton deleteBtn;

        public HolderSchedule(@NonNull View itemView) {
            super(itemView);

            // init ui views
            scheduleNameTv = binding.scheduleNameTv;
            scheduleStartDateTv = binding.scheduleStartDateTv;
            scheduleWalletTv = binding.scheduleWalletTv;
            scheduleTypeTv = binding.scheduleTypeTv;
            scheduleCategoryTv = binding.scheduleCategoryTv;
            scheduleAmountTv = binding.scheduleAmountTv;
            schedulePeriodTv = binding.schedulePeriodTv;

            scheduleEnabledSw = binding.scheduleEnabledSw;
            deleteBtn = binding.deleteBtn;
        }
    }

}

package am.softlab.arfinance.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.activities.OperationAddActivity;
import am.softlab.arfinance.databinding.RowOperationBinding;
import am.softlab.arfinance.filters.FilterOperation;
import am.softlab.arfinance.models.ModelOperation;

public class AdapterOperation extends RecyclerView.Adapter<AdapterOperation.HolderOperation> implements Filterable {

    private final Context context;
    public ArrayList<ModelOperation> operationArrayList, filterList;

    //view binding
    private RowOperationBinding binding;

    //instance of our filter class
    private FilterOperation filter;

    //resources
    private Resources res;

    private FirebaseAuth firebaseAuth;
    //firebase current user
    private FirebaseUser firebaseUser;

    public AdapterOperation(Context context, ArrayList<ModelOperation> operationArrayList) {
        this.context = context;
        this.operationArrayList = operationArrayList;
        this.filterList = operationArrayList;

        //get resources
        res = this.context.getResources();
    }

    @NonNull
    @Override
    public AdapterOperation.HolderOperation onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind row_operation.xml
        binding = RowOperationBinding.inflate(LayoutInflater.from(context), parent, false);

        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        return new AdapterOperation.HolderOperation(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOperation.HolderOperation holder, int position) {
        // get data
        ModelOperation model = operationArrayList.get(position);
        String id = model.getId();
        String walletId = model.getWalletId();
        String category = MyApplication.getCategoryById(model.getCategoryId());
        String notes = model.getNotes();
        boolean isIncome = model.getIsIncome();
        double amount = model.getAmount();
        long operationTimestamp = model.getOperationTimestamp();

        //format date, already made in function in MyApplication class
        String dateStr = MyApplication.formatTimestamp(operationTimestamp);

        //set data
        holder.operDateTv.setText(dateStr);
        String amountStr = NumberFormat.getCurrencyInstance().format(amount);
        holder.operAmountTv.setText(amountStr);
        holder.categoryTv.setText(category);
        holder.operNotesTv.setText(notes);

        // handle click, delete operation
        holder.deleteBtn.setOnClickListener(view -> {
            if (firebaseAuth.getCurrentUser() != null) {
                if (firebaseUser.isEmailVerified()) {
                    //confirm delete dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(res.getString(R.string.delete))
                            .setMessage(res.getString(R.string.sure_delete_operation))
                            .setPositiveButton(res.getString(R.string.delete), (dialogInterface, i) -> {
                                //begin delete
                                Toast.makeText(context, res.getString(R.string.deleting), Toast.LENGTH_SHORT).show();
                                deleteOperation(model);
                            })
                            .setNegativeButton(res.getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                            .show();
                }
                else
                    Toast.makeText(context, res.getString(R.string.not_verified_detailed), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
        });

        //handle item click
        holder.itemView.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() != null) {
                if (firebaseUser.isEmailVerified()) {
                    Intent intent = new Intent(context, OperationAddActivity.class);
                    intent.putExtra("operId", id);
                    intent.putExtra("walletId", walletId);
                    intent.putExtra("isIncome", isIncome);
                    context.startActivity(intent);
                }
                else
                    Toast.makeText(context, res.getString(R.string.not_verified_detailed), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
        });
    }

    private String deleteWalletId, deleteCategoryId;
    private double deleteAmount;
    private boolean isIncome;
    private void deleteOperation(ModelOperation model) {
        //det id of operation to delete
        String id = model.getId();
        deleteWalletId = model.getWalletId();
        deleteCategoryId = model.getCategoryId();
        deleteAmount = model.getAmount();
        isIncome = model.getIsIncome();

        //Firebase DB > Operations > operationId >
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // deleted successfully
                    MyApplication.updateWalletBalance(deleteWalletId, 0-deleteAmount, isIncome, Constants.ROW_DELETED);
                    MyApplication.updateCategoryUsage(deleteCategoryId, -1);
                    Toast.makeText(context, res.getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // failed to delete
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return operationArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterOperation(filterList, this);
        }
        return filter;
    }


    // View holder class to hold UI views for row_operation.xml
    class HolderOperation extends RecyclerView.ViewHolder{
        //ui views of row_operation.xml
        TextView operDateTv, operAmountTv, categoryTv, operNotesTv;
        ImageButton deleteBtn;

        public HolderOperation(@NonNull View itemView) {
            super(itemView);

            // init ui views
            operDateTv = binding.operDateTv;
            operAmountTv = binding.operAmountTv;
            categoryTv = binding.categoryTv;
            operNotesTv = binding.operNotesTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}

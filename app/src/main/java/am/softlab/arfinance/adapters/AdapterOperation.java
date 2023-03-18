package am.softlab.arfinance.adapters;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import am.softlab.arfinance.Constants;
import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.R;
import am.softlab.arfinance.activities.OperationAddActivity;
import am.softlab.arfinance.activities.OperationsActivity;
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

    private String currencySymbol;

    private FirebaseAuth firebaseAuth;
    //firebase current user
    private FirebaseUser firebaseUser;

    //resources
    private Resources res;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private static final String TAG = "OPERATION_ADAPTER_TAG";

    public AdapterOperation(Context context, ArrayList<ModelOperation> operationArrayList, String currencySymbol) {
        this.context = context;
        this.operationArrayList = operationArrayList;
        this.currencySymbol = currencySymbol;
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
        String imageUrl = model.getImageUrl();
        boolean hasImage = (imageUrl != null) && !imageUrl.isEmpty();

        //format date, already made in function in MyApplication class
        String dateStr = MyApplication.formatTimestamp(operationTimestamp);

        //set data
        holder.operDateTv.setText(dateStr);
        String amountStr = MyApplication.formatDouble(amount) + " " + currencySymbol;
        holder.operAmountTv.setText(amountStr);
        holder.categoryTv.setText(category);
        holder.operNotesTv.setText(notes);

        if (hasImage) {
            holder.attachBtn.setVisibility(View.VISIBLE);
            holder.attachBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context instanceof OperationsActivity) {
                        //confirm download dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(res.getString(R.string.download))
                                .setMessage(res.getString(R.string.sure_download_image))
                                .setPositiveButton(res.getString(R.string.download), (dialogInterface, i) -> {
                                    //begin download
                                    Log.d(TAG_DOWNLOAD, "onClick: Checking permission");
                                    if (MyApplication.checkPermission(
                                            ((OperationsActivity) context),
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Constants.WRITE_EXTERNAL_STORAGE))
                                    {
                                        Log.d(TAG_DOWNLOAD, "onClick: Permission already granted, can download image");
                                        Toast.makeText(context, res.getString(R.string.downloading), Toast.LENGTH_SHORT).show();
                                        MyApplication.downloadImage(((OperationsActivity) context), "" + id, operationTimestamp, "" + imageUrl);
                                    }
                                })
                                .setNegativeButton(res.getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                                .show();
                    }
                }
            });
        }
        else {
            holder.attachBtn.setVisibility(View.INVISIBLE);
            holder.attachBtn.setOnClickListener(null);
        }

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
        //get id of operation to delete
        String id = model.getId();
        deleteWalletId = model.getWalletId();
        deleteCategoryId = model.getCategoryId();
        deleteAmount = model.getAmount();
        isIncome = model.getIsIncome();
        String imageUrl = model.getImageUrl();

        //Firebase DB > Operations > operationId >
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // deleted successfully
                    MyApplication.updateWalletBalance(deleteWalletId, deleteCategoryId, 0-deleteAmount, isIncome, Constants.ROW_DELETED);
                    Toast.makeText(context, res.getString(R.string.deleted), Toast.LENGTH_SHORT).show();

                    String filePathAndName = "OperationImages/" + id;

                    if (!imageUrl.isEmpty()) {
                        //storage reference - delete File from FirebaseStorage
                        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
                        reference
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "uploadImage: Deleted from FirebaseStorage server...");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete image due to " + e.getMessage());
                                    }
                                });
                    }
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
        ImageButton deleteBtn, attachBtn;

        public HolderOperation(@NonNull View itemView) {
            super(itemView);

            // init ui views
            operDateTv = binding.operDateTv;
            operAmountTv = binding.operAmountTv;
            categoryTv = binding.categoryTv;
            operNotesTv = binding.operNotesTv;
            deleteBtn = binding.deleteBtn;
            attachBtn = binding.attachBtn;
        }
    }
}

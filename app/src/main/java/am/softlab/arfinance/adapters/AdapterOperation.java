package am.softlab.arfinance.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

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
    Resources res;

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

        return new AdapterOperation.HolderOperation(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOperation.HolderOperation holder, int position) {
        // get data
        ModelOperation model = operationArrayList.get(position);
        String id = model.getId();
        String categoryId = model.getCategoryId();
        String notes = model.getNotes();
        boolean isIncome = model.getIsIncome();
        double amount = model.getAmount();
        long operationTimestamp = model.getOperationTimestamp();

        //format date, already made in function in MyApplication class
        String dateStr = MyApplication.formatTimestamp(operationTimestamp);

        //set data
        holder.operDateTv.setText(dateStr);
        holder.operAmountTv.setText(""+amount);
        holder.categoryTv.setText(categoryId);
        holder.operNotesTv.setText(notes);

        // handle click, delete operation
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(res.getString(R.string.delete))
                        .setMessage(res.getString(R.string.sure_delete_operation))
                        .setPositiveButton(res.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //begin delete
                                Toast.makeText(context, res.getString(R.string.deleting), Toast.LENGTH_SHORT).show();
                                deleteOperation(model, holder);

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

        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OperationAddActivity.class);
                intent.putExtra("operId", id);
                intent.putExtra("isIncome", isIncome);
                context.startActivity(intent);
            }
        });
    }

    private void deleteOperation(ModelOperation model, AdapterOperation.HolderOperation holder) {
        //det id of operation to delete
        String id = model.getId();
        //Firebase DB > Operations > operationId >
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");
        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // deleted successfully
                        Toast.makeText(context, res.getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed to delete
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
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

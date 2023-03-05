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

import am.softlab.arfinance.R;
import am.softlab.arfinance.activities.CategoryAddActivity;
import am.softlab.arfinance.databinding.RowCategoryBinding;
import am.softlab.arfinance.filters.FilterCategory;
import am.softlab.arfinance.models.ModelCategory;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> implements Filterable {
    private final Context context;
    public ArrayList<ModelCategory> categoryArrayList, filterList;

    //view binding
    private RowCategoryBinding binding;

    //instance of our filter class
    private FilterCategory filter;

    //resources
    Resources res;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;

        //get resources
        res = this.context.getResources();
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind row_category.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        // get data
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String notes = model.getNotes();
        Boolean isIncome = model.getIsIncome();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        //set data
        holder.categoryTv.setText(category);
        holder.categoryTypeTv.setText(isIncome ? res.getString(R.string.income) : res.getString(R.string.expenses) );
        holder.categoryNotesTv.setText(notes);

        // handle click, delete category
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(res.getString(R.string.delete))
                        .setMessage(res.getString(R.string.sure_delete_category))
                        .setPositiveButton(res.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //begin delete
                                Toast.makeText(context, res.getString(R.string.deleting), Toast.LENGTH_SHORT).show();
                                deleteCategory(model, holder);

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

        //handle item click, goto to PdfListAdminActivity, also pass pdf category and categoryId
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CategoryAddActivity.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryName", category);
                intent.putExtra("categoryNotes", notes);
                intent.putExtra("isIncome", ""+isIncome);
                context.startActivity(intent);
            }
        });
    }

    private void deleteCategory(ModelCategory model, HolderCategory holder) {
        //det id of category to delete
        String id = model.getId();
        //Firebase DB > Categories > categoryId >
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
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
        return categoryArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterCategory(filterList, this);
        }
        return filter;
    }


    // View holder class to hold UI views for row_category.xml
    class HolderCategory extends RecyclerView.ViewHolder{
        //ui views of row_category.xml
        TextView categoryTv, categoryNotesTv, categoryTypeTv;
        ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            // init ui views
            categoryTv = binding.categoryTv;
            categoryTypeTv = binding.categoryTypeTv;
            categoryNotesTv = binding.categoryNotesTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}

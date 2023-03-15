package am.softlab.arfinance.adapters;

import android.annotation.SuppressLint;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import am.softlab.arfinance.MyApplication;
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
    private final Resources res;

    private FirebaseAuth firebaseAuth;

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

        firebaseAuth = FirebaseAuth.getInstance();

        return new HolderCategory(binding.getRoot());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        // get data
        ModelCategory model = categoryArrayList.get(position);
        String id = model.getId();
        String category = model.getCategory();
        String notes = model.getNotes();
        boolean isIncome = model.getIsIncome();
        int usageCount = model.getUsageCount();

        //set data
        holder.categoryTv.setText(category);
        holder.categoryTypeTv.setText(isIncome ? res.getString(R.string.income) : res.getString(R.string.expenses) );
        holder.categoryNotesTv.setText(notes);

        String amountStr = res.getString(R.string.usage_count) + " " + MyApplication.formatInteger(usageCount);
        holder.usageCountTv.setText(amountStr);

        // handle click, delete category
        holder.deleteBtn.setOnClickListener(view -> {
            if (firebaseAuth.getCurrentUser() != null) {
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(res.getString(R.string.delete))
                        .setMessage(res.getString(R.string.sure_delete_category))
                        .setPositiveButton(
                                res.getString(R.string.delete),
                                (dialogInterface, i) -> {
                                    //begin delete
                                    Toast.makeText(context, res.getString(R.string.deleting), Toast.LENGTH_SHORT).show();
                                    checkAndDeleteCategory(model);
                                }
                        )
                        .setNegativeButton(
                                res.getString(R.string.cancel),
                                (dialogInterface, i) -> dialogInterface.dismiss()
                        )
                        .show();
            }
            else
                Toast.makeText(context, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
        });

        //handle item click
        holder.itemView.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() != null) {
                Intent intent = new Intent(context, CategoryAddActivity.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryName", category);
                intent.putExtra("categoryNotes", notes);
                intent.putExtra("isIncome", isIncome);
                context.startActivity(intent);
            }
            else
                Toast.makeText(context, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
        });
    }

    private String deleteCategoryId = "";
    private void checkAndDeleteCategory(ModelCategory model) {
        deleteCategoryId = model.getId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");
        ref.orderByChild("categoryId").equalTo(deleteCategoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long rowCount = snapshot.getChildrenCount();
                        if (rowCount <= 0) {
                            deleteCategory(deleteCategoryId);
                        } else {
                            Toast.makeText(context, res.getString(R.string.category_used), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }

    private void deleteCategory(String categoryId) {
        //Firebase DB > Categories > categoryId >
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    // deleted successfully
                    Toast.makeText(context, res.getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // failed to delete
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        TextView categoryTv, categoryNotesTv, categoryTypeTv, usageCountTv;
        ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            // init ui views
            categoryTv = binding.categoryTv;
            categoryTypeTv = binding.categoryTypeTv;
            usageCountTv = binding.usageCountTv;
            categoryNotesTv = binding.categoryNotesTv;
            deleteBtn = binding.deleteBtn;
        }
    }
}

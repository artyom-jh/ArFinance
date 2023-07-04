package am.softlab.arfinance.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import am.softlab.arfinance.activities.WalletAddActivity;
import am.softlab.arfinance.databinding.RowWalletBinding;
import am.softlab.arfinance.filters.FilterWallet;
import am.softlab.arfinance.models.ModelWallet;

public class AdapterWallet extends RecyclerView.Adapter<AdapterWallet.HolderWallet> implements Filterable {
    private final Context context;
    public ArrayList<ModelWallet> walletArrayList, filterList;

    //view binding
    private RowWalletBinding binding;

    //instance of our filter class
    private FilterWallet filter;

    //resources
    private final Resources res;

    private FirebaseAuth firebaseAuth;

    public AdapterWallet(Context context, ArrayList<ModelWallet> walletArrayList) {
        this.context = context;
        this.walletArrayList = walletArrayList;
        this.filterList = walletArrayList;

        //get resources
        res = this.context.getResources();
    }

    @NonNull
    @Override
    public AdapterWallet.HolderWallet onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind row_category.xml
        binding = RowWalletBinding.inflate(LayoutInflater.from(context), parent, false);

        firebaseAuth = FirebaseAuth.getInstance();

        return new AdapterWallet.HolderWallet(binding.getRoot());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterWallet.HolderWallet holder, int position) {
        // get data
        ModelWallet model = walletArrayList.get(position);
        String id = model.getId();
        String walletName = model.getWalletName();
        String currencyCode = model.getCurrencyCode();
        String currencyName = model.getCurrencyName();
        String currencySymbol = model.getCurrencySymbol();
        String notes = model.getNotes();
        double totalIncome = model.getTotalIncome();
        double totalExpenses = model.getTotalExpenses();
        double balance = model.getBalance();

        //set data
        holder.walletTv.setText(walletName);
        holder.flagBtn.setImageDrawable(MyApplication.getCountryFlag(currencyCode));
        holder.currencyTv.setText(currencyName);
        holder.currencyCodeTv.setText(currencyCode);
        holder.walletNotesTv.setText(notes);

        //String amountStr = NumberFormat.getCurrencyInstance().format(amount);
        String amountStr = MyApplication.formatDouble(totalIncome) + " " + currencySymbol;
        holder.totalIncomeTv.setText(amountStr);

        amountStr = MyApplication.formatDouble(totalExpenses) + " " + currencySymbol;
        holder.totalExpensesTv.setText(amountStr);

        amountStr = MyApplication.formatDouble(balance) + " " + currencySymbol;
        holder.balanceTv.setText(amountStr);
        if (balance < 0)
            holder.balanceTv.setTextColor(Color.RED);
        else
            holder.balanceTv.setTextColor(Color.BLACK);

        // handle click, delete category
        holder.deleteBtn.setOnClickListener(view -> {
            if (firebaseAuth.getCurrentUser() != null) {
                //confirm delete dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(res.getString(R.string.delete_title))
                        .setMessage(res.getString(R.string.sure_delete_wallet))
                        .setPositiveButton(
                                res.getString(R.string.delete),
                                (dialogInterface, i) -> {
                                    //begin delete
                                    Toast.makeText(context, res.getString(R.string.deleting), Toast.LENGTH_SHORT).show();
                                    checkAndDeleteWallet(model);
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
                Intent intent = new Intent(context, WalletAddActivity.class);
                intent.putExtra("walletId", id);
                intent.putExtra("walletName", walletName);
                intent.putExtra("walletNotes", notes);
                intent.putExtra("currencyCode", currencyCode);
                intent.putExtra("currencyName", currencyName);
                intent.putExtra("currencySymbol", currencySymbol);
                context.startActivity(intent);
            }
            else
                Toast.makeText(context, res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
        });
    }

    private String deleteWalletId = "";
    private void checkAndDeleteWallet(ModelWallet model) {
        deleteWalletId = model.getId();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");
        ref.orderByChild("walletId").equalTo(deleteWalletId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long rowCount = snapshot.getChildrenCount();
                        if (rowCount <= 0) {
                            deleteWallet(deleteWalletId);
                        } else {
                            Toast.makeText(context, res.getString(R.string.wallet_used), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }

    private void deleteWallet(String walletId) {
        //Firebase DB > Wallets > walletId >
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
        ref.child(walletId)
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
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterWallet(filterList, this);
        }
        return filter;
    }

    @Override
    public int getItemCount() {
        return walletArrayList.size();
    }

    // View holder class to hold UI views for row_wallet.xml
    class HolderWallet extends RecyclerView.ViewHolder{
        //ui views of row_wallet.xml
        TextView walletTv, walletNotesTv, currencyTv, currencyCodeTv, totalIncomeTv, totalExpensesTv, balanceTv;
        ImageButton deleteBtn;
        ImageView flagBtn;

        public HolderWallet(@NonNull View itemView) {
            super(itemView);

            // init ui views
            walletTv = binding.walletTv;
            currencyTv = binding.currencyTv;
            currencyCodeTv = binding.currencyCodeTv;
            totalIncomeTv = binding.totalIncomeTv;
            totalExpensesTv = binding.totalExpensesTv;
            balanceTv = binding.balanceTv;
            walletNotesTv = binding.walletNotesTv;
            deleteBtn = binding.deleteBtn;
            flagBtn = binding.flagBtn;
        }
    }
}

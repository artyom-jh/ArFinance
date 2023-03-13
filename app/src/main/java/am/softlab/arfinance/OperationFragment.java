package am.softlab.arfinance;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import am.softlab.arfinance.activities.OperationAddActivity;
import am.softlab.arfinance.adapters.AdapterOperation;
import am.softlab.arfinance.databinding.FragmentOperationBinding;
import am.softlab.arfinance.models.ModelOperation;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OperationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OperationFragment extends Fragment {

    //that we passed while creating instance of this fragment
    private String walletId, currencySymbol;
    private boolean isIncome;

    //view binding
    private FragmentOperationBinding binding;

    private Context context;
    private Resources res;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    //arraylist to store operation
    private ArrayList<ModelOperation> operationArrayList;
    private AdapterOperation adapterOperation;

    private static final String TAG = "OPERATION_FRAGMENT_TAG";

    public OperationFragment() {
        // Required empty public constructor
    }

    public static OperationFragment newInstance(String walletId, String currencySymbol, boolean income) {
        OperationFragment fragment = new OperationFragment();
        Bundle args = new Bundle();
        args.putString("walletId", walletId);
        args.putString("currencySymbol", currencySymbol);
        args.putBoolean("isIncome", income);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            walletId = getArguments().getString("walletId");
            currencySymbol = getArguments().getString("currencySymbol");
            isIncome = getArguments().getBoolean("isIncome");
        }

        context = getContext();

        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        //get resources
        res = this.getResources();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOperationBinding.inflate(LayoutInflater.from(context), container, false);

        Log.d(TAG, "onCreateView: OperationFragment: " + walletId);
        loadOperations();

        //edit text change listener, search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //noop
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //called as and when user type each letter
                try{
                    adapterOperation.getFilter().filter(charSequence);
                }catch (Exception e){
                    //noop
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //noop
            }
        });

        //handle click, start operation add screen
        binding.addOperationBtn.setOnClickListener(view -> {
            if (firebaseAuth.getCurrentUser() != null) {
                if (firebaseAuth.getCurrentUser() != null) {
                    if (firebaseUser.isEmailVerified()) {
                        Intent intent = new Intent(getActivity(), OperationAddActivity.class);
                        intent.putExtra("isIncome", isIncome);
                        intent.putExtra("walletId", walletId);
                        startActivity(intent);
                    }
                    else
                        Toast.makeText(getActivity(), res.getString(R.string.not_verified_detailed), Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getActivity(), res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getActivity(), res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
        });

        return binding.getRoot();
    }

    private void loadOperations() {
        // init arraylist
        operationArrayList = new ArrayList<>();
        if (firebaseAuth.getCurrentUser() != null) {
            //get all operations from firebase > Operations
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");
            ref.orderByChild("uid_walletId").equalTo(firebaseAuth.getCurrentUser().getUid() + "_" + walletId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // clear arraylist before adding data into it
                            operationArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // get data
                                ModelOperation model = ds.getValue(ModelOperation.class);

                                if (model.getIsIncome() == isIncome) {
                                    //add to arraylist
                                    operationArrayList.add(model);
                                }
                            }
                            //setup adapter
                            adapterOperation = new AdapterOperation(context, operationArrayList, currencySymbol);
                            //set adapter tp recyclerview
                            binding.operationsRv.setAdapter(adapterOperation);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //noop
                        }
                    });
        }
    }

}
package am.softlab.arfinance;

import static com.google.android.material.datepicker.MaterialDatePicker.Builder.dateRangePicker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;

import am.softlab.arfinance.activities.StatisticsActivity;
import am.softlab.arfinance.databinding.FragmentPieBinding;
import am.softlab.arfinance.models.ModelOperation;
import am.softlab.arfinance.models.ModelWallet;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PieFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PieFragment extends Fragment {

    //that we passed while creating instance of this fragment
    private int statPageId;
    private boolean isIncome;

    //progress dialog
    private ProgressDialog progressDialog;
    private MaterialDatePicker materialDatePicker;
    private Pair<Long, Long> dateRange = null;  // new Pair(-1, -1);

    //view binding
    private FragmentPieBinding binding;
    private PieChart categoryPieChart;

    private Context context;
    private Resources res;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //arraylist to hold odf wallets
    private ArrayList<String> walletNameArrayList, walletIdArrayList;
    private String selectedWalletId="", selectedWalletName="";

    private static final String TAG = "PIE_FRAGMENT_TAG";

    public PieFragment() {
        // Required empty public constructor
    }

    public static PieFragment newInstance(int statPageId) {
        PieFragment fragment = new PieFragment();
        Bundle args = new Bundle();
        args.putInt("statPageId", statPageId);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statPageId = getArguments().getInt("statPageId");
        }

        context = getContext();
        firebaseAuth = FirebaseAuth.getInstance();

        //get resources
        res = this.getResources();

        // create the calendar constraint builder
        CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder();

        // instantiate the Material date picker dialog
        // builder
        final MaterialDatePicker.Builder materialDatePickerBuilder = MaterialDatePicker.Builder.dateRangePicker();
        materialDatePickerBuilder.setTitleText(res.getString(R.string.select_date_range));

        // now pass the constrained calendar builder to
        // material date picker Calendar constraints
        materialDatePickerBuilder.setCalendarConstraints(calendarConstraintBuilder.build());

        // now build the material date picker dialog
        materialDatePicker = materialDatePickerBuilder.build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPieBinding.inflate(LayoutInflater.from(getContext()), container, false);

        isIncome = (statPageId == Constants.PAGE_INCOME);

        //configure progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(res.getString(R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);

        categoryPieChart = binding.categoryPieChart;
        setupCategoryPieChart();

        Log.d(TAG, "onCreateView: Pie: " + statPageId);

        loadWallets();

        binding.chooseWalletTv.setOnClickListener(view -> walletPickDialog());

        //date range - materialDatePicker
        materialDatePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>) selection -> {
            MyApplication.hideKeyboard(materialDatePicker);
            updateDateRangeButton(selection);
        });
        //date range - selectDateRangeTv
        binding.selectDateRangeTv.setOnClickListener(view -> {
            materialDatePicker.show(getActivity().getSupportFragmentManager(), "DATE_PICKER_RANGE");
        });
        binding.selectDateRangeTv.setOnLongClickListener(v -> {
            updateDateRangeButton(null);
            return true;  //it has a return value, which should be true if long-click events are handled
        });

        if (firebaseAuth.getCurrentUser() == null) {
            showDemoPieChart();
        }

        return binding.getRoot();
    }

    private void updateDateRangeButton(Pair<Long, Long> selection) {
        dateRange = selection;
        if (dateRange == null)
            binding.selectDateRangeTv.setText(res.getString(R.string.for_all_time));
        else {
            binding.selectDateRangeTv.setText(
                    String.format("%s - %s",
                            MyApplication.formatTimestampShort(dateRange.first),
                            MyApplication.formatTimestampShort(dateRange.second)
                    ));
        }

        if (selectedWalletId.isEmpty()) {
            Toast.makeText(getContext(), res.getString(R.string.select_wallet), Toast.LENGTH_SHORT).show();
        } else
            loadCategoriesByWallet();
    }

    private void loadWallets() {
        Log.d(TAG, "loadWallets: Loading wallets...");
        walletNameArrayList = new ArrayList<>();
        walletIdArrayList = new ArrayList<>();

        if (firebaseAuth.getCurrentUser() != null) {
            //db reference to load categories... db > Wallets
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Wallets");
            ref.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            walletNameArrayList.clear(); // clear before adding data
                            walletIdArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ModelWallet model = ds.getValue(ModelWallet.class);

                                walletNameArrayList.add(model.getWalletName());
                                walletIdArrayList.add("" + model.getId());

                                Log.d(TAG, "onDataChange: ID: " + model.getId());
                                Log.d(TAG, "onDataChange: Wallet: " + model.getWalletName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //noop
                        }
                    });
        }
    }

    private void walletPickDialog() {
        Log.d(TAG, "walletPickDialog: showing wallet pick dialog");

        if (firebaseAuth.getCurrentUser() != null) {
            //get string array of wallets from arraylist
            String[] walletsArray = new String[walletNameArrayList.size()];
            for (int i = 0; i < walletNameArrayList.size(); i++) {
                walletsArray[i] = walletNameArrayList.get(i);
            }

            //alert dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Pick Wallet")
                    .setItems(
                            walletsArray,
                            (dialogInterface, which) -> {
                                progressDialog.setMessage(res.getString(R.string.loading_operations));
                                progressDialog.show();

                                //handle item click
                                //get clicked item from list
                                selectedWalletName = walletNameArrayList.get(which);
                                selectedWalletId = walletIdArrayList.get(which);
                                //set to category textview
                                binding.chooseWalletTv.setText(selectedWalletName);

                                loadCategoriesByWallet();

                                Log.d(TAG, "onClick: Selected Wallet: " + selectedWalletId + " " + selectedWalletName);
                            }
                    )
                    .show();
        }
        else
            Toast.makeText(getContext(), res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
    }

    private void setupCategoryPieChart() {
        categoryPieChart.setDrawHoleEnabled(true);
        categoryPieChart.setUsePercentValues(true);
        categoryPieChart.setEntryLabelTextSize(12);
        categoryPieChart.setEntryLabelColor(Color.BLACK);
        categoryPieChart.setCenterText(res.getString(R.string.spending_category));
        categoryPieChart.setCenterTextSize(24);
        categoryPieChart.getDescription().setEnabled(false);

        Legend legend = categoryPieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);

        categoryPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pieEntry = (PieEntry)e;
                String amountStr = NumberFormat.getCurrencyInstance().format(pieEntry.getValue());

                new AlertDialog.Builder(getContext())
                        .setTitle(pieEntry.getLabel())
                        .setCancelable(true)
                        .setMessage(amountStr)
                        .show();
            }

            @Override
            public void onNothingSelected() {
                //noop
            }
        });
    }

    private void loadCategoriesByWallet() {
        //get all wallet operations from firebase > Operation
        if (firebaseAuth.getCurrentUser() != null && !selectedWalletId.isEmpty()) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Operations");

            ref.orderByChild("walletId").equalTo(selectedWalletId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList<PieEntry> entries = new ArrayList<>();
                            int size = (int) snapshot.getChildrenCount();
                            int lastIndex = -1;

                            // operArray: 0-categoryId, 1-categoryTitle, 2-amount
                            String[][] operArray = new String[size][2];
                            // Fill each row with 0.0
                            for (String[] row: operArray)
                                Arrays.fill(row, "0.0");

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // get data
                                ModelOperation model = ds.getValue(ModelOperation.class);
                                if (model.getIsIncome() == isIncome) {
                                    if ( dateRange == null ||
                                         ((model.getOperationTimestamp() >= dateRange.first) &&
                                          (model.getOperationTimestamp() <= dateRange.second)) )
                                    {
                                        float fAmount = (float) model.getAmount();
                                        if (fAmount > 0) {
                                            //group by category id - search id and increase amount
                                            boolean isFound = false;
                                            for (int i = 0; i <= lastIndex; i++) {
                                                if (model.getCategoryId().equals(operArray[i][0])) {
                                                    operArray[i][1] = String.valueOf(Float.parseFloat(operArray[i][1]) + fAmount);
                                                    isFound = true;
                                                    break;
                                                }
                                            }
                                            //if not found -> add
                                            if (!isFound) {
                                                lastIndex++;
                                                operArray[lastIndex][0] = model.getCategoryId();
                                                operArray[lastIndex][1] = String.valueOf(fAmount);
                                            }
                                        }
                                    }
                                }
                            }

                            Arrays.sort(operArray, new Comparator<String[]>() {
                                public int compare(String[] a, String[] b) {
                                    return Double.compare(Float.parseFloat(b[1]), Float.parseFloat(a[1]));
                                }
                            });

                            //show only Constants.PIE_MAX_ENTRIES entries, all other show in one amount
                            float otherAmountSum = 0;
                            for (int i=0; i <= lastIndex; i++) {
                                if (i < Constants.PIE_MAX_ENTRIES) {
                                    entries.add(
                                            new PieEntry(
                                                    Float.parseFloat(operArray[i][1]),
                                                    ((StatisticsActivity) getActivity()).getCategoryNameById(operArray[i][0])
                                            )
                                    );
                                }
                                else
                                    otherAmountSum += Float.parseFloat(operArray[i][1]);
                            }
                            if (otherAmountSum > 0) {
                                entries.add(
                                        new PieEntry(
                                                otherAmountSum,
                                                res.getString(R.string.other_categories)
                                        )
                                );
                            }
                            progressDialog.dismiss();
                            loadPieChartData(entries, isIncome);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                        }
                    });
        }
        else {
            progressDialog.dismiss();
            Toast.makeText(getContext(), res.getString(R.string.not_logged_in_detailed), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPieChartData(ArrayList<PieEntry> entries, boolean isIncome) {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color: ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        for (int color: ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(
                entries,
                isIncome ? res.getString(R.string.income_category) : res.getString(R.string.expenses_category)
        );
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(categoryPieChart));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        categoryPieChart.setData(data);
        categoryPieChart.invalidate();

        categoryPieChart.animateY(1400, Easing.EaseInOutQuad);
    }

    private void showDemoPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(
                new PieEntry(
                        1000,
                        "Demo Category 1"
                )
        );
        entries.add(
                new PieEntry(
                        2000,
                        "Demo Category 2"
                )
        );
        if (isIncome)
            entries.add(
                    new PieEntry(
                            1500,
                            "Demo Category 3"
                    )
            );

        loadPieChartData(entries, isIncome);
    }

}
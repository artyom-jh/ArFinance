package am.softlab.arfinance;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import am.softlab.arfinance.databinding.FragmentPieBinding;
import am.softlab.arfinance.models.ModelCategory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PieFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PieFragment extends Fragment {

    //that we passed while creating instance of this fragment
    private int statPageId;

    //view binding
    private FragmentPieBinding binding;
    private PieChart categoryPieChart;

    Resources res;

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

        //get resources
        res = this.getResources();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPieBinding.inflate(LayoutInflater.from(getContext()), container, false);

        categoryPieChart = binding.categoryPieChart;
        setupCategoryPieChart();

        Log.d(TAG, "onCreateView: Pie: " + statPageId);
        if (statPageId == Constants.PAGE_INCOME) {
            //load income categories for pie
            loadCategories(true);
        }
        else {
            //load expenses categories for pie
            loadCategories(false);
        }

        return binding.getRoot();
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
    }

    private void loadCategories(boolean isIncome) {
        //get all categories from firebase > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.orderByChild("isIncome").equalTo(isIncome)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<PieEntry> entries = new ArrayList<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // get data
                            ModelCategory model = ds.getValue(ModelCategory.class);
                            float fAmount = (float) model.getAmount();
                            if (fAmount > 0) {
                                entries.add(new PieEntry(fAmount, model.getCategory()));
                            }
                        }
                        loadPieChartData(entries, isIncome);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }

    private void loadPieChartData(ArrayList<PieEntry> entries, boolean isIncome) {
        //entries.add(new PieEntry(0.2f, "label1"));
        //entries.add(new PieEntry(0.15f, "label3"));

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
}
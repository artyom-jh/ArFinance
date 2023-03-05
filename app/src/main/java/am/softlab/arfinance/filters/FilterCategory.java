package am.softlab.arfinance.filters;

import android.widget.Filter;

import java.util.ArrayList;

import am.softlab.arfinance.adapters.AdapterCategory;
import am.softlab.arfinance.models.ModelCategory;

public class FilterCategory extends Filter {
    //arraylist in which we want to search
    ArrayList<ModelCategory> filterList;
    // adapter in which filter need to be implemented
    AdapterCategory adapterCategory;

    //constructor
    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //value should not be null or empty
        if(charSequence != null && charSequence.length() > 0 ){
            //change to upper case or lower case to avoid case sensitivity
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++){
                //validate
                if(filterList.get(i).getCategory().toUpperCase().contains(charSequence)){
                    //add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        //apply filter changes
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>) filterResults.values;

        //notify changes
        adapterCategory.notifyDataSetChanged();
    }
}

package am.softlab.arfinance.filters;

import android.widget.Filter;

import java.util.ArrayList;

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.adapters.AdapterOperation;
import am.softlab.arfinance.models.ModelOperation;

public class FilterOperation extends Filter {
    //arraylist in which we want to search
    ArrayList<ModelOperation> filterList;
    // adapter in which filter need to be implemented
    AdapterOperation adapterOperation;

    //constructor
    public FilterOperation(ArrayList<ModelOperation> filterList, AdapterOperation adapterOperation) {
        this.filterList = filterList;
        this.adapterOperation = adapterOperation;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //value should not be null or empty
        if(charSequence != null && charSequence.length() > 0 ){
            //change to upper case or lower case to avoid case sensitivity
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelOperation> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++){
                //validate
                if ( filterList.get(i).getNotes().toUpperCase().contains(charSequence) ||
                     MyApplication.getCategoryById(filterList.get(i).getCategoryId()).toUpperCase().contains(charSequence) )
                {
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
        adapterOperation.operationArrayList = (ArrayList<ModelOperation>) filterResults.values;

        //notify changes
        adapterOperation.notifyDataSetChanged();
    }
}

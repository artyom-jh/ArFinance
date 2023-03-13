package am.softlab.arfinance.filters;

import android.widget.Filter;

import java.util.ArrayList;

import am.softlab.arfinance.adapters.AdapterWallet;
import am.softlab.arfinance.models.ModelWallet;


public class FilterWallet extends Filter {
    //arraylist in which we want to search
    ArrayList<ModelWallet> filterList;
    // adapter in which filter need to be implemented
    AdapterWallet adapterWallet;

    //constructor
    public FilterWallet(ArrayList<ModelWallet> filterList, AdapterWallet adapterWallet) {
        this.filterList = filterList;
        this.adapterWallet = adapterWallet;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        //value should not be null or empty
        if(charSequence != null && charSequence.length() > 0 ){
            //change to upper case or lower case to avoid case sensitivity
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelWallet> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++){
                //validate
                if ( filterList.get(i).getWalletName().toUpperCase().contains(charSequence) ||
                     filterList.get(i).getCurrencyName().toUpperCase().contains(charSequence) ||
                     filterList.get(i).getNotes().toUpperCase().contains(charSequence) )
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
        adapterWallet.walletArrayList = (ArrayList<ModelWallet>) filterResults.values;

        //notify changes
        adapterWallet.notifyDataSetChanged();
    }
}
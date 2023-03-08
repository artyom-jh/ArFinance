package am.softlab.arfinance;

import android.app.Application;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import am.softlab.arfinance.models.ModelCategory;

public class MyApplication extends Application {

    private static List<List<String>> categoryArrayList = new ArrayList<List<String>>();

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //created a static method to convert timestamp to proper date format, so we can use
    //it everywhere in project, no need to rewrite again
    public static String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        //formatting timestamp to dd/MM/yyyy
        return DateFormat.format("dd/MM/yyyy", cal).toString();
    }

    public static void loadCategoryList() {
        //get category using categoryId
        categoryArrayList = new ArrayList<List<String>>();

        //get all categories from firebase > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist before adding data into it
                categoryArrayList.clear();

                for (DataSnapshot ds: snapshot.getChildren()){
                    // get data
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //add to arraylist
                    List<String> currentList = new ArrayList<String>();
                    currentList.add(model.getId());
                    currentList.add(model.getCategory());
                    categoryArrayList.add(currentList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //noop
            }
        });
    }
    public static String getCategoryById(String catId) {
        if (categoryArrayList != null && categoryArrayList.size() > 1) {
            for (List<String> stringList : categoryArrayList) {
                if (stringList.get(0).equals(catId)) {
                    return stringList.get(1);
                }
            }
        }
        return "";
    }

    public static void updateCategoryAmount(String categoryId, double addAmount) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get views count
                        String amountStr = ""+snapshot.child("amount").getValue();
                        //in case of null replace with 0
                        if (amountStr.equals("") || amountStr.equals("null")) {
                            amountStr = "0";
                        }

                        //2) Increment views count
                        double newAmount = Double.parseDouble(amountStr) + addAmount;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("amount", newAmount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
                        reference.child(categoryId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //noop
                    }
                });
    }
}

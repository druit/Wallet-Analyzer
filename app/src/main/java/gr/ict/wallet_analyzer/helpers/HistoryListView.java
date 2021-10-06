package gr.ict.wallet_analyzer.helpers;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import Adapters.HistoryListAdapter;
import data_class.History;
import gr.ict.wallet_analyzer.R;

public class HistoryListView {

    RoundedCornerListView mainListView;
    private HistoryListAdapter mainAdapter;
    private Activity activity;
    private HistoryArrayList historyArrayList = new HistoryArrayList();
    private DatabaseReference baseReference;
    private ListeningVariable<Double> totalPrice;

    public HistoryListView(Activity activity, DatabaseReference baseReference, ListeningVariable<Double> totalPrice) {
        this.activity = activity;
        this.baseReference = baseReference;
        this.totalPrice = totalPrice;
    }

    public void setListView() {
        mainAdapter = new HistoryListAdapter(activity, historyArrayList.getList());
        mainListView = activity.findViewById(R.id.list);
        mainListView.setAdapter(mainAdapter);

        DatabaseReference declare = baseReference.child("history");
        declare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                historyArrayList.clear();
                History history;
                totalPrice.setObject(0.0);

                for (DataSnapshot child : children) {
                    history = child.getValue(History.class);
                    totalPrice.setObject(totalPrice.getObject() + history.getReceipt().getTotalPrice());
                    historyArrayList.add(history);
                    mainAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
            }
        });

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReceiptPopup receiptPopup = new ReceiptPopup(position, activity, historyArrayList.getList(), baseReference);
                receiptPopup.showReceiptPopup();
            }
        });
    }

    public void filterDate() {
        ArrayList<History> tempHistoryList = historyArrayList.getList();
        tempHistoryList.sort(Comparator.comparing(o -> o.getReceipt().getDate()));
        historyArrayList.setHistoryArrayList(tempHistoryList);
    }

    public void filterReverseDate() {
        ArrayList<History> tempHistoryList = historyArrayList.getList();
        tempHistoryList.sort(Collections.reverseOrder(Comparator.comparing(o -> o.getReceipt().getDate())));
        historyArrayList.setHistoryArrayList(tempHistoryList);
    }

    public void filterByCategory() {
        ArrayList<History> tempHistoryList = historyArrayList.getList();
        tempHistoryList.sort(Comparator.comparing(o -> o.getReceipt().getStoreType()));
        historyArrayList.setHistoryArrayList(tempHistoryList);
    }

    public void filterReverseCategory() {
        ArrayList<History> tempHistoryList = historyArrayList.getList();
        tempHistoryList.sort(Collections.reverseOrder(Comparator.comparing(o -> o.getReceipt().getStoreType())));
        historyArrayList.setHistoryArrayList(tempHistoryList);
    }

    public void filterByPrice() {
        ArrayList<History> tempHistoryList = historyArrayList.getList();
        tempHistoryList.sort(Comparator.comparing(o -> o.getReceipt().getTotalPrice()));
        historyArrayList.setHistoryArrayList(tempHistoryList);
    }

    public void filterReversePrice() {
        ArrayList<History> tempHistoryList = historyArrayList.getList();
        tempHistoryList.sort(Collections.reverseOrder(Comparator.comparing(o -> o.getReceipt().getTotalPrice())));
        historyArrayList.setHistoryArrayList(tempHistoryList);
    }

    public void notifyDataSet() {
        mainAdapter.notifyDataSetChanged();
    }

    public HistoryArrayList getHistoryArrayList() {
        return historyArrayList;
    }
}

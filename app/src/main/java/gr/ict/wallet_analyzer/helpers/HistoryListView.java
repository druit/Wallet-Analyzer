package gr.ict.wallet_analyzer.helpers;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;

import Adapters.HistoryListAdapter;
import data_class.History;
import gr.ict.wallet_analyzer.R;

public class HistoryListView {
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
        ListView mainListView;

        mainAdapter = new HistoryListAdapter(activity, historyArrayList.getHistoryArrayList());
        mainListView = activity.findViewById(R.id.list);
        mainListView.setAdapter(mainAdapter);

        DatabaseReference declare = baseReference.child("history");
        declare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                historyArrayList.clear();
                // TODO: dataset for the graph with the listeners
//                dataSet.clear();
//                dataSet.addEntry(new Entry(1, 0));
                History history;
                totalPrice.setObject(0.0);

                for (DataSnapshot child : children) {
                    history = child.getValue(History.class);
                    totalPrice.setObject(totalPrice.getObject() + history.getReceipt().getTotalPrice());
                    historyArrayList.add(history);
                    // sort the array every time, any better ideas would be greatly valued
                    Collections.sort(historyArrayList.getHistoryArrayList());
                    mainAdapter.notifyDataSetChanged();

                    // show receipts for current month
                    // TODO: get this from listener
                    // fillGraphFromCurrentMonth();
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
                ReceiptPopup receiptPopup = new ReceiptPopup(position, activity, historyArrayList.getHistoryArrayList(), baseReference);
                receiptPopup.showReceiptPopup();
            }
        });
    }
}

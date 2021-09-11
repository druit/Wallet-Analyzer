package gr.ict.wallet_analyzer.helpers;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import data_class.History;

public class HistoryArrayList {
    private ArrayList<History> historyArrayList = new ArrayList<>();
    private ListeningVariable.ChangeListener listener;

    public ArrayList<History> getHistoryArrayList() {
        return historyArrayList;
    }

    public void setHistoryArrayList(ArrayList<History> historyArrayList) {
        this.historyArrayList = historyArrayList;
        if (listener != null) listener.onChange(historyArrayList);
    }

    public void add(History history) {
        historyArrayList.add(history);
        Collections.sort(historyArrayList);
        if (listener != null) listener.onChange(history);
    }

    public void remove(int index) {
        historyArrayList.remove(index);
        if (listener != null) listener.onChange(historyArrayList);
    }

    public void clear() {
        historyArrayList.clear();
        if (listener != null) listener.onChange(historyArrayList);
    }

    public ListeningVariable.ChangeListener getListener() {
        return listener;
    }

    public void setListener(ListeningVariable.ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChange(ArrayList<History> historyArrayList);

        void onChange(History history);
    }

    public void callBackHistoryArrayList(DatabaseReference baseReference, final FirebaseResultInterface firebaseResultInterface) {

        DatabaseReference declare = baseReference.child("history");

        declare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                historyArrayList.clear();
                History history;

                for (DataSnapshot child : children) {
                    history = child.getValue(History.class);
                    historyArrayList.add(history);
                    Collections.sort(historyArrayList,Collections.<History>reverseOrder());
                }
                firebaseResultInterface.onSuccess(historyArrayList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseResultInterface.onFailed(databaseError.toException());
            }
        });

    }
}

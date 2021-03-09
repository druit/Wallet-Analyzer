package gr.ict.wallet_analyzer.helpers;

import java.util.ArrayList;

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
}

package Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import data_class.History;
import gr.ict.wallet_analyzer.R;

public class MyListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private ArrayList<History> historyArrayList;

    public MyListAdapter(Activity context, ArrayList<History> historyArrayList) {
        super(context, R.layout.mylist);

        this.context = context;
        this.historyArrayList = historyArrayList;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.mylist, null, true);

        History history = historyArrayList.get(position);

        TextView titleText = rowView.findViewById(R.id.title);
        TextView subtitleText = rowView.findViewById(R.id.subtitle);

        titleText.setText(history.getReceipt().getStoreName());
        DecimalFormat decimalFormat = new DecimalFormat("#.##");


        subtitleText.setText( decimalFormat.format(history.getReceipt().getTotalPrice()) + " €");

        return rowView;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return historyArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
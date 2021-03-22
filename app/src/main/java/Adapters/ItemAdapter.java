package Adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import data_class.Item;
import gr.ict.wallet_analyzer.R;

public class ItemAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private List<Item> itemArrayList;

    public ItemAdapter(Activity context, List<Item> itemArrayList) {
        super(context, R.layout.mylist);

        this.context = context;
        this.itemArrayList = itemArrayList;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.mylist, null, true);

        Item item = itemArrayList.get(position);

        TextView titleText = rowView.findViewById(R.id.title);
        TextView subtitleText = rowView.findViewById(R.id.subtitle);

        titleText.setText(item.getName());
        subtitleText.setText(item.getPrice() + " €");

        return rowView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
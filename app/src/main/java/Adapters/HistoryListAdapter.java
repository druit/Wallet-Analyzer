package Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import data_class.Gradient;
import data_class.History;
import data_class.Receipt;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.CategoryIconSelector;
import gr.ict.wallet_analyzer.helpers.HistoryArrayList;

public class HistoryListAdapter extends ArrayAdapter<String> implements Filterable {

    private final Activity context;
    ArrayList<History> backupHistoryList;
    private HistoryArrayList historyArrayList;
    private final ItemFilter mFilter = new ItemFilter();

    public HistoryListAdapter(Activity context, HistoryArrayList historyArrayList) {
        super(context, R.layout.history_list);

        this.context = context;
        this.historyArrayList = historyArrayList;
        this.backupHistoryList = historyArrayList.getList();
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.history_list, null, true);

        History history = historyArrayList.getList().get(position);

        TextView titleText = rowView.findViewById(R.id.title);
        TextView subtitleText = rowView.findViewById(R.id.subtitle);
        TextView dateTextView = rowView.findViewById(R.id.date);

        titleText.setText(history.getReceipt().getStoreName());
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        subtitleText.setText(decimalFormat.format(history.getReceipt().getTotalPrice()) + " €");

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = formatter.format(history.getReceipt().getDate());

        dateTextView.setText(strDate);

        // set category icon
        CategoryIconSelector categoryIconSelector = new CategoryIconSelector(context);
        Drawable drawable = categoryIconSelector.getDrawableIcon(history.getReceipt().getStoreType());
        ImageView icon = rowView.findViewById(R.id.category_icon);
        icon.setImageDrawable(drawable);

//        Gradient gradient = StoreTypeFinder.findGradient(history.getReceipt().getStoreType());
//        setGradientBackground(rowView, gradient);

        return rowView;
    }

    private void setGradientBackground(View layout, Gradient itemGradient) {
        int colorStart = itemGradient.getStartColor();
        int colorEnd = itemGradient.getEndColor();

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{colorStart, colorEnd});
        gradient.setCornerRadius(0f);

        layout.setBackgroundDrawable(gradient);
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return historyArrayList.getList().size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Filter getFilter() {
        return mFilter;
    }

    public ArrayList<History> getFilteredList(String query) {
        query = query.toLowerCase();
        ArrayList<History> filteredHistoryArray = new ArrayList<>();
        for (History history : backupHistoryList) {
            Receipt receipt = history.getReceipt();
            boolean storeTypeQ = receipt.getStoreType().toLowerCase().contains(query);
            boolean addressTypeQ = receipt.getAddress().toLowerCase().contains(query);
            boolean storeNameTypeQ = receipt.getStoreName().toLowerCase().contains(query);
            if (storeTypeQ || addressTypeQ || storeNameTypeQ) {
                filteredHistoryArray.add(history);
            }
        }
        return filteredHistoryArray;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();

            final List<History> list = getFilteredList(filterString);
            int count = list.size();

            results.values = list;
            results.count = count;

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            historyArrayList.setHistoryArrayList((ArrayList<History>) results.values);
            notifyDataSetChanged();
        }
    }
}
package Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import data_class.Gradient;
import data_class.History;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.CategoryIconSelector;

public class HistoryListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private ArrayList<History> historyArrayList;

    public HistoryListAdapter(Activity context, ArrayList<History> historyArrayList) {
        super(context, R.layout.history_list);

        this.context = context;
        this.historyArrayList = historyArrayList;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint("ViewHolder") View rowView = inflater.inflate(R.layout.history_list, null, true);

        History history = historyArrayList.get(position);

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
        return historyArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
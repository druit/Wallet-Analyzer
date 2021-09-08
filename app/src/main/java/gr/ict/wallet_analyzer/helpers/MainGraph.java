package gr.ict.wallet_analyzer.helpers;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import data_class.History;
import gr.ict.wallet_analyzer.R;

public class MainGraph {
    private Activity activity;
    private LineChart chart;
    private HistoryArrayList historyArrayList;
    private LineDataSet dataSet;

    public MainGraph(Activity activity, HistoryArrayList historyArrayList, LineDataSet dataSet) {
        this.activity = activity;
        this.historyArrayList = historyArrayList;
        this.dataSet = dataSet;
        setListener();
    }

    private void setListener() {
        historyArrayList.setListener(new ListeningVariable.ChangeListener() {
            @Override
            public void onChange(Object object) {
                setGraphView(historyArrayList.getHistoryArrayList());
            }
        });
    }

    private void setGraphView(ArrayList<History> historyArrayList) {
        chart = activity.findViewById(R.id.chart);

        List<Entry> entries = new ArrayList<>();

        if (historyArrayList.size() != 0) {
            for (int i = 0; i < 6; i++) {
                if (historyArrayList.size() > i) {
                    History tempHistory = historyArrayList.get(i);
                    entries.add(new Entry(i, (float) tempHistory.getReceipt().getTotalPrice()));
                }
            }
        }

        dataSet = new LineDataSet(entries, "April"); // add entries to dataset

        // make line curvy
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // circles color
        dataSet.setCircleColor(R.color.colorMainGraphStart);
        dataSet.setCircleHoleColor(R.color.colorMainGraphStart);

        // Gradient fill
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.gradient_main_graph);
        dataSet.setFillDrawable(drawable);

        // line color
        dataSet.setColor(Color.rgb(53, 54, 67));
        // values text color
        dataSet.setValueTextColor(Color.rgb(255, 255, 255));

        // disable cross on click
        dataSet.setHighlightEnabled(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        // hide values in left and right side
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().setDrawLabels(false);

        // no zoom
        chart.setScaleEnabled(false);

        // grid lines color
        chart.getXAxis().setGridColor(R.color.colorMainGraphGrid);
        chart.getAxisLeft().setGridColor(R.color.colorMainGraphGrid);
        chart.getAxisRight().setGridColor(R.color.colorMainGraphGrid);

        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);

        // show outlines of the grid
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getXAxis().setDrawAxisLine(false);

        chart.getXAxis().setAxisLineColor(R.color.colorMainGraphGrid);
        chart.getAxisLeft().setAxisLineColor(R.color.colorMainGraphGrid);
        chart.getAxisRight().setAxisLineColor(R.color.colorMainGraphGrid);

        // text color of labels in x axis
        chart.getXAxis().setTextColor(Color.argb(50, 255, 255, 255));
        chart.getAxisLeft().setTextColor(Color.argb(50, 255, 255, 255));

//        chart.getAxisLeft().setAxisMinimum(0);
//        chart.getXAxis().setAxisMaximum(30);
//        chart.getXAxis().setAxisMinimum(0);
//        chart.getXAxis().setLabelCount(30, true);

//        chart.setVisibleXRangeMaximum(10);
    }
}

package gr.ict.wallet_analyzer.helpers;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import data_class.History;
import gr.ict.wallet_analyzer.R;

public class MainGraph {
    private Activity activity;
    private LineChart chart;
    private HistoryArrayList historyArrayList;
    private List<ILineDataSet> lineDataSet = new ArrayList<>();

    public MainGraph(Activity activity, HistoryArrayList historyArrayList) {
        this.activity = activity;
        this.historyArrayList = historyArrayList;
        setListener();
    }

    private void setListener() {
        historyArrayList.setListener(new ListeningVariable.ChangeListener() {
            @Override
            public void onChange(Object object) {
                secondLine(historyArrayList.getHistoryArrayList());
                setGraphView(historyArrayList.getHistoryArrayList());
                addAllDataSets();
            }
        });
    }

    private void setGraphView(ArrayList<History> historyArrayList) {
        chart = activity.findViewById(R.id.chart);

        List<String> xAxisValues = new ArrayList<>();
        List<Entry> entries = new ArrayList<>();

        int maxIterations = 6;
        if (historyArrayList.size() != 0) {
            if (historyArrayList.size() < maxIterations) {
                maxIterations = historyArrayList.size();
            }

            for (int i = 0; i < maxIterations; i++) {
                History tempHistory = historyArrayList.get(i);
                float totalPrice = (float) tempHistory.getReceipt().getTotalPrice();
                entries.add(new Entry(i, totalPrice));

                String dateString = new SimpleDateFormat("dd/MM/yy").format(tempHistory.getReceipt().getDate());
                xAxisValues.add(dateString);
            }
        } else {
            maxIterations = 0;
        }

        //String setter in x-Axis
        chart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisValues));

        LineDataSet dataSet = new LineDataSet(entries, "April"); // add entries to dataset

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

        lineDataSet.add(dataSet);

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

        chart.animateXY(400, 500);

        chart.getXAxis().setLabelCount(maxIterations, true);

//        chart.getAxisLeft().setAxisMinimum(0);
//        chart.getXAxis().setAxisMaximum(30);
//        chart.getXAxis().setAxisMinimum(0);
//        chart.setVisibleXRangeMaximum(6);
//        chart.setVisibleXRangeMinimum(6);
    }

    private void secondLine(ArrayList<History> historyArrayList) {
        chart = activity.findViewById(R.id.chart);

        LineDataSet dataSet2;
        List<Entry> entries = new ArrayList<>();
        List<String> xAxisValues = new ArrayList<>();

        lineDataSet.clear();

        int maxIterations = 6;
        if (historyArrayList.size() != 0) {
            if (historyArrayList.size() < maxIterations) {
                maxIterations = historyArrayList.size();
            }

            for (int i = 0; i < maxIterations; i++) {
                History tempHistory = historyArrayList.get(i);
                float totalPrice = (float) tempHistory.getReceipt().getTotalPrice();
                entries.add(new Entry(i, totalPrice + 1 + i));

                String dateString = new SimpleDateFormat("dd/MM/yy").format(tempHistory.getReceipt().getDate());
                xAxisValues.add(dateString);
            }
        }

        //String setter in x-Axis
        chart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(xAxisValues));

        dataSet2 = new LineDataSet(entries, "April"); // add entries to dataset

        // line color
        dataSet2.setColor(Color.argb(40, 255, 255, 255));

        // circles color
        dataSet2.setCircleColor(Color.argb(0, 0, 0, 0));
        dataSet2.setCircleHoleColor(Color.argb(0, 0, 0, 0));

        // values text color
        dataSet2.setValueTextColor(Color.argb(0, 255, 255, 255));

        // make line curvy
        dataSet2.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // Gradient fill
        dataSet2.setDrawFilled(false);
        Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.transparent);
        dataSet2.setFillDrawable(drawable);

        lineDataSet.add(dataSet2);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        // hide values in left and right side
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().setDrawLabels(false);

        // no zoom
        chart.setScaleEnabled(false);
    }

    private void addAllDataSets() {
        LineData lineData = new LineData(lineDataSet);

        chart.setData(lineData);
        chart.invalidate(); // refresh
    }
}

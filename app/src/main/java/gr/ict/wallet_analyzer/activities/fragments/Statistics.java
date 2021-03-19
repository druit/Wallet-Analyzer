package gr.ict.wallet_analyzer.activities.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import data_class.History;
import data_class.YourData;
import gr.ict.wallet_analyzer.R;

public class Statistics extends Fragment {

    private PieChart chart;
    private PieDataSet dataSet;
    final List<PieEntry> entries = new ArrayList<>();



    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // setFilter per category
        createFilterCategory();

        // filter for months
        setFilterMonths();

        // set Chart options
        setOptionsChart();

        // Create PieEntry ArrayList
        createPieEntries();

        // PieDataSet
        createPieDataSet();


        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.invalidate(); // refresh

        chart.setCenterText("Category: " + 18.8f + "€");

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                chart.setCenterText(pe.getLabel()+": " + e.getY() + "€");
            }

            @Override
            public void onNothingSelected() {

            }
        });




        super.onViewCreated(view, savedInstanceState);
    }

    private void createFilterCategory() {
        Spinner spinner1 =  getActivity().findViewById(R.id.filterChart1);
        final Spinner spinner2 =  getActivity().findViewById(R.id.filterChart2);
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        String[] items = new String[]{
                getString(R.string.filter_obj_1), getString(R.string.filter_obj_2),
                getString(R.string.filter_obj_3)
        };

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("POSITION: " + position);
                spinner2.setVisibility(View.INVISIBLE);

                switch(position){
                    case 0 :

                        break;
                    case 1 :
                        setFilterMonths();
                        spinner2.setVisibility(View.VISIBLE);
                        break;
                    case 2 :

                        break;
                    default:
                        break;
                }
//                ArrayList<History> historyMonthList = getHistoryListByMonth(position + 1);

                // clear graph first
//                clearChart();

                // add graph values
//                for (History history : historyMonthList) {
//                    updateGraph(history);
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, items);
        //set the spinners adapter to the previously created one.
        spinner1.setAdapter(spinnerAdapter);
        spinner1.setSelection(Integer.parseInt(dateFormat.format(date)) - 1);
    }

    private void setFilterMonths() {
        Spinner spinner2 =  getActivity().findViewById(R.id.filterChart2);
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();

        String[] items = new String[]{
                getString(R.string.gen_month_1), getString(R.string.gen_month_2),
                getString(R.string.gen_month_3), getString(R.string.gen_month_4),
                getString(R.string.gen_month_5), getString(R.string.gen_month_6),
                getString(R.string.gen_month_7), getString(R.string.gen_month_8),
                getString(R.string.gen_month_9), getString(R.string.gen_month_10),
                getString(R.string.gen_month_11), getString(R.string.gen_month_12)
        };

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                ArrayList<History> historyMonthList = getHistoryListByMonth(position + 1);

                // clear graph first
//                clearChart();

                // add graph values
//                for (History history : historyMonthList) {
//                    updateGraph(history);
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, items);
        //set the spinners adapter to the previously created one.
        spinner2.setAdapter(spinnerAdapter);
        spinner2.setSelection(Integer.parseInt(dateFormat.format(date)) - 1);
    }

    private void clearChart() {
        dataSet.clear();

        dataSet.notifyDataSetChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void createPieDataSet() {
        dataSet = new PieDataSet(entries, ""); // add entries to dataset

        dataSet.setValueFormatter(new PercentFormatter(chart));

        // line color
        dataSet.setColors(new int[] {Color.argb(50,100,92,110),Color.argb(50,20,20,110),Color.argb(50,200,250,110), Color.BLUE,Color.RED,Color.GREEN,Color.YELLOW,Color.LTGRAY});
        // values text color
        dataSet.setValueTextColor(Color.rgb(255, 255, 255));
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
    }

    private void createPieEntries() {
        entries.add(new PieEntry(18,"Green"));
        entries.add(new PieEntry(26, "Yellow"));
        entries.add(new PieEntry(30, "Blue"));
        entries.add(new PieEntry(12, "EN"));
        entries.add(new PieEntry(2f, "EL"));
    }

    private void setOptionsChart() {
        chart = getActivity().findViewById(R.id.pieChartView);
//        chart.setHoleRadius(20);
//        chart.setTransparentCircleRadius(25);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraLeftOffset(5);

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setDrawCenterText(true);
        chart.setDrawEntryLabels(false);



        chart.animateXY(900,500);

    }

    private class MyValueFormater extends ValueFormatter implements IValueFormatter{

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            System.out.println("VALUE" + value + "%");
            return "10%";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statistics_fragment,container,false);
    }


}

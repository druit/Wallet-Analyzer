package gr.ict.wallet_analyzer.activities.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import data_class.History;
import data_class.YourData;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.FirebaseResultInterface;
import gr.ict.wallet_analyzer.helpers.HistoryArrayList;

import static com.github.mikephil.charting.animation.Easing.Linear;

public class Statistics extends Fragment {

    LineChart lineChart;
    private LineDataSet lineDataSet1,lineDataSet2;

    private PieChart chart;
    private PieDataSet dataSet;
    final List<PieEntry> entries = new ArrayList<>();
    private HistoryArrayList historyArrayList = new HistoryArrayList();
    private HashMap<String, Double> historyHashMap = new HashMap();
    private List<String> categories = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String uid = user.getUid();
    DatabaseReference baseReference = FirebaseDatabase.getInstance().getReference()
            .child("users").child(uid);
    DateFormat dateFormatMonths = new SimpleDateFormat("MM");
    Date currentDate = new Date();
    String positionMonths = String.valueOf(Integer.parseInt(dateFormatMonths.format(currentDate)) - 1);
    int lastYearSelectedInMonths =  new Date().getYear();

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
//        setFilterMonths();
        setLineChart();
        // set Chart options
        setOptionsChart();

        // Create PieEntry ArrayList

        FirebaseResultInterface firebaseResultInterface = new FirebaseResultInterface<ArrayList<History>>() {
            @Override
            public void onSuccess(ArrayList<History> historyArrayList) {

                filterArrayBySelectedSpinner(historyArrayList,"category",null);

                createPieEntries(historyHashMap);

                // PieDataSet
                createPieDataSet();

                setPieData();

            }

            @Override
            public void onFailed(Throwable error) {

            }
        };
        historyArrayList.callBackHistoryArrayList(baseReference,firebaseResultInterface);





        super.onViewCreated(view, savedInstanceState);
    }

    private void setLineChart() {
        lineChart = getActivity().findViewById(R.id.lineChart);


        YourData[] dataObjects = {
                new YourData(0, 0),
                new YourData(1, 1500),
                new YourData(2, 1300),
                new YourData(3, 1100),
                new YourData(4, 1000),
                new YourData(5, 800)
        };
        YourData[] dataObjects2 = {
                new YourData(0, 0),
                new YourData(1, 0),
                new YourData(2, 200),
                new YourData(3, 400),
                new YourData(4, 500),
                new YourData(5, 700)
        };

        List<Entry> entries = new ArrayList<>();
        List<Entry> entries2 = new ArrayList<>();

        for (YourData data : dataObjects) {
            // turn your data into Entry objects
            entries.add(new Entry(data.getX(), data.getY()));
        }
        for (YourData data : dataObjects2) {
            // turn your data into Entry objects
            entries2.add(new Entry(data.getX(), data.getY()));
        }

        List<ILineDataSet> lineDataSet = new ArrayList<ILineDataSet>();

        lineDataSet1 = new LineDataSet(entries, "Income"); // add entries to dataset
        lineDataSet2 = new LineDataSet(entries2, "Expenses");



        // make line curvy
        lineDataSet1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // circles color
        lineDataSet1.setCircleColor(Color.rgb(95, 115, 193));
        lineDataSet1.setCircleHoleColor(Color.rgb(95, 115, 193));

        // Gradient fill
        lineDataSet1.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.graph_gradient);
        lineDataSet1.setFillDrawable(drawable);

        // line color
        lineDataSet1.setColor(Color.rgb(95, 115, 193));
        // values text color
        lineDataSet1.setValueTextColor(Color.rgb(255, 255, 255));

        // make line curvy
        lineDataSet2.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // circles color
        lineDataSet2.setCircleColor(Color.RED);
        lineDataSet2.setCircleHoleColor(Color.RED);

        // Gradient fill
        lineDataSet2.setDrawFilled(true);
        Drawable drawable2 = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.gradient_background_linechart);
        lineDataSet2.setFillDrawable(drawable2);

        // line color
        lineDataSet2.setColor(Color.RED);
        // values text color
        lineDataSet2.setValueTextColor(Color.rgb(255, 255, 255));

        lineDataSet.add(lineDataSet1);
        lineDataSet.add(lineDataSet2);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        // hide values in left and right side
        lineChart.getAxisRight().setDrawLabels(false);
//        chart.getAxisLeft().setDrawLabels(false);

        // no zoom
        lineChart.setScaleEnabled(false);

        // grid lines color
        lineChart.getXAxis().setGridColor(R.color.colorButton);
        lineChart.getAxisLeft().setGridColor(R.color.colorButton);
        lineChart.getAxisRight().setGridColor(R.color.colorButton);
        lineChart.getAxisRight().setDrawGridLines(false);

        // show outlines of the grid
        lineChart.getAxisLeft().setDrawAxisLine(true);
        lineChart.getAxisRight().setDrawAxisLine(true);
        lineChart.getXAxis().setDrawAxisLine(true);

        lineChart.getXAxis().setAxisLineColor(R.color.colorButton);
        lineChart.getAxisLeft().setAxisLineColor(R.color.colorButton);
        lineChart.getAxisRight().setAxisLineColor(R.color.colorButton);

        // text color of labels in x axis
        lineChart.getXAxis().setTextColor(Color.argb(50, 255, 255, 255));
        lineChart.getAxisLeft().setTextColor(Color.argb(50, 255, 255, 255));

        lineChart.getAxisLeft().setAxisMinimum(0);
        lineChart.getXAxis().setAxisMaximum(5);
        lineChart.getXAxis().setAxisMinimum(0);
        lineChart.getXAxis().setLabelCount(30, true);
        lineChart.animateXY(800,1000, Linear );

    }

    private void setPieData() {
        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.invalidate(); // refresh

        if(!categories.isEmpty()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            chart.setCenterText(categories.get(0) + ": " + Float.valueOf(decimalFormat.format(historyHashMap.get(categories.get(0)))) + "€");
        }else{
            chart.setCenterText("Category: " + "0.0 €");
        }

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
    }

    private void filterArrayBySelectedSpinner(ArrayList<History> historyArrayList, String type,String selected) {
        for (History history : historyArrayList) {
            boolean thereIsCategory = false;
            boolean filterFind = false;
            switch (type){
                case "category":
                    thereIsCategory = checkThereIsCategorie(history);
                    break;
                case "year":
                    if(history.getReceipt().getDate().toString().contains(selected)){
                        thereIsCategory = checkThereIsCategorie(history);
                        filterFind = true;
                    }
                    break;
                case "week":
                    int currentDate = new Date().getYear();
                    String d1 = String.valueOf(history.getReceipt().getDate().getDate());
                    String d2 = String.valueOf(history.getReceipt().getDate().getMonth()+1);
                    String receiptDate;


                    if(currentDate == history.getReceipt().getDate().getYear()){
                        System.out.println("DATE1 : "+ currentDate);
                        System.out.println("HISTORY1 : "+ history.getReceipt().getDate().getYear());
                        System.out.println("FULL : "+ d1 + "/" + d2);
                        if(d1.length() == 1){
                            d1 = "0"+ d1;
                        }
                        if(d2.length() == 1){
                            d2 = "0"+ d2;
                        }
                        receiptDate = d1 + "/" + d2;
                        System.out.println("SELECTED: " + receiptDate + "   -    " + selected);
                        if(receiptDate.contains(selected)){
                            thereIsCategory = checkThereIsCategorie(history);
                            filterFind = true;
                        }
                    }
                    break;
                case "month":
                    int year = lastYearSelectedInMonths;
                    String month =  String.valueOf(history.getReceipt().getDate().getMonth()+1);
                    if(year == history.getReceipt().getDate().getYear()){
                        if(month.length() == 1){
                            month = "0"+month;
                        }

                        if(month.contains(positionMonths)){
                            thereIsCategory = checkThereIsCategorie(history);
                            filterFind = true;
                        }
                    }

                    break;
                default:
                    break;
            }

            if(thereIsCategory) {
                historyHashMap.put(history.getReceipt().getStoreType(), historyHashMap.get(history.getReceipt().getStoreType()) + history.getReceipt().getTotalPrice());
            }else if (type.equals("category") || filterFind){
                historyHashMap.put(history.getReceipt().getStoreType(),history.getReceipt().getTotalPrice());
                categories.add(history.getReceipt().getStoreType());
            }

        }


    }

    private boolean checkThereIsCategorie(History history) {
        if(categories.size()> 0) {
            for (String category : categories) {
                if (category.equals(history.getReceipt().getStoreType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createFilterCategory() {
        Spinner spinner1 =  getActivity().findViewById(R.id.filterChart1);
        final Spinner spinner2 =  getActivity().findViewById(R.id.filterChart2);
        final Spinner spinner3 =  getActivity().findViewById(R.id.filterChart3);

        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        String items[] = {
                getString(R.string.filter_obj_2),
                getString(R.string.filter_obj_3),  getString(R.string.filter_obj_1), getString(R.string.filter_obj_4)
        };

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                spinner2.setVisibility(View.INVISIBLE);
                spinner3.setVisibility(View.INVISIBLE);

                switch(position){
                    case 0 :
                        setFilter("month");
                        spinner2.setVisibility(View.VISIBLE);
                        spinner3.setVisibility(View.VISIBLE);
                        break;
                    case 1 :
                        setFilter("week");
                        spinner2.setVisibility(View.VISIBLE);
                        break;
                    case 2 :
                        setFilter("category");
                        break;
                    case 3 :
                        setFilter("year");
                        spinner2.setVisibility(View.VISIBLE);
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
//        spinner1.setSelection(Integer.parseInt(dateFormat.format(date)) - 1);
        spinner1.setSelection(2,true);
    }

    private void setFilter(final String type) {
        String[] items={};
        String[] yearsOrDates = {};
        Spinner spinner2 =  getActivity().findViewById(R.id.filterChart2);
        Spinner spinner3 = getActivity().findViewById(R.id.filterChart3);
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        switch (type){
            case "month":
                items = new String[]{
                        getString(R.string.gen_month_1), getString(R.string.gen_month_2),
                        getString(R.string.gen_month_3), getString(R.string.gen_month_4),
                        getString(R.string.gen_month_5), getString(R.string.gen_month_6),
                        getString(R.string.gen_month_7), getString(R.string.gen_month_8),
                        getString(R.string.gen_month_9), getString(R.string.gen_month_10),
                        getString(R.string.gen_month_11), getString(R.string.gen_month_12)
                };
                break;
            case "week":
                yearsOrDates = new String[]{getPreviousYearOrDay(0,"week"), getPreviousYearOrDay(-1,"week"), getPreviousYearOrDay(-2,"week"), getPreviousYearOrDay(-3,"week"), getPreviousYearOrDay(-4,"week"), getPreviousYearOrDay(-5,"week"), getPreviousYearOrDay(-6,"week")};
                items = yearsOrDates;
                break;
            case "year":
                yearsOrDates = new String[]{getPreviousYearOrDay(0,"year"),getPreviousYearOrDay(-1,"year"),getPreviousYearOrDay(-2,"year"),getPreviousYearOrDay(-3,"year"),getPreviousYearOrDay(-4,"year"),getPreviousYearOrDay(-5,"year"),getPreviousYearOrDay(-6,"year"),getPreviousYearOrDay(-7,"year"),getPreviousYearOrDay(-8,"year"), getPreviousYearOrDay(-9,"year"), getPreviousYearOrDay(-10,"year")};
                items = yearsOrDates;
                break;
            default:
                callBackPieDataChart(type,null);
                break;
        }


        final String[] finalItems = items;
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
//                ArrayList<History> historyMonthList = getHistoryListByMonth(position + 1);
                if(type.equals("month")) {
                    positionMonths = String.valueOf(position + 1);

                    if (positionMonths.length() == 1) {
                        positionMonths = "0" + positionMonths;
                    }
                    callBackPieDataChart(type, positionMonths);
                }else{
                    callBackPieDataChart(type,finalItems[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,final int position, long l) {
                lastYearSelectedInMonths = new Date().getYear() - position;
                callBackPieDataChart(type, positionMonths);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        yearsOrDates = new String[]{getPreviousYearOrDay(0,"year"),getPreviousYearOrDay(-1,"year"),getPreviousYearOrDay(-2,"year"),getPreviousYearOrDay(-3,"year"),getPreviousYearOrDay(-4,"year"),getPreviousYearOrDay(-5,"year"),getPreviousYearOrDay(-6,"year"),getPreviousYearOrDay(-7,"year"),getPreviousYearOrDay(-8,"year"), getPreviousYearOrDay(-9,"year"), getPreviousYearOrDay(-10,"year")};

        ArrayAdapter spinnerAdapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, yearsOrDates);
        //set the spinners adapter to the previously created one.
        spinner3.setAdapter(spinnerAdapter2);
        spinner3.setSelection(0,true);

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, items);
        //set the spinners adapter to the previously created one.
        spinner2.setAdapter(spinnerAdapter);
        if(type.equals("month")){
            spinner2.setSelection(Integer.parseInt(dateFormat.format(date)) - 1,true);
        }else{
            spinner2.setSelection(0,true);
        }

    }

    private void callBackPieDataChart(final String type, final String item) {
        FirebaseResultInterface firebaseResultInterface = new FirebaseResultInterface<ArrayList<History>>() {

            @Override
            public void onSuccess(ArrayList<History> historyArrayList) {
                clearChart();
                categories.clear();
                setOptionsChart();
                filterArrayBySelectedSpinner(historyArrayList,type,item);
                //createEntries
                createPieEntries(historyHashMap);
                // PieDataSet
                createPieDataSet();
                //setData on Pie
                setPieData();
                dataSet.notifyDataSetChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
            }

            @Override
            public void onFailed(Throwable error) {

            }
        };

        // clear graph first
//                clearChart();

        // add graph values
//                for (History history : historyMonthList) {
//                    updateGraph(history);
//                }
        historyArrayList.callBackHistoryArrayList(baseReference,firebaseResultInterface);
    }

    private String getPreviousYearOrDay(int i, String type) {
        Calendar prevYear = Calendar.getInstance();
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy");
        switch(type){
            case "week" :
                isoFormat = new SimpleDateFormat("dd/MM");
                prevYear.add(Calendar.DAY_OF_MONTH, i);
                break;
            case "year" :
                prevYear.add(Calendar.YEAR, i);
                break;
            default :
                break;
        }
        String previousYearDate = isoFormat.format(prevYear.getTime());

        return previousYearDate;
    }

    private void clearChart() {
        dataSet.clear();
        entries.clear();

        dataSet.notifyDataSetChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void createPieDataSet() {
        dataSet = new PieDataSet(entries, ""); // add entries to dataset

        dataSet.setValueFormatter(new PercentFormatter(chart));
        // line color
        dataSet.setColors(new int[] {Color.argb(70,155,55,69),Color.argb(70,192,106,0),Color.argb(70,161,161,0), Color.argb(70,0,57,69),Color.argb(70,20,81,111),Color.argb(70,20,99,61),});

        // values text color
        dataSet.setValueTextColor(Color.rgb(255, 255, 255));
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
    }

    private void createPieEntries(HashMap<String, Double> historyHashMap) {
        System.out.println("CATEGORIES: "+ categories);
        if(categories.size()>0) {
            for (String category : categories) {
                entries.add(new PieEntry(historyHashMap.get(category).floatValue(), category));
            }
            chart.getLegend().setEnabled(true);
        }else{
            entries.add(new PieEntry(0f, ""));
            chart.getLegend().setEnabled(false);
        }

//        entries.add(new PieEntry(18,"Green"));
//        entries.add(new PieEntry(26, "Yellow"));
//        entries.add(new PieEntry(30, "Blue"));
//        entries.add(new PieEntry(12, "EN"));
//        entries.add(new PieEntry(2f, "EL"));
    }

    private void setOptionsChart() {
        chart = getActivity().findViewById(R.id.pieChartView);
//        chart.setHoleRadius(20);
//        chart.setTransparentCircleRadius(25);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraLeftOffset(5);

        chart.setDrawHoleEnabled(true);
//        chart.setHoleColor(Color.WHITE);
        chart.setHoleColor(0);
        chart.setCenterTextColor(Color.WHITE);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setDrawCenterText(true);
        chart.setDrawEntryLabels(false);



        chart.animateXY(900,500);

    }

    private class MyValueFormater extends ValueFormatter implements IValueFormatter{

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            return "10%";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statistics_fragment,container,false);
    }


}

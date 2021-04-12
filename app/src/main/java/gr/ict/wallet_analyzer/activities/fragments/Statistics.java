package gr.ict.wallet_analyzer.activities.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import Adapters.MyAccountAdapter;
import data_class.BankAccount;
import data_class.History;
import data_class.Receipt;
import data_class.Salary;
import data_class.YourData;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.BankEditPopup;
import gr.ict.wallet_analyzer.helpers.FirebaseResultInterface;
import gr.ict.wallet_analyzer.helpers.HistoryArrayList;

import static com.github.mikephil.charting.animation.Easing.Linear;

public class Statistics extends Fragment {

    //Line Chart Values
    private LineChart lineChart;
    private LineDataSet lineDataSet1,lineDataSet2,lineDataSetFormat;
    private  List<ILineDataSet> lineDataSet = new ArrayList<>();
    final List<Entry> entries1 = new ArrayList<>();
    final List<Entry> entries2 = new ArrayList<>();
    private ArrayList<String> xEntrys = new ArrayList<>();
    private String[] xData = {};
    private String lastType = "category";

    //Pie Chart Values
    private PieChart chart;
    private PieDataSet dataSet;
    final List<PieEntry> entries = new ArrayList<>();
    //DATE format
    DateFormat dateFormatMonths = new SimpleDateFormat("MM");
    Date currentDate = new Date();
    String positionMonths = String.valueOf(Integer.parseInt(dateFormatMonths.format(currentDate)) - 1);
    int lastYearSelectedInMonths =  new Date().getYear();

    Calendar c = Calendar.getInstance();
    int currentYear =  c.get(Calendar.YEAR);
    int lastMonthSelected = Integer.valueOf(c.get(Calendar.MONTH)+1);

    //HISTORY Values
    private HistoryArrayList historyArrayList = new HistoryArrayList();
    private HashMap<String, Double> historyHashMap = new HashMap();
    private List<String> categories = new ArrayList<>();
    private ArrayList<Receipt>  receiptArrayList = new ArrayList<>();

    //FIREBASE Connections
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String uid = user.getUid();
    DatabaseReference baseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
    BankAccount selectedBankAccount = new BankAccount();
    ArrayList<Salary> salaryArrayList = new ArrayList<>();
    private BankEditPopup bankEditPopup = new BankEditPopup();

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

        // set Pie Chart options
        setOptionsChart();
        //set Line Chart options
        setXDatas();
        setOptionLineChart();

        // START CREATE CHARTS
        FirebaseResultInterface firebaseResultInterface = new FirebaseResultInterface<ArrayList<History>>() {
            @Override
            public void onSuccess(ArrayList<History> historyArrayList) {

                //get bank account with salary
                FirebaseResultInterface firebaseResultInterface2 = new FirebaseResultInterface<ArrayList<Salary>>() {

                    @Override
                    public void onSuccess(ArrayList<Salary> data) {
                        salaryArrayList = data;
                    }

                    @Override
                    public void onFailed(Throwable error) {

                    }
                };

                bankEditPopup.getSalaryList(baseReference,firebaseResultInterface2);

                filterArrayBySelectedSpinner(historyArrayList,"category",null);

                createPieEntries(historyHashMap);

                // set Line Graph
                setLineChart(salaryArrayList);


                // PieDataSet
                createPieDataSet();

                setPieData();
            }

            @Override
            public void onFailed(Throwable error) { }
        };
        historyArrayList.callBackHistoryArrayList(baseReference,firebaseResultInterface);
        super.onViewCreated(view, savedInstanceState);
    }



    private void setLineChart(ArrayList<Salary> salaryList) {
//        ArrayList<Salary> salaryList = new ArrayList<>();
//        salaryList = account.getSalaryArrayList();
        double totalIncome = 50;
        double totalExpenses = 0;

        for (Salary salary: salaryList) {
            if (salary.getLastUpdate().getYear() == lastYearSelectedInMonths &&  Integer.valueOf(salary.getLastUpdate().getMonth()+1) == lastMonthSelected){
                totalIncome = salary.getCurrentSalary();
            }
        }






        int previousReceiptMonth = 0;


        if(!receiptArrayList.isEmpty()){
            entries1.add(new Entry(0, (float) totalIncome));
            entries2.add(new Entry(0, (float) totalExpenses));
        }
        for (Receipt receipt :receiptArrayList) {
            totalIncome -= receipt.getTotalPrice();
            totalExpenses += receipt.getTotalPrice();

            previousReceiptMonth = createLineEntries(receipt,previousReceiptMonth,totalIncome,totalExpenses,lastType);
        }


//        for (YourData data : dataObjects2) {
//            // turn your data into Entry objects
//            entries2.add(new Entry(data.getX(), data.getY()));
//        }

        String incomes = getContext().getResources().getString(R.string.gen_income);
        String expenses = getContext().getResources().getString(R.string.gen_expenses);

        lineDataSet1 = new LineDataSet(entries1, incomes); // add entries to dataset

        lineDataSet2 = new LineDataSet(entries2, expenses);

        setLineDataSetValueFormat();

        lineDataSet.add(lineDataSet1);
        lineDataSet.add(lineDataSet2);

        LineData lineData = new LineData(lineDataSet);
        if(!categories.isEmpty()) {
            lineData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    if (value > 0) {
                        return super.getFormattedValue(Float.parseFloat(value + " €"));
                    } else {
                        return "";
                    }
                }
            });

            XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    if(value> 0 && xData.length > value) return xData[(int) value]; // xVal is a string array
                    else return "";
                }
            });
        }
        lineChart.setData(lineData);
//        lineChart.invalidate(); // refresh
    }

    // FORMAT VALUES IN LINE CHART
    private void setLineDataSetValueFormat() {

        lineDataSet1.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return super.getFormattedValue(Float.parseFloat(value + " €"));
            }
        });

        lineDataSet2.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return super.getFormattedValue(Float.parseFloat(value + " €"));
            }
        });


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
    }

    // X data in graph (labels) LINE CHART
    private void setXDatas() {
        xEntrys.clear();
        for(int i = 1; i < xData.length; i++){
            xEntrys.add(xData[i]);
        }
    }

    //ENTRIES LINE CHART
    private int createLineEntries(Receipt receipt,int previousReceiptMonth,double localSalary,double totalExpenses,String type) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        switch (type){
            case "category" :
            case "year" :
                if(Integer.valueOf(receipt.getDate().getMonth()+1) == previousReceiptMonth){
                    int index = Integer.valueOf(entries.size()-1);
                    String value = decimalFormat.format(localSalary);
                    entries1.get(entries.size()-index).setY((float)localSalary);

                    entries2.get(entries.size()-index).setY((float)totalExpenses);
                } else {
                    entries1.add(new Entry(Integer.valueOf(receipt.getDate().getMonth() + 1), (float) localSalary));
                    entries2.add(new Entry(Integer.valueOf(receipt.getDate().getMonth() + 1), (float) totalExpenses));
                    previousReceiptMonth = Integer.valueOf(receipt.getDate().getMonth()+1);
                }
                break;
            case "month" :
                if(Integer.valueOf(receipt.getDate().getDate()) == previousReceiptMonth){
                    int index = Integer.valueOf(entries.size()-1);
                    entries1.get(entries.size()-index).setY((float)localSalary);
                    entries2.get(entries.size()-index).setY((float) totalExpenses);
                } else {
                    entries1.add(new Entry(Integer.valueOf(receipt.getDate().getDate()), (float)localSalary));
                    entries2.add(new Entry(Integer.valueOf(receipt.getDate().getDate()),  (float) totalExpenses));
                    previousReceiptMonth = Integer.valueOf(receipt.getDate().getDate());
                }
                break;
            case "week" :
                int pos = 0;
                for(int i = 0; i< xData.length; i++){
                    if( xData[i].contains(String.valueOf(receipt.getDate().getDate()))){
                        pos = i;
                    }
                }
                if(Integer.valueOf(pos) == previousReceiptMonth){
                    int index = Integer.valueOf(entries.size()-1);
                    entries1.get(entries.size()-index).setY((float)localSalary);
                    entries2.get(entries.size()-index).setY((float) totalExpenses);
                } else {
                    entries1.add(new Entry(Integer.valueOf(pos), (float)localSalary));
                    entries2.add(new Entry(Integer.valueOf(pos),  (float) totalExpenses));
                    previousReceiptMonth = Integer.valueOf(pos);
                }
                break;
            default:
                break;

        }

        return previousReceiptMonth;
    }

    //OPTIONS LINE CHART
    private void setOptionLineChart() {
        lineChart = getActivity().findViewById(R.id.lineChart);
        lineChart.getDescription().setEnabled(true);
        String contextString;
        Description description = new Description();
        switch(lastType){
            case "year":
                contextString = getContext().getResources().getString(R.string.filter_obj_4);
                break;
            case "week":
                contextString = getContext().getResources().getString(R.string.filter_obj_3);
                break;
            default:
                contextString = getContext().getResources().getString(R.string.filter_obj_2);
                break;
        }
        description.setText(contextString);
        lineChart.setDescription(description);
        lineChart.getLegend().setEnabled(true);

        // hide values in left and right side
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getAxisLeft().setDrawLabels(false);

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

//        lineChart.getAxisLeft().setAxisMinimum(0);
//        lineChart.getXAxis().setAxisMaximum(5);
//        lineChart.getXAxis().setAxisMinimum(0);

//        lineChart.getXAxis().setLabelCount(5, true);
        lineChart.animateXY(300,300 );
    }

    private void setPieData() {
        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.invalidate(); // refresh

        if(!categories.isEmpty()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            chart.setCenterText(categories.get(0) + ": " + decimalFormat.format(historyHashMap.get(categories.get(0))) + "€");
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
            boolean filterFindForLine = false;
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

                    int firstDay = Integer.valueOf(getPreviousYearOrDay(-6,"week").split("/")[0]);
                    int lastDay = Integer.valueOf(getPreviousYearOrDay(0,"week").split("/")[0]);
//                    System.out.println("FIRST: " + firstDay);

                    if(Integer.valueOf(d1) >= firstDay &&  Integer.valueOf(d1) <= lastDay){
                        filterFindForLine = true;
                    }


                    if(currentDate == history.getReceipt().getDate().getYear()){
//                        System.out.println("DATE1 : "+ currentDate);
//                        System.out.println("HISTORY1 : "+ history.getReceipt().getDate().getYear());
//                        System.out.println("FULL : "+ d1 + "/" + d2);
                        if(d1.length() == 1){
                            d1 = "0"+ d1;
                        }
                        if(d2.length() == 1){
                            d2 = "0"+ d2;
                        }
                        receiptDate = d1 + "/" + d2;
//                        System.out.println("SELECTED: " + receiptDate + "   -    " + selected);
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
            lastType = type;

            if(thereIsCategory) {
                historyHashMap.put(history.getReceipt().getStoreType(), historyHashMap.get(history.getReceipt().getStoreType()) + history.getReceipt().getTotalPrice());
//                receiptArrayList.add(history.getReceipt());
            }else if (type.equals("category") || filterFind){
                historyHashMap.put(history.getReceipt().getStoreType(),history.getReceipt().getTotalPrice());
                categories.add(history.getReceipt().getStoreType());
//                receiptArrayList.add(history.getReceipt());
            }
            if(type.contains("category") && history.getReceipt().getDate().toString().contains(getPreviousYearOrDay(0,"year"))){
                receiptArrayList.add(history.getReceipt());
            }else if(!type.contains("category") && (filterFind || filterFindForLine)){
                receiptArrayList.add(history.getReceipt());
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
        xData = new String[]{
                "",
                getString(R.string.gen_month_fast_1), getString(R.string.gen_month_fast_2),
                getString(R.string.gen_month_fast_3), getString(R.string.gen_month_fast_4),
                getString(R.string.gen_month_fast_5), getString(R.string.gen_month_fast_6),
                getString(R.string.gen_month_fast_7), getString(R.string.gen_month_fast_8),
                getString(R.string.gen_month_fast_9), getString(R.string.gen_month_fast_10),
                getString(R.string.gen_month_fast_11), getString(R.string.gen_month_fast_12)
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

                // Get the number of days in that month
                @SuppressLint({"NewApi", "LocalSuppress"}) YearMonth yearMonthObject = YearMonth.of(currentYear, lastMonthSelected);
                @SuppressLint({"NewApi", "LocalSuppress"}) int daysInMonth = yearMonthObject.lengthOfMonth();
//                for(int i = 0; i > daysInMonth ; i++) {
//                    xData[i] = getPreviousYearOrDay(i,"week");
//                }
                xData = getDatesOfMonth(daysInMonth);
//                xData = yearsOrDates;
                break;
            case "week":
                yearsOrDates = new String[]{getPreviousYearOrDay(0,"week"), getPreviousYearOrDay(-1,"week"), getPreviousYearOrDay(-2,"week"), getPreviousYearOrDay(-3,"week"), getPreviousYearOrDay(-4,"week"), getPreviousYearOrDay(-5,"week"), getPreviousYearOrDay(-6,"week")};
                items  =  yearsOrDates;
                xData = new String[]{"", getPreviousYearOrDay(-6,"week"), getPreviousYearOrDay(-5,"week"),getPreviousYearOrDay(-4,"week"),getPreviousYearOrDay(-3,"week"), getPreviousYearOrDay(-2,"week"), getPreviousYearOrDay(-1,"week"),getPreviousYearOrDay(0,"week")};

                break;
            case "year":

                yearsOrDates = new String[]{getPreviousYearOrDay(0,"year"),getPreviousYearOrDay(-1,"year"),getPreviousYearOrDay(-2,"year"),getPreviousYearOrDay(-3,"year"),getPreviousYearOrDay(-4,"year"),getPreviousYearOrDay(-5,"year"),getPreviousYearOrDay(-6,"year"),getPreviousYearOrDay(-7,"year"),getPreviousYearOrDay(-8,"year"), getPreviousYearOrDay(-9,"year"), getPreviousYearOrDay(-10,"year")};
                items = yearsOrDates;
                xData = new String[]{
                        "",
                        getString(R.string.gen_month_fast_1), getString(R.string.gen_month_fast_2),
                        getString(R.string.gen_month_fast_3), getString(R.string.gen_month_fast_4),
                        getString(R.string.gen_month_fast_5), getString(R.string.gen_month_fast_6),
                        getString(R.string.gen_month_fast_7), getString(R.string.gen_month_fast_8),
                        getString(R.string.gen_month_fast_9), getString(R.string.gen_month_fast_10),
                        getString(R.string.gen_month_fast_11), getString(R.string.gen_month_fast_12)
                };
                break;
            default:
                xData = new String[]{
                        "",
                        getString(R.string.gen_month_fast_1), getString(R.string.gen_month_fast_2),
                        getString(R.string.gen_month_fast_3), getString(R.string.gen_month_fast_4),
                        getString(R.string.gen_month_fast_5), getString(R.string.gen_month_fast_6),
                        getString(R.string.gen_month_fast_7), getString(R.string.gen_month_fast_8),
                        getString(R.string.gen_month_fast_9), getString(R.string.gen_month_fast_10),
                        getString(R.string.gen_month_fast_11), getString(R.string.gen_month_fast_12)
                };
                callBackPieDataChart(type,null);
                break;
        }


        final String[] finalItems = items;
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
//                ArrayList<History> historyMonthList = getHistoryListByMonth(position + 1);
                if(type.equals("month")) {
                    lastMonthSelected = position+1;
                    positionMonths = String.valueOf(position + 1);

                    if (positionMonths.length() == 1) {
                        positionMonths = "0" + positionMonths;
                    }
                    // Get the number of days in that month
                    @SuppressLint({"NewApi", "LocalSuppress"}) YearMonth yearMonthObject = YearMonth.of(currentYear, lastMonthSelected);
                    @SuppressLint({"NewApi", "LocalSuppress"}) int daysInMonth = yearMonthObject.lengthOfMonth();
                    xData = getDatesOfMonth(daysInMonth);
                    callBackPieDataChart(type, positionMonths);
                }else{
                    callBackPieDataChart(type,finalItems[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final String[] finalYearsOrDates = yearsOrDates;
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,final int position, long l) {
                lastYearSelectedInMonths = new Date().getYear() - position;
                if( finalYearsOrDates.length>0 && type.contains("month")) {
                    currentYear = c.get(Integer.parseInt(finalYearsOrDates[position]));

                    // Get the number of days in that month
                    @SuppressLint({"NewApi", "LocalSuppress"}) YearMonth yearMonthObject = YearMonth.of(currentYear, lastMonthSelected);
                    @SuppressLint({"NewApi", "LocalSuppress"}) int daysInMonth = yearMonthObject.lengthOfMonth();
                    xData = getDatesOfMonth(daysInMonth);
                }
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

    private String[] getDatesOfMonth(int daysInMonth) {
        String datesOfMonth[] = {};
        switch (daysInMonth){
            case 28:
                datesOfMonth = new String[]{
                        getCurrentDayOfSelected(-1),
                        getCurrentDayOfSelected(0),
                        getCurrentDayOfSelected(1),
                        getCurrentDayOfSelected(2),
                        getCurrentDayOfSelected(3),
                        getCurrentDayOfSelected(4),
                        getCurrentDayOfSelected(5),
                        getCurrentDayOfSelected(6),
                        getCurrentDayOfSelected(7),
                        getCurrentDayOfSelected(8),
                        getCurrentDayOfSelected(9),
                        getCurrentDayOfSelected(10),
                        getCurrentDayOfSelected(11),
                        getCurrentDayOfSelected(12),
                        getCurrentDayOfSelected(13),
                        getCurrentDayOfSelected(14),
                        getCurrentDayOfSelected(15),
                        getCurrentDayOfSelected(16),
                        getCurrentDayOfSelected(17),
                        getCurrentDayOfSelected(18),
                        getCurrentDayOfSelected(19),
                        getCurrentDayOfSelected(20),
                        getCurrentDayOfSelected(21),
                        getCurrentDayOfSelected(22),
                        getCurrentDayOfSelected(23),
                        getCurrentDayOfSelected(24),
                        getCurrentDayOfSelected(25),
                        getCurrentDayOfSelected(26),
                        getCurrentDayOfSelected(27),
                        getCurrentDayOfSelected(28)
                };
                break;
            case 29:
                datesOfMonth = new String[]{
                        getCurrentDayOfSelected(-1),
                        getCurrentDayOfSelected(0),
                        getCurrentDayOfSelected(1),
                        getCurrentDayOfSelected(2),
                        getCurrentDayOfSelected(3),
                        getCurrentDayOfSelected(4),
                        getCurrentDayOfSelected(5),
                        getCurrentDayOfSelected(6),
                        getCurrentDayOfSelected(7),
                        getCurrentDayOfSelected(8),
                        getCurrentDayOfSelected(9),
                        getCurrentDayOfSelected(10),
                        getCurrentDayOfSelected(11),
                        getCurrentDayOfSelected(12),
                        getCurrentDayOfSelected(13),
                        getCurrentDayOfSelected(14),
                        getCurrentDayOfSelected(15),
                        getCurrentDayOfSelected(16),
                        getCurrentDayOfSelected(17),
                        getCurrentDayOfSelected(18),
                        getCurrentDayOfSelected(19),
                        getCurrentDayOfSelected(20),
                        getCurrentDayOfSelected(21),
                        getCurrentDayOfSelected(22),
                        getCurrentDayOfSelected(23),
                        getCurrentDayOfSelected(24),
                        getCurrentDayOfSelected(25),
                        getCurrentDayOfSelected(26),
                        getCurrentDayOfSelected(27),
                        getCurrentDayOfSelected(28),
                        getCurrentDayOfSelected(29)
                };
                break;
            case 30:
                datesOfMonth = new String[]{
                        getCurrentDayOfSelected(-1),
                        getCurrentDayOfSelected(0),
                        getCurrentDayOfSelected(1),
                        getCurrentDayOfSelected(2),
                        getCurrentDayOfSelected(3),
                        getCurrentDayOfSelected(4),
                        getCurrentDayOfSelected(5),
                        getCurrentDayOfSelected(6),
                        getCurrentDayOfSelected(7),
                        getCurrentDayOfSelected(8),
                        getCurrentDayOfSelected(9),
                        getCurrentDayOfSelected(10),
                        getCurrentDayOfSelected(11),
                        getCurrentDayOfSelected(12),
                        getCurrentDayOfSelected(13),
                        getCurrentDayOfSelected(14),
                        getCurrentDayOfSelected(15),
                        getCurrentDayOfSelected(16),
                        getCurrentDayOfSelected(17),
                        getCurrentDayOfSelected(18),
                        getCurrentDayOfSelected(19),
                        getCurrentDayOfSelected(20),
                        getCurrentDayOfSelected(21),
                        getCurrentDayOfSelected(22),
                        getCurrentDayOfSelected(23),
                        getCurrentDayOfSelected(24),
                        getCurrentDayOfSelected(25),
                        getCurrentDayOfSelected(26),
                        getCurrentDayOfSelected(27),
                        getCurrentDayOfSelected(28),
                        getCurrentDayOfSelected(29),
                        getCurrentDayOfSelected(30)
                };
                break;
            case 31:
                datesOfMonth = new String[]{
                        getCurrentDayOfSelected(-1),
                        getCurrentDayOfSelected(0),
                        getCurrentDayOfSelected(1),
                        getCurrentDayOfSelected(2),
                        getCurrentDayOfSelected(3),
                        getCurrentDayOfSelected(4),
                        getCurrentDayOfSelected(5),
                        getCurrentDayOfSelected(6),
                        getCurrentDayOfSelected(7),
                        getCurrentDayOfSelected(8),
                        getCurrentDayOfSelected(9),
                        getCurrentDayOfSelected(10),
                        getCurrentDayOfSelected(11),
                        getCurrentDayOfSelected(12),
                        getCurrentDayOfSelected(13),
                        getCurrentDayOfSelected(14),
                        getCurrentDayOfSelected(15),
                        getCurrentDayOfSelected(16),
                        getCurrentDayOfSelected(17),
                        getCurrentDayOfSelected(18),
                        getCurrentDayOfSelected(19),
                        getCurrentDayOfSelected(20),
                        getCurrentDayOfSelected(21),
                        getCurrentDayOfSelected(22),
                        getCurrentDayOfSelected(23),
                        getCurrentDayOfSelected(24),
                        getCurrentDayOfSelected(25),
                        getCurrentDayOfSelected(26),
                        getCurrentDayOfSelected(27),
                        getCurrentDayOfSelected(28),
                        getCurrentDayOfSelected(29),
                        getCurrentDayOfSelected(30),
                        getCurrentDayOfSelected(31)
                };
                break;
            default:
                break;
        }
        return datesOfMonth;
    }

    private String getCurrentDayOfSelected(int i) {
        Calendar currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.YEAR,lastYearSelectedInMonths);
        currentMonth.set(Calendar.MONTH,lastMonthSelected-1);
        currentMonth.set(Calendar.DAY_OF_MONTH,1);

        SimpleDateFormat isoFormat = new SimpleDateFormat("dd/MM");
        currentMonth.add(Calendar.DAY_OF_MONTH, i);
        String previousYearDate = isoFormat.format(currentMonth.getTime());

        return previousYearDate;
    }

    private void callBackPieDataChart(final String type, final String item) {
        FirebaseResultInterface firebaseResultInterface = new FirebaseResultInterface<ArrayList<History>>() {

            @Override
            public void onSuccess(ArrayList<History> historyArrayList) {

                // refresh PieChart
                clearChart();
                categories.clear();
                setOptionsChart();

                //refresh LineChart
                clearLineChart();
                receiptArrayList.clear();

//                setXDatas();


                filterArrayBySelectedSpinner(historyArrayList,type,item);
                //createEntries
                createPieEntries(historyHashMap);

                setOptionLineChart();
                // set Line Graph
                setLineChart(salaryArrayList);

                // PieDataSet
                createPieDataSet();
                //setData on Pie
                setPieData();
                dataSet.notifyDataSetChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();

//                lineDataSet.notifyAll();
                lineDataSet1.notifyDataSetChanged();
                lineDataSet2.notifyDataSetChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
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

    private void clearLineChart() {
        lineDataSet.clear();
        lineDataSet1.clear();
        lineDataSet2.clear();
        entries1.clear();
        entries2.clear();

        lineDataSet1.notifyDataSetChanged();
        lineDataSet2.notifyDataSetChanged();
//        lineDataSet.notifyAll();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

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

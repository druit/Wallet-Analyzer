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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
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
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import data_class.BankAccount;
import data_class.History;
import data_class.Receipt;
import data_class.Salary;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.BankEditPopup;
import gr.ict.wallet_analyzer.helpers.FirebaseResultInterface;
import gr.ict.wallet_analyzer.helpers.HistoryArrayList;
import gr.ict.wallet_analyzer.helpers.RoundedSlicesPieChartRenderer;

public class Statistics extends Fragment {

    final List<Entry> entries1 = new ArrayList<>();
    final List<Entry> entries2 = new ArrayList<>();
    final List<Entry> entries3 = new ArrayList<>();
    final List<Entry> entries4 = new ArrayList<>();
    final List<PieEntry> entries = new ArrayList<>();
    double totalIncome = 0;
    double totalExpenses = 0;
    double lastExpensesOfPrevMonth = 0;
    //DATE format
    DateFormat dateFormatMonths = new SimpleDateFormat("MM");
    Date currentDate = new Date();
    String positionMonths = String.valueOf(Integer.parseInt(dateFormatMonths.format(currentDate)) - 1);
    int lastYearSelectedInMonths = new Date().getYear();
    Calendar c = Calendar.getInstance();
    int currentYear = c.get(Calendar.YEAR);
    int lastMonthSelected = Integer.valueOf(c.get(Calendar.MONTH) + 1);
    BankAccount selectedBankAccount = new BankAccount();
    ArrayList<Salary> salaryArrayList = new ArrayList<>();
    //Line Chart Values
    private LineChart lineChart;
    private LineDataSet lineDataSet1, lineDataSet2, lineDataSet3, lineDataSet4;
    private List<ILineDataSet> lineDataSet = new ArrayList<>();
    private ArrayList<String> xEntrys = new ArrayList<>();
    private String[] xData = {};
    private String lastType = "category";
    //Pie Chart Values
    private PieChart chart;
    private PieDataSet dataSet;
    //HISTORY Values
    private HistoryArrayList historyArrayList = new HistoryArrayList();
    private HashMap<String, Double> historyHashMap = new HashMap();
    private List<String> categories = new ArrayList<>();
    private ArrayList<Receipt> receiptArrayList = new ArrayList<>();
    //FIREBASE Connections
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String uid = user.getUid();
    DatabaseReference baseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
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

                bankEditPopup.getSalaryList(baseReference, firebaseResultInterface2);

                filterArrayBySelectedSpinner(historyArrayList, "category", null);

                createPieEntries(historyHashMap);

                // set Line Graph
                setLineChart(salaryArrayList, historyArrayList);


                // PieDataSet
                createPieDataSet();

                setPieData();
            }

            @Override
            public void onFailed(Throwable error) {
            }
        };
        historyArrayList.callBackHistoryArrayList(baseReference, firebaseResultInterface);
        super.onViewCreated(view, savedInstanceState);
    }


    private void setLineChart(ArrayList<Salary> salaryList, ArrayList<History> historyArrayList) {
        totalIncome = 0;
        totalExpenses = 0;
        lastExpensesOfPrevMonth = 0;
        double prevTotalMonth = 0;
        ArrayList<Double> totalIncomeArray = new ArrayList<>();
        ArrayList<Double> incomes = new ArrayList<>();
        Salary monthSalary = new Salary();
        ArrayList<Integer> totalIncomeArrayMonthly = new ArrayList<>();
        int previousReceiptMonth = 0;
        if (!receiptArrayList.isEmpty()) {
            for (Salary salary : salaryList) {
                if (salary.getUpdateDate().getYear() == lastYearSelectedInMonths) {
                    totalIncomeArray.add(salary.getCurrentSalary());
                    incomes.add(salary.getSalaryAdd());
                    totalIncomeArrayMonthly.add(salary.getLastUpdate().getMonth());
                }

                if (salary.getUpdateDate().getYear() == lastYearSelectedInMonths && Integer.valueOf(salary.getLastUpdate().getMonth() + 1) == lastMonthSelected) {
                    totalIncome += salary.getCurrentSalary();
                    monthSalary = salary;
                    prevTotalMonth = totalIncome - salary.getSalaryAdd();
                }
            }

            if (lastType.equals("month") || lastType.equals("week")) {


                for (History history : historyArrayList) {
                    Receipt receipt = history.getReceipt();
                    Date date = getFirstDateOfWeek(-6);
//                    System.out.println("DATE: " + date.getDate() + " receipt date : " + receipt.getDate().getDate());
                    if (Integer.valueOf(receipt.getDate().getYear()) <= lastYearSelectedInMonths && (Integer.valueOf(receipt.getDate().getMonth() + 1) < lastMonthSelected && lastType.equals("month"))) {
                        lastExpensesOfPrevMonth += receipt.getTotalPrice();
                        totalIncome -= receipt.getTotalPrice();
                        prevTotalMonth -= receipt.getTotalPrice();
                    } else if ((lastType.equals("week")) && receipt.getDate().getTime() <= date.getTime()) {
                        lastExpensesOfPrevMonth += receipt.getTotalPrice();
                        totalIncome -= receipt.getTotalPrice();
                        prevTotalMonth -= receipt.getTotalPrice();
                    }
                }

                if(lastType.equals("week")){
                    prevTotalMonth += monthSalary.getSalaryAdd();
                }

                entries1.add(new Entry(0, (float) prevTotalMonth));
                entries2.add(new Entry(0, (float) totalExpenses));

                entries3.add(new Entry(0, 0));
                entries4.add(new Entry(0, 0));
                if (lastType.equals("month")) {
                    for (int i = 0; i < totalIncomeArrayMonthly.size(); i++) {
                        if (lastMonthSelected == Integer.valueOf(totalIncomeArrayMonthly.get(i) + 1)) {
                            double value = incomes.get(i);
                            int date = Integer.valueOf(monthSalary.getLastUpdate().getDate());

                            entries3.add(new Entry(date, (float) value));
                        }
                    }
                }
            } else {
                entries1.add(new Entry(0, (float) 0));
                entries2.add(new Entry(0, (float) 0));
                entries3.add(new Entry(0, (float) 0));
                entries4.add(new Entry(0, (float) 0));
            }


        }
        for (Receipt receipt : receiptArrayList) {
//            if((lastType.equals("week"))){
//                if(receipt.getDate().getYear() == lastYearSelectedInMonths){
//                    previousReceiptMonth = doAction(receipt,previousReceiptMonth, -1,-1);
//                }
//            }else {
//                if(lastType.equals("year") || lastType.equals("category")){
            int findIncomeArray = -1;
            for (int i = 0; i < totalIncomeArray.size(); i++) {
                if (Integer.valueOf(totalIncomeArrayMonthly.get(i)) == Integer.valueOf(receipt.getDate().getMonth()) && receipt.getDate().getYear() == lastYearSelectedInMonths) {
                    findIncomeArray = i;
                }
            }
            if (findIncomeArray != -1) {
                previousReceiptMonth = doAction(receipt, previousReceiptMonth, (totalIncomeArray.get(findIncomeArray) - lastExpensesOfPrevMonth), incomes.get(findIncomeArray));
//                        totalIncomeArray.set(findIncomeArray,Double.valueOf(totalIncomeArray.get(findIncomeArray)-receipt.getTotalPrice()));
            } else {
                previousReceiptMonth = doAction(receipt, previousReceiptMonth, -1, -1);
            }

//                }else{
//                    previousReceiptMonth = doAction(receipt,previousReceiptMonth, -1,-1);
//                }
//            }
        }

        if ((incomes.size() > 0 && (incomes.size() != entries3.size() - 1)) && (lastType.equals("category") || lastType.equals("year"))) {
            double valueIncomes = incomes.get(Integer.valueOf(incomes.size() - 1));
            double valueLocalSalary = entries1.get(Integer.valueOf(entries1.size() - 1)).getY() + salaryList.get(Integer.valueOf(salaryList.size() - 1)).getSalaryAdd();
//            double valueTotalExpenses = totalExpenses;
            entries3.add(new Entry(Integer.valueOf(currentDate.getMonth() + 1), (float) valueIncomes));
            entries1.add(new Entry(Integer.valueOf(currentDate.getMonth() + 1), (float) valueLocalSalary));
            entries2.add(new Entry(Integer.valueOf(currentDate.getMonth() + 1), (float) 0));
        } else if (lastMonthSelected < Integer.valueOf(currentDate.getMonth() + 1)) {
            // Get the number of days in that month
            @SuppressLint({"NewApi", "LocalSuppress"}) YearMonth yearMonthObject = YearMonth.of(currentYear, lastMonthSelected);
            @SuppressLint({"NewApi", "LocalSuppress"}) int daysInMonth = yearMonthObject.lengthOfMonth();
            if (entries1.size() >= 1) {
                double valueLocalSalary = entries1.get(Integer.valueOf(entries1.size() - 1)).getY();
                entries3.add(new Entry(Integer.valueOf(daysInMonth), (float) 0));
                entries1.add(new Entry(Integer.valueOf(daysInMonth), (float) valueLocalSalary));
                entries2.add(new Entry(Integer.valueOf(daysInMonth), (float) 0));
            }

        }

        String incomesTitle = getActivity().getBaseContext().getResources().getString(R.string.gen_income);
        String expenses = getActivity().getBaseContext().getString(R.string.gen_expenses);
        String total = getActivity().getBaseContext().getString(R.string.gen_total_2);

        lineDataSet3 = new LineDataSet(entries3, incomesTitle);

        lineDataSet1 = new LineDataSet(entries1, total); // add entries to dataset

        lineDataSet2 = new LineDataSet(entries2, expenses);

        lineDataSet4 = new LineDataSet(entries4, expenses);


        setLineDataSetValueFormat();

//        lineDataSet.add(lineDataSet4);
        lineDataSet.add(lineDataSet3);
        lineDataSet.add(lineDataSet1);
        lineDataSet.add(lineDataSet2);


        LineData lineData = new LineData(lineDataSet);
        if (!categories.isEmpty()) {
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
                    if (value > 0 && xData.length > value)
                        return xData[(int) value]; // xVal is a string array
                    else return "";
                }
            });
        }
        lineChart.setData(lineData);
//        lineChart.invalidate(); // refresh
    }

    private int doAction(Receipt receipt, int previousReceiptMonth, double totalIncomeArrayNumber, double incomes) {
        Date date = getFirstDateOfWeek(-6);
        if (!lastType.equals("week") || receipt.getDate().getTime() >= date.getTime()) {
            totalExpenses += receipt.getTotalPrice();
        }

        if (totalIncomeArrayNumber != -1) {
            totalIncomeArrayNumber -= totalExpenses;
            return createLineEntries(receipt, previousReceiptMonth, totalIncomeArrayNumber, totalExpenses, lastType, incomes);
        } else {
            totalIncome -= receipt.getTotalPrice();
            return createLineEntries(receipt, previousReceiptMonth, totalIncome, totalExpenses, lastType, 0);
        }


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
        lineDataSet3.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return super.getFormattedValue(Float.parseFloat(value + " €"));
            }
        });

        lineDataSet4.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return super.getFormattedValue(Float.parseFloat(value + " €"));
            }
        });

        // make line curvy
        lineDataSet1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // circles color
        lineDataSet1.setCircleColor(ContextCompat.getColor(getActivity(), R.color.colorLineChart1));
        lineDataSet1.setCircleHoleColor(ContextCompat.getColor(getActivity(), R.color.colorLineChart1));

        // Gradient fill
        lineDataSet1.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.gradient_background_linechart1);
        lineDataSet1.setFillDrawable(drawable);

        // line color
        lineDataSet1.setColor(ContextCompat.getColor(getActivity(), R.color.colorLineChart1));
        // values text color
        lineDataSet1.setValueTextColor(Color.rgb(255, 255, 255));

        // make line curvy
        lineDataSet2.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // circles color
        lineDataSet2.setCircleColor(ContextCompat.getColor(getActivity(), R.color.colorLineChart2));
        lineDataSet2.setCircleHoleColor(ContextCompat.getColor(getActivity(), R.color.colorLineChart2));

        // Gradient fill
        lineDataSet2.setDrawFilled(true);
        Drawable drawable2 = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.gradient_background_linechart2);
        lineDataSet2.setFillDrawable(drawable2);

        // line color
        lineDataSet2.setColor(ContextCompat.getColor(getActivity(), R.color.colorLineChart2));
        // values text color
        lineDataSet2.setValueTextColor(Color.rgb(255, 255, 255));

        // make line curvy
        lineDataSet3.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // circles color
        lineDataSet3.setCircleColor(ContextCompat.getColor(getActivity(), R.color.colorLineChart3));
        lineDataSet3.setCircleHoleColor(ContextCompat.getColor(getActivity(), R.color.colorLineChart3));

        // Gradient fill
        lineDataSet3.setDrawFilled(true);
        Drawable drawable3 = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.gradient_background_linechart3);
        lineDataSet3.setFillDrawable(drawable3);

        // line color
        lineDataSet3.setColor(ContextCompat.getColor(getActivity(), R.color.colorLineChart3));
        // values text color
        lineDataSet3.setValueTextColor(Color.rgb(255, 255, 255));

        // make line curvy
        lineDataSet4.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        // circles color
        lineDataSet4.setCircleColor(R.color.colorLineChart4);
        lineDataSet4.setCircleHoleColor(R.color.colorLineChart4);

        // Gradient fill
        lineDataSet4.setDrawFilled(true);
        Drawable drawable4 = ContextCompat.getDrawable(getActivity().getBaseContext(), R.drawable.gradient_background_linechart4);
        lineDataSet4.setFillDrawable(drawable4);

        // line color
        lineDataSet4.setColor(R.color.colorLineChart4);
        // values text color
        lineDataSet4.setValueTextColor(R.color.colorLineChart4);

        lineDataSet1.setValueTextSize(8);
        lineDataSet2.setValueTextSize(8);
        lineDataSet3.setValueTextSize(8);
        lineDataSet4.setValueTextSize(8);
    }

    // X data in graph (labels) LINE CHART
    private void setXDatas() {
        xEntrys.clear();
        for (int i = 1; i < xData.length; i++) {
            xEntrys.add(xData[i]);
        }
    }

    //ENTRIES LINE CHART
    private int createLineEntries(Receipt receipt, int previousReceiptMonth, double localSalary, double totalExpenses, String type, double incomes) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        switch (type) {
            case "category":
            case "year":
                if (Integer.valueOf(receipt.getDate().getMonth() + 1) == previousReceiptMonth) {
                    int index = Integer.valueOf(entries.size() - 1);
//                    String value = decimalFormat.format(localSalary);
                    entries1.get(entries1.size() - 1).setY((float) localSalary);

                    entries2.get(entries2.size() - 1).setY((float) totalExpenses);
                    float value = entries4.get(Integer.valueOf(entries4.size() - 1)).getY();
                    value = (float) (value + (float) receipt.getTotalPrice());
                    entries4.get(entries2.size() - 1).setY((float) value);
                } else {
                    entries1.add(new Entry(Integer.valueOf(receipt.getDate().getMonth() + 1), (float) localSalary));
                    entries2.add(new Entry(Integer.valueOf(receipt.getDate().getMonth() + 1), (float) totalExpenses));
                    entries4.add(new Entry(Integer.valueOf(receipt.getDate().getMonth() + 1), (float) receipt.getTotalPrice()));
                    if (incomes != -1) {
                        entries3.add(new Entry(Integer.valueOf(receipt.getDate().getMonth() + 1), (float) incomes));
                    }
                    previousReceiptMonth = Integer.valueOf(receipt.getDate().getMonth() + 1);
                }
                break;
            case "month":
                if (Integer.valueOf(receipt.getDate().getDate()) == previousReceiptMonth) {
                    int index = Integer.valueOf(entries.size() - 1);
                    entries1.get(entries1.size() - 1).setY((float) localSalary);
                    entries2.get(entries2.size() - 1).setY((float) totalExpenses);
                    float value = entries4.get(Integer.valueOf(entries4.size() - 1)).getY();
                    value = (float) (value + (float) receipt.getTotalPrice());
                    entries4.get(Integer.valueOf(entries4.size() - 1)).setY((float) value);
                } else {
                    entries1.add(new Entry(Integer.valueOf(receipt.getDate().getDate()), (float) localSalary));
                    entries2.add(new Entry(Integer.valueOf(receipt.getDate().getDate()), (float) totalExpenses));
                    if (entries3.size() > 1) {
                        entries3.add(new Entry(Integer.valueOf(receipt.getDate().getDate()), (float) 0));
                    }
                    entries4.add(new Entry(Integer.valueOf(receipt.getDate().getDate()), (float) receipt.getTotalPrice()));
                    previousReceiptMonth = Integer.valueOf(receipt.getDate().getDate());
                }
                break;
            case "week":
                int pos = 1;
                int currentDateYear = new Date().getYear();
                Date firstDateOfWeek = getFirstDateOfWeek(-6);
                if (currentDateYear == receipt.getDate().getYear() && receipt.getDate().getTime() >= firstDateOfWeek.getTime()) {
                    String d1 = String.valueOf(receipt.getDate().getDate());
                    String d2 = String.valueOf(receipt.getDate().getMonth() + 1);
                    Date salaryDate = salaryArrayList.get(Integer.valueOf(salaryArrayList.size() - 1)).getUpdateDate();


                    String receiptDate = "";
                    if (d1.length() == 1) {
                        d1 = "0" + d1;
                    }
                    if (d2.length() == 1) {
                        d2 = "0" + d2;
                    }
                    receiptDate = d1 + "/" + d2;


                    for (int i = 0; i < xData.length; i++) {
                        if (xData[i].equals(receiptDate) && currentDateYear == receipt.getDate().getYear()) {
                            pos = i;
                        }
                        String date[] = xData[i].split("/");

                        String salaryStringDate = String.valueOf(salaryDate.getDate());
                        if (salaryStringDate.length() == 1) {
                            salaryStringDate = "0" + salaryStringDate;
                        }

                        if (incomes != -1 && salaryStringDate.equals(date[0])) {
                            entries3.add(new Entry(i, (float) incomes));
                        }
                    }

                    if (entries3 == null) {
                        entries3.add(new Entry(0, (float) incomes));
                    }


                    if (Integer.valueOf(pos) == previousReceiptMonth) {
                        int index = Integer.valueOf(entries1.size() - 1);
                        entries1.get(index).setY((float) localSalary);
                        entries2.get(Integer.valueOf(entries2.size() - 1)).setY((float) totalExpenses);
                        float value = entries4.get(Integer.valueOf(entries4.size() - 1)).getY();
                        value = (float) (value + (float) receipt.getTotalPrice());
                        entries4.get(entries2.size() - 1).setY((float) value);
                    } else {
                        entries1.add(new Entry(Integer.valueOf(pos), (float) localSalary));
                        entries2.add(new Entry(Integer.valueOf(pos), (float) totalExpenses));

                        entries4.add(new Entry(Integer.valueOf(pos), (float) receipt.getTotalPrice()));
                        previousReceiptMonth = Integer.valueOf(pos);
                    }
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
        switch (lastType) {
            case "year":
            case "category":
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

        lineChart.getLegend().setYEntrySpace(5);
        lineChart.getLegend().setXEntrySpace(5);

        // hide values in left and right side
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getAxisLeft().setDrawLabels(false);

        // no zoom
//        lineChart.setScaleEnabled(false);

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
        int counter = -1;
        switch (lastType) {
            case "year":
            case "category":
                for (int i = 0; i < xData.length; i++) {
                    if (Integer.valueOf(currentDate.getMonth() + 1) == i && lastYearSelectedInMonths == currentDate.getYear()) {
                        counter = i;
                    }
                }
                break;

            default:
                for (int i = 0; i < xData.length; i++) {
                    if (Integer.valueOf(currentDate.getDate()) == i && Integer.valueOf(currentDate.getMonth() + 1) == lastMonthSelected && lastYearSelectedInMonths == Integer.valueOf(currentDate.getYear())) {
                        counter = i;
                    }
                }

        }
        if (counter != -1)
            lineChart.getXAxis().setAxisMaximum(counter);
        else
            lineChart.getXAxis().setAxisMaximum(xData.length);
//        lineChart.getXAxis().setAxisMinimum(0);

//        lineChart.getXAxis().setLabelCount(5, true);
        lineChart.animateXY(300, 300);
    }

    private void setPieData() {
        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.invalidate(); // refresh

        chart.setRenderer(new RoundedSlicesPieChartRenderer(chart, chart.getAnimator(), chart.getViewPortHandler()));

        // these needs to be called last
        chart.setHoleColor(0x00ffffff);
        chart.setTransparentCircleAlpha(0);
        chart.setCenterTextColor(Color.WHITE);
        chart.setHoleRadius(65f);

        if (!categories.isEmpty()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            chart.setCenterText(categories.get(0) + ": " + decimalFormat.format(historyHashMap.get(categories.get(0))) + "€");
        } else {
            chart.setCenterText("Category: " + "0.0 €");
        }

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                chart.setCenterText(pe.getLabel() + ": " + e.getY() + "€");
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void filterArrayBySelectedSpinner(ArrayList<History> historyArrayList, String type, String selected) {
        for (History history : historyArrayList) {
            boolean thereIsCategory = false;
            boolean filterFind = false;
            boolean filterFindForLine = false;
            switch (type) {
                case "category":
                    thereIsCategory = checkThereIsCategory(history);
                    break;
                case "year":
                    if (history.getReceipt().getDate().toString().contains(selected)) {
                        thereIsCategory = checkThereIsCategory(history);
                        filterFind = true;
                    }
                    break;
                case "week":
                    int currentDate = new Date().getYear();
                    String d1 = String.valueOf(history.getReceipt().getDate().getDate());
                    String d2 = String.valueOf(history.getReceipt().getDate().getMonth() + 1);
                    String receiptDate;

                    int firstDay = Integer.valueOf(getPreviousYearOrDay(-6, "week").split("/")[0]);
                    int lastDay = Integer.valueOf(getPreviousYearOrDay(0, "week").split("/")[0]);

                    if (Integer.valueOf(d1) >= firstDay && Integer.valueOf(d1) <= lastDay) {
                        filterFindForLine = true;
                    }


                    if (currentDate == history.getReceipt().getDate().getYear()) {
                        if (d1.length() == 1) {
                            d1 = "0" + d1;
                        }
                        if (d2.length() == 1) {
                            d2 = "0" + d2;
                        }
                        receiptDate = d1 + "/" + d2;
                        if (receiptDate.contains(selected)) {
                            thereIsCategory = checkThereIsCategory(history);
                            filterFind = true;
                        }
                    }
                    break;
                case "month":
                    int year = lastYearSelectedInMonths;
                    String month = String.valueOf(history.getReceipt().getDate().getMonth() + 1);
                    if (year == history.getReceipt().getDate().getYear()) {
                        if (month.length() == 1) {
                            month = "0" + month;
                        }

                        if (month.contains(positionMonths)) {
                            thereIsCategory = checkThereIsCategory(history);
                            filterFind = true;
                        }
                    }

                    break;
                default:
                    break;
            }
            lastType = type;

            if (thereIsCategory) {
                historyHashMap.put(history.getReceipt().getStoreType(), historyHashMap.get(history.getReceipt().getStoreType()) + history.getReceipt().getTotalPrice());
//                receiptArrayList.add(history.getReceipt());
            } else if (type.equals("category") || filterFind) {
                historyHashMap.put(history.getReceipt().getStoreType(), history.getReceipt().getTotalPrice());
                categories.add(history.getReceipt().getStoreType());
//                receiptArrayList.add(history.getReceipt());
            }
            if (type.contains("category") && history.getReceipt().getDate().toString().contains(getPreviousYearOrDay(0, "year"))) {
                receiptArrayList.add(history.getReceipt());
            } else if (!type.contains("category") && (filterFind || filterFindForLine)) {
                receiptArrayList.add(history.getReceipt());
            }

        }


    }

    private boolean checkThereIsCategory(History history) {
        if (categories.size() > 0) {
            for (String category : categories) {
                if (category.equals(history.getReceipt().getStoreType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createFilterCategory() {
        Spinner spinner1 = getActivity().findViewById(R.id.filterChart1);
        final Spinner spinner2 = getActivity().findViewById(R.id.filterChart2);
        final Spinner spinner3 = getActivity().findViewById(R.id.filterChart3);

        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        String items[] = {
                getString(R.string.filter_obj_2),
                getString(R.string.filter_obj_3), getString(R.string.filter_obj_1), getString(R.string.filter_obj_4)
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

                switch (position) {
                    case 0:
                        setFilter("month");
                        spinner2.setVisibility(View.VISIBLE);
                        spinner3.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        setFilter("week");
                        spinner2.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        setFilter("category");
                        break;
                    case 3:
                        setFilter("year");
                        spinner2.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
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
        spinner1.setSelection(2, true);
    }

    private void setFilter(final String type) {
        String[] items = {};
        String[] yearsOrDates = {};
        Spinner spinner2 = getActivity().findViewById(R.id.filterChart2);
        Spinner spinner3 = getActivity().findViewById(R.id.filterChart3);
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        lastMonthSelected = Integer.valueOf(c.get(Calendar.MONTH) + 1);
        lastYearSelectedInMonths = new Date().getYear();
        switch (type) {
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
                yearsOrDates = new String[]{getPreviousYearOrDay(0, "week"), getPreviousYearOrDay(-1, "week"), getPreviousYearOrDay(-2, "week"), getPreviousYearOrDay(-3, "week"), getPreviousYearOrDay(-4, "week"), getPreviousYearOrDay(-5, "week"), getPreviousYearOrDay(-6, "week")};
                items = yearsOrDates;
                xData = new String[]{"", getPreviousYearOrDay(-6, "week"), getPreviousYearOrDay(-5, "week"), getPreviousYearOrDay(-4, "week"), getPreviousYearOrDay(-3, "week"), getPreviousYearOrDay(-2, "week"), getPreviousYearOrDay(-1, "week"), getPreviousYearOrDay(0, "week")};

                break;
            case "year":

                yearsOrDates = new String[]{getPreviousYearOrDay(0, "year"), getPreviousYearOrDay(-1, "year"), getPreviousYearOrDay(-2, "year"), getPreviousYearOrDay(-3, "year"), getPreviousYearOrDay(-4, "year"), getPreviousYearOrDay(-5, "year"), getPreviousYearOrDay(-6, "year"), getPreviousYearOrDay(-7, "year"), getPreviousYearOrDay(-8, "year"), getPreviousYearOrDay(-9, "year"), getPreviousYearOrDay(-10, "year")};
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
                callBackPieDataChart(type, null);
                break;
        }


        final String[] finalItems = items;
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
//                ArrayList<History> historyMonthList = getHistoryListByMonth(position + 1);
                if (type.equals("month")) {
                    lastMonthSelected = position + 1;
                    positionMonths = String.valueOf(position + 1);

                    if (positionMonths.length() == 1) {
                        positionMonths = "0" + positionMonths;
                    }
                    // Get the number of days in that month
                    @SuppressLint({"NewApi", "LocalSuppress"}) YearMonth yearMonthObject = YearMonth.of(currentYear, lastMonthSelected);
                    @SuppressLint({"NewApi", "LocalSuppress"}) int daysInMonth = yearMonthObject.lengthOfMonth();
                    xData = getDatesOfMonth(daysInMonth);
                    callBackPieDataChart(type, positionMonths);
                } else {
                    if (type.equals("year")) {
                        lastYearSelectedInMonths = new Date().getYear() - position;
                    }
//
                    callBackPieDataChart(type, finalItems[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final String[] finalYearsOrDates = yearsOrDates;
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long l) {
                lastYearSelectedInMonths = new Date().getYear() - position;
                if (finalYearsOrDates.length > 0 && type.contains("month")) {
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
        yearsOrDates = new String[]{getPreviousYearOrDay(0, "year"), getPreviousYearOrDay(-1, "year"), getPreviousYearOrDay(-2, "year"), getPreviousYearOrDay(-3, "year"), getPreviousYearOrDay(-4, "year"), getPreviousYearOrDay(-5, "year"), getPreviousYearOrDay(-6, "year"), getPreviousYearOrDay(-7, "year"), getPreviousYearOrDay(-8, "year"), getPreviousYearOrDay(-9, "year"), getPreviousYearOrDay(-10, "year")};

        ArrayAdapter spinnerAdapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, yearsOrDates);
        //set the spinners adapter to the previously created one.
        spinner3.setAdapter(spinnerAdapter2);
        spinner3.setSelection(0, true);

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, items);
        //set the spinners adapter to the previously created one.
        spinner2.setAdapter(spinnerAdapter);
        if (type.equals("month")) {
            spinner2.setSelection(Integer.parseInt(dateFormat.format(date)) - 1, true);
        } else {
            spinner2.setSelection(0, true);
        }

    }

    private String[] getDatesOfMonth(int daysInMonth) {
        String datesOfMonth[] = {};
        switch (daysInMonth) {
            case 28:
                datesOfMonth = new String[30];

                for (int i = 0; i < 30; i++) {
                    datesOfMonth[i] = getCurrentDayOfSelected(i - 1);
                }
                break;
            case 29:
                datesOfMonth = new String[31];

                for (int i = 0; i < 31; i++) {
                    datesOfMonth[i] = getCurrentDayOfSelected(i - 1);
                }
                break;
            case 30:
                datesOfMonth = new String[32];

                for (int i = 0; i < 32; i++) {
                    datesOfMonth[i] = getCurrentDayOfSelected(i - 1);
                }
                break;
            case 31:
                datesOfMonth = new String[33];

                for (int i = 0; i < 33; i++) {
                    datesOfMonth[i] = getCurrentDayOfSelected(i - 1);
                }
                break;
            default:
                break;
        }
        return datesOfMonth;
    }

    private String getCurrentDayOfSelected(int i) {
        Calendar currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.YEAR, lastYearSelectedInMonths);
        currentMonth.set(Calendar.MONTH, lastMonthSelected - 1);
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

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


                filterArrayBySelectedSpinner(historyArrayList, type, item);
                //createEntries
                createPieEntries(historyHashMap);

                setOptionLineChart();
                // set Line Graph
                setLineChart(salaryArrayList, historyArrayList);

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
                lineDataSet4.notifyDataSetChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }

            @Override
            public void onFailed(Throwable error) {

            }
        };
        historyArrayList.callBackHistoryArrayList(baseReference, firebaseResultInterface);
    }

    private String getPreviousYearOrDay(int i, String type) {
        Calendar prevYear = Calendar.getInstance();
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy");
        switch (type) {
            case "week":
                isoFormat = new SimpleDateFormat("dd/MM");
                prevYear.add(Calendar.DAY_OF_MONTH, i);
                break;
            case "year":
                prevYear.add(Calendar.YEAR, i);
                break;
            default:
                break;
        }
        String previousYearDate = isoFormat.format(prevYear.getTime());

        return previousYearDate;
    }

    private Date getFirstDateOfWeek(int i) {
        Calendar prevYear = Calendar.getInstance();
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy");
        isoFormat = new SimpleDateFormat("dd/MM/yyyy");
        prevYear.add(Calendar.DAY_OF_MONTH, i);
        Date firstDateOfWeek = new Date(prevYear.getTime().toString());
        return firstDateOfWeek;
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
        lineDataSet4.clear();
        entries1.clear();
        entries2.clear();
        entries3.clear();
        entries4.clear();

        lineDataSet1.notifyDataSetChanged();
        lineDataSet2.notifyDataSetChanged();
        lineDataSet3.notifyDataSetChanged();
        lineDataSet4.notifyDataSetChanged();
//        lineDataSet.notifyAll();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private void createPieDataSet() {
        dataSet = new PieDataSet(entries, ""); // add entries to dataset

        dataSet.setValueFormatter(new PercentFormatter(chart));

        // line color
        int[] dataSetColors = {
                ContextCompat.getColor(getActivity(), R.color.colorPie1),
                ContextCompat.getColor(getActivity(), R.color.colorPie2),
                ContextCompat.getColor(getActivity(), R.color.colorPie3),
                ContextCompat.getColor(getActivity(), R.color.colorPie4),
        };
        dataSet.setColors(dataSetColors);

        // values text color
        dataSet.setValueTextColor(Color.rgb(255, 255, 255));
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
    }

    private void createPieEntries(HashMap<String, Double> historyHashMap) {
        if (categories.size() > 0) {
            for (String category : categories) {
                entries.add(new PieEntry(historyHashMap.get(category).floatValue(), category));
            }
            chart.getLegend().setEnabled(true);
        } else {
            entries.add(new PieEntry(0f, ""));
            chart.getLegend().setEnabled(false);
        }
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
        chart.setHoleColor(0x00ffffff);
        chart.setCenterTextColor(Color.WHITE);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setDrawCenterText(true);
        chart.setDrawEntryLabels(false);


        chart.animateXY(900, 500);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statistics_fragment, container, false);
    }

    private class MyValueFormater extends ValueFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

            return "10%";
        }
    }


}

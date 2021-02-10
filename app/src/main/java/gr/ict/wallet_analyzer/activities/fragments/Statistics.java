package gr.ict.wallet_analyzer.activities.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import data_class.YourData;
import gr.ict.wallet_analyzer.R;

public class Statistics extends Fragment {

    private PieChart chart;
    private PieDataSet dataSet;


    @SuppressLint("ResourceType")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        chart = getActivity().findViewById(R.id.pieChartView);
//        chart.setHoleRadius(20);
//        chart.setTransparentCircleRadius(25);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setUsePercentValues(true);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setDrawCenterText(true);


        chart.animateXY(900,500);
        chart.getDescription().setEnabled(false);


        List<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(18.5f, "Green"));
        entries.add(new PieEntry(26.7f, "Yellow"));
        entries.add(new PieEntry(24.0f, "Red"));
        entries.add(new PieEntry(30.8f, "Blue"));
        entries.add(new PieEntry(320.8f, "Be"));
        entries.add(new PieEntry(5.8f, "DE"));
        entries.add(new PieEntry(100.8f, "EN"));
        entries.add(new PieEntry(200.8f, "EL"));

        PieDataSet dataSet = new PieDataSet(entries, ""); // add entries to dataset
        // line color
        dataSet.setColors(new int[] {Color.argb(50,100,92,110),Color.argb(50,20,20,110),Color.argb(50,200,250,110), Color.BLUE,Color.RED,Color.GREEN,Color.YELLOW,Color.LTGRAY});
        // values text color
        dataSet.setValueTextColor(Color.rgb(255, 255, 255));




        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.invalidate(); // refresh


        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.statistics_fragment,container,false);
    }


}

package gr.ict.wallet_analyzer.activities;

import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieDataSet;
import com.google.android.material.tabs.TabLayout;

import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.activities.ui.main.SectionsPagerAdapter;

public class PortfolioActivity extends BaseActivity {
    private PieChart chart;
    private PieDataSet dataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

//        chart = findViewById(R.id.pieChartView);
//
//        YourData[] dataObjects = {
//                new YourData(2, 7),
//                new YourData(3, 12),
//                new YourData(4, 3)
//        };
//
//        List<PieEntry> entries = new ArrayList<>();
//
//        for (YourData data : dataObjects) {
//            // turn your data into Entry objects
//            entries.add(new PieEntry(data.getX(), data.getY()));
//        }
//
//        PieDataSet dataSet = new PieDataSet(entries, "Label"); // add entries to dataset
//        // line color
//        dataSet.setColor(Color.rgb(95, 115, 193));
//        // values text color
//        dataSet.setValueTextColor(Color.rgb(255, 255, 255));
//
//
//
//
//        PieData pieData = new PieData(dataSet);
//        chart.setData(pieData);
//        chart.invalidate(); // refresh
//        FloatingActionButton fab = findViewById(R.id.fab);
//
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }
}
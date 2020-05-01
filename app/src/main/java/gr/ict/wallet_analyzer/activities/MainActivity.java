package gr.ict.wallet_analyzer.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import Adapters.MyListAdapter;
import data_class.YourData;
import gr.ict.wallet_analyzer.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });

        // list view
        ListView list;

        String[] maintitle = {
                "Title 1", "Title 2",
                "Title 3", "Title 4",
                "Title 5", "Title 5", "Title 5",
        };

        String[] subtitle = {
                "$5", "$5,42",
                "$13,50", "$25",
                "$32", "$32", "$32",
        };

        MyListAdapter adapter = new MyListAdapter(this, maintitle, subtitle);
        list = findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView receiptTextView = view.findViewById(R.id.title);
                String receiptString = receiptTextView.getText().toString();

                showPopUp(receiptString);
            }
        });

        // GraphView
        LineChart chart = findViewById(R.id.chart);

        YourData[] dataObjects = {
                new YourData(1, 5),
                new YourData(2, 8),
                new YourData(3, 3),
                new YourData(4, 13)
        };
        List<Entry> entries = new ArrayList<>();
        for (YourData data : dataObjects) {
            // turn your data into Entry objects
            entries.add(new Entry(data.getX(), data.getY()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "April"); // add entries to dataset

        // Gradient fill
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.graph_gradient);
        dataSet.setFillDrawable(drawable);

        dataSet.setColor(Color.rgb(95, 115, 193));
        dataSet.setValueTextColor(Color.rgb(255, 255, 255));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        // Spinner above the graph
        Spinner spinner = findViewById(R.id.month_spinner);
        String[] items = new String[]{"January", "February", "March", "April"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        //set the spinners adapter to the previously created one.
        spinner.setAdapter(spinnerAdapter);
    }

    private void showPopUp(String receiptString) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.receipt_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        TextView textView = popupView.findViewById(R.id.receipt_text);
        textView.setText(receiptString);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(findViewById(R.id.list), Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
}

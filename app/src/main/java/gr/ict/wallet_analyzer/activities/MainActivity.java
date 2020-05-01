package gr.ict.wallet_analyzer.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
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

                TextView priceTextView = view.findViewById(R.id.subtitle);
                String priceString = priceTextView.getText().toString();

                showPopUp(receiptString, priceString);
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

        // make line curvy
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // circles color
        dataSet.setCircleColor(Color.rgb(95, 115, 193));
        dataSet.setCircleHoleColor(Color.rgb(95, 115, 193));

        // Gradient fill
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.graph_gradient);
        dataSet.setFillDrawable(drawable);

        // line color
        dataSet.setColor(Color.rgb(95, 115, 193));
        // values text color
        dataSet.setValueTextColor(Color.rgb(255, 255, 255));

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
        chart.getXAxis().setGridColor(R.color.colorButton);
        chart.getAxisLeft().setGridColor(R.color.colorButton);
        chart.getAxisRight().setGridColor(R.color.colorButton);

        chart.getXAxis().setTextColor(Color.argb(50, 255, 255, 255));

        // Spinner above the graph
        Spinner spinner = findViewById(R.id.month_spinner);
        String[] items = new String[]{"January", "February", "March", "April"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        //set the spinners adapter to the previously created one.
        spinner.setAdapter(spinnerAdapter);
    }

    private void showPopUp(String receiptString, String priceString) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.receipt_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        TextView receiptTextView = popupView.findViewById(R.id.receipt_text);
        receiptTextView.setText(receiptString);

        TextView textView = popupView.findViewById(R.id.price_text_view);
        textView.setText(priceString);

        // TODO: add ListView for every product in the receipt

        // blur effect
        float radius = 10f;

        View decorView = getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        Drawable windowBackground = decorView.getBackground();

        BlurView blurView = popupView.findViewById(R.id.blurView);
        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);

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

    public void onBackPressed() {
        finishAffinity();
    }
}

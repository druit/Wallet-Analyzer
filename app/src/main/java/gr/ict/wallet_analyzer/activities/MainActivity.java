package gr.ict.wallet_analyzer.activities;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixplicity.easyprefs.library.Prefs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Adapters.ItemAdapter;
import Adapters.MyListAdapter;
import data_class.History;
import data_class.Receipt;
import data_class.YourData;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import gr.ict.wallet_analyzer.R;
import io.opencensus.resource.Resource;

public class MainActivity extends BaseActivity {

    ImageView profileImage, profileImagePop;
    TextView nameProfile, nameProfilePop, profileEmail,totalPriceMonth;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ArrayList<History> historyArrayList = new ArrayList<>();

    private LineChart chart;
    private LineDataSet dataSet;

    private float maximumReceiptPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileImage = findViewById(R.id.profileImage);
        nameProfile = findViewById(R.id.nameMain);
        totalPriceMonth = findViewById(R.id.sum_text_view);

        // GraphView
        setGraphView();

        // set profile name and image
        setProfile("MAIN");

        // floating button that opens the scan activity
        setFloatingButton();

        // list view
        setListView();

        // spinner for the graph
        monthlyGraph();

        // open menu when clicked on the username on top right corner
        setMenuOpener();
    }

    private void setMenuOpener() {
        LinearLayout menuOpenerLayout = findViewById(R.id.menu_opener);
        menuOpenerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_settings:
                                openSettings();
                                // TODO: add activity_settings page
                                return true;
                            case R.id.action_logout:
                                FirebaseAuth.getInstance().signOut();
                                MainActivity.this.finish();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_main, popup.getMenu());
                popup.show();
            }
        });
    }

    private void setFloatingButton() {
        FloatingActionButton scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setProfile(String type) {
        profileImage.setImageResource(R.drawable.guest);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            switch (type) {
                case "MAIN":
                    if (!user.getDisplayName().isEmpty()) {
                        nameProfile.setText(user.getDisplayName());
                    }
                    break;
                case "POP":
                    if (!user.getDisplayName().isEmpty()) {
                        nameProfilePop.setText(user.getDisplayName());
                        profileEmail.setText(user.getEmail());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void openSettings() {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_settings, null);

        // create the popup window
        int width = 1000;
        int height = 1000;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // blur effect
        BlurView settingsBlur = popupView.findViewById(R.id.settings_blur);
        setBlurEffect(settingsBlur);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, -300);

        profileImagePop = popupView.findViewById(R.id.profileImagePop);
        nameProfilePop = popupView.findViewById(R.id.profileName);
        profileEmail = popupView.findViewById(R.id.profileEmail);

        // TODO: add activity_settings page
        profileImagePop.setImageResource(R.drawable.guest);

        setProfile("POP");

        // open Edit Profile
        FloatingActionButton floatingActionButton = popupView.findViewById(R.id.edit_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });
    }
    private void showReceiptPopup(int itemPosition) {
        Receipt listItemReceipt = historyArrayList.get(itemPosition).getReceipt();


        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.receipt_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        TextView storeNameTextView = popupView.findViewById(R.id.store_name_text_view);
        storeNameTextView.setText(listItemReceipt.getStoreName());

        TextView textView = popupView.findViewById(R.id.price_text_view);
        textView.setText(listItemReceipt.getTotalPrice() + "€");

        // set receipt location
        TextView addressTextView = popupView.findViewById(R.id.address_text_view);
        addressTextView.setText(listItemReceipt.getAddress());

        // set date of the receipt
        TextView dateTextView = popupView.findViewById(R.id.date_text_view);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(listItemReceipt.getDate());
        dateTextView.setText(date);

        // set receipt category
        TextView categoryTextView = popupView.findViewById(R.id.category_text_view);
        categoryTextView.setText(listItemReceipt.getStoreType());

        // list view in popup
        ListView list;

        ItemAdapter itemAdapter = new ItemAdapter(this, listItemReceipt.getItems());
        list = popupView.findViewById(R.id.list_popup);
        list.setAdapter(itemAdapter);

        // blur effect
        BlurView blurView = popupView.findViewById(R.id.blurView);
        setBlurEffect(blurView);

        // show the popup window
        popupWindow.showAtLocation(findViewById(R.id.list), Gravity.CENTER, 0, 0);

        Button floatingActionButton = popupView.findViewById(R.id.trash_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TEST","YES");
            }
        });
        // dismiss the popup window when touched

//        popupView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                popupWindow.dismiss();
//                return true;
//            }
//        });
    }

    public void onBackPressed() {

        finishAffinity();
    }

    private void setBlurEffect(BlurView blurView) {
        // blur effect
        float radius = 10f;

        View decorView = getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);
    }

    private void monthlyGraph() {
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        Log.d("Month", dateFormat.format(date));

        // Spinner above the graph
        Spinner spinner = findViewById(R.id.month_spinner);
        String[] items = new String[]{
                getString(R.string.gen_month_1),getString(R.string.gen_month_2),
                getString(R.string.gen_month_3),getString(R.string.gen_month_4),
                getString(R.string.gen_month_5),getString(R.string.gen_month_6),
                getString(R.string.gen_month_7),getString(R.string.gen_month_8),
                getString(R.string.gen_month_9),getString(R.string.gen_month_10),
                getString(R.string.gen_month_11),getString(R.string.gen_month_12)
        };


        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        //set the spinners adapter to the previously created one.
        spinner.setAdapter(spinnerAdapter);
    }

    private void setListView() {
        FirebaseUser user = mAuth.getCurrentUser();
        ListView list;
        final double[] totalPrice = new double[1];

        final MyListAdapter adapter = new MyListAdapter(this, historyArrayList);
        list = findViewById(R.id.list);
        list.setAdapter(adapter);

        DatabaseReference declare = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("history");
        declare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                historyArrayList.clear();
                dataSet.clear();
                dataSet.addEntry(new Entry(1, 0));

                for (DataSnapshot child : children) {

                    History history = child.getValue(History.class);
                    totalPrice[0] += history.getReceipt().getTotalPrice();
                    totalPriceMonth.setText( totalPrice[0] + " €");
                    historyArrayList.add(history);
                    adapter.notifyDataSetChanged();

                    updateGraph(history);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showReceiptPopup(position);
            }
        });
    }

    private void setGraphView() {
        chart = findViewById(R.id.chart);

        YourData[] dataObjects = {
                new YourData(2, 7),
                new YourData(3, 12),
                new YourData(4, 3)
        };

        List<Entry> entries = new ArrayList<>();

        for (YourData data : dataObjects) {
            // turn your data into Entry objects
            entries.add(new Entry(data.getX(), data.getY()));
        }

        dataSet = new LineDataSet(entries, "April"); // add entries to dataset

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
        chart.getAxisRight().setDrawGridLines(false);

        // show outlines of the grid
        chart.getAxisLeft().setDrawAxisLine(true);
        chart.getAxisRight().setDrawAxisLine(true);
        chart.getXAxis().setDrawAxisLine(true);

        chart.getXAxis().setAxisLineColor(R.color.colorButton);
        chart.getAxisLeft().setAxisLineColor(R.color.colorButton);
        chart.getAxisRight().setAxisLineColor(R.color.colorButton);

        // text color of labels in x axis
        chart.getXAxis().setTextColor(Color.argb(50, 255, 255, 255));

        chart.getAxisLeft().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(10);
        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setLabelCount(10);
    }

    private void updateGraph(History history) {
        String dateString = new SimpleDateFormat("dd").format(history.getReceipt().getDate());
        System.out.println(dateString);
        float date = Float.valueOf(dateString);
        float price = (float) history.getReceipt().getTotalPrice();

        dataSet.addEntry(new Entry(date, price));

        // set maximum value in the graph
        if (maximumReceiptPrice < history.getReceipt().getTotalPrice()) {
            maximumReceiptPrice = (float) history.getReceipt().getTotalPrice();
            chart.getAxisLeft().setAxisMaximum(maximumReceiptPrice + 10);
        }

        dataSet.notifyDataSetChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
}
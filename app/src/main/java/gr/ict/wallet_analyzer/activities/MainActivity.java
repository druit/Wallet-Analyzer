package gr.ict.wallet_analyzer.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import Adapters.HistoryListAdapter;
import data_class.CircleTransform;
import data_class.History;
import eightbitlab.com.blurview.BlurView;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.BankEditPopup;
import gr.ict.wallet_analyzer.helpers.BlurEffect;
import gr.ict.wallet_analyzer.helpers.HistoryListView;
import gr.ict.wallet_analyzer.helpers.ListeningVariable;
import gr.ict.wallet_analyzer.helpers.MainGraph;

public class MainActivity extends BaseActivity {

    public ArrayList<History> historyArrayList = new ArrayList<>();
    ImageView profileImage;
    TextView nameProfile, totalPriceMonth;
    TextView monthlyLimitTextView;
    DatabaseReference baseReference;
    //    double totalPrice;
    private ListeningVariable<Double> totalPrice = new ListeningVariable<>(Double.class);
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String uid;
    private LineChart chart;
    private LineDataSet dataSet;
    private float maximumReceiptPrice = 0;
    private String monthString = new SimpleDateFormat("MM").format(new Date());
    private boolean isReceiptEditPressed = false;
    private HistoryListAdapter mainAdapter;
    private BankEditPopup bankEditPopup = new BankEditPopup();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (user != null && user.isEmailVerified()) {
            uid = user.getUid();
            baseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

            profileImage = findViewById(R.id.profileImage);
            nameProfile = findViewById(R.id.nameMain);
            totalPriceMonth = findViewById(R.id.sum_text_view);


            // set profile name and image
            setProfile();

            // floating button that opens the scan activity
            setFloatingButton();

            // list view
            // setListView();
            HistoryListView historyListView = new HistoryListView(this, baseReference, totalPrice);

            totalPrice.setListener(new ListeningVariable.ChangeListener<Double>() {
                @Override
                public void onChange(Double object) {
                    totalPriceMonth.setText(String.format("%.2f", object) + "€");
                }
            });

            // GraphView
            MainGraph mainGraph = new MainGraph(this, historyListView.getHistoryArrayList());

            // spinner for the graph
//            monthlyGraph();

            // open menu when clicked on the username on top right corner
            setMenuOpener();

            setGoal();
            bindGoalClick();

            setFullHistory();

            setDrawer();

            bankEditPopup.checkSalary(baseReference);
        }
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
                                return true;
                            case R.id.action_logout:
                                FirebaseAuth.getInstance().signOut();
                                finishAffinity();
                                startActivity(new Intent(getApplication(), LoginActivity.class));
                                return true;
                            case R.id.action_portfolio:
                                openPortfolio();
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

    private void openPortfolio() {
        Intent intent = new Intent(getApplicationContext(), PortfolioActivity.class);
        startActivity(intent);
    }

    private void setFloatingButton() {
        FloatingActionButton scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChooseScanActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setProfile() {
        if (user != null) {

            Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.guest).transform(new CircleTransform()).into(profileImage);
            nameProfile.setText("Guest");

            if (user.getDisplayName() != null) {
                if (user.getDisplayName().length() > 1)
                    nameProfile.setText(user.getDisplayName());
            }
        }
    }

    private void openSettings() {
        // open Edit Profile
        Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String strEditText = data.getStringExtra("displayName");
                nameProfile.setText(strEditText);
                String uriPhoto = data.getStringExtra("photoUrl");
                profileImage.invalidate();
                user.reload();
                Picasso.get().load(Uri.parse(uriPhoto)).placeholder(R.drawable.guest).memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .noFade().transform(new CircleTransform()).into(profileImage);
            }
        }
    }

    public void onBackPressed() {
        finishAffinity();
    }

    private void monthlyGraph() {
        DateFormat dateFormat = new SimpleDateFormat("MM");
        Date date = new Date();
        Log.d("Month", dateFormat.format(date));

        // Spinner above the graph
        Spinner spinner = findViewById(R.id.month_spinner);
        String[] items = new String[]{
                getString(R.string.gen_month_1), getString(R.string.gen_month_2),
                getString(R.string.gen_month_3), getString(R.string.gen_month_4),
                getString(R.string.gen_month_5), getString(R.string.gen_month_6),
                getString(R.string.gen_month_7), getString(R.string.gen_month_8),
                getString(R.string.gen_month_9), getString(R.string.gen_month_10),
                getString(R.string.gen_month_11), getString(R.string.gen_month_12)
        };

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<History> historyMonthList = getHistoryListByMonth(position + 1);

                // clear graph first
                clearGraph();

                // add graph values
                for (History history : historyMonthList) {
                    updateGraph(history);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        //set the spinners adapter to the previously created one.
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(Integer.parseInt(dateFormat.format(date)) - 1);

        // TODO: set current month
    }

    // TODO: days that have no receipts get a value of 0 and days that have more than one receipt should make a sum
    private void updateGraph(History history) {
        String dateString = new SimpleDateFormat("dd").format(history.getReceipt().getDate());
        float date = Float.valueOf(dateString);
        float price = (float) history.getReceipt().getTotalPrice();

        dataSet.addEntry(new Entry(date, price));

        // set maximum value in the graph
        if (maximumReceiptPrice < history.getReceipt().getTotalPrice()) {
            maximumReceiptPrice = (float) history.getReceipt().getTotalPrice();
            chart.getAxisLeft().setAxisMaximum(maximumReceiptPrice + (maximumReceiptPrice * 0.1f));
        }

        dataSet.notifyDataSetChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private ArrayList<History> getHistoryListByMonth(int month) {
        ArrayList<History> historyMonthList = new ArrayList<>();
        for (History history : historyArrayList) {
            if (isHistoryInGivenMonth(history, month)) {
                historyMonthList.add(history);
            }
        }
        return historyMonthList;
    }

    private void clearGraph() {
        dataSet.clear();

        dataSet.notifyDataSetChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private boolean isHistoryInGivenMonth(History history, int month) {
        String monthString = new SimpleDateFormat("MM").format(history.getReceipt().getDate());
        int historyMonth = Integer.parseInt(monthString);
        return month == historyMonth;
    }

    // it reverses the historyArrayList because the History with the older date is at the end of the Array
    // and the newer ones at the start of the Array, then it loops every History Object in the Array
    // and checks if it's in the current month
    public void fillGraphFromCurrentMonth() {
        ArrayList<History> reversedHistoryList = historyArrayList;
        Collections.reverse(reversedHistoryList);
        clearGraph();
        for (History history : reversedHistoryList) {
            if (isHistoryInGivenMonth(history, Integer.parseInt(monthString))) {
                updateGraph(history);
            }
        }
    }

    private void setGoal() {
        monthlyLimitTextView = findViewById(R.id.monthly_limit_text_view);

        // check if there is already a goal set on the user
        final DatabaseReference databaseReference = baseReference.child("goal");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Integer goal = dataSnapshot.getValue(Integer.class);
                    if (goal != null) {
                        monthlyLimitTextView.setText("" + goal);
                    } else {
                        monthlyLimitTextView.setText("" + 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // on text change warn the users that only numbers are allowed and on finish save to firebase
        monthlyLimitTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().matches("[0-9][^.]*")) {
                    Toast.makeText(MainActivity.this, "Only numbers are allowed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    databaseReference.setValue(Integer.valueOf(s.toString()));
                }
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        });
    }

    private void bindGoalClick() {
        monthlyLimitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoalPopup();
            }
        });

        ConstraintLayout constraintLayout2 = findViewById(R.id.constraintLayout2);
        constraintLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoalPopup();
            }
        });
    }

    private void showGoalPopup() {
        // inflate the layout of the popup window
        LayoutInflater genericInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View goalPopupView = genericInflater.inflate(R.layout.goal_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        final PopupWindow goalPopup = new PopupWindow(goalPopupView, width, height, true);

        final EditText editText = goalPopupView.findViewById(R.id.goal_edit_text);
        editText.setText(monthlyLimitTextView.getText());

        Button finishButton = goalPopupView.findViewById(R.id.finish_goal_edit_button);

        // show the popup window
        goalPopup.showAtLocation(monthlyLimitTextView, Gravity.CENTER, 0, 0);

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String monthlyLimit = String.valueOf(editText.getText());
                monthlyLimitTextView.setText(monthlyLimit);
                goalPopup.dismiss();
            }
        });
    }

    private void setFullHistory() {
        Button fullHistoryButton = findViewById(R.id.full_history_button);
        fullHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FullHistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setDrawer() {
        final FlowingDrawer mDrawer = findViewById(R.id.drawerlayout);
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);
        mDrawer.setOnDrawerStateChangeListener(new ElasticDrawer.OnDrawerStateChangeListener() {
            @Override
            public void onDrawerStateChange(int oldState, int newState) {
                if (newState == ElasticDrawer.STATE_CLOSED) {
                    Log.i("MainActivity", "Drawer STATE_CLOSED");
                }
            }

            @Override
            public void onDrawerSlide(float openRatio, int offsetPixels) {
                Log.i("MainActivity", "openRatio=" + openRatio + " ,offsetPixels=" + offsetPixels);
            }
        });

        BlurView blurView = findViewById(R.id.drawer_blur_view);
        new BlurEffect().setBlurEffect(this, blurView, false);

        ImageView sidebarOpener = findViewById(R.id.sidebar_opener);
        sidebarOpener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openMenu();
            }
        });

        // sidebar listeners
        View portfolioOpener = findViewById(R.id.sidebar_portfolio_opener);
        portfolioOpener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPortfolio();
            }
        });

        View profileOpener = findViewById(R.id.sidebar_profile_opener);
        profileOpener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        View settingsOpener = findViewById(R.id.sidebar_settings_opener);
        settingsOpener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        View helpOpener = findViewById(R.id.sidebar_help_opener);

        View logoutSidebar = findViewById(R.id.sidebar_logout);
        logoutSidebar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finishAffinity();
                startActivity(new Intent(getApplication(), LoginActivity.class));
            }
        });
    }
}
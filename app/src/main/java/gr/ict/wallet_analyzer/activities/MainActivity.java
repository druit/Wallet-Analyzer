package gr.ict.wallet_analyzer.activities;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import Adapters.ItemAdapter;
import Adapters.MyListAdapter;
import data_class.CircleTransform;
import data_class.History;
import data_class.Item;
import data_class.Receipt;
import data_class.YourData;
import eightbitlab.com.blurview.BlurView;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.BankEditPopup;
import gr.ict.wallet_analyzer.helpers.BlurEffect;
import gr.ict.wallet_analyzer.helpers.HistoryListView;
import gr.ict.wallet_analyzer.helpers.ListeningVariable;
import gr.ict.wallet_analyzer.helpers.ReceiptPopup;

public class MainActivity extends BaseActivity {

    public ArrayList<History> historyArrayList = new ArrayList<>();
    ImageView profileImage;
    TextView nameProfile, totalPriceMonth;
    EditText monthlyLimitTextView;
    //    double totalPrice;
    private ListeningVariable<Double> totalPrice = new ListeningVariable<>(Double.class);

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String uid;
    DatabaseReference baseReference ;

    private LineChart chart;
    private LineDataSet dataSet;
    private float maximumReceiptPrice = 0;
    private String monthString = new SimpleDateFormat("MM").format(new Date());
    private boolean isReceiptEditPressed = false;
    private MyListAdapter mainAdapter;
    private BankEditPopup bankEditPopup = new BankEditPopup();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(user!= null && user.isEmailVerified()){
            uid = user.getUid();
            baseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        profileImage = findViewById(R.id.profileImage);
        nameProfile = findViewById(R.id.nameMain);
        totalPriceMonth = findViewById(R.id.sum_text_view);

        // GraphView
        setGraphView();

        // set profile name and image
        setProfile();

        // floating button that opens the scan activity
        setFloatingButton();

        // list view
        // setListView();
        HistoryListView historyListView = new HistoryListView(this, baseReference, totalPrice);
        historyListView.setListView();

        totalPrice.setListener(new ListeningVariable.ChangeListener<Double>() {
            @Override
            public void onChange(Double object) {
                totalPriceMonth.setText(String.format("%.2f", object) + "€");
            }
        });

        // spinner for the graph
        monthlyGraph();

        // open menu when clicked on the username on top right corner
        setMenuOpener();

        setGoal();

        setFullHistory();

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
        Intent intent = new Intent(getApplicationContext(), Portfolio.class);
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
//            if (user.getPhotoUrl() != null) {
//                Picasso.get().load(user.getPhotoUrl()).transform(new CircleTransform()).into(profileImage);
//            } else {
//                profileImage.setImageResource(R.drawable.guest);
//            }

            nameProfile.setText("Guest");

            if(user.getDisplayName() != null){
                if(user.getDisplayName().length()>1)
                    nameProfile.setText(user.getDisplayName());
            }

        }
    }

    private void openSettings() {
//        if (user.getPhotoUrl() != null) {
//            Picasso.get().load(user.getPhotoUrl()).transform(new CircleTransform()).into(profileImagePop);
//        } else {
//            profileImagePop.setImageResource(R.drawable.guest);
//        }

        // open Edit Profile
        Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
        startActivityForResult(intent,1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
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

    private void showReceiptPopup(final int itemPosition) {
        final History showingHistory = historyArrayList.get(itemPosition);
        final Receipt listItemReceipt = showingHistory.getReceipt();
        final String currentReceiptId = showingHistory.getId();

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.receipt_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        final TextView storeNameTextView = popupView.findViewById(R.id.store_name_text_view);
        storeNameTextView.setText(listItemReceipt.getStoreName());

        final TextView receiptPriceTextView = popupView.findViewById(R.id.price_text_view);
        receiptPriceTextView.setText(listItemReceipt.getTotalPrice() + "€");

        // set receipt location
        final TextView addressTextView = popupView.findViewById(R.id.address_text_view);
        addressTextView.setText(listItemReceipt.getAddress());

        // set date of the receipt
        final TextView dateTextView = popupView.findViewById(R.id.date_text_view);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(listItemReceipt.getDate());
        dateTextView.setText(date);

        // set receipt category
        final TextView categoryTextView = popupView.findViewById(R.id.category_text_view);
        categoryTextView.setText(listItemReceipt.getStoreType());

        // popupListView view in popup
        final ListView popupListView;

        final ItemAdapter popupAdapter = new ItemAdapter(this, listItemReceipt.getItems());
        popupListView = popupView.findViewById(R.id.list_popup);
        popupListView.setAdapter(popupAdapter);

        // blur effect
        BlurView blurView = popupView.findViewById(R.id.blurView);
        new BlurEffect().setBlurEffect(this, blurView);

        // show the popup window
        popupWindow.showAtLocation(findViewById(R.id.list), Gravity.CENTER, 0, 0);

        Button trashBtn = popupView.findViewById(R.id.trash_button);
        trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDeclare = new AlertDialog.Builder(MainActivity.this);
                alertDeclare.setMessage(getString(R.string.alert_delete_receipt)).setCancelable(false)
                        .setPositiveButton(getString(R.string.gen_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseReference declare = baseReference.child("history").child(currentReceiptId);
                                totalPrice.setObject(totalPrice.getObject() - historyArrayList.get(itemPosition).getReceipt().getTotalPrice());
                                declare.removeValue();

                                finish();
                                popupWindow.dismiss();
                            }
                        })
                        .setNegativeButton(getString(R.string.gen_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = alertDeclare.create();
                alert.setTitle(getString(R.string.gen_warning));
                alert.show();
            }
        });

        addressTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReceiptEditPressed) {
                    String label = "Shop Address";
                    String firebaseUrl = "/history/" + currentReceiptId + "/receipt/address/";
                    showGenericPopup(label, addressTextView, findViewById(R.id.list), firebaseUrl);
                } else {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);

                    Bundle args = new Bundle();
                    args.putSerializable("history", (Serializable) historyArrayList);
                    intent.putExtra("BUNDLE", args);
                    intent.putExtra("itemPosition", itemPosition);

                    startActivity(intent);
                }
            }
        });

        // Edit
        Button editReceiptButton = popupView.findViewById(R.id.edit_button);
        editReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReceiptEditPressed = !isReceiptEditPressed;

                if (isReceiptEditPressed) {
                    Toast.makeText(MainActivity.this, "Edit Mode On", Toast.LENGTH_SHORT).show();

                    popupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            if (isReceiptEditPressed) {
                                // inflate the layout of the popup window
                                LayoutInflater editInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                View editView = editInflater.inflate(R.layout.edit_receipt_popup, null);

                                // create the popup window
                                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                                boolean focusable = true; // lets taps outside the popup also dismiss it
                                final PopupWindow editWindow = new PopupWindow(editView, width, height, focusable);

                                final Item currentlyEditItem = listItemReceipt.getItems().get(position);
                                String currentProductTitle = currentlyEditItem.getName();
                                String currentProductPrice = String.valueOf(currentlyEditItem.getPrice());

                                final EditText titleEditText = editView.findViewById(R.id.product_name_edit_text);
                                final EditText priceEditText = editView.findViewById(R.id.product_price_edit_text);
                                Button finishEditButton = editView.findViewById(R.id.finish_edit_button);

                                titleEditText.setText(currentProductTitle);
                                priceEditText.setText(currentProductPrice);

                                // show the popup window
                                editWindow.showAtLocation(findViewById(R.id.list), Gravity.CENTER, 0, 0);

                                finishEditButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String changedTitle = String.valueOf(titleEditText.getText());
                                        String changedPrice = String.valueOf(priceEditText.getText());

                                        currentlyEditItem.setName(changedTitle);
                                        currentlyEditItem.setPrice(Double.parseDouble(changedPrice));

                                        DatabaseReference receiptReference = baseReference.child("history").child(currentReceiptId)
                                                .child("receipt/");
                                        DatabaseReference editedItemReference = receiptReference.child("items").child(String.valueOf(position));
                                        editedItemReference.setValue(currentlyEditItem);

                                        double editedTotalPrice = listItemReceipt.updateTotalPrice();
                                        mainAdapter.notifyDataSetChanged();
                                        popupAdapter.notifyDataSetChanged();
                                        receiptPriceTextView.setText(editedTotalPrice + "€");

                                        receiptReference.child("totalPrice").setValue(editedTotalPrice);

                                        editWindow.dismiss();
                                    }
                                });
                            }
                        }
                    });

                    storeNameTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isReceiptEditPressed) {
                                String label = "Shop Name";
                                String firebaseUrl = "/history/" + currentReceiptId + "/receipt/storeName/";
                                showGenericPopup(label, storeNameTextView, findViewById(R.id.list), firebaseUrl);
                            }
                        }
                    });

                    categoryTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isReceiptEditPressed) {
                                String label = "Category";
                                String firebaseUrl = "/history/" + currentReceiptId + "/receipt/storeType/";
                                showGenericPopup(label, categoryTextView, findViewById(R.id.list), firebaseUrl);
                            }
                        }
                    });

                    dateTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isReceiptEditPressed) {
                                String firebaseUrl = "/history/" + currentReceiptId + "/receipt/date/";
                                showDatePickerPopup(listItemReceipt, dateTextView, findViewById(R.id.list), firebaseUrl);
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Edit Mode Off", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // dismiss the popup window when touched
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isReceiptEditPressed = false;
            }
        });
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

    private void setListView() {
        ListView mainListView;

        mainAdapter = new MyListAdapter(this, historyArrayList);
        mainListView = findViewById(R.id.list);
        mainListView.setAdapter(mainAdapter);

        DatabaseReference declare = baseReference.child("history");
        declare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                historyArrayList.clear();
                dataSet.clear();
                dataSet.addEntry(new Entry(1, 0));
                History history;
                totalPrice.setObject(0.0);

                for (DataSnapshot child : children) {
                    history = child.getValue(History.class);
                    totalPrice.setObject(totalPrice.getObject() + history.getReceipt().getTotalPrice());
                    totalPriceMonth.setText(String.format("%.2f", totalPrice) + "€");
                    historyArrayList.add(history);
                    // sort the array every time, any better ideas would be greatly valued
                    Collections.sort(historyArrayList);
                    mainAdapter.notifyDataSetChanged();

                    // show receipts for current month
                    fillGraphFromCurrentMonth();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
            }
        });

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                showReceiptPopup(position);
                ReceiptPopup receiptPopup = new ReceiptPopup(position, MainActivity.this, historyArrayList, baseReference);
                receiptPopup.showReceiptPopup();
            }
        });
    }

    // TODO: Show 10 latest records or 15
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
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

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
//        chart.getAxisLeft().setDrawLabels(false);

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
        chart.getAxisLeft().setTextColor(Color.argb(50, 255, 255, 255));

        chart.getAxisLeft().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(30);
        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setLabelCount(30, true);

//        chart.setVisibleXRangeMaximum(10);
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
                    if(goal!= null) {
                        monthlyLimitTextView.setText("" + goal);
                    }else{
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

    private void showGenericPopup(String label, final TextView textViewClicked, View viewToShowAt, final String firebaseUrl) {
        String editTextValue = String.valueOf(textViewClicked.getText());
        // inflate the layout of the popup window`
        LayoutInflater genericInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View genericView = genericInflater.inflate(R.layout.edit_receipt_default_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        final PopupWindow genericPopup = new PopupWindow(genericView, width, height, true);

        TextView textView = genericView.findViewById(R.id.receipt_property_text_view);
        final EditText editText = genericView.findViewById(R.id.receipt_property_edit_text);
        Button finishButton = genericView.findViewById(R.id.finish_property_edit_button);

        textView.setText(label);
        editText.setText(editTextValue);

        // show the popup window
        genericPopup.showAtLocation(viewToShowAt, Gravity.CENTER, 0, 0);

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String receiptEditedLabel = String.valueOf(editText.getText());

                DatabaseReference receiptReference = baseReference.child(firebaseUrl);
                receiptReference.setValue(receiptEditedLabel);

                textViewClicked.setText(receiptEditedLabel);

                genericPopup.dismiss();
            }
        });
    }

    private void showDatePickerPopup(final Receipt listItemReceipt, final TextView dateTextView, View viewToShowAt, final String firebaseUrl) {
        Date receiptDate = listItemReceipt.getDate();

        // inflate the layout of the popup window`
        LayoutInflater genericInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View datePickerView = genericInflater.inflate(R.layout.datepicker_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        final PopupWindow datePickerPopup = new PopupWindow(datePickerView, width, height, true);

        final DatePicker datePicker = datePickerView.findViewById(R.id.date_picker_widget);
        Button finishButton = datePickerView.findViewById(R.id.finish_property_edit_button);

        // show the popup window
        datePickerPopup.showAtLocation(viewToShowAt, Gravity.CENTER, 0, 0);

        int day = Integer.parseInt(new SimpleDateFormat("dd").format(receiptDate));
        int month = Integer.parseInt(new SimpleDateFormat("MM").format(receiptDate));
        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(receiptDate));
        datePicker.updateDate(year, month - 1, day);

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();
                Date date = new GregorianCalendar(year, month, day).getTime();

                DatabaseReference dateReference = baseReference.child(firebaseUrl);
                dateReference.setValue(date);

                listItemReceipt.setDate(date);

                String dateStr = day + "/" + (month + 1) + "/" + year;
                dateTextView.setText(dateStr);

                datePickerPopup.dismiss();
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
}
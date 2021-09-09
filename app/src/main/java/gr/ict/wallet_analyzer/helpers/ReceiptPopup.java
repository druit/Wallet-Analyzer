package gr.ict.wallet_analyzer.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import Adapters.ItemAdapter;
import data_class.History;
import data_class.Item;
import data_class.Receipt;
import eightbitlab.com.blurview.BlurView;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.activities.MapsActivity;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ReceiptPopup {
    private ArrayList<History> historyArrayList;
    private Context context;
    private Activity activity;
    private boolean isReceiptEditPressed = false;
    private DatabaseReference baseReference;
    private double totalPrice;
    private PopupWindow popupWindow;
    private Receipt receipt;
    private String currentReceiptId;
    private int itemPosition;

    private ListView popupListView;
    private TextView receiptPriceTextView;
    private TextView storeNameTextView;
    private TextView categoryTextView;
    private TextView dateTextView;
    private TextView addressTextView;

    public ReceiptPopup(int itemPosition, Activity activity, ArrayList<History> historyArrayList, DatabaseReference baseReference) {
        context = activity.getApplicationContext();
        this.activity = activity;

        this.historyArrayList = historyArrayList;

        this.baseReference = baseReference;

        this.itemPosition = itemPosition;
    }

    public void showReceiptPopup() {
        final History showingHistory = historyArrayList.get(itemPosition);
        receipt = showingHistory.getReceipt();
        currentReceiptId = showingHistory.getId();

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.receipt_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        storeNameTextView = popupView.findViewById(R.id.store_name_text_view);
        storeNameTextView.setText(receipt.getStoreName());

        receiptPriceTextView = popupView.findViewById(R.id.price_text_view);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String value = decimalFormat.format(receipt.getTotalPrice());
        receiptPriceTextView.setText(value + " €");

        // set receipt location
        addressTextView = popupView.findViewById(R.id.address_text_view);
        addressTextView.setText(receipt.getAddress());

        // set date of the receipt
        dateTextView = popupView.findViewById(R.id.date_text_view);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(receipt.getDate());
        dateTextView.setText(date);

        // set receipt category
        categoryTextView = popupView.findViewById(R.id.category_text_view);
        categoryTextView.setText(receipt.getStoreType());

        // popupListView view in popup

        final ItemAdapter popupAdapter = new ItemAdapter(activity, receipt.getItems());
        popupListView = popupView.findViewById(R.id.list_popup);
        popupListView.setAdapter(popupAdapter);

        // blur effect
        BlurView blurView = popupView.findViewById(R.id.blurView);
        new BlurEffect().setBlurEffect(activity, blurView, true);

        // show the popup window
        popupWindow.showAtLocation(activity.findViewById(R.id.list), Gravity.CENTER, 0, 0);

        Button trashBtn = popupView.findViewById(R.id.trash_button);
        trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteReceipt();
            }
        });

        addressTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressPressed();
            }
        });

        // Edit
        Button editReceiptButton = popupView.findViewById(R.id.edit_button);
        editReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editReceipt();
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

    private void editReceipt() {
        isReceiptEditPressed = !isReceiptEditPressed;

        if (isReceiptEditPressed) {
            Toast.makeText(activity, R.string.gen_edit_mode_on, Toast.LENGTH_SHORT).show();

            popupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                    if (isReceiptEditPressed) {
                        // inflate the layout of the popup window
                        LayoutInflater editInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
                        View editView = editInflater.inflate(R.layout.edit_receipt_popup, null);

                        // create the popup window
                        int width = LinearLayout.LayoutParams.MATCH_PARENT;
                        int height = LinearLayout.LayoutParams.MATCH_PARENT;
                        boolean focusable = true; // lets taps outside the popup also dismiss it
                        final PopupWindow editWindow = new PopupWindow(editView, width, height, focusable);

                        final Item currentlyEditItem = receipt.getItems().get(position);
                        String currentProductTitle = currentlyEditItem.getName();
                        String currentProductPrice = String.valueOf(currentlyEditItem.getPrice());

                        final EditText titleEditText = editView.findViewById(R.id.product_name_edit_text);
                        final EditText priceEditText = editView.findViewById(R.id.product_price_edit_text);
                        Button finishEditButton = editView.findViewById(R.id.finish_edit_button);

                        titleEditText.setText(currentProductTitle);
                        priceEditText.setText(currentProductPrice);

                        // show the popup window
                        editWindow.showAtLocation(activity.findViewById(R.id.list), Gravity.CENTER, 0, 0);

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

                                double editedTotalPrice = receipt.updateTotalPrice();
                                // TODO: create an interface for a list containing all adapters and then cycle through them
                                // and notify data changes like so:
                                // mainAdapter.notifyDataSetChanged();
                                // popupAdapter.notifyDataSetChanged();
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
                        showGenericPopup(label, storeNameTextView, activity.findViewById(R.id.list), firebaseUrl);
                    }
                }
            });

            categoryTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isReceiptEditPressed) {
                        String label = "Category";
                        String firebaseUrl = "/history/" + currentReceiptId + "/receipt/storeType/";
                        showGenericPopup(label, categoryTextView, activity.findViewById(R.id.list), firebaseUrl);
                    }
                }
            });

            dateTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isReceiptEditPressed) {
                        String firebaseUrl = "/history/" + currentReceiptId + "/receipt/date/";
                        showDatePickerPopup(receipt, dateTextView, activity.findViewById(R.id.list), firebaseUrl);
                    }
                }
            });
        } else {
            Toast.makeText(activity, R.string.gen_edit_mode_off, Toast.LENGTH_SHORT).show();
        }
    }

    private void addressPressed() {
        if (isReceiptEditPressed) {
            String label = "Shop Address";
            String firebaseUrl = "/history/" + currentReceiptId + "/receipt/address/";
            showGenericPopup(label, addressTextView, activity.findViewById(R.id.list), firebaseUrl);
        } else {
            Intent intent = new Intent(activity, MapsActivity.class);

            Bundle args = new Bundle();
            args.putSerializable("history", (Serializable) historyArrayList);
            intent.putExtra("BUNDLE", args);
            intent.putExtra("itemPosition", itemPosition);

            activity.startActivity(intent);
        }
    }

    private void deleteReceipt() {
        AlertDialog.Builder alertDeclare = new AlertDialog.Builder(activity);
        alertDeclare.setMessage(activity.getString(R.string.alert_delete_receipt)).setCancelable(false)
                .setPositiveButton(activity.getString(R.string.gen_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference declare = baseReference.child("history").child(currentReceiptId);
                        // TODO: create a listener to notify stuff
                        // for example to notify and remove the current receipt from historyArrayList
                        // and to notify the totalPrice (it's the one showing the total amount of expenses in the month)
                        // totalPrice -= historyArrayList.get(itemPosition).getReceipt().getTotalPrice();
                        declare.removeValue();

//                        activity.finish();
                        popupWindow.dismiss();
                    }
                })
                .setNegativeButton(activity.getString(R.string.gen_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = alertDeclare.create();
        alert.setTitle(activity.getString(R.string.gen_warning));
        alert.show();
    }

    private void showGenericPopup(String label, final TextView textViewClicked, View viewToShowAt, final String firebaseUrl) {
        String editTextValue = String.valueOf(textViewClicked.getText());
        // inflate the layout of the popup window`
        LayoutInflater genericInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
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
        LayoutInflater genericInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
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
}

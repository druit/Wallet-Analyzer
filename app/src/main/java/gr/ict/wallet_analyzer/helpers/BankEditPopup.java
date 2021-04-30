package gr.ict.wallet_analyzer.helpers;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import data_class.BankAccount;
import data_class.Salary;
import gr.ict.wallet_analyzer.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class BankEditPopup {
    Context context;
    Activity activity;
    EditText bankAccount;
    EditText bankDescription;
    EditText bankSalary;
    Button actionBtn;
    TextView bankDate;
    LayoutInflater inflater;
    CheckBox checkBox;
    View popupView;

    String currentId;

    private ArrayList<BankAccount> myAccount = new ArrayList<>();

    public BankEditPopup(Context context,Activity activity){
        this.context = context;
        this.activity = activity;
    }
    public  BankEditPopup(){}

    public static void switchButton(String currentId,int isActive, DatabaseReference baseReference) {
        DatabaseReference declare = baseReference.child("bankAccounts").child(currentId);
        declare.child("active").setValue(isActive);
    }

    public void ShowBankPopup(final boolean editable, final BankAccount myBankAccount, final DatabaseReference baseReference,final List<BankAccount> myAccountBank){
        // inflate the layout of the popup window

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.add_item_my_account, null);
        if(editable) {
            currentId = myBankAccount.getId();
        }else {
            currentId = baseReference.push().getKey();
        }
        final DatabaseReference declare = baseReference.child("bankAccounts").child(currentId);

        bankAccount = popupView.findViewById(R.id.myAccount_title_editText);
        bankDescription = popupView.findViewById(R.id.myAccount_description_editText);
        bankSalary = popupView.findViewById(R.id.myAccount_salary_editText);
        actionBtn = popupView.findViewById(R.id.complete_myAccount_button);
        bankDate = popupView.findViewById(R.id.date_text_bank_view);
        checkBox = popupView.findViewById(R.id.mainBankCheckBox);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        // show the popup window
        popupWindow.showAtLocation(activity.findViewById(R.id.fragment2), Gravity.CENTER, 0, 0);

      bankDate.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Date date = new Date();
              if(editable){
                  ArrayList<Salary> salaryArrayList = myBankAccount.getSalaryArrayList();
                  date = salaryArrayList.get(salaryArrayList.size()-1).getLastUpdate();
                  showDatePickerPopup(activity.findViewById(R.id.fragment2),(Date)date, myBankAccount.getSalaryArrayList(), declare,editable);
              }else {
                  showDatePickerPopup(activity.findViewById(R.id.fragment2), (Date) date, null, declare, editable);
              }
          }
      });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton,final boolean b) {
                boolean alreadyChecked = false;

                        for (BankAccount account: myAccountBank) {
                            if(account.isSalaryBank() && myBankAccount.getId() != account.getId()){
                                alreadyChecked = true;
                            }
                        }
                        if(alreadyChecked){
                            compoundButton.setChecked(false);
                            Toast.makeText(activity.getBaseContext(),activity.getBaseContext().getResources().getString(R.string.gen_error_bank_already),Toast.LENGTH_SHORT).show();
                        }else{

                            declare.child("salaryBank").setValue(b);
                        }
                    }
        });

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bankAccount.getText().toString().length() > 0  && bankSalary.getText().toString().length() > 0 && bankDescription.getText().toString().length() > 0){
                    String bank = bankAccount.getText().toString();
                    String desc = bankDescription.getText().toString();
                    double salary = Double.valueOf(bankSalary.getText().toString());

                    BankAccount bankAccount;
                    if(editable){
                        bankAccount = new BankAccount(currentId,bank,desc,salary,myBankAccount.isActive(), myBankAccount.getSalaryArrayList(),checkBox.isChecked());
                    }else{
                        Calendar myCal = Calendar.getInstance();
                        String[] dateSplit = bankDate.getText().toString().split("/");
                        if(dateSplit.length>0) {
                            myCal.set(Calendar.YEAR, Integer.parseInt(dateSplit[2]));
                            myCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSplit[0]));
                            myCal.set(Calendar.MONTH, Integer.parseInt(dateSplit[1]) - 1);
                        }
                        Date selectedDate = new Date(String.valueOf(myCal.getTime()));
                        Salary newSalary = new Salary(selectedDate,salary,salary,selectedDate);
                        ArrayList<Salary> salaryArrayList = new ArrayList<>();
                        salaryArrayList.add(newSalary);
                        bankAccount = new BankAccount(currentId,bank,desc,salary,0, salaryArrayList, false);
                    }
                    declare.setValue(bankAccount);
                    popupWindow.dismiss();
                }else{
                    Toast.makeText(activity.getBaseContext(),activity.getBaseContext().getResources().getString(R.string.gen_error_add_item),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePickerPopup(View viewToShowAt, Date date, final ArrayList<Salary> salaryArrayList, final DatabaseReference declare,final  boolean editable) {
        Date receiptDate = date;

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
        datePickerPopup.showAtLocation( viewToShowAt, Gravity.CENTER, 0, 0);

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
                final Date date = new GregorianCalendar(year, month, day).getTime();

                if(editable) {
                    declare.child("salaryArrayList").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            ArrayList<Salary> listSalary = new ArrayList<>();
                            int i = 1;
                            for (DataSnapshot child : children) {
                                Salary salary = child.getValue(Salary.class);

                                if (i == salaryArrayList.size()) {
                                    salary.setUpdateDate(date);
                                    salary.setLastUpdate(date);
                                    listSalary.add(salary);
                                }
                                i++;
                            }
                            declare.child("salaryArrayList").setValue(listSalary);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
//                DatabaseReference dateReference = baseReference.child(firebaseUrl);
//                dateReference.setValue(date);

//                listItemReceipt.setDate(date);

                String dateStr = day + "/" + (month + 1) + "/" + year;
                bankDate.setText(dateStr);

                datePickerPopup.dismiss();
            }
        });
    }

    public void setItemBankPopup(boolean isMainBank, String bankTitle, String description, String salary, String choice,Date date){

        bankAccount.setText(bankTitle);
        bankDescription.setText(description);
        String[] salaryNumber = salary.split("€");
        bankSalary.setText(salaryNumber[0]);
        checkBox.setVisibility(View.VISIBLE);
        checkBox.setChecked(isMainBank);

        String textDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
        bankDate.setText(textDate);


        switch(choice){
            case "SAVE":
                actionBtn.setText(context.getResources().getString(R.string.gen_add));
                break;
            case "EDIT":
                actionBtn.setText(context.getResources().getString(R.string.gen_save));
                break;
            default:
                break;
        }
    }

    public void setText(String choice){
        if(choice.contains("SAVE")){
            actionBtn.setText(context.getResources().getString(R.string.gen_add));
            String textDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            bankDate.setText(textDate);
            checkBox.setVisibility(View.INVISIBLE);
        }
    }


    public void callBackMyAccount(DatabaseReference baseReference, final FirebaseResultInterface firebaseResultInterface) {

        DatabaseReference declare = baseReference.child("bankAccounts");

        declare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                BankAccount bankAccount;
                myAccount.clear();
                for (DataSnapshot child : children) {
                    bankAccount = child.getValue(BankAccount.class);
                    myAccount.add(bankAccount);
//                    Collections.sort(myAccount);
                }
                firebaseResultInterface.onSuccess(myAccount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseResultInterface.onFailed(databaseError.toException());
            }
        });

    }
    public void getSalaryList(DatabaseReference baseReference, final FirebaseResultInterface firebaseResultInterface) {
        DatabaseReference declare = baseReference.child("bankAccounts");
//
        declare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    BankAccount bankAccount;
//                    Salary salary;
                    ArrayList<Salary> listSalary = new ArrayList<>();
                    ArrayList<Salary> sendSalary = new ArrayList<>();

                    for (DataSnapshot child : children) {
                        bankAccount = child.getValue(BankAccount.class);
//                        salary = bankAccount.getSalaryArrayList().get(bankAccount.getSalaryArrayList().size() - 1);
                        if(bankAccount.isActive() == 1 ) {
                            listSalary = bankAccount.getSalaryArrayList();
//                            if(bankAccount.isSalaryBank()){
                                for (Salary sal: listSalary) {
                                    sendSalary.add(sal);
//                                }
                            }


//                            listSalary.add(salary);
                        }
                    }
                    firebaseResultInterface.onSuccess(sendSalary);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseResultInterface.onFailed(databaseError.toException());
            }
        });
    }


    // Check for update salary every month
    public void checkSalary(DatabaseReference baseReference){
        final DatabaseReference declare = baseReference.child("bankAccounts");
        declare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    BankAccount bankAccount;
                    BankAccount currentBank = null;
                    Salary salary;
                    ArrayList<Salary> listSalary = new ArrayList<>();

                    for (DataSnapshot child : children) {
                        bankAccount = child.getValue(BankAccount.class);
                        salary = bankAccount.getSalaryArrayList().get(bankAccount.getSalaryArrayList().size() - 1);
                        if(bankAccount.isSalaryBank() == true) {
                            currentBank = bankAccount;
                            listSalary.add(salary);
                        }
                    }
                    if(listSalary.size()>0) {

                        Salary currentSalary = listSalary.get(listSalary.size() - 1);

                        // Current date/month/year
                        Date currentDate = new Date();
                        int currentMonth = Integer.valueOf( currentDate.getMonth() + 1);
                        int currentYear = currentDate.getYear();

                        // Salary date/month/year
                        Date dateSalary = currentSalary.getLastUpdate();
                        int prevMonthSalary = Integer.valueOf( dateSalary.getMonth() + 1);
                        int salaryYear = dateSalary.getYear();

                        if (currentDate.getDate() >= dateSalary.getDate() && prevMonthSalary < currentMonth && salaryYear >= currentYear) {

                            Salary newSalary = new Salary(currentDate,Double.valueOf(currentSalary.getCurrentSalary()+ currentSalary.getSalaryAdd()), currentSalary.getSalaryAdd(), currentDate);

                            System.out.println("NEW SALARY: " + newSalary.getCurrentSalary());
                            listSalary.add(newSalary);
                            declare.child(currentBank.getId()).child("salary").setValue(Double.valueOf(currentSalary.getCurrentSalary()+ currentSalary.getSalaryAdd()));
                            declare.child(currentBank.getId()).child("salaryArrayList").setValue(listSalary);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}

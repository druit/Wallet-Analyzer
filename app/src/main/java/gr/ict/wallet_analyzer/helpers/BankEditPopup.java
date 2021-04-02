package gr.ict.wallet_analyzer.helpers;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import Adapters.MyAccountAdapter;
import data_class.BankAccount;
import data_class.History;
import gr.ict.wallet_analyzer.R;

public class BankEditPopup {
    Context context;
    Activity activity;
    EditText bankAccount;
    EditText bankDescription;
    EditText bankSalary;
    Button actionBtn;
    LayoutInflater inflater;
    View popupView;

    String currentId;

    private ArrayList<BankAccount> myAccount = new ArrayList<>();

    public BankEditPopup(Context context,Activity activity){
        this.context = context;
        this.activity = activity;
    }

    public static void switchButton(String currentId,int isActive, DatabaseReference baseReference) {
        DatabaseReference declare = baseReference.child("bankAccounts").child(currentId);
        declare.child("active").setValue(isActive);
    }

    public void ShowBankPopup(final boolean editable, final BankAccount myBankAccount, DatabaseReference baseReference ){
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

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        // show the popup window
        popupWindow.showAtLocation(activity.findViewById(R.id.fragment2), Gravity.CENTER, 0, 0);

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bank = bankAccount.getText().toString();
                String desc = bankDescription.getText().toString();
                double salary = Double.valueOf(bankSalary.getText().toString());
                BankAccount bankAccount;
                if(editable){
                    bankAccount = new BankAccount(currentId,bank,desc,salary,myBankAccount.isActive());
                }else{
                    bankAccount = new BankAccount(currentId,bank,desc,salary,0);
                }
                declare.setValue(bankAccount);
                popupWindow.dismiss();
            }
        });
    }

    public void setItemBankPopup(String bankTitle, String description, String salary, String choice){

        bankAccount.setText(bankTitle);
        bankDescription.setText(description);
        String[] salaryNumber = salary.split("€");
        bankSalary.setText(salaryNumber[0]);


        switch(choice){
            case "SAVE":
                actionBtn.setText("ADD");
                break;
            case "EDIT":
                actionBtn.setText("SAVE");
                break;
            default:
                break;
        }
    }

    public void setText(String choice){
        if(choice.contains("SAVE")){
            actionBtn.setText("ADD");
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

}

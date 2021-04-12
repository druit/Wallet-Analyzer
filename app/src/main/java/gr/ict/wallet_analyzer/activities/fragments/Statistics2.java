package gr.ict.wallet_analyzer.activities.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Adapters.MyAccountAdapter;
import data_class.BankAccount;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.BankEditPopup;
import gr.ict.wallet_analyzer.helpers.FirebaseResultInterface;

public class Statistics2 extends Fragment {

    PopupWindow popupWindow;
    ListView listView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String uid = user.getUid();
    DatabaseReference baseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
    ArrayList<BankAccount> myAccount = new ArrayList<>();

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statistics2_fragment,container,false);
//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final BankEditPopup bankEditPopup = new BankEditPopup(getActivity().getBaseContext(), getActivity());

        listView = getActivity().findViewById(R.id.listViewAccount);

        View addBank = getActivity().findViewById(R.id.addItem);

        addBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit or save pop up window


                bankEditPopup.ShowBankPopup(false,null,baseReference, myAccount);
                bankEditPopup.setText("SAVE");
//                openPopup();
            }
        });

        Switch mSwitch = getActivity().findViewById(R.id.switch1);
        ImageButton edit = getActivity().findViewById(R.id.bank_editBtn);
        ImageButton delete = getActivity().findViewById(R.id.bank_deleteBtn);
        final TextView totalBankAccount = getActivity().findViewById(R.id.totalBankAccount);


//        BankAccount bankAccount1 = new BankAccount("MyBank1","Credit Card",800,0);
//        BankAccount bankAccount2 = new BankAccount("MyBank2","Credit Card2",1000,1);
//        BankAccount bankAccount3 = new BankAccount("MyBank3","Credit Card3",1200,0);
//        myAccount.add(bankAccount1);
//        myAccount.add(bankAccount2);
//        myAccount.add(bankAccount3);
//        final String id = baseReference.push().getKey();
        DatabaseReference declare = baseReference.child("bankAccounts");
//
        declare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                BankAccount bankAccount;
                double totalSalary = 0;
                for (DataSnapshot child : children) {
                    bankAccount = child.getValue(BankAccount.class);
                    if(bankAccount.isActive() == 1){
                        totalSalary += bankAccount.getSalary();
                    }
                }
                totalBankAccount.setText(String.valueOf(totalSalary) + " €");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//        declare.setValue(bankAccount1);
//        declare.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
//                BankAccount bankAccount;
//                myAccount.clear();
//                for (DataSnapshot child : children) {
//
//                    bankAccount = child.getValue(BankAccount.class);
//                    myAccount.add(bankAccount);
//                    System.out.println("YES: " + bankAccount.isActive());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
        FirebaseResultInterface firebaseResultInterface =  new FirebaseResultInterface<ArrayList<BankAccount>>() {

            @Override
            public void onSuccess(ArrayList<BankAccount> data) {
                myAccount = data;
                if(getActivity() != null) {
                    MyAccountAdapter adapter = new MyAccountAdapter(getContext(), myAccount, getActivity());
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                        }
                    });
                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                            Toast.makeText(getActivity(), "LONG", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    });
                }
            }

            @Override
            public void onFailed(Throwable error) {

            }
        };

        bankEditPopup.callBackMyAccount(baseReference,firebaseResultInterface);
    }

    private void openPopup() {
        // inflate the layout of the popup window

        LayoutInflater inflater = (LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.add_item_my_account, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);
        // show the popup window
        popupWindow.showAtLocation(getActivity().findViewById(R.id.fragment2), Gravity.CENTER, 0, 0);
    }


}

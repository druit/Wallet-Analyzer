package Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import data_class.BankAccount;
import data_class.History;
import data_class.Salary;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.activities.fragments.Statistics2;
import gr.ict.wallet_analyzer.helpers.BankEditPopup;
import gr.ict.wallet_analyzer.helpers.FirebaseResultInterface;
import gr.ict.wallet_analyzer.helpers.HistoryArrayList;

public class MyAccountAdapter extends ArrayAdapter<BankAccount> {
    Context context;
    Activity activity;

    private List<BankAccount> myAccount;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String uid = user.getUid();
    DatabaseReference baseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

    public MyAccountAdapter(@NonNull Context c,@NonNull List<BankAccount> myAccount,@NonNull Activity activity){

        super(c, R.layout.my_account_row, myAccount);
        this.context = c;
        this.myAccount = myAccount;
        this.activity = activity;
    }

    public MyAccountAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.my_account_row,parent,false);
        final BankEditPopup bankEditPopup = new BankEditPopup(activity.getBaseContext(), activity);

        Switch mSwitch = (Switch) row.findViewById(R.id.switch1);


//        ImageView images = row.findViewById(R.id.imageMyAccount);
        final TextView myTitle = row.findViewById(R.id.mTitleView);
        final TextView myDescription = row.findViewById(R.id.mDescriptionView);
        final TextView mySalary = row.findViewById(R.id.mSalary);

//        images.setImageResource(myAccount.get(position).getImages());
        myTitle.setText(myAccount.get(position).getBankTitle());
        myDescription.setText(myAccount.get(position).getDescription());
        HistoryArrayList historyArrayList = new HistoryArrayList();

        if(myAccount.get(position).isSalaryBank()){
            FirebaseResultInterface firebaseResultInterface = new FirebaseResultInterface<ArrayList<History>>() {
                @Override
                public void onSuccess(ArrayList<History> histories) {

                    double totalPrices = 0.0;
                    for (History history:histories) {
                            if(myAccount.get(position).getSalaryArrayList().get(0).getUpdateDate().getTime() <= history.getReceipt().getDate().getTime()){
                                totalPrices += history.getReceipt().getTotalPrice();
                            }
                    }
                    DecimalFormat decimalFormat = new DecimalFormat("#.###");
//                totalBankAccount.setText(String.valueOf(decimalFormat.format(finalTotalSalary[0])) + " €");
                    mySalary.setText(String.valueOf(decimalFormat.format((Double.valueOf(myAccount.get(position).getSalary() - totalPrices))))+" €");
                }

                @Override
                public void onFailed(Throwable error) {

                }
            };
            historyArrayList.callBackHistoryArrayList(baseReference,firebaseResultInterface);
        }else{
            mySalary.setText(String.valueOf(myAccount.get(position).getSalary())+" €");
        }



        if(myAccount.get(position).isActive() == 1){
            mSwitch.setChecked(true);
        }

        ViewHolder holder;
        holder = new ViewHolder();

        // set Buttons edit/delete
        holder.edit = row.findViewById(R.id.bank_editBtn);

        holder.delete = row.findViewById(R.id.bank_deleteBtn);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               DatabaseReference declare = baseReference.child("bankAccounts").child(myAccount.get(position).getId());
                declare.removeValue();
                Toast.makeText(getContext(),"Deleted",Toast.LENGTH_SHORT).show();
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bankEditPopup.ShowBankPopup(true,myAccount.get(position),baseReference,myAccount);
//                Toast.makeText(activity,mySalary.getText().toString(),Toast.LENGTH_SHORT).show();
                ArrayList<Salary> salary = myAccount.get(position).getSalaryArrayList();
                bankEditPopup.setItemBankPopup(myAccount.get(position).isSalaryBank() ,(String) myTitle.getText(), (String) myDescription.getText(),(String) mySalary.getText(),"EDIT",salary.get(salary.size()-1).getLastUpdate());
            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                compoundButton.setChecked(b);
                if (b) {
                    BankEditPopup.switchButton(myAccount.get(position).getId(),1,baseReference);
                }else{
                    BankEditPopup.switchButton(myAccount.get(position).getId(),0,baseReference);
                }
//

            }
        });


        return row;
    }


    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {

        public ImageButton edit;
        public ImageButton delete;
    }
}

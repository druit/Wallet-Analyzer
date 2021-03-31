package gr.ict.wallet_analyzer.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.HistoryListView;
import gr.ict.wallet_analyzer.helpers.ListeningVariable;

public class FullHistoryActivity extends BaseActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private String uid = user.getUid();
    DatabaseReference baseReference = FirebaseDatabase.getInstance().getReference()
            .child("users").child(uid);
    private ListeningVariable<Double> totalPrice = new ListeningVariable<>(Double.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_history);

        HistoryListView historyListView = new HistoryListView(this, baseReference, totalPrice);
        historyListView.setListView();
    }
}

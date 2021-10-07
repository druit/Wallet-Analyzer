package gr.ict.wallet_analyzer.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.HistoryListView;
import gr.ict.wallet_analyzer.helpers.ListeningVariable;

public class FullHistoryActivity extends BaseActivity {

    private final int ASCENDING = 0;
    private final int DESCENDING = 1;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    private final String uid = user.getUid();
    DatabaseReference baseReference = FirebaseDatabase.getInstance().getReference()
            .child("users").child(uid);
    private ListeningVariable<Double> totalPrice = new ListeningVariable<>(Double.class);
    private HistoryListView historyListView;
    private int currentFilterId = R.id.filter_Date;
    private int currentOrder = ASCENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_history);

        historyListView = new HistoryListView(this, baseReference, totalPrice);
        setFilterMenu();
        setOrderMenu();

        setSearchView();
    }

    @SuppressLint("NonConstantResourceId")
    private void setFilterMenu() {
        Button menuOpenerLayout = findViewById(R.id.filter_button);
        menuOpenerLayout.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(FullHistoryActivity.this, v);
            popup.setOnMenuItemClickListener(item -> {
                filter(item.getItemId());
                historyListView.notifyDataSet();
                return true;
            });
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_filter, popup.getMenu());
            popup.show();
        });
    }

    @SuppressLint("NonConstantResourceId")
    private void setOrderMenu() {
        Button menuOpenerLayout = findViewById(R.id.order_button);
        menuOpenerLayout.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(FullHistoryActivity.this, v);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.ascending_order:
                        currentOrder = ASCENDING;
                        filter(currentFilterId);
                        break;
                    case R.id.descending_order:
                        currentOrder = DESCENDING;
                        filter(currentFilterId);
                        break;
                }
                menuOpenerLayout.setText(item.getTitle());
                historyListView.notifyDataSet();
                return true;
            });
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_order, popup.getMenu());
            popup.show();
        });
    }

    private void filter(int id) {
        switch (id) {
            case R.id.filter_category:
                if (currentOrder == ASCENDING)
                    historyListView.filterByCategory();
                else
                    historyListView.filterReverseCategory();
                break;
            case R.id.filter_price:
                if (currentOrder == ASCENDING)
                    historyListView.filterByPrice();
                else
                    historyListView.filterReversePrice();
                break;
            case R.id.filter_Date:
                if (currentOrder == ASCENDING)
                    historyListView.filterReverseDate();
                else
                    historyListView.filterDate();
                break;
        }
        currentFilterId = id;
    }

    private void setSearchView() {
        SearchView searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                historyListView.getMainAdapter().getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                historyListView.getMainAdapter().getFilter().filter(newText);
                return false;
            }
        });

        // anywhere else than searchView make it lose focus
//        ClearFocusClickListener clearFocusClickListener = new ClearFocusClickListener();
//
//        Button filterButton = findViewById(R.id.filter_button);
//        filterButton.setOnClickListener(clearFocusClickListener);
//
//        Button orderButton = findViewById(R.id.order_button);
//        orderButton.setOnClickListener(clearFocusClickListener);
//
//        ConstraintLayout parent = findViewById(R.id.full_history_parent);
//        parent.setOnClickListener(clearFocusClickListener);
//
//        RoundedCornerListView rRoundedCornerListView = findViewById(R.id.list);
//        rRoundedCornerListView.setOnClickListener(clearFocusClickListener);
    }

//    private class ClearFocusClickListener implements View.OnClickListener {
//        @Override
//        public void onClick(View v) {
//            FullHistoryActivity.this.getCurrentFocus().clearFocus();
//        }
//    }
}

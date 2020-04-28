package gr.ict.wallet_analyzer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import Adapters.MyListAdapter;
import gr.ict.wallet_analyzer.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });


        // list view
        ListView list;

        String[] maintitle = {
                "Title 1", "Title 2",
                "Title 3", "Title 4",
                "Title 5", "Title 5", "Title 5",
        };

        String[] subtitle = {
                "$5", "$5,42",
                "$13,50", "$25",
                "$32", "$32", "$32",
        };

        MyListAdapter adapter = new MyListAdapter(this, maintitle, subtitle);
        list = findViewById(R.id.list);
        list.setAdapter(adapter);
    }
}

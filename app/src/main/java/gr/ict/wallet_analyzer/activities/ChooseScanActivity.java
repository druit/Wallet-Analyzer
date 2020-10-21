package gr.ict.wallet_analyzer.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import gr.ict.wallet_analyzer.R;

public class ChooseScanActivity extends AppCompatActivity {

    Button scanBarcode,scanPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_scan);

        scanBarcode = findViewById(R.id.scan_with_barcode);
        scanPhoto = findViewById(R.id.scan_with_photo);

        scanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseScanActivity.this, ScanBarcodeActivity.class);
                startActivity(intent);
            }
        });

        scanPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChooseScanActivity.this, ScanPhotoActivity.class);
                startActivity(intent);
            }
        });
    }

}
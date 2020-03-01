package gr.ict.wallet_analyzer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import gr.ict.wallet_analyzer.Functions.CheckPermissions;
import gr.ict.wallet_analyzer.Functions.TesseractOCR;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static int REQUEST_IMAGE_CAPTURE = 542;
    public ImageView imageView;
    public TextView textView;
    public TesseractOCR tesseractOCR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //   check permissions
        CheckPermissions checkPermissions = new CheckPermissions();
        checkPermissions.checkPermissions(this);

        Button button = findViewById(R.id.scan_button);
        button.setOnClickListener(this);
        imageView = findViewById(R.id.ocr_image);
        textView = findViewById(R.id.ocr_text);


        try {
            tesseractOCR = new TesseractOCR(this, "eng");
        } catch (Exception exception) {
            System.out.print(exception.getMessage());
        }
    }

    private void scanButton() {
        //prepare intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                // TODO: change language dynamically

                Bitmap bmp = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");

                imageView.setImageBitmap(bmp);
                tesseractOCR.doOCR(bmp, textView);
            } catch (Exception ex) {
                Log.i(getClass().getSimpleName(), Objects.requireNonNull(ex.getMessage()));
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        scanButton();
    }
}

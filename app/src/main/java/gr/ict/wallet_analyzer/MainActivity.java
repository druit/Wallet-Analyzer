package gr.ict.wallet_analyzer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import gr.ict.wallet_analyzer.Functions.CheckPermissions;
import gr.ict.wallet_analyzer.Functions.ImageFile;
import gr.ict.wallet_analyzer.Functions.TesseractOCR;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static int REQUEST_IMAGE_CAPTURE = 542;
    public ImageView imageView;
    public TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.scan_button);
        button.setOnClickListener(this);
        imageView = findViewById(R.id.ocr_image);
        textView = findViewById(R.id.ocr_text);

        //   check permissions
        CheckPermissions checkPermissions = new CheckPermissions();
        checkPermissions.checkPermissions(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            Bitmap bmp;

            try {
                // TODO: change language dynamically
                TesseractOCR tesseractOCR = new TesseractOCR(this, "ell");

                File photoFile = new ImageFile(this).createImageFile();
                Uri photoURI = Uri.fromFile(photoFile);

                if (photoURI != null) {
                    InputStream is = this.getContentResolver().openInputStream(photoURI);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    bmp = BitmapFactory.decodeStream(is, null, options);

                    imageView.setImageBitmap(bmp);
                    tesseractOCR.doOCR(bmp, textView);

                    OutputStream os;

                    os = new FileOutputStream(Objects.requireNonNull(photoURI.getPath()));
                    if (bmp != null) {
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
                    }
                    os.flush();
                    os.close();
                }
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

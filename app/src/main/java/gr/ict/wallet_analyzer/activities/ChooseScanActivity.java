package gr.ict.wallet_analyzer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import data_class.History;
import data_class.Item;
import data_class.Receipt;
import gr.ict.wallet_analyzer.R;

public class ChooseScanActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 99;
    String currentPhotoPath;
    HashMap<String, Object> parentKey = new HashMap<>();
    HashMap<String, Double> itemList = new HashMap<>();
    HashMap<String, Object> infoList = new HashMap<>();
    ArrayList<String> barcodeList = new ArrayList<>();
    private Button scanBarcode, scanPhoto;
    private ImageView imageView;
    private Bitmap imageBitmap;
    private Uri photoURI;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_scan);

        scanBarcode = findViewById(R.id.scan_with_barcode);
        scanPhoto = findViewById(R.id.scan_with_photo);
        imageView = findViewById(R.id.ocr_image);

        scanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScanBarcode();
            }
        });

        scanPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        uid = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("history");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    History historyOfUser = child.getValue(History.class);
                    barcodeList.add(historyOfUser.getReceipt().getBarcode());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void startScanBarcode() {
        IntentIntegrator intent = new IntentIntegrator(this);
        intent.setCaptureActivity(CaptureActivity.class);
        intent.setOrientationLocked(false);
        intent.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intent.setPrompt("Scanning Code");
        intent.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }


//            if (photoURI != null) {
//                try {
//                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
//                    imageView.setImageBitmap(imageBitmap);
//                    detectTextFromImage();
//                } catch (IOException e) {
//                    Toast.makeText(this, "Error while capturing Image", Toast.LENGTH_LONG).show();
//                    e.printStackTrace();
//                }
//            } else {
//                Toast.makeText(this, "Error while capturing Image", Toast.LENGTH_LONG).show();
//            }
        } else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (result != null) {
                if (result.getContents() != null) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("barcodes").child(result.getContents());
                    final String barcode = result.getContents();

                    mDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                            for (DataSnapshot child : children) {
                                if (child.hasChildren()) {
                                    for (DataSnapshot ch : child.getChildren()) {
                                        if (child.getKey().contains("items")) {
                                            String value = ch.getValue().toString();
                                            itemList.put(ch.getKey(), Double.valueOf(value));
                                        } else if (child.getKey().contains("info")) {
                                            infoList.put(ch.getKey(), ch.getValue());
                                        }
                                    }
                                } else {
                                    parentKey.put(child.getKey(), child.getValue());
                                }
                            }

                            getBarcodeData(parentKey, infoList, itemList, barcode);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("ERROR", "loadPost:onCancelled", databaseError.toException());
                        }
                    });
                } else {
                    Toast.makeText(this, "No result", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            }
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                64);

        int preference = ScanConstants.OPEN_CAMERA;
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                photoURI = FileProvider.getUriForFile(this,
//                        "gr.ict.wallet_analyzer.fileprovider",
//                        photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//            }
//        }
    }


    private void detectTextFromImage() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        scanBarcode.setEnabled(false);
        detector.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                scanBarcode.setEnabled(true);
                processTextDetectResult(firebaseVisionText);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        scanBarcode.setEnabled(true);
                        Toast.makeText(ChooseScanActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                });
    }

    private void processTextDetectResult(FirebaseVisionText result) {
        List<FirebaseVisionText.TextBlock> blockList = result.getTextBlocks();
        if (blockList.size() == 0) {
            Toast.makeText(this, "No Text Found in Image, please try again", Toast.LENGTH_LONG).show();
        } else {
            for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                String blockText = block.getText();

                if (!blockText.contains("*")) {

                }

                System.out.println("height = " + imageBitmap.getHeight() + ", width = " + imageBitmap.getWidth());
                System.out.println("x = " + block.getCornerPoints()[0].x + ", y = " + block.getCornerPoints()[0].y + " = " + blockText);
            }
        }
    }

    private void getBarcodeData(HashMap<String, Object> parent, HashMap<String, Object> info, HashMap<String, Double> items, String barcode) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

//        CUSTOM DATA

//        Item newItem = new Item("Kalodio", 10.05);
//        List<Item> list = new ArrayList<>();
//        list.add(newItem);
//        list.add(newItem);
//        Date date = new Date();
//        String storeName = "Masoutis";
//        Receipt receipt1 = new Receipt(list, "Alamanas 13", "SuperMarket", "http://test.com", 15.20, "1231321312300", date, storeName);
//        String id = mDatabase.push().getKey();
//        History history = new History(id, receipt1);

        boolean barcodeExists = false;

        for (String barcodeFromList : barcodeList) {
            if (barcode.equals(barcodeFromList)) {
                barcodeExists = true;
                break;
            }
        }

        if (barcodeExists) {
            Toast.makeText(this, "Already have barcode: " + barcode, Toast.LENGTH_LONG).show();
        } else {
            // print message
            Toast.makeText(this, "Receipt Added", Toast.LENGTH_LONG).show();

            // create the object and send it to firebase
            List<Item> list = new ArrayList<>();
            for (Map.Entry<String, Double> item : items.entrySet()) {
                Item newItem = new Item(item.getKey(), item.getValue());
                list.add(newItem);
            }

            Date date;
            try {
                String d = info.get("date").toString();
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                date = format.parse(d);
            } catch (ParseException e) {
                date = new Date();
                e.printStackTrace();
            }
            String storeName = parent.get("shop_name").toString();
            Receipt receipt = new Receipt(list, info.get("address").toString(), info.get("type").toString(),
                    "http://test.com", Double.valueOf(parent.get("final_price").toString()), barcode, date, storeName);
            final String id = mDatabase.push().getKey();
            final History history = new History(id, receipt);

            mDatabase.child("users").child(uid).child("history").child(id).setValue(history);

            // finish activity if successfully scanned barcode
            finish();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private boolean isHorizontallyCentered(Point[] point, int width, int height) {
        final int X_CENTER = width / 2;

        Point topLeftCorner = point[0];
        Point topRightCorner = point[1];

        int middleXValue = (topLeftCorner.x + topRightCorner.x) / 2;

        return X_CENTER - middleXValue >= 20;
    }

    private boolean isOnTopSpace(Point[] point, int height) {
        final int Y_TOP = (int) (height * 0.3); // top 30% of the image

        Point topLeftCorner = point[0];
        Point bottomLeftCorner = point[3];

        int middleYValue = (topLeftCorner.y + bottomLeftCorner.y) / 2;

        return middleYValue <= Y_TOP;
    }

    // if 2 points are parallel on y axis returns true
    private boolean arePointsHParallel(Point[] point1, Point[] point2) {
        return point1[0].y - point2[0].y <= 20;
    }
}


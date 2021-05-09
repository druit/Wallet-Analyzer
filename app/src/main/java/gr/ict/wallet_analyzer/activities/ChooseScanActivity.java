package gr.ict.wallet_analyzer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

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
import me.pqpo.smartcropperlib.view.CropImageView;

public class ChooseScanActivity extends BaseActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final String CROP_STEP = "CROP_STEP";
    private final String INFORMATION_STEP = "INFORMATION_STEP";
    private final String PRODUCT_STEP = "PRODUCT_STEP";
    String currentPhotoPath;
    HashMap<String, Object> parentKey = new HashMap<>();
    HashMap<String, Double> itemList = new HashMap<>();
    HashMap<String, Object> infoList = new HashMap<>();
    ArrayList<String> barcodeList = new ArrayList<>();
    private Button scanBarcode, scanPhoto, finishScanningButton;
    private ImageView imageView;
    private Bitmap imageBitmap;
    private Uri photoURI;
    private String uid;
    private CropImageView ivCrop;
    private TextView recognizedTextView;
    private RelativeLayout mainRelativeLayout, cameraRelativelayout;
    private String cropFlag = this.CROP_STEP;

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_scan);

        scanBarcode = findViewById(R.id.scan_with_barcode);
        scanPhoto = findViewById(R.id.scan_with_photo);
        finishScanningButton = findViewById(R.id.finish_scanning);

        imageView = findViewById(R.id.ocr_image);

        ivCrop = findViewById(R.id.iv_crop);

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

        mainRelativeLayout = findViewById(R.id.main_relative_layout);
        cameraRelativelayout = findViewById(R.id.camera_relative_layout);

        recognizedTextView = findViewById(R.id.recognized_text_view);

        finishScanningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishScanning();
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
            if (photoURI != null) {
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                    if (imageBitmap != null) {
                        imageBitmap = rotateImage(imageBitmap, 90);
                        mainRelativeLayout.setVisibility(View.GONE);
                        cameraRelativelayout.setVisibility(View.VISIBLE);
                        ivCrop.setImageToCrop(imageBitmap);
                    } else {
                        Toast.makeText(this, "No image was captures", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "Error while capturing Image", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Error while capturing Image", Toast.LENGTH_LONG).show();
            }
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

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "gr.ict.wallet_analyzer.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void detectTextFromImage(Bitmap bmp) {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bmp);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        scanBarcode.setEnabled(false);
        detector.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                scanBarcode.setEnabled(true);

                if (cropFlag.equals(INFORMATION_STEP)) {
                    shopInformationRecognition(firebaseVisionText);
                } else if (cropFlag.equals(PRODUCT_STEP)) {
                    productsRecognition(firebaseVisionText);
                }
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

    private void shopInformationRecognition(FirebaseVisionText result) {
        List<FirebaseVisionText.TextBlock> blockList = result.getTextBlocks();
        if (blockList.size() == 0) {
            Toast.makeText(this, "No Text Found in Image, please try again", Toast.LENGTH_LONG).show();
        } else {
            ArrayList<FirebaseVisionText.TextBlock> discoveredStringSets = new ArrayList<>();
            discoveredStringSets.addAll(result.getTextBlocks());

            for (FirebaseVisionText.TextBlock block : discoveredStringSets) {
                String blockText = block.getText();

                String[] lines = blockText.split("\\r?\\n");
                for (String line : lines) {

                }
            }
            cropFlag = PRODUCT_STEP;
        }
    }

    private void productsRecognition(FirebaseVisionText result) {
        List<FirebaseVisionText.TextBlock> blockList = result.getTextBlocks();
        if (blockList.size() == 0) {
            Toast.makeText(this, "No Text Found in Image, please try again", Toast.LENGTH_LONG).show();
        } else {
            ArrayList<FirebaseVisionText.TextBlock> discoveredStringSets = new ArrayList<>(result.getTextBlocks());

            HashMap<String, Double> matchedMap = new HashMap<>();

            ArrayList<String> lastWords = new ArrayList<>();
            ArrayList<Double> lastDoubles = new ArrayList<>();

            for (FirebaseVisionText.TextBlock block : discoveredStringSets) {
                String blockText = block.getText();

                String[] lines = blockText.split("\\r?\\n");
                for (String line : lines) {
                    try {
                        Double lastDouble = Double.valueOf(line);

                        if (!lastWords.isEmpty()) {
                            String matchedWord = lastWords.get(0);
                            lastWords.remove(0);
                            matchedMap.put(matchedWord, lastDouble);
                        } else {
                            lastDoubles.add(lastDouble);
                        }
                    } catch (NumberFormatException ex) {
                        if (!lastDoubles.isEmpty()) {
                            Double matchedDouble = lastDoubles.get(0);
                            lastDoubles.remove(0);
                            matchedMap.put(line, matchedDouble);
                        } else {
                            lastWords.add(line);
                        }
                    }
                }
            }

            int i = 0;
            for (Map.Entry<String, Double> entry : matchedMap.entrySet()) {
                String key = entry.getKey();
                Double value = entry.getValue();

                System.out.println("entry: " + i);
                System.out.println("product = " + key);
                System.out.println("price = " + value);
                i++;
            }

            cameraScan(matchedMap);
        }
    }

    private boolean isSameLine(FirebaseVisionText.TextBlock currentBlock, FirebaseVisionText.TextBlock previousBlock) {
        int height = currentBlock.getBoundingBox().height();

        int previousBlockHeight = previousBlock.getBoundingBox().height();

        int currentMeanY = currentBlock.getCornerPoints()[0].y - height / 2;
        int previousMeanY = previousBlock.getCornerPoints()[0].y + previousBlockHeight / 2;

        return Math.abs(currentMeanY - previousMeanY) >= 5;
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

    /*
     * in cropFlag case 0 we have the initial crop for the image to seem like document
     * 1: select shop information
     * 2: select products
     * 3: select whatever
     */
    private void finishScanning() {
        if (ivCrop != null) {
            Bitmap crop = ivCrop.crop();

            switch (cropFlag) {
                case CROP_STEP:
                    ivCrop.setImageBitmap(crop);
                    Point[] points = {
                            new Point(10, 10),
                            new Point(crop.getWidth() - 10, 10),
                            new Point(crop.getWidth() - 10, crop.getHeight() - 10),
                            new Point(10, crop.getHeight() - 10)
                    };
                    ivCrop.setCropPoints(points);
                    recognizedTextView.setText("Select Information");
                    cropFlag = INFORMATION_STEP;
                    break;
                case INFORMATION_STEP:
                    detectTextFromImage(crop);
                    recognizedTextView.setText("Select Products");
                    break;
                case PRODUCT_STEP:
                    recognizedTextView.setText("Hmmm");
                    detectTextFromImage(crop);
//                    cropFlag = 3;
                    break;
//                case; 3:
////                    recognizedTextView.setText("Select Date");
////                    detectTextFromImage(crop);
////                    cropFlag++;
////                    break
            }

//            imageView.setImageBitmap(crop);
//            ivCrop.setVisibility(View.GONE);
//            detectTextFromImage(crop);
        } else {
            Toast.makeText(ChooseScanActivity.this, "Image capture error", Toast.LENGTH_LONG).show();
        }
    }

    public void cameraScan(HashMap<String, Double> matchedMap) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        // create the object and send it to firebase
        List<Item> list = new ArrayList<>();
        double finalPrice = 0;
        for (Map.Entry<String, Double> item : matchedMap.entrySet()) {
            Item newItem = new Item(item.getKey(), item.getValue());
            list.add(newItem);
            finalPrice += item.getValue();
        }

        // TODO: date
        Date date;
        try {
            String d = "1619155851";
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            date = format.parse(d);
        } catch (ParseException e) {
            date = new Date();
            e.printStackTrace();
        }

        // TODO: address
        String address = "address";

        // TODO: store name
        String storeName = "store name";

        // TODO: store type;
        String storeType = "shop type";

        // TODO: image;
        String imagePath = "image path";

        final String id = mDatabase.push().getKey();

        Receipt receipt = new Receipt(list, address, storeType, imagePath, finalPrice, id, date, storeName);
        final History history = new History(id, receipt);

        mDatabase.child("users").child(uid).child("history").child(id).setValue(history);

        // print message
        Toast.makeText(this, "Receipt Added", Toast.LENGTH_LONG).show();
    }
}


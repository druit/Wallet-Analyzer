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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import data_class.History;
import data_class.Item;
import data_class.Receipt;
import gr.ict.wallet_analyzer.R;
import gr.ict.wallet_analyzer.helpers.ReceiptBubbleChoice;
import me.pqpo.smartcropperlib.view.CropImageView;

public class ChooseScanActivity extends BaseActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public final static String CROP_STEP = "CROP_STEP";

    public final static String SHOP_NAME_STEP = "INFORMATION_STEP";
    public final static String LOCATION_STEP = "LOCATION_STEP";
    public final static String PRODUCT_STEP = "PRODUCT_STEP";

    public static String cropFlag = CROP_STEP;

    private final ArrayList<Item> itemsArrayList = new ArrayList<>();

    String currentPhotoPath;

    HashMap<String, Object> parentKey = new HashMap<>();
    HashMap<String, Double> itemList = new HashMap<>();
    HashMap<String, Object> infoList = new HashMap<>();
    ArrayList<String> barcodeList = new ArrayList<>();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    DatabaseReference baseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

    private Button scanBarcode, scanPhoto, finishScanningButton;
    private ImageView imageView;
    private Bitmap imageBitmap;

    private Uri photoURI;
    private String uid;
    private CropImageView ivCrop;
    private TextView recognizedTextView;
    private RelativeLayout mainRelativeLayout, cameraRelativeLayout;

    private History history;
    private double finalPrice = 0;
    private Date date;
    public static String shopName = "";
    public static String shopAddress = "";

    private static Bitmap rotateImage(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) 90);
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
        cameraRelativeLayout = findViewById(R.id.camera_relative_layout);

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
                        imageBitmap = rotateImage(imageBitmap);
                        mainRelativeLayout.setVisibility(View.GONE);
                        cameraRelativeLayout.setVisibility(View.VISIBLE);
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

                switch (cropFlag) {
                    case SHOP_NAME_STEP:
                        shopInformationRecognition(firebaseVisionText);
                        break;
                    case PRODUCT_STEP:
                        productsRecognition(firebaseVisionText);
                        break;
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
            ArrayList<String> allWords = returnWordArray(result);
            ReceiptBubbleChoice bubbleChoicePopup = new ReceiptBubbleChoice(this, allWords, finishScanningButton);
        }
    }

    private ArrayList<String> returnWordArray(FirebaseVisionText result) {
        ArrayList<String> allWords = new ArrayList<>();
        for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            String[] lines = blockText.split("\\r?\\n");
            allWords.addAll(Arrays.asList(lines));
        }
        return allWords;
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
            populateHistoryWithProducts(matchedMap);
            showDatePickerPopup(imageView);
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

    /*
     * in cropFlag case 0 we have the initial crop for the image to seem like document
     * 1: select shop information
     * 2: select products
     * 3: select location
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
                    cropFlag = SHOP_NAME_STEP;
                    break;
                case SHOP_NAME_STEP:
                    detectTextFromImage(crop);
                    recognizedTextView.setText("Select Products");
                    break;
                case PRODUCT_STEP:
                    detectTextFromImage(crop);
                    recognizedTextView.setText("Select Date");
                    break;
            }
        } else {
            Toast.makeText(ChooseScanActivity.this, "Image capture error", Toast.LENGTH_LONG).show();
        }
    }

    private void populateHistoryWithProducts(HashMap<String, Double> matchedMap) {
        // create the object and send it to firebase
        itemsArrayList.clear();
        finalPrice = 0;
        for (Map.Entry<String, Double> item : matchedMap.entrySet()) {
            Item newItem = new Item(item.getKey(), item.getValue());
            itemsArrayList.add(newItem);
            finalPrice += item.getValue();
        }
    }

    private void showDatePickerPopup(View viewToShowAt) {
        // inflate the layout of the popup window`
        LayoutInflater genericInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View datePickerView = genericInflater.inflate(R.layout.datepicker_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        final PopupWindow datePickerPopup = new PopupWindow(datePickerView, width, height, true);

        final DatePicker datePicker = datePickerView.findViewById(R.id.date_picker_widget);
        Button finishButton = datePickerView.findViewById(R.id.finish_property_edit_button);

        // show the popup window
        datePickerPopup.showAtLocation(viewToShowAt, Gravity.CENTER, 0, 0);

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();
                date = new GregorianCalendar(year, month, day).getTime();

                datePickerPopup.dismiss();
                createHistoryObject();
            }
        });
    }

    private void createHistoryObject() {
        final String id = baseReference.push().getKey();

        // TODO: store type;
        String storeType = "shop type";

        // TODO: image;
        String imagePath = "image path";

        Receipt receipt = new Receipt(itemsArrayList, shopAddress.trim(), storeType, imagePath, finalPrice, id, date, shopName.trim());
        final History history = new History(id, receipt);

        baseReference.child("history").child(id).setValue(history);

        // print message
        Toast.makeText(this, "Receipt Added", Toast.LENGTH_LONG).show();
        this.finish();
    }
}


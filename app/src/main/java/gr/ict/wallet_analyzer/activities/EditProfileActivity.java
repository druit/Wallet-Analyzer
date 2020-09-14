package gr.ict.wallet_analyzer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import data_class.CircleTransform;
import gr.ict.wallet_analyzer.R;
import settings.Language;

public class EditProfileActivity extends BaseActivity {

    private static final int GALLERY_REQUEST_CODE = 123;
    private static final int REQUEST_IMAGE_CAPTURE = 1;;
    Uri mUri;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

//    FirebaseStorage storage = FirebaseStorage.getInstance();
//    StorageReference storageRef = storage.getReference();
    StorageReference Folder;

    Button saveBtn,languageBtn;
    ImageButton editAvatar,openCamera;
    EditText firstName, lastName;
    TextView emailTextView;
    ImageView profileImage;
    Uri imageDAta;

    String language = Prefs.getString("My_Lang","en");

    UserProfileChangeRequest profileUpdates;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_edit_profile);
        Folder = FirebaseStorage.getInstance().getReference("Avatars");

                //change actionbar title, if you don't change it will be according to your systems default language
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setTitle(getResources().getString(R.string.app_name));


        saveBtn = findViewById(R.id.saveBtn);
        languageBtn = findViewById(R.id.changeLanguage);
        editAvatar = findViewById(R.id.edit_image);
        openCamera = findViewById(R.id.edit_image_camera);

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);

        profileImage = findViewById(R.id.profileImage);

        emailTextView = findViewById(R.id.profileEmail);

        emailTextView.setText(user.getEmail());

        String userName = user.getDisplayName();
        if (userName != null) {
            if (!userName.isEmpty()) {
                int firstSpace = userName.indexOf(" "); // detect the first space character
                String first = userName.substring(0, firstSpace);  // get everything upto the first space character
                String last = userName.substring(firstSpace).trim();

                firstName.setText(first);
                lastName.setText(last);
            }
        }
        if(user.getPhotoUrl() != null){
            Picasso.get().load( user.getPhotoUrl()).transform(new CircleTransform()).into(profileImage);
//            profileImage.setImageURI(user.getPhotoUrl());
//            setProfileImage(uri);
        }else {
            profileImage.setImageResource(R.drawable.guest);
        }


        languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogChangeLanguage();
            }
        });
        
        saveBtn.setOnClickListener(new View.OnClickListener() {
            Uri downloadUri = null;
            @Override
            public void onClick(View view) {

                if (user != null) {
                    name = (firstName.getText().toString() + " " + lastName.getText().toString());
                    // TODO: image Uri from galery,camera.
                    profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name)
                            .setPhotoUri(user.getPhotoUrl())
                            .build();
                    if(imageDAta != null){

                        profileImage.setDrawingCacheEnabled(true);
                        profileImage.buildDrawingCache();
                        Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                        byte[] data = baos.toByteArray();

//                        UploadTask uploadTask = storageRef.putBytes(data);

                        Uri file = imageDAta;
                        final StorageReference riversRef = Folder.child(System.currentTimeMillis()+"."+getExtension(imageDAta));
                        UploadTask uploadTask = riversRef.putFile(file);

                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                            }
                        });


                        uploadTask = riversRef.putFile(file);

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return riversRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    downloadUri = task.getResult();
                                    profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name)
                                            .setPhotoUri(downloadUri)
                                            .build();
                                    if(downloadUri != null)  updateProfileChanges();

                                } else {
                                    // Handle failures
                                    // ...
                                }
                            }
                        });

                    }

                    updateProfileChanges();
                }
            }
        });

        editAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Pick an image"),GALLERY_REQUEST_CODE);
            }
        });

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                askCameraPermissions();
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (camera.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(camera, REQUEST_IMAGE_CAPTURE );
                }
            }
        });
    }

    private void updateProfileChanges() {

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "User Profile updated", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case GALLERY_REQUEST_CODE :
                if(resultCode == RESULT_OK){
                    setProfileImage(data.getData());
                }
                break;
            case REQUEST_IMAGE_CAPTURE :
                if(resultCode == RESULT_OK){
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Glide.with(this)
                            .load(stream.toByteArray())
                            .apply(RequestOptions.circleCropTransform())
                            .into(profileImage);

                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    Uri tempUri = getImageUri(getApplicationContext(), image);
                    setProfileImage(tempUri);
                }
                break;
            default:
                break;
        }
    }

    private Uri getImageUri(Context applicationContext, Bitmap imageBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), imageBitmap, "Title", null);
        return Uri.parse(path);
    }

    private void setProfileImage(Uri data) {
        Picasso.get().load(data).transform(new CircleTransform()).into(profileImage);
        imageDAta = data;
    };

    private void openDialogChangeLanguage() {
        final String[] listItems = { getString(R.string.gen_en), getString(R.string.gen_el) };

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle(getString(R.string.gen_select_language));

        int lang;

        if (language.contains("el")){
            lang = 1;
        }else{
            lang = 0;
        }


        mBuilder.setSingleChoiceItems(listItems,lang, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    //setLocale("en");
                    Prefs.putString("My_Lang","en");
//                    language.setCurrentLanguage("en");
                    //recreate();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                else if(which == 1){
                    //setLocale("el");
                    Prefs.putString("My_Lang","el");
//                    language.setCurrentLanguage("el");
                    //recreate();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();

    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
        //save data to shared preferences
        //SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        Prefs.putString("My_Lang",lang);
    }
    // load language saved in shared preferences
    public void loadLocale(){
        String language = Prefs.getString("My_Lang","");
        setLocale(language);
    }

    private String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

}
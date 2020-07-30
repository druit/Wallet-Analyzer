package gr.ict.wallet_analyzer.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import data_class.CircleTransform;
import gr.ict.wallet_analyzer.R;
import settings.Language;

public class EditProfileActivity extends BaseActivity {

    private static final int GALLERY_REQUEST_CODE = 123;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
//    StorageReference Folder;
    Button saveBtn,languageBtn;
    ImageButton editAvatar;
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


        //change actionbar title, if you don't change it will be according to your systems default language
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setTitle(getResources().getString(R.string.app_name));


        saveBtn = findViewById(R.id.saveBtn);
        languageBtn = findViewById(R.id.changeLanguage);
        editAvatar = findViewById(R.id.edit_image);

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
                        final StorageReference riversRef = storageRef.child("avatars/"+file.getLastPathSegment());
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            setProfileImage(data.getData());
        }
    }

    private void setProfileImage(Uri data) {
        Picasso.get().load(data).transform(new CircleTransform()).into(profileImage);
//        imageDAta = data;
//        try {
//            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageDAta);
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            Glide.with(this)
//                    .load(stream.toByteArray())
//                    .apply(RequestOptions.circleCropTransform())
//                    .into(profileImage);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    };

    private void openDialogChangeLanguage() {
        final String[] listItems = { getString(R.string.gen_en), getString(R.string.gen_el) };

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle(getString(R.string.gen_select_language));
//        switch (language){
//            case "el":
//                mBuilder.setTitle("Επέλεξε γλώσσα:");
//                break;
//            case "en":
//                mBuilder.setTitle("Choose Language:");
//                break;
//            default:
//                break;
//        }


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

}

package gr.ict.wallet_analyzer.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Locale;

import gr.ict.wallet_analyzer.R;
import settings.Language;

public class EditProfileActivity extends BaseActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    Button saveBtn,languageBtn;
    EditText firstName, lastName;
    TextView emailTextView;
    ImageView profileImage;

    String language = Prefs.getString("My_Lang","en");


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

        profileImage.setImageResource(R.drawable.guest);


        languageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 openDialogChangeLanguage();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    String name = (firstName.getText().toString() + " " + lastName.getText().toString());
                    // TODO: image Uri from galery,camera.
                    Uri uriImg = Uri.parse("https://www.pngitem.com/pimgs/m/279-2799324_transparent-guest-png-become-a-member-svg-icon.png");
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name)
                            .setPhotoUri(uriImg)
                            .build();
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
            }
        });
    }

    private void openDialogChangeLanguage() {
        final String[] listItems = {"EN","EL"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        switch (language){
            case "el":
                mBuilder.setTitle("Επέλεξε γλώσσα:");
                break;
            case "en":
                mBuilder.setTitle("Choose Language:");
                break;
            default:
                break;
        }


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
                    recreate();
                    /*Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();*/
                }
                else if(which == 1){
                    //setLocale("el");
                    Prefs.putString("My_Lang","el");
//                    language.setCurrentLanguage("el");
                    recreate();
                    /*Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();*/
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
        //save data to sared preferences
//        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        Prefs.putString("My_Lang",lang);
    }
    // load language saved in shared preferences
    public void loadLocale(){
        String language = Prefs.getString("My_Lang","");
        setLocale(language);
    }

}

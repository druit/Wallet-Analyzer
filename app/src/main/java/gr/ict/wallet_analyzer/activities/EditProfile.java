package gr.ict.wallet_analyzer.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import gr.ict.wallet_analyzer.R;

public class EditProfile extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    Button saveBtn;
    TextView firstname,lastname;
    ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        saveBtn = findViewById(R.id.saveBtn);
        firstname = findViewById(R.id.firstName);
        lastname = findViewById(R.id.lastName);
        profileImage = findViewById(R.id.profileImage);


        int firstSpace =  user.getDisplayName().indexOf(" "); // detect the first space character
        String first =  user.getDisplayName().substring(0, firstSpace);  // get everything upto the first space character
        String last = user.getDisplayName().substring(firstSpace).trim();

        firstname.setText(first);
        lastname.setText(last);

        profileImage.setImageResource(R.drawable.guest);



        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user != null) {
                    String name = (firstname.getText().toString() + " " + lastname.getText().toString());
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
                                        Toast.makeText(EditProfile.this, "User Profile updated", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                }
                            });
                }
            }
        });


//        String test = user.getDisplayName();
//        Log.d("TEST",test);
    }
}

package gr.ict.wallet_analyzer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {


    private EditText email, password;
    private Button login;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();

//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                if(firebaseAuth.getCurrentUser() == null){
//
//                }
//            }
//        };

        login.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signIn();
                    }
                });

    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        Toast.makeText(this, "XAXA", Toast.LENGTH_SHORT).show();
////        updateUI(currentUser);
    }

    private void signIn() {
        String mail = email.getText().toString();
        String pass = password.getText().toString();

        if (TextUtils.isEmpty(mail) || TextUtils.isEmpty((pass))) {
            Toast.makeText(Login.this, "Field are empty.", Toast.LENGTH_LONG).show();
        }
        mAuth.signInWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(Login.this, "Something went wrong", Toast.LENGTH_LONG).show();
                } else {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user.isEmailVerified()) {
                        Log.d("MY_USER:", user.getUid());
                        startActivity(new Intent(Login.this, Home.class));
                        Toast.makeText(Login.this, "Logged in successful.", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(Login.this, "Please verify your email.", Toast.LENGTH_LONG).show();
                    }
//                  updateUI(user);
                }
            }
        });
    }


}

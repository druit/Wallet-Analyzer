package gr.ict.wallet_analyzer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import data_class.User;
import gr.ict.wallet_analyzer.R;

public class LoginActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    private ProgressBar loadingProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        final EditText emailEditText = findViewById(R.id.email);
        final EditText passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);

        loadingProgressBar = findViewById(R.id.loading);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    signIn(emailEditText.getText().toString(), passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                signIn(emailEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });

        TextView forgotPass  = findViewById(R.id.forgotPass);
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        TextView notAUserText = findViewById(R.id.not_a_user_text);
        notAUserText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void updateUiWithUser(FirebaseUser user) {
        // TODO : initiate successful logged in experience
        Toast.makeText(LoginActivity.this, getApplicationContext().getResources().getString(R.string.login_success_message), Toast.LENGTH_LONG).show();
        loadingProgressBar.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(this, MainActivity.class);


        startActivity(intent);
    }

    private void showLoginFailed() {
        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        loadingProgressBar.setVisibility(View.INVISIBLE);
    }

    private void signIn(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty((password))) {
            Toast.makeText(LoginActivity.this, "Field are empty.", Toast.LENGTH_LONG).show();
            loadingProgressBar.setVisibility(View.INVISIBLE);
        }else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        showLoginFailed();
                    } else {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null && user.isEmailVerified()) {
                            Log.d("MY_USER:", user.getUid());
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            updateUiWithUser(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Please verify your email.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            updateUiWithUser(currentUser);
        }
    }
}

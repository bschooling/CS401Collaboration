package com.example.cs401collaboration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.cs401collaboration.DatabaseService;

public class LoginActivity extends AppCompatActivity
{
    /* Firebase Auth */
    private FirebaseAuth mAuth;

    /* Database */
    private DatabaseService mDB;

    /* Handlers for UI elements */
    Button btLogin;
    Button btSignup;
    EditText etEmail;
    EditText etPassword;
    EditText etName;

    /* State */
    boolean signupBtClicked = false;

    /* Log tag */
    private final String LOG_TAG_MAIN = "LoginActivity";

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDB = DatabaseService.getInstance();

        /* Handle UI elements */
        btLogin = (Button) findViewById(R.id.bt_loginact_login);
        btSignup = (Button) findViewById(R.id.bt_loginact_signup);
        etEmail = (EditText) findViewById(R.id.et_loginact_email);
        etPassword = (EditText) findViewById(R.id.etp_loginact_password);
        etName = (EditText) findViewById(R.id.et_loginact_name);

        /* Attach button OnClickListeners */
        btLogin.setOnClickListener(new btLoginOnClickListener());
        btSignup.setOnClickListener(new btSignupOnClickListener());
    }

    /**
     * Handles onClick of Login button.
     *
     * Check if fields are null, try to login, and either toast failure
     * or return to calling activity.
     *
     * @author Arshdeep Padda
     */
    private class btLoginOnClickListener implements View.OnClickListener
    {
        public void onClick (View v)
        {
            // Clear signup-specific fields
            signupBtClicked = false;
            etName.setText("");
            etName.setVisibility(View.GONE);

            // Get fields
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // Check if null, abort if so.
            if (email.isEmpty() || password.isEmpty())
            {
                Toast.makeText (
                        LoginActivity.this,
                        "Fields cannot be empty.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            // Attempt login
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // Success: return to calling activity
                            Log.d(LOG_TAG_MAIN, "signInWithEmail:success");
                            finish();
                        }
                        else
                        {
                            // Failure: report
                            Log.w(LOG_TAG_MAIN, "signInWithEmail:failure", task.getException());
                            Toast.makeText (
                                    LoginActivity.this,
                                    "Could not log you in",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
        }
    }

    /**
     * Handles onClick of Signup button.
     *
     * Check if fields are null, password length is good, tries to create user,
     * and either toast failure or return to calling activity upon successful creation
     * and login of new user.
     *
     * @author Arshdeep Padda
     */
    private class btSignupOnClickListener implements View.OnClickListener
    {
        public void onClick (View v)
        {
            if (!signupBtClicked)
            {
                signupBtClicked = true;
                etName.setVisibility(View.VISIBLE);
                return;
            }

            // Get email and password
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            String name = etName.getText().toString();

            // Ensure neither field is empty, and password of acceptable length.
            // Abort otherwise.
            if (email.isEmpty() || password.isEmpty() || name.isEmpty())
            {
                Toast.makeText (
                        LoginActivity.this,
                        "Fields cannot be empty.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }
            if (password.length() < 7)
            {
                Toast.makeText (
                        LoginActivity.this,
                        "Password must be at least 6 characters long.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            // Attempt Creation
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                // Success: return to calling activity with user logged in
                                Log.d(LOG_TAG_MAIN, "createUserWithEmail:success");
                                mDB.createUser(mAuth.getUid(), name);
                                finish();
                            }
                            else
                            {
                                // Failure: report
                                Log.d(LOG_TAG_MAIN, "createUserWithEmail:failure", task.getException());
                                Log.d(LOG_TAG_MAIN, task.getException().getLocalizedMessage());
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Unable to authenticate.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                    });
        }
    }

}

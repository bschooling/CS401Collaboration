package com.example.cs401collaboration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeScreenActivity extends AppCompatActivity
{

    /* Firebase Auth */
    private FirebaseAuth mAuth;

    private String LOG_TAG_MAIN = "HomeScreenActivity";

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Check if user is logged in. Direct to login screen if not.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FirebaseAuth.getInstance().signOut();

        if(currentUser == null)
        {
            Log.d(LOG_TAG_MAIN, "onCreate: User not logged in");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else Log.d(LOG_TAG_MAIN, "onCreate: User logged in");
    }

    @Override
    protected void onStart ()
    {
        super.onStart();

        if (mAuth.getCurrentUser() == null) return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            Log.d(LOG_TAG_MAIN, "onStart: User logged in");
        else
            Log.d(LOG_TAG_MAIN, "onStart: User not logged in");
    }

}

package com.example.cs401collaboration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeScreenActivity extends AppCompatActivity
{

    /* Firebase Auth */
    private FirebaseAuth mAuth;

    private final String LOG_TAG_MAIN = "HomeScreenActivity";

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

    /* Setup Menu */
    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.homescreen_menu, menu);
        return true;
    }

    /* Handle Menu Item Clicks */
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (item.getItemId() == R.id.miLogout)
        {
            Log.d(LOG_TAG_MAIN, "onOptionsItemSelected: logout option selected");
            this.mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            Log.d (
                    LOG_TAG_MAIN,
                    "onOptionsItemSelected: logging out, sending to login screen"
            );
            return true;
        }

        Log.d(LOG_TAG_MAIN, "onOptionsItemSelected: default triggered");
        return super.onOptionsItemSelected(item);
    }

}

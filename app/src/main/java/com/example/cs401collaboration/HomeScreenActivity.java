package com.example.cs401collaboration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.cs401collaboration.DatabaseService;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.interfaces.OnCollectionsRetrievedCallback;

import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.rvAdapters.CollectionRvAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeScreenActivity extends AppCompatActivity
{

    /* Firebase Auth */
    private FirebaseAuth mAuth;

    /* Database */
    private DatabaseService mDB;

    /* UI Element Handlers */
    private RecyclerView collectionRView;
    private FloatingActionButton fab;

    private final String LOG_TAG_MAIN = "HomeScreenActivity";

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Check if user is logged in. Direct to login screen if not.
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            Log.d(LOG_TAG_MAIN, "onCreate: User not logged in");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        else Log.d(LOG_TAG_MAIN, "onCreate: User logged in");

        mDB = DatabaseService.getInstance();

        fab = findViewById(R.id.floatingActionButton);
    }

    @Override
    protected void onStart ()
    {
        super.onStart();

        if (mAuth.getCurrentUser() == null) return;

        fab.setOnClickListener(new fabOnClickListener());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            Log.d(LOG_TAG_MAIN, "onStart: User logged in");
        else
            Log.d(LOG_TAG_MAIN, "onStart: User not logged in");

        mDB.getCollections(null, new OnSuccessListener<ArrayList<Collection>>() {
            @Override
            public void onSuccess(ArrayList<Collection> collections) {
                // Populate retrieved collections on home screen rv
                collectionRView = findViewById(R.id.collection_view_rv);
                CollectionRvAdapter collectionRvAdapter = new CollectionRvAdapter(HomeScreenActivity.this, collections);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 2);
                collectionRView.setLayoutManager(gridLayoutManager);
                collectionRView.setAdapter(collectionRvAdapter);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText (
                        HomeScreenActivity.this,
                        "Unable to retrieve collections",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

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

    /* FAB On Click */
    private class fabOnClickListener implements View.OnClickListener
    {
        public void onClick (View v)
        {
            Log.d(LOG_TAG_MAIN, "fabOnClickListener: directing to NewCollectionActivity");
            Intent intent = new Intent (
                    HomeScreenActivity.this,
                    NewCollectionActivity.class
            );
            intent.putExtra("collectionID", "");
            startActivity(intent);
        }
    }

}

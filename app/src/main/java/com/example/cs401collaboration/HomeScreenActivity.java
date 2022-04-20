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

import com.example.cs401collaboration.DatabaseService;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.interfaces.OnCollectionsRetrievedCallback;

import com.example.cs401collaboration.rvAdapters.CollectionRvAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeScreenActivity extends AppCompatActivity
{

    /* Firebase Auth */
    private FirebaseAuth mAuth;

    /* Database */
    private DatabaseService mDB;

    /* recycler view for displaying collections*/
    private RecyclerView collectionRView;

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

        mDB.getHomeCollections(new OnSuccessListener<ArrayList<Collection>>() {
            @Override
            public void onSuccess(ArrayList<Collection> collections) {
                for (Collection collection : collections) {
                    Log.d(
                            LOG_TAG_MAIN,
                            "start:OnCollectionsRetrievedCallback: " +
                            collection.getDocID() + " " + collection.getOwner() + " " +
                            collection.getName() + " " +  collection.getDescription() + " " +
                            collection.getLocation()
                    );
                }

                // rView handler
                collectionRView = findViewById(R.id.collection_view_rv);
                CollectionRvAdapter collectionRvAdapter = new CollectionRvAdapter(HomeScreenActivity.this, collections);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 2);
                collectionRView.setLayoutManager(gridLayoutManager);
                collectionRView.setAdapter(collectionRvAdapter);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

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

}

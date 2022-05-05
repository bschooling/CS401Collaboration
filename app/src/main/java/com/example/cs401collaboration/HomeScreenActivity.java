package com.example.cs401collaboration;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.Adapters.EntityRvAdapter;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.model.Item;
import com.example.cs401collaboration.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Class for the Main Home Screen Activity
 * Displays root collections for user
 *
 */
public class HomeScreenActivity extends AppCompatActivity
{

    /* Firebase Auth */
    private FirebaseAuth mAuth;

    /* Database */
    private DatabaseService mDB;

    /* UI Element Handlers */
    private RecyclerView collectionRView;
    private FloatingActionButton fab;

    /** Flag: isFirstLaunch of application */
    Boolean isFirstLaunch = true;

    private final String LOG_TAG_MAIN = "HomeScreenActivity";

    /**
     * Instance to User. Represents logged in user, set during onStart.
     */
    private User user;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        /* Handle providers, ui elements */
        mDB = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        fab = findViewById(R.id.floatingActionButton);

        // Check First Launch
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        this.isFirstLaunch = sharedPref.getBoolean("isFirstLaunch", true);
        if (isFirstLaunch)
        {
            Log.d(LOG_TAG_MAIN, "IS_FIRST_LAUNCH");

            startActivity(new Intent(this, OnboardActivity.class));

            // Set flag -- first launch reached
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isFirstLaunch", false);
            editor.apply();
            return;
        }

        // Check if user is logged in. Direct to login screen if not.
        if (currentUser == null)
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

        // Get User From DB and Assign to this.user
        if (this.user == null)
        {
            mDB.getUser(mAuth.getUid(), new OnSuccessListener<User>() {
                @Override
                public void onSuccess(User user) {
                    HomeScreenActivity.this.user = user;
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

        fab.setOnClickListener(new fabOnClickListener());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
            Log.d(LOG_TAG_MAIN, "onStart: User logged in");
        else
            Log.d(LOG_TAG_MAIN, "onStart: User not logged in");

        // get the initial collections for signed in user
        mDB.getCollections(null, new OnSuccessListener<ArrayList<Collection>>() {
            @Override
            public void onSuccess(ArrayList<Collection> collections) {
                ArrayList<Entity> entityList = new ArrayList<>();
                // convert collections found to entity type
                for (Collection collection : collections)
                {
                    Entity entity = new Entity(
                            collection.getName(),
                            collection.getLocation(),
                            collection.getImageResourceID(),
                            collection.getDocID(),
                            Entity.TYPE_COLLECTION
                    );
                    entity.extras.put("isOwned", String.valueOf(collection.getOwner().getId().equals(mAuth.getCurrentUser().getUid())));
                    entityList.add(entity);
                }
                // Populate retrieved collections on home screen rv
                collectionRView = findViewById(R.id.collection_view_rv);
                EntityRvAdapter entityRvAdapter = new EntityRvAdapter(HomeScreenActivity.this, entityList);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(HomeScreenActivity.this, 2);
                collectionRView.setLayoutManager(gridLayoutManager);
                collectionRView.setAdapter(entityRvAdapter);
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

    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
        // Populate Menu Items for Greeting and Sign Out with dynamic user name and email

        if (user == null) return super.onPrepareOptionsMenu(menu);

        MenuItem itemGreeting = menu.findItem(R.id.miGreeting);
        itemGreeting.setTitle("Welcome, " + user.getName() + "!");

        MenuItem itemLogout = menu.findItem(R.id.miLogout);
        itemLogout.setTitle("Sign Out (" + user.getEmail() + ")");

        return super.onPrepareOptionsMenu(menu);
    }

    /* Handle Menu Item Clicks */
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (item.getItemId() == R.id.miLogout)
        {
            Log.d(LOG_TAG_MAIN, "onOptionsItemSelected: logout option selected");
            this.user = null;
            this.mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            Log.d (
                    LOG_TAG_MAIN,
                    "onOptionsItemSelected: logging out, sending to login screen"
            );
            return true;
        }

        else if (item.getItemId() == R.id.miScanQR)
        {
            Log.d(LOG_TAG_MAIN, "onOptionsItemSelected: ScanQR option selected");

            Intent scanIntent = new Intent(HomeScreenActivity.this, QRScanActivity.class);
            scanIntent.putExtra("RequestCode", QRScanActivity.QR_REQUEST);

            startActivityForResult(scanIntent, QRScanActivity.QR_REQUEST);

            return true;
        }

        Log.d(LOG_TAG_MAIN, "onOptionsItemSelected: default triggered");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String resultString;
        String entityID;
        String type;

        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOG_TAG_MAIN, "ResultCode from QRScan: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == QRScanActivity.QR_REQUEST) {
                Log.d(LOG_TAG_MAIN, "ResultString available: " + data.hasExtra("ResultString"));
                Log.d(LOG_TAG_MAIN, "Result of ScanQR Intent: " + data.getStringExtra("ResultString"));

                resultString = data.getStringExtra("ResultString");
                type = resultString.substring(0, 1);
                entityID = resultString.substring(2);

                if (type.equals("c"))
                {
                    Intent entityIntent = new Intent(HomeScreenActivity.this, CollectionViewActivity.class);
                    entityIntent.putExtra("entity_clicked_id", entityID);
                    startActivity(entityIntent);
                }
                else if (type.equals("i"))
                {
                    Toast errorToast = Toast.makeText (HomeScreenActivity.this,
                                    "An error occurred.",
                                    Toast.LENGTH_LONG);
                    mDB.getItem(entityID, new OnSuccessListener<Item>() {
                        @Override
                        public void onSuccess(Item item) {
                            mDB.getCollection(item.getParentCollection().getId(), new OnSuccessListener<Collection>() {
                                @Override
                                public void onSuccess(Collection collection) {
                                    Intent entityIntent = new Intent(HomeScreenActivity.this, ItemViewActivity.class);
                                    entityIntent.putExtra("entity_clicked_id", entityID);
                                    startActivity(entityIntent);
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (e.getMessage().equals("UserInvalidPermissions"))
                                        errorToast.setText("Invalid Code");
                                    errorToast.show();
                                }
                            });
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e.getMessage().equals("ItemDoesNotExist"))
                                errorToast.setText("Invalid Code");
                            errorToast.show();
                        }
                    });
                }
                else
                {
                    Log.d(LOG_TAG_MAIN, "Scanned QR Label has invalid tag");
                    Toast.makeText (HomeScreenActivity.this,
                            "Invalid QR Code",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        }

        else
            Log.d(LOG_TAG_MAIN, "No result from ScanQR Intent");
    }

    /* FAB On Click */
    private class fabOnClickListener implements View.OnClickListener
    {
        public void onClick (View v)
        {
            Log.d(LOG_TAG_MAIN, "fabOnClickListener: directing to NewCollectionActivity");
            Intent intent = new Intent (
                    HomeScreenActivity.this,
                    NewHomeCollectionActivity.class
            );
            intent.putExtra("collectionID", "");
            startActivity(intent);
        }
    }

}

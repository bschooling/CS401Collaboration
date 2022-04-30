package com.example.cs401collaboration;

import android.content.Intent;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

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
                ArrayList<Entity> entityList = new ArrayList<>();
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

        super.onActivityResult(requestCode, resultCode, data);

        Log.d(LOG_TAG_MAIN, "ResultCode from QRScan: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == QRScanActivity.QR_REQUEST) {
                Log.d(LOG_TAG_MAIN, "ResultString available: " + data.hasExtra("ResultString"));
                Log.d(LOG_TAG_MAIN, "Result of ScanQR Intent: " + data.getStringExtra("ResultString"));

                resultString = data.getStringExtra("ResultString");
                Log.d(LOG_TAG_MAIN, "Processing resultString");

                mDB.getCollection(resultString, new OnSuccessListener<Collection>() {
                    @Override
                    public void onSuccess(Collection collection) {
                        Intent entityIntent = new Intent(HomeScreenActivity.this, CollectionViewActivity.class);
                        entityIntent.putExtra("entity_clicked_id", resultString);

                        startActivity(entityIntent);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (Objects.equals(e.getMessage(), "UserInvalidPermissions"))
                        {
                            Toast.makeText(
                                    HomeScreenActivity.this,
                                    "Not Authorized to Access Collection",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        else if (Objects.equals(e.getMessage(), "NoCollectionFound"))
                        {
                            Log.d(LOG_TAG_MAIN, "resultString is not a Collection");

                            mDB.getItem(resultString, new OnSuccessListener<Item>() {
                                @Override
                                public void onSuccess(Item item) {
                                    Log.d(LOG_TAG_MAIN, "resultString is an Item");

                                    Intent entityIntent = new Intent(HomeScreenActivity.this, ItemViewActivity.class);
                                    entityIntent.putExtra("entity_clicked_id", resultString);

                                    startActivity(entityIntent);
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText (
                                            HomeScreenActivity.this,
                                            "Unable to retrieve ScanQR result",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            });
                        }
                    }
                });
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

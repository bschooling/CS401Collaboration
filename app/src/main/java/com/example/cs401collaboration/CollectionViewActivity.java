package com.example.cs401collaboration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.Adapters.EntityRvAdapter;
import com.example.cs401collaboration.glide.GlideApp;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.model.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Bryce Schooling
 */
public class CollectionViewActivity extends AppCompatActivity {

    /* Database */
    private DatabaseService mDB;

    /* UI Element Handlers */
    private TextView mCollectionLocation;
    private TextView mCollectionDescription;
    private Toolbar mCollectionBar;
    private ImageView mCollectionImage;
    private FloatingActionButton mAddFab, mAddCollectionFab, mAddItemFab;
    private TextView addCollectionsFabLabel, addItemsFabLabel;
    private boolean isFabVisible;

    /**
     * suppressGetCollectionInvalidPermission
     *
     * False by default. Ignore invalid permissions exception in db.getCollection in onStart.
     *
     * This flag is set by collaborator screen launcher's onActivityResult, based on parameters
     * in intent's extras.finish. Relevant when a collaborator removes themselves from a collection
     * and is returned to the collection view of a collection they no longer have access to,
     * but db.getCollection calls in onStart before they are bounced back to calling activity.
     */
    Boolean suppressGetCollectionInvalidPermission = false;

    /**
     * ActivityResultLauncher for launching collaboration screen.
     */
    ActivityResultLauncher<Intent> collabScreenLauncher;

    // EntityID holds the parent ID passed in
    String entityID;
    String entityOwner;

    /* recyclerView UI element */
    private RecyclerView entityRvView;

    private final String TAG = "CollectionViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_view);

        collabScreenLauncher = registerForActivityResult (
                new ActivityResultContracts.StartActivityForResult(),
                new collabScreenLauncherActivityResultCallback()
        );

        // Database
        mDB = DatabaseService.getInstance();

        // Location and Description text
        mCollectionLocation = findViewById(R.id.collection_view_location);
        mCollectionDescription = findViewById(R.id.collection_view_description);

        // Bar and Image fields
        mCollectionBar = findViewById(R.id.collectionToolbar);
        mCollectionImage = findViewById(R.id.collection_app_bar_image);

        // Fab
        mAddFab = findViewById(R.id.add_collection_item_fab);
        mAddCollectionFab = findViewById(R.id.add_collection_fab);
        mAddItemFab = findViewById(R.id.add_item_fab);

        // Fab Labels
        addCollectionsFabLabel = findViewById(R.id.add_collection_text);
        addItemsFabLabel = findViewById(R.id.add_item_text);

        // Add Fab onClick
        mAddFab.setOnClickListener(addFabListener);

        // Add Collection onClick
        mAddCollectionFab.setOnClickListener(addCollectionFabListener);

        // Add Item onClick
        mAddItemFab.setOnClickListener(addItemFabListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        entityID = intent.getStringExtra("entity_clicked_id");

        // Fab and Label starting visibility.  Set to hidden
        mAddCollectionFab.setVisibility(View.GONE);
        addCollectionsFabLabel.setVisibility(View.GONE);
        mAddItemFab.setVisibility(View.GONE);
        addItemsFabLabel.setVisibility(View.GONE);

        // Fab visibility boolean
        isFabVisible = false;

        mDB.getCollection(entityID, new OnSuccessListener<Collection>() {
            @Override
            public void onSuccess(Collection collection) {
                // Get the ParentCollection
                entityOwner = collection.getOwner().getId();

                // Sets UI fields with Collection Data
                mCollectionLocation.setText(collection.getLocation());
                mCollectionDescription.setText(collection.getDescription());
                mCollectionBar.setTitle(collection.getName());

                mDB.getAllEntitiesForCollection(entityID, new OnSuccessListener<ArrayList<Entity>>() {
                    @Override
                    public void onSuccess(ArrayList<Entity> entities) {

                        // Populate retrieved collections on home screen rv
                        entityRvView = findViewById(R.id.collectionViewActivity_rv);
                        EntityRvAdapter entityRvAdapter = new EntityRvAdapter(CollectionViewActivity.this, entities);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(CollectionViewActivity.this, 2);
                        entityRvView.setLayoutManager(gridLayoutManager);
                        entityRvView.setAdapter(entityRvAdapter);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (
                                CollectionViewActivity.this,
                                "Unable to retrieve collections",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e.getMessage().equals("UserInvalidPermissions"))
                {
                    if (!suppressGetCollectionInvalidPermission)
                    {
                        Toast.makeText (
                                CollectionViewActivity.this,
                                "Not Authorized to Access Collection",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
                else
                {
                    Toast.makeText (
                            CollectionViewActivity.this,
                            "Unable to Retrieve Collection Information",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });

        // Set Image
        String resourceID = getIntent().getStringExtra("getImageResourceID");
        if (resourceID == null) resourceID = "placeholder.png";
        StorageReference resourceSR =
                FirebaseStorage.getInstance().getReference().child(resourceID);

        Log.d(TAG, "ResourceSR: " + resourceSR);

        GlideApp.with(CollectionViewActivity.this)
                .load(resourceSR)
                .into(mCollectionImage);

    }

    private View.OnClickListener addFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!isFabVisible) {
                mAddCollectionFab.show();
                mAddItemFab.show();
                addCollectionsFabLabel.setVisibility(View.VISIBLE);
                addItemsFabLabel.setVisibility(View.VISIBLE);

                isFabVisible = true;
            } else {
                mAddCollectionFab.hide();
                mAddItemFab.hide();
                addCollectionsFabLabel.setVisibility(View.GONE);
                addItemsFabLabel.setVisibility(View.GONE);

                isFabVisible = false;
            }
        }
    };

    private View.OnClickListener addCollectionFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent createCollectionIntent = new Intent(CollectionViewActivity.this, NewEntityActivity.class);
            createCollectionIntent.putExtra("entity_type", Entity.TYPE_COLLECTION);
            createCollectionIntent.putExtra("collectionID", entityID);
            createCollectionIntent.putExtra("entity_owner", entityOwner);

            startActivity(createCollectionIntent);
        }
    };

    private View.OnClickListener addItemFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (entityID != null){
                Intent createItemIntent = new Intent(CollectionViewActivity.this, NewEntityActivity.class);
                createItemIntent.putExtra("entity_type", Entity.TYPE_ITEM);
                createItemIntent.putExtra("collectionID", entityID);

                startActivity(createItemIntent);
            }
        }
    };

    /* Setup Menu */
    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater().inflate(R.menu.collectionview_menu, menu);
        return true;
    }

    /* Handle Menu Item Clicks */
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (item.getItemId() == R.id.miCollectionCollabScreen)
        {
            Log.d(TAG, "onOptionsItemSelected: to collab screen option selected");
            Intent collaboratorIntent = new Intent(this, CollaboratorViewActivity.class);
            collaboratorIntent.putExtra("collection_id", entityID);
            collabScreenLauncher.launch(collaboratorIntent);
            return true;
        }

        else if (item.getItemId() == R.id.miScanQR)
        {
            Log.d(TAG, "onOptionsItemSelected: scanQR option selected");

            // TODO ScanQR things here
            Intent scanIntent = new Intent(CollectionViewActivity.this, QRScanActivity.class);
            scanIntent.putExtra("RequestCode", QRScanActivity.QR_REQUEST);

            startActivityForResult(scanIntent, QRScanActivity.QR_REQUEST);
            return true;
        }

        else if (item.getItemId() == R.id.miChangeImage)
        {
            Log.d(TAG, "onOptionsItemSelected: changeImage option selected");

            Intent imageIntent = new Intent(CollectionViewActivity.this, QRScanActivity.class);
            imageIntent.putExtra("RequestCode", QRScanActivity.CAMERA_REQUEST);

            startActivityForResult(imageIntent, QRScanActivity.CAMERA_REQUEST);

            return true;
        }

        Log.d(TAG, "onOptionsItemSelected: default triggered");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String resultString;

        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "ResultCode from QRScan: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == QRScanActivity.QR_REQUEST) {
                Log.d(TAG, "ResultString available: " + data.hasExtra("ResultString"));
                Log.d(TAG, "Result of ScanQR Intent: " + data.getStringExtra("ResultString"));

                resultString = data.getStringExtra("ResultString");
                Log.d(TAG, "Processing resultString");

                mDB.getCollection(resultString, new OnSuccessListener<Collection>() {
                    @Override
                    public void onSuccess(Collection collection) {
                        Intent entityIntent = new Intent(CollectionViewActivity.this, CollectionViewActivity.class);
                        entityIntent.putExtra("entity_clicked_id", resultString);

                        startActivity(entityIntent);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (Objects.equals(e.getMessage(), "UserInvalidPermissions"))
                        {
                            Toast.makeText(
                                    CollectionViewActivity.this,
                                    "Not Authorized to Access Collection",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                        else if (Objects.equals(e.getMessage(), "NoCollectionFound"))
                        {
                            Log.d(TAG, "resultString is not a Collection");

                            mDB.getItem(resultString, new OnSuccessListener<Item>() {
                                @Override
                                public void onSuccess(Item item) {
                                    Log.d(TAG, "resultString is an Item");

                                    Intent entityIntent = new Intent(CollectionViewActivity.this, ItemViewActivity.class);
                                    entityIntent.putExtra("entity_clicked_id", resultString);

                                    startActivity(entityIntent);
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText (
                                            CollectionViewActivity.this,
                                            "Unable to retrieve ScanQR result",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            });
                        }
                    }
                });
            }

            if (requestCode == QRScanActivity.CAMERA_REQUEST) {
                Collection updatedCollection = new Collection();
                String imageFilePath = data.getStringExtra("ImageResourceID");
                String imageFileName = data.getStringExtra("ImageFileName");

                Log.d(TAG, "Result from ScanQR: " + data);
                Log.d(TAG, "Result ImageResourceID: " + imageFilePath);
                Log.d(TAG, "Result ImageFileName: " + imageFileName);

                mDB.getCollection(entityID, new OnSuccessListener<Collection>() {
                    @Override
                    public void onSuccess(Collection collection) {
                        // Copy collection
                        updatedCollection.copyOther(collection);
                        updatedCollection.setImageResourceID(imageFileName);

                        mDB.updateCollection(updatedCollection, new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                Toast.makeText (CollectionViewActivity.this,
                                        "Update Collection Successful", Toast.LENGTH_SHORT).show();
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText (CollectionViewActivity.this,
                                        "Could not update Collection", Toast.LENGTH_LONG).show();
                            }
                        });

                        String newImageResourceID = updatedCollection.getImageResourceID();
                        String oldImageResourceID = collection.getImageResourceID();

                        if (newImageResourceID != null && !newImageResourceID.equals(oldImageResourceID)) {
                            StorageReference resourceSR =
                                    FirebaseStorage.getInstance().getReference().child(newImageResourceID);

                            Log.d(TAG, "ResourceSR: " + resourceSR);

                            GlideApp.with(CollectionViewActivity.this)
                                    .load(resourceSR)
                                    .into(mCollectionImage);

                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CollectionViewActivity.this,
                                "Cant Find Collection Data", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        else
            Log.d(TAG, "No result from ScanQR Intent");
    }

    /**
     * ActivityResultCallback<ActivityResult> for Collaboration Screen Launcher
     * ActivityResultLauncher<Intent>.
     *
     * Handles bouncing user to previous screen if extras.finish is set (e.g. when logged in user
     * removes self from collaborators list on present activity).
     *
     * Checks extras.finish. If true, sets $suppressGetCollectionInvalidPermission to true
     * and calls finish().
     */
    private class collabScreenLauncherActivityResultCallback
            implements ActivityResultCallback<ActivityResult>
    {
        @Override
        public void onActivityResult(ActivityResult result)
        {
            Log.d(TAG, "collabScreenLauncherActivityResultCallback: running");
            Intent intent = result.getData();
            if (
                    intent.hasExtra("finish") &&
                            (intent.getBooleanExtra("finish", false) == true)
            )
            {
                suppressGetCollectionInvalidPermission = true;
                finish();
            }
        }
    }

}

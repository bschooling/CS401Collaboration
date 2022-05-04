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
 * Class for the Collection View Activity
 * Displays collections and Items the user has access to inside a recycler view
 *
 * @author Bryce Schooling
 */
public class CollectionViewActivity extends AppCompatActivity {

    /* Database */
    private DatabaseService mDB;

    /* Storage */
    private StorageService mStorage;

    /* UI Element Handlers */
    private TextView mCollectionLocation;
    private TextView mCollectionDescription;
    private TextView mNoEntitiesMessage;
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

    /**
     * Collection being presently displayed on this screen. Set in onStart getCollection.
     */
    private Collection mCollection;

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

        // Database, Storage
        mDB = DatabaseService.getInstance();
        mStorage = StorageService.getInstance();

        // Location and Description text
        mCollectionLocation = findViewById(R.id.collection_view_location);
        mCollectionDescription = findViewById(R.id.collection_view_description);
        mNoEntitiesMessage = findViewById(R.id.collection_view_noentitiesmsg);

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

        // Inflate Menu
        mCollectionBar.inflateMenu(R.menu.collectionview_menu);
        mCollectionBar.setOnMenuItemClickListener(new MenuItemListener());

        // Handle RV
        entityRvView = findViewById(R.id.collectionViewActivity_rv);
        entityRvView.setNestedScrollingEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        // get the collection or item clicked when coming to this activity
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
                // Set retrieved collection to class instance
                CollectionViewActivity.this.mCollection = collection;

                // Get the ParentCollection
                entityOwner = collection.getOwner().getId();

                // Sets UI fields with Collection Data
                mCollectionDescription.setText (
                        (collection.getDescription() != null && !collection.getDescription().isEmpty()) ?
                                collection.getDescription() :
                                "No Description Information"
                );
                mCollectionLocation.setText (
                        (collection.getLocation() != null && !collection.getLocation().isEmpty()) ?
                                collection.getLocation() :
                                "No Location Information"
                );
                mCollectionBar.setTitle(collection.getName());

                // retrieve a list of all sub collections or items tied to the current collection
                mDB.getAllEntitiesForCollection(entityID, new OnSuccessListener<ArrayList<Entity>>() {
                    @Override
                    public void onSuccess(ArrayList<Entity> entities) {
                        // Hide or show the "No Entities" Message
                        mNoEntitiesMessage.setVisibility (
                                (entities.isEmpty()) ? View.VISIBLE : View.GONE
                        );

                        // Populate retrieved collections on home screen rv
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

    // Listener for the add Fab.  Toggles the visibility of the popup menu
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

    // Add Collection FAB onClick listener
    private View.OnClickListener addCollectionFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // create activity for adding new collection
            Intent createCollectionIntent = new Intent(CollectionViewActivity.this, NewEntityActivity.class);
            // sets the intent for collection, the id of current collection, and the owner of current collection
            createCollectionIntent.putExtra("entity_type", Entity.TYPE_COLLECTION);
            createCollectionIntent.putExtra("collectionID", entityID);
            createCollectionIntent.putExtra("entity_owner", entityOwner);

            startActivity(createCollectionIntent);
        }
    };

    // Add Item FAB onClick listener
    private View.OnClickListener addItemFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (entityID != null){
                // create activity for adding new item
                Intent createItemIntent = new Intent(CollectionViewActivity.this, NewEntityActivity.class);
                // sets the intent for item, and the id of the current collection
                createItemIntent.putExtra("entity_type", Entity.TYPE_ITEM);
                createItemIntent.putExtra("collectionID", entityID);

                startActivity(createItemIntent);
            }
        }
    };

    /* Handle Menu Item Clicks */
    private class MenuItemListener implements Toolbar.OnMenuItemClickListener
    {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.miCollectionCollabScreen)
            {
                Log.d(TAG, "onOptionsItemSelected: to collab screen option selected");
                Intent collaboratorIntent = new Intent(CollectionViewActivity.this, CollaboratorViewActivity.class);
                collaboratorIntent.putExtra("collection_id", entityID);
                collabScreenLauncher.launch(collaboratorIntent);
                return true;
            }

            else if (item.getItemId() == R.id.miScanQR)
            {
                Log.d(TAG, "onOptionsItemSelected: scanQR option selected");

                Intent scanIntent = new Intent(CollectionViewActivity.this, QRScanActivity.class);
                scanIntent.putExtra("RequestCode", QRScanActivity.QR_REQUEST);

                startActivityForResult(scanIntent, QRScanActivity.QR_REQUEST);
                return true;
            }

            else if (item.getItemId() == R.id.miChangeImage)
            {
                Log.d(TAG, "onOptionsItemSelected: changeImage option selected");

                String imageResourceID = mCollection.getImageResourceID();
                if (imageResourceID == null) imageResourceID = "placeholder.png";

                Intent imageIntent = new Intent(CollectionViewActivity.this, QRScanActivity.class);
                imageIntent.putExtra("RequestCode", QRScanActivity.CAMERA_REQUEST);
                imageIntent.putExtra("imageResourceID", imageResourceID);

                startActivityForResult(imageIntent, QRScanActivity.CAMERA_REQUEST);

                return true;
            }

            else if (item.getItemId() == R.id.miGenQR)
            {
                Log.d(TAG, "onOptionsItemSelected: genQR option selected");

                Intent qrViewIntent = new Intent(CollectionViewActivity.this, QRViewActivity.class);
                String inputTitle = mCollectionBar.getTitle().toString() + " Collection";

                qrViewIntent.putExtra("qrTitle", inputTitle);
                qrViewIntent.putExtra("encodeString", entityID);
                qrViewIntent.putExtra("entityType", "collection");

                startActivity(qrViewIntent);

                return true;
            }

            Log.d(TAG, "onOptionsItemSelected: default triggered");
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "ResultCode from QRScan: " + resultCode);

        if (resultCode == RESULT_OK) {
            if (requestCode == QRScanActivity.QR_REQUEST) {
                String resultString;
                String readEntityID;
                String type;

                Log.d(TAG, "ResultString available: " + data.hasExtra("ResultString"));
                Log.d(TAG, "Result of ScanQR Intent: " + data.getStringExtra("ResultString"));

                resultString = data.getStringExtra("ResultString");
                type = resultString.substring(0, 1);
                readEntityID = resultString.substring(2);

                if (type.equals("c"))
                {
                    Intent entityIntent = new Intent(CollectionViewActivity.this, CollectionViewActivity.class);
                    entityIntent.putExtra("entity_clicked_id", readEntityID);
                    startActivity(entityIntent);
                }
                else if (type.equals("i"))
                {
                    Toast errorToast = Toast.makeText (CollectionViewActivity.this,
                            "An error occurred.",
                            Toast.LENGTH_LONG);
                    mDB.getItem(readEntityID, new OnSuccessListener<Item>() {
                        @Override
                        public void onSuccess(Item item) {
                            mDB.getCollection(item.getParentCollection().getId(), new OnSuccessListener<Collection>() {
                                @Override
                                public void onSuccess(Collection collection) {
                                    Intent entityIntent = new Intent(CollectionViewActivity.this, ItemViewActivity.class);
                                    entityIntent.putExtra("entity_clicked_id", readEntityID);
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
                    Log.d(TAG, "Scanned QR Label has invalid tag");
                    Toast.makeText (CollectionViewActivity.this,
                            "Invalid QR Code",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            if (requestCode == QRScanActivity.CAMERA_REQUEST) {
                Collection updatedCollection = new Collection();
                String imageResourceID = data.getStringExtra("imageResourceID");

                Log.d(TAG, "Result from ScanQR: " + data);
                Log.d(TAG, "Result imageResourceID: " + imageResourceID);

                mDB.getCollection(entityID, new OnSuccessListener<Collection>() {
                    @Override
                    public void onSuccess(Collection collection) {
                        // Copy collection
                        updatedCollection.copyOther(collection);
                        updatedCollection.setImageResourceID(imageResourceID);

                        mDB.updateCollection(updatedCollection, new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                Toast.makeText (CollectionViewActivity.this,
                                        "Update Collection Successful", Toast.LENGTH_SHORT).show();
                                // Delete old image
                                if (!collection.getImageResourceID().equals("placeholder.png"))
                                {
                                    mStorage.deleteResource(collection.getImageResourceID(),
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                            }
                                        }, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d (TAG,
                                                        "Unable to delete orphaned image id="
                                                                + collection.getImageResourceID()
                                                );
                                            }
                                        });
                                }
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText (CollectionViewActivity.this,
                                        "Could not update Collection", Toast.LENGTH_LONG).show();
                            }
                        });

                        String oldImageResourceID = collection.getImageResourceID();
                        if
                        (
                            imageResourceID != null && !imageResourceID.equals(oldImageResourceID)
                        )
                        {
                            Log.d(TAG, "newImageResourceID: " + imageResourceID);
                            Log.d(TAG, "oldImageResourceID: " + oldImageResourceID);

                            StorageReference resourceSR =
                                    FirebaseStorage.getInstance().getReference().child(imageResourceID);

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
        {
            Log.d(TAG, "No result from ScanQR Intent");
        }
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

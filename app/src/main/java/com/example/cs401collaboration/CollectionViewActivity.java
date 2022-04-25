package com.example.cs401collaboration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.glide.GlideApp;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.Adapters.EntityRvAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * @author Bryce Schooling
 */
public class CollectionViewActivity extends AppCompatActivity {

    /* Database */
    private DatabaseService mDB;

    /* Storage */
    private StorageService mStorage = StorageService.getInstance();

    /* UI Element Handlers */
    private TextView mCollectionLocation;
    private TextView mCollectionDescription;
    private Toolbar mCollectionBar;
    private ImageView mCollectionImage;
    private FloatingActionButton mAddFab, mAddCollectionFab, mAddItemFab;
    private TextView addCollectionsFabLabel, addItemsFabLabel;
    private boolean isFabVisible;

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
                Toast.makeText (
                        CollectionViewActivity.this,
                        "Unable to retrieve collection info",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // Set Image
        String resourceID = getIntent().getStringExtra("getImageResourceID");
        if (resourceID == null) resourceID = "placeholder.png";
        StorageReference resourceSR =
                FirebaseStorage.getInstance().getReference().child(resourceID);

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
            startActivity(collaboratorIntent);
            return true;
        }
        Log.d(TAG, "onOptionsItemSelected: default triggered");
        return super.onOptionsItemSelected(item);
    }

}

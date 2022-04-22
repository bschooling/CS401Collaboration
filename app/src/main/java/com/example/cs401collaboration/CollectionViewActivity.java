package com.example.cs401collaboration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.rvAdapters.EntityRvAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

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

    /* recyclerView UI element */
    private RecyclerView entityRView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_view);

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

        // Fab and Label starting visibility.  Set to hidden
        mAddCollectionFab.setVisibility(View.GONE);
        addCollectionsFabLabel.setVisibility(View.GONE);
        mAddItemFab.setVisibility(View.GONE);
        addItemsFabLabel.setVisibility(View.GONE);

        // Fab visibility boolean
        isFabVisible = false;

        // Add Fab onClick
        mAddFab.setOnClickListener(new View.OnClickListener() {
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
        });

        // Add Collection onClick
        mAddCollectionFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                //TODO Add Collection
            }
        });

        mAddItemFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Add Item
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        String entityID = intent.getStringExtra("entity_clicked_id");

        mDB.getCollection(entityID, new OnSuccessListener<Collection>() {
            @Override
            public void onSuccess(Collection collection) {

                mCollectionLocation.setText(collection.getLocation());
                mCollectionDescription.setText(collection.getDescription());
                mCollectionBar.setTitle(collection.getName());
                mCollectionImage.setImageResource(android.R.drawable.ic_menu_gallery);

                mDB.getAllEntitiesForCollection(entityID, new OnSuccessListener<ArrayList<Entity>>() {
                    @Override
                    public void onSuccess(ArrayList<Entity> entities) {
                        ArrayList<Entity> entityList = new ArrayList<>();

                        // Populate retrieved collections on home screen rv
                        entityRView = findViewById(R.id.collectionViewActivity_rv);
                        EntityRvAdapter entityRvAdapter = new EntityRvAdapter(CollectionViewActivity.this, entities);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(CollectionViewActivity.this, 2);
                        entityRView.setLayoutManager(gridLayoutManager);
                        entityRView.setAdapter(entityRvAdapter);


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

    }
}
package com.example.cs401collaboration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.R;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.rvAdapters.EntityRvAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class CollectionViewActivity extends AppCompatActivity {

    /* Database */
    private DatabaseService mDB;

    /* UI Element Handlers */
    private TextView mCollectionLocation;
    private TextView mCollectionDescription;
    private Toolbar mCollectionBar;
    private ImageView mCollectionImage;

    /* recyclerView UI element */
    private RecyclerView entityRView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_view);

        mDB = DatabaseService.getInstance();

        mCollectionLocation = findViewById(R.id.collection_view_location);
        mCollectionDescription = findViewById(R.id.collection_view_description);
        mCollectionBar = findViewById(R.id.collectionToolbar);
        mCollectionImage = findViewById(R.id.collection_app_bar_image);

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
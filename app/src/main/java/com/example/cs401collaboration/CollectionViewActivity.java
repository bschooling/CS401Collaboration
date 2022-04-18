package com.example.cs401collaboration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.example.cs401collaboration.model.Collection;

public class CollectionViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_view);
        Intent intent = getIntent();

        TextView mCollectionLocation = findViewById(R.id.collection_view_location);
        TextView mCollectionDescription = findViewById(R.id.collection_view_description);
        Toolbar mCollectionBar = findViewById(R.id.collectionToolbar);
        ImageView mCollectionImage = findViewById(R.id.collection_app_bar_image);

        mCollectionLocation.setText(intent.getStringExtra("collection_clicked_location"));
        mCollectionDescription.setText(intent.getStringExtra("collection_clicked_description"));
        mCollectionBar.setTitle(intent.getStringExtra("collection_clicked_title"));
        mCollectionImage.setImageResource(intent.getIntExtra("collection_clicked_image", 0));

        //Id is passed in, and collected here.  No functionality is used yet
        int collectionID = intent.getIntExtra("collection_clicked_id", 0);

    }
}
package com.example.cs401collaboration;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.Entity;
import com.example.cs401collaboration.model.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * @author Bryce Schooling
 */
public class NewEntityActivity extends AppCompatActivity {
    DatabaseService mDB;

    private final String TAG = "NewCollectionActivity";

    /* UI Element Handles */
    private TextView header;
    private Button btCreate;
    EditText etName;
    EditText etLocation;
    EditText etDescription;

    /* Type for Entity */
    private Integer type;
    private String ParentEntityID;
    private String ParentEntityOwner;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entity);

        /* Action Bar */
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        /* Initialize db */
        mDB = DatabaseService.getInstance();

        /* Handle UI Elements */
        header = (TextView) findViewById(R.id.tv_new_entity_header);
        btCreate = (Button) findViewById(R.id.bt_new_entity_create);
        etName = (EditText) findViewById(R.id.et_new_entity_name);
        etLocation = (EditText) findViewById(R.id.et_new_entity_location);
        etDescription = (EditText) findViewById(R.id.et_new_entity_desc);

        /* create listener for Create button */
        btCreate.setOnClickListener(new NewEntityActivity.btCreateOnClickListener());

        /* Intent handler
        *  Required for Item: entity_type, collectionID
        *
        *  Required for Collection: entity_type, collectionID, entity_owner
        */
        Intent intent = getIntent();
        type = intent.getIntExtra("entity_type", 0);
        ParentEntityID = intent.getStringExtra("collectionID");
        ParentEntityOwner = intent.getStringExtra(("entity_owner"));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (type.equals(Entity.TYPE_COLLECTION)) {
            header.setText(R.string.create_new_collection_header);
        } else if (type.equals(Entity.TYPE_ITEM)) {
            header.setText(R.string.create_new_item_header);
        }
    }

    /* Allow Back Button Functionality */
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        Log.d(TAG, "onOptionsItemSelected: entered execution");
        if (item.getItemId() == android.R.id.home)
        {
            Log.d(TAG, "onOptionsItemSelected: home item selected");
            finish();
            return super.onOptionsItemSelected(item);
        }

        Log.d (
                "TAG",
                "onOptionsItemSelected: home item selection not detected, " +
                        "ret=" + super.onOptionsItemSelected(item)
        );
        return super.onOptionsItemSelected(item);
    }

    private class btCreateOnClickListener implements View.OnClickListener
    {
        public void onClick (View v)
        {
            String name = etName.getText().toString();
            String location = etLocation.getText().toString();
            String description = etDescription.getText().toString();

            if (name.isEmpty())
            {
                Toast.makeText (
                        NewEntityActivity.this,
                        "Name field cannot be empty.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            // Type check
            if (type.equals(Entity.TYPE_COLLECTION)) {
                 // Create new Collection based on fields filled out
                Collection newCollection = new Collection(name, location, description);
                mDB.createCollection(newCollection, ParentEntityID, ParentEntityOwner, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {finish();}
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (
                                NewEntityActivity.this,
                                "Could not create the collection.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            } else if (type.equals(Entity.TYPE_ITEM)) {
                // Create new Item based on fields filled out
                Item newItem = new Item(name, location, description);
                mDB.createItem(newItem, ParentEntityID, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        finish();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (
                                NewEntityActivity.this,
                                "Could not create the item.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        }
    }
}

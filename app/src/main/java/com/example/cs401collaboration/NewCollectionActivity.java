package com.example.cs401collaboration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cs401collaboration.DatabaseService;
import com.example.cs401collaboration.model.Collection;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * @author Arshdeep Padda
 */
public class NewCollectionActivity extends AppCompatActivity
{
    DatabaseService mDB;

    private final String TAG = "NewCollectionActivity";

    /* UI Element Handles */

    private Button btCreate;
    EditText etName;
    EditText etLocation;
    EditText etDescription;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_collection);

        /* Action Bar */

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mDB = DatabaseService.getInstance();

        /* Handle UI Elements */

        btCreate = (Button) findViewById(R.id.bt_newcolact_create);
        etName = (EditText) findViewById(R.id.et_newcolact_name);
        etLocation = (EditText) findViewById(R.id.et_newcolact_loc);
        etDescription = (EditText) findViewById(R.id.et_newcolact_desc);

        btCreate.setOnClickListener(new btCreateOnClickListener());
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
            String parentCollectionID = getIntent().getExtras().getString("collectionID");
            String name = etName.getText().toString();
            String location = etLocation.getText().toString();
            String description = etDescription.getText().toString();

            if (name.isEmpty())
            {
                Toast.makeText (
                        NewCollectionActivity.this,
                        "Name field cannot be empty.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            Collection newCollection = new Collection(name, location, description);

            mDB.createCollection(newCollection, parentCollectionID, new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s)
                {
                    finish();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText (
                            NewCollectionActivity.this,
                            "Could not create the collection.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
            });
        }
    }

}

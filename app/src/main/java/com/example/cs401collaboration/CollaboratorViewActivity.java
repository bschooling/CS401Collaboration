package com.example.cs401collaboration;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 * @author Bryce Schooling
 */
public class CollaboratorViewActivity extends AppCompatActivity {

    /* Database */
    private DatabaseService mDB;

    /* UI Element Handlers*/
    private TextView displayOwner;
    private EditText etNewCollab;
    private Button btNewCollab;

    private RecyclerView collaboratorRvView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collaboration_activity);

        // Database
        mDB = DatabaseService.getInstance();

        // Bind UI Elements
        displayOwner = findViewById(R.id.display_owner);
        etNewCollab = findViewById(R.id.etNewCollaborator);
        btNewCollab = findViewById(R.id.btNewCollaborator);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Intent intent = getIntent();


    }
}

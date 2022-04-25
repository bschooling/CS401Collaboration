package com.example.cs401collaboration;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.Adapters.CollaboratorRvAdapter;
import com.example.cs401collaboration.Adapters.EntityRvAdapter;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Bryce Schooling
 */
public class CollaboratorViewActivity extends AppCompatActivity {

    /* Database */
    private DatabaseService mDB;

    /* Firebase Auth */
    private FirebaseAuth mAuth;

    /* UI Element Handlers*/
    private TextView displayOwner;
    private EditText etNewCollab;
    private Button btNewCollab;

    // Logged in user
    private FirebaseUser currentUser;

    // Recycler view
    private RecyclerView collaboratorRvView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collaboration_activity);

        // Database
        mDB = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind UI Elements
        displayOwner = findViewById(R.id.display_owner);
        etNewCollab = findViewById(R.id.etNewCollaborator);
        btNewCollab = findViewById(R.id.btNewCollaborator);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Intent intent = getIntent();

        // Get current logged in user
        currentUser = Objects.requireNonNull(mAuth.getCurrentUser(), "User cant be null");

        String currentCollection = intent.getStringExtra("collection_id");
        if (currentCollection != null){
            mDB.getCollection(currentCollection, new OnSuccessListener<Collection>() {
                @Override
                public void onSuccess(Collection collection) {
                    // TODO set display owner with collection.getOwner

                    mDB.getCollabs(collection, new OnSuccessListener<ArrayList<User>>() {
                        @Override
                        public void onSuccess(ArrayList<User> users) {

                            // Populate retrieved collections on home screen rv
                            collaboratorRvView = findViewById(R.id.rvCollaborator);
                            CollaboratorRvAdapter collaboratorRvAdapter = new CollaboratorRvAdapter(CollaboratorViewActivity.this, users, null);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CollaboratorViewActivity.this);
                            collaboratorRvView.setLayoutManager(linearLayoutManager);
                            collaboratorRvView.setAdapter(collaboratorRvAdapter);
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {}
                    });
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {}
            });
        }


    }
}

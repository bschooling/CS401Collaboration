package com.example.cs401collaboration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs401collaboration.Adapters.CollaboratorRvAdapter;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Class for the Collaborator view Activity
 * Displays all collaborators assigned to current collection
 *
 * @author Bryce Schooling
 */
public class CollaboratorViewActivity extends AppCompatActivity {

    /* Database */
    private DatabaseService mDB;

    /* Firebase Auth */
    private FirebaseAuth mAuth;

    /* UI Element Handlers*/
    private TextView displayOwnerName, displayOwnerEmail;
    private EditText etNewCollab;
    private Button btNewCollab;

    // Logged in user
    private FirebaseUser currentFbUser;

    /*Boolean representation of whether or not logged in user is the owner of collection
    * False by default
    * True represent current user is owner
    */
    private Boolean isOwner = false;

    // Current collection
    Collection currCollection;

    // Recycler view
    private RecyclerView collaboratorRvView;
    private CollaboratorRvAdapter collaboratorRvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collaboration_activity);

        // Database
        mDB = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind UI Elements
        displayOwnerName = findViewById(R.id.display_owner);
        displayOwnerEmail = findViewById(R.id.tvOwnerEmail);
        etNewCollab = findViewById(R.id.etNewCollaborator);
        btNewCollab = findViewById(R.id.btNewCollaborator);

        // Button and EditText hidden by Default
        btNewCollab.setVisibility(View.GONE);
        etNewCollab.setVisibility(View.GONE);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Intent intent = getIntent();
        this.setResult(Activity.RESULT_CANCELED, intent);

        // Get current logged in user
        currentFbUser = Objects.requireNonNull(mAuth.getCurrentUser(), "User cant be null");

        // retrieve the collection the user is currently in
        String currentCollection = intent.getStringExtra("collection_id");
        if (currentCollection != null){
            mDB.getCollection(currentCollection, new OnSuccessListener<Collection>() {
                @Override
                public void onSuccess(Collection collection) {
                    // Get collection owner
                    currCollection = collection;
                    mDB.getUser(collection.getOwner().getId(), new OnSuccessListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            displayOwnerName.setText(user.getName());
                            displayOwnerEmail.setText(user.getEmail());
                            if (user.getUid().equals(currentFbUser.getUid())){
                                btNewCollab.setVisibility(View.VISIBLE);
                                etNewCollab.setVisibility(View.VISIBLE);
                                isOwner = true;
                            }
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                    // get list of collaborators for the current collection
                    mDB.getCollabs(collection, new OnSuccessListener<ArrayList<User>>() {
                        @Override
                        public void onSuccess(ArrayList<User> users) {

                            // Populate retrieved collections on home screen rv
                            collaboratorRvView = findViewById(R.id.rvCollaborator);
                            collaboratorRvAdapter = new CollaboratorRvAdapter(
                                    CollaboratorViewActivity.this,
                                    currCollection,
                                    users,
                                    currentFbUser.getUid(),
                                    isOwner);
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
        // onclick listener for the new collaborator button
        btNewCollab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // checks for a user based on the email entered into text box
                mDB.getUserByEmail(etNewCollab.getText().toString(), new OnSuccessListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user == null){
                            Toast.makeText (
                                    CollaboratorViewActivity.this,
                                    "Can not add User: No user found",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else {
                            // add the user found as a new collaborator to the collection in the database
                            mDB.addCollab(currCollection, user.getUid(), new OnSuccessListener<Boolean>() {
                                @Override
                                public void onSuccess(Boolean aBoolean) {

                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                            // update local client of changes to collaborator RV
                            collaboratorRvAdapter.addUserToAdapter(user);
                            Toast.makeText (
                                    CollaboratorViewActivity.this,
                                    "User Added",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });

    }
}

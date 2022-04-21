package com.example.cs401collaboration;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.example.cs401collaboration.model.User;
import com.example.cs401collaboration.model.Collection;
import com.example.cs401collaboration.interfaces.OnCollectionsRetrievedCallback;

/**
 * DatabaseService incorporates functionality for accessing backend database
 * and querying for and updating data in a useful manner.
 *
 * This is a singleton class.
 *
 * @author Arshdeep Padda
 */
public class DatabaseService
{
    private static DatabaseService INSTANCE;

    private DatabaseService ()
    {

    }

    public static DatabaseService getInstance ()
    {
        if(INSTANCE == null)
            INSTANCE = new DatabaseService();
        return INSTANCE;
    }

    /** Firebase Auth Handle */
    FirebaseAuth auth = FirebaseAuth.getInstance();
    /** Firebase Firestore Handle */
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Internal Log Tag */
    private final String TAG = "DatabaseService";

    /**
     * Create User.
     *
     * onCreationFailureCB may handle db query failure by deleting created user in fire-auth
     * and alerting user, or trying again.
     *
     * @param uid uid of authenticated user
     * @param name name of user
     * @param onCreationFailureCB Of OnFailureListener, this callback is called if user creation
     *                            query fails with database.
     */
    public void createUser (String uid, String name, OnFailureListener onCreationFailureCB)
    {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);

        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "createUser: success");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "createUser: failure", e);
                    onCreationFailureCB.onFailure(e);
                }
            });
    }

    /**
     * Get Owned Collections for Home.
     * @param onSuccess Ultimate callback to work with chained data.
     * @param onFailure If any db failure occurs.
     * @param onCompletionPassthrough Callback to facilitate chaining between
     *                                getHomeCollectionsOwned and getHomeCollectionsAuthorized.
     */
    private void getHomeCollectionsOwned (
            OnSuccessListener<ArrayList<Collection>> onSuccess,
            OnFailureListener onFailure,
            OnSuccessListener<ArrayList<Collection>> onCompletionPassthrough
    )
    {
        DocumentReference userDocRef =
                db.collection("users").document(auth.getUid());

        // Retrieve User
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot userDocSnap = task.getResult();
                    if (userDocSnap.exists())
                    {
                        /* Proceed with accessing all collections for authorized user. */
                        User user = userDocSnap.toObject(User.class);

                        ArrayList<Collection> collections = new ArrayList<Collection>();

                        db.collection("collections")
                            .whereEqualTo("parentCollection", null)
                            .whereEqualTo("owner", userDocSnap.getReference())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful())
                                    {
                                        // Have collections
                                        Log.d (
                                                TAG,
                                                "getHomeCollectionsOwned: collection return count = "
                                                        + task.getResult().size()
                                        );
                                        for (QueryDocumentSnapshot document : task.getResult())
                                        {
                                            Log.d (
                                                    TAG,
                                                    "getHomeCollectionsOwned " +
                                                            document.getId() +
                                                            " => " + document.getData()
                                            );
                                            collections.add(document.toObject(Collection.class));
                                        }
                                        // passthrough
                                        onCompletionPassthrough.onSuccess(collections);
                                    }
                                    // Failure to get collections
                                    else
                                    {
                                        Log.d (
                                                TAG,
                                                "getHomeCollections: Error getting collections ",
                                                task.getException()
                                        );
                                        onFailure.onFailure(task.getException());
                                    }
                                }
                            });
                    }
                    else
                    {
                        Log.d (
                                TAG,
                                "getHomeCollections: No such document of collection 'users'."
                        );
                        onFailure.onFailure(task.getException());
                    }
                }
                // User Retrieve Task Unsuccessful
                else
                {
                    Log.d (TAG,
                            "getHomeCollectionsOwned: Unable to retrieve user ",
                            task.getException()
                    );
                    onFailure.onFailure(task.getException());
                }
            }
        });
    }

    /**
     * Get Authorized Collections for Home.
     * @param onSuccess Callback to handle when all data is received (owned + authorized).
     * @param onFailure If any db failure occurs.
     * @param collections List of collections (from getHomeCollectionsOwned).
     */
    private void getHomeCollectionsAuthorized (
            OnSuccessListener<ArrayList<Collection>> onSuccess,
            OnFailureListener onFailure,
            ArrayList<Collection> collections
    )
    {
        DocumentReference userDocRef =
                db.collection("users").document(auth.getUid());

        // Retrieve User
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot userDocSnap = task.getResult();
                    if (userDocSnap.exists())
                    {
                        /* Proceed with accessing all collections for authorized user. */
                        User user = userDocSnap.toObject(User.class);

                        db.collection("collections")
                                .whereEqualTo("parentCollection", null)
                                .whereArrayContains("authUsers", userDocSnap.getReference())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful())
                                        {
                                            // Have collections
                                            Log.d (
                                                    TAG,
                                                    "getHomeCollectionsAuthorized: collection return count = "
                                                            + task.getResult().size()
                                            );
                                            for (QueryDocumentSnapshot document : task.getResult())
                                            {
                                                Log.d (
                                                        TAG,
                                                        "getHomeCollectionsAuthorized " +
                                                                document.getId() +
                                                                " => " + document.getData()
                                                );
                                                collections.add(document.toObject(Collection.class));
                                            }
                                            // Call callback, should update UI here
                                            onSuccess.onSuccess(collections);
                                        }
                                        // Failure to get collections
                                        else
                                        {
                                            Log.d (
                                                    TAG,
                                                    "getHomeCollectionsAuthorized: Error getting collections ",
                                                    task.getException()
                                            );
                                            onFailure.onFailure(task.getException());
                                        }
                                    }
                                });
                    }
                    else
                    {
                        Log.d (
                                TAG,
                                "getHomeCollectionsAuthorized: No such document of collection 'users'."
                        );
                        onFailure.onFailure(task.getException());
                    }
                }
                // User Retrieve Task Unsuccessful
                else
                {
                    Log.d (TAG,
                            "getHomeCollectionsAuthorized: Unable to retrieve user ",
                            task.getException()
                    );
                    onFailure.onFailure(task.getException());
                }
            }
        });
    }


    /**
     * Get all collections, owned and authorized, for home screen.
     * @param onSuccess On success when all data is retrieved.
     * @param onFailure On failure for whenever any db error occurs.
     */
    public void getHomeCollections (
            OnSuccessListener<ArrayList<Collection>> onSuccess,
            OnFailureListener onFailure
    )
    {
        if (auth.getUid() == null) return;

        this.getHomeCollectionsOwned(onSuccess, onFailure, new OnSuccessListener<ArrayList<Collection>>() {
            @Override
            public void onSuccess(ArrayList<Collection> collections) {
                getHomeCollectionsAuthorized(onSuccess, onFailure, collections);
            }
        });
    }

    public void getCollection (String docId, OnSuccessListener<Collection> successCB, OnFailureListener failureCB)
    {
        db.collection("collections")
                .document(docId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists())
                            {
                                Collection collection = document.toObject(Collection.class);
                                DocumentReference currentUserDocRef = db.document (
                                        "/users/" + auth.getUid()
                                );
                                // ensure current user is allowed to access collection
                                if (
                                        collection.getOwner() == currentUserDocRef ||
                                        collection.getAuthUsers().contains(currentUserDocRef)
                                )
                                {
                                    successCB.onSuccess(collection);
                                    Log.d(TAG, "getCollection w/ id=" + document.getData());
                                }
                                else
                                {
                                    Log.d (
                                            TAG,
                                            "getCollection invalid permissions"
                                                    + document.getData()
                                    );
                                    failureCB.onFailure(new Exception("UserInvalidPermissions"));
                                }
                            }
                            else
                            {
                                Log.d(TAG, "getCollection: No such document");
                            }
                        }
                        else
                        {
                            failureCB.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * createRootCollection creates a collection without a parent,
     * e.g. one to be displayed on the home screen.
     *
     * @param collection Collection to create.
     * @param successCB Callback for successful creation.
     * @param failureCB Callback for any creation related failures.
     */
    public void createRootCollection (
            Collection collection,
            OnSuccessListener<String> successCB,
            OnFailureListener failureCB
    )
    {
        if (auth.getUid() == null)
        {
            Log.d(TAG, "createRootCollection: user not logged in!");
            return;
        }

        collection.setOwner(db.collection("users").document(auth.getUid()));
        collection.setArrayFieldsEmpty();

        db.collection("collections")
            .add(collection)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                // Collection Creation Success
                public void onSuccess (DocumentReference documentReference)
                {
                    Log.d (
                            TAG,
                            "createRootCollection: written with id="
                                    + documentReference.getId()
                    );
                    successCB.onSuccess(documentReference.getId());
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                // Collection Creation Failure
                public void onFailure (@NonNull Exception e)
                {
                    Log.w(TAG, "createRootCollection: error adding document ", e);
                    failureCB.onFailure(e);
                }
            });
    }

    /**
     * createCollection that is a child collection, e.g. belongs within a parent collection.
     *
     * @param collection Collection to add.
     * @param parentCollectionID Parent collection id, document id.
     * @param parentCollectionOwner Parent collection owner, as Document Reference.
     * @param successCB Callback for successful creation.
     * @param failureCB Callback for any creation related failures.
     */
    public void createCollection (
            Collection collection,
            String parentCollectionID,
            DocumentReference parentCollectionOwner,
            OnSuccessListener<String> successCB,
            OnFailureListener failureCB
    )
    {
        if (auth.getUid() == null)
        {
            Log.d(TAG, "createCollection: user not logged in!");
            return;
        }

        collection.setOwner(parentCollectionOwner);
        collection.setParentCollection (
                db.collection("collections").document(parentCollectionID)
        );

        collection.setArrayFieldsEmpty();
        db.collection("collections")
                .add(collection)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    // Collection Creation Success
                    public void onSuccess (DocumentReference documentReference)
                    {
                        Log.d (
                                TAG,
                                "createCollection: written with id="
                                        + documentReference.getId()
                        );
                        // Add present user as authorized user to collection
                        documentReference.update (
                                "authUsers",
                                FieldValue.arrayUnion(
                                        db.document("/users/" + auth.getUid())
                                )
                        );
                        // Add newly created collection as child of parent collection
                        collection.getParentCollection().update (
                                "childrenCollections",
                                FieldValue.arrayUnion(documentReference)
                        );
                        // call onSuccess
                        successCB.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    // Collection Creation Failure
                    public void onFailure (@NonNull Exception e)
                    {
                        Log.w(TAG, "createCollection: error adding document ", e);
                        failureCB.onFailure(e);
                    }
                });
    }

}

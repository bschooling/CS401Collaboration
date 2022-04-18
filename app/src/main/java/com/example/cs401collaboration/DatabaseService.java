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
        user.put("collabs", Arrays.asList());
        user.put("collections", Arrays.asList());

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
     * Get all collections that a user can access.
     *
     * This includes the authenticated user's collections, as well as any collections of current
     * user's collaborators.
     *
     * @param cb Callback of interface OnCollectionsRetrievedCallback to be called with returned
     *           collections once database successfully returns collections.
     */
    public void getAllCollections (OnCollectionsRetrievedCallback cb)
    {
        if (auth.getUid() == null) return;

        DocumentReference userDocRef = db.collection("users")
                .document(auth.getUid());

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
                        ArrayList<DocumentReference> userGrp = new ArrayList<DocumentReference>();
                        ArrayList<DocumentReference> collabs = user.getCollabs();

                        // Populate userGrp
                        userGrp.add(userDocSnap.getReference());
                        for (DocumentReference collab : collabs)
                            userGrp.add(collab);

                        db.collection("collections").whereIn("owner", userGrp)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful())
                                    { // Have collections
                                        Log.d(
                                                TAG,
                                                "getAllCollections: collection return count = "
                                                        + task.getResult().size()
                                        );
                                        for (QueryDocumentSnapshot document : task.getResult())
                                        {
                                            Log.d(
                                                    TAG,
                                                    "getAllCollections" +
                                                            document.getId() +
                                                            " => " + document.getData()
                                            );
                                            collections.add(document.toObject(Collection.class));
                                        }
                                        // Call callback, should update UI here
                                        cb.OnCollectionsRetrieved(collections);
                                    } // Failure to get collections
                                    else
                                    {
                                        Log.d(
                                                TAG,
                                                "getAllCollections: Error getting collections ",
                                                task.getException()
                                        );
                                    }
                                }
                            });
                    }
                    else
                    {
                        Log.d(TAG, "getAllCollections: No such document of collection users.");
                    }
                } // User Retrieve Task Unsuccessful
                else
                {
                    Log.d(TAG, "getAllCollections: Unable to retrieve user", task.getException());
                }
            }
        });
    }

}

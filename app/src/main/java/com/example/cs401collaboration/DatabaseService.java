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
import com.example.cs401collaboration.model.Item;
import com.example.cs401collaboration.model.Entity;
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
        user.put("email", auth.getCurrentUser().getEmail());

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
     * Get User Model Object for User Given UID.
     * @param uid UID of User.
     * @param onSuccess On Success, passed User Model Object.
     * @param onFailure On Failure, passed task exception.
     */
    public void getUser (
            String uid,
            OnSuccessListener<User> onSuccess,
            OnFailureListener onFailure
    )
    {
        db.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "getUser: task success");
                            onSuccess.onSuccess(task.getResult().toObject(User.class));
                        }
                        else
                            Log.d(TAG, "getUser: task failed... " + task.getException());
                    }
                });
    }

    /**
     * Get owned collections for a given parent collection.
     *
     * Parent collection is provided as a DocRef. If it is null, home collections are queried.
     *
     * This method is intended to be called from getCollections and chained with
     * getCollectionsAuthorized.
     *
     * @param onSuccess Ultimate callback to work with chained data.
     * @param onFailure If any db failure occurs.
     * @param onCompletionPassthrough Callback to facilitate chaining between
     *                                getCollectionsOwned and getCollectionsAuthorized.
     */
    private void getCollectionsOwned (
            DocumentReference parentCollectionReference,
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
                            .whereEqualTo("parentCollection", parentCollectionReference)
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
                                                "getCollectionsOwned: collection return count = "
                                                        + task.getResult().size()
                                        );
                                        for (QueryDocumentSnapshot document : task.getResult())
                                        {
                                            Log.d (
                                                    TAG,
                                                    "getCollectionsOwned " +
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
                                                "getCollectionsOwned: Error getting collections ",
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
                                "getCollectionsOwned: No such document of collection 'users'."
                        );
                        onFailure.onFailure(task.getException());
                    }
                }
                // User Retrieve Task Unsuccessful
                else
                {
                    Log.d (TAG,
                            "getCollectionsOwned: Unable to retrieve user ",
                            task.getException()
                    );
                    onFailure.onFailure(task.getException());
                }
            }
        });
    }

    /**
     * Get authorized collections for a given parent collection.
     *
     * Parent collection is provided as a DocRef. If it is null, home collections are queried.
     *
     * This method is intended to be called from getCollectionsOwned with a list of collections
     * it retrieved. It adds its own collections onto it, and calls onSuccess.
     *
     * @param onSuccess Callback to handle when all data is received (owned + authorized).
     * @param onFailure If any db failure occurs.
     * @param collections List of collections (from getCollectionsOwned).
     */
    private void getCollectionsAuthorized (
            DocumentReference parentCollectionReference,
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
                                .whereEqualTo("parentCollection", parentCollectionReference)
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
                                                    "getCollectionsAuthorized: collection return count = "
                                                            + task.getResult().size()
                                            );
                                            for (QueryDocumentSnapshot document : task.getResult())
                                            {
                                                Log.d (
                                                        TAG,
                                                        "getCollectionsAuthorized " +
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
                                                    "getCollectionsAuthorized: Error getting collections ",
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
                                "getCollectionsAuthorized: No such document of collection 'users'."
                        );
                        onFailure.onFailure(task.getException());
                    }
                }
                // User Retrieve Task Unsuccessful
                else
                {
                    Log.d (TAG,
                            "getCollectionsAuthorized: Unable to retrieve user ",
                            task.getException()
                    );
                    onFailure.onFailure(task.getException());
                }
            }
        });
    }


    /**
     * Get all collections, owned and authorized, for given parentCollectionID.
     *
     * Provide null string to retrieve home screen collections.
     *
     * @param onSuccess On success when all data is retrieved.
     * @param onFailure On failure for whenever any db error occurs.
     */
    public void getCollections (
            String parentCollectionID,
            OnSuccessListener<ArrayList<Collection>> onSuccess,
            OnFailureListener onFailure
    )
    {
        if (auth.getUid() == null) return;

        DocumentReference parentCollectionReference = (parentCollectionID == null) ? null :
                db.document("/collections/" + parentCollectionID) ;

        this.getCollectionsOwned(parentCollectionReference, onSuccess, onFailure, new OnSuccessListener<ArrayList<Collection>>() {
            @Override
            public void onSuccess(ArrayList<Collection> collections) {
                getCollectionsAuthorized(parentCollectionReference, onSuccess, onFailure, collections);
            }
        });
    }

    /**
     * Update Collection.
     *
     * @param collection Collection to update.
     * @param successCB On Success.
     * @param failureCB On Failure.
     */
    public void updateCollection (
            Collection collection,
            OnSuccessListener<Boolean> successCB,
            OnFailureListener failureCB
    )
    {
        db.collection("collections").document(collection.getDocID())
            .set(collection)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                        successCB.onSuccess(true);
                    else
                        failureCB.onFailure(task.getException());
                }
            });
    }

    /**
     * Get Items for a Collection.
     *
     * @param collectionID Collection ID.
     * @param onSuccess On Success.
     * @param onFailure On Failure.
     */
    public void getItemsForCollection (
        String collectionID,
        OnSuccessListener<ArrayList<Item>> onSuccess,
        OnFailureListener onFailure
    )
    {
        if (auth.getUid() == null) return;

        DocumentReference collectionDocRef =
                db.document("/collections/" + collectionID);

        db.collection("items")
                .whereEqualTo("parentCollection", collectionDocRef)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            ArrayList<Item> items = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                Log.d (
                                        TAG,
                                        "getItemsForCollection: " +
                                                document.getId() +
                                                " => " + document.getData()
                                );
                                items.add(document.toObject(Item.class));
                            }
                            onSuccess.onSuccess(items);
                        }
                        else
                        {
                            Log.d (
                                    TAG,
                                    "getItemsForCollection: task to retrieve all matching " +
                                        "items of provided collection id failed " +
                                        task.getException()
                            );
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Create Item.
     *
     * @param item Item to add.
     * @param parentCollectionID Parent Collection ID.
     * @param onSuccess On Success.
     * @param onFailure On Failure.
     */
    public void createItem (
            Item item,
            String parentCollectionID,
            OnSuccessListener<String> onSuccess,
            OnFailureListener onFailure
    )
    {
        if (auth.getUid() == null) return;

        item.setParentCollection(db.document("/collections/" + parentCollectionID));

        db.collection("items")
                .add(item)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful())
                        {
                            Log.d (
                                    TAG,
                                    "createItem: success id=" + task.getResult().getId()
                            );
                            onSuccess.onSuccess(task.getResult().getId());
                        }
                        else
                        {
                            Log.d (
                              TAG,
                              "createItem: failed to create item document " +
                                      task.getException()
                            );
                            onFailure.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Get Single Item by ID.
     *
     * @param itemID Item ID.
     * @param successCB Called with Item on Sucess.
     * @param failureCB Called if failure occurs.
     */
    public void getItem (
        String itemID,
        OnSuccessListener<Item> successCB,
        OnFailureListener failureCB
    )
    {
        db.collection("items")
                .document(itemID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists())
                            {
                                Item item = document.toObject(Item.class);
                                Log.d(TAG, "getItem w/ id=" + document.getData());
                                successCB.onSuccess(item);
                            }
                            else
                            {
                                Log.d(TAG, "getItem: No such document");
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
     * Gets all items and collections, as Entity objects, for a given collection.
     *
     * OnSuccess receives an array list of Entity objects describing all items and collections
     * in the specifed collection.
     *
     * @param collectionID Collection ID to target.
     * @param successCB On Success.
     * @param failureCB On Failure.
     */
    public void getAllEntitiesForCollection (
            String collectionID,
            OnSuccessListener<ArrayList<Entity>> successCB,
            OnFailureListener failureCB
    )
    {
        if (auth.getUid() == null) return;

        getCollections(collectionID, new OnSuccessListener<ArrayList<Collection>>() {
            @Override
            public void onSuccess(ArrayList<Collection> collections) {
                getItemsForCollection(collectionID, new OnSuccessListener<ArrayList<Item>>() {
                    @Override
                    public void onSuccess(ArrayList<Item> items) {
                        Log.d (TAG, "collections = " + collections.size() + " items = " + items.size());
                        ArrayList<Entity> ents = new ArrayList<>();
                        for (Collection collection : collections)
                            ents.add(new Entity (
                                    collection.getName(),
                                    collection.getLocation(),
                                    collection.getImageResourceID(),
                                    collection.getDocID(),
                                    Entity.TYPE_COLLECTION
                            ));
                        for (Item item : items)
                            ents.add(new Entity (
                                    item.getName(),
                                    item.getLocation(),
                                    item.getImageResourceID(),
                                    item.getDocID(),
                                    Entity.TYPE_ITEM
                            ));
                        successCB.onSuccess(ents);
                    }
                }, failureCB);
            }
        }, failureCB);
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
                                        collection.getOwner().equals(currentUserDocRef) ||
                                        collection.getAuthUsers().contains(currentUserDocRef)
                                )
                                {
                                    Log.d(TAG, "getCollection w/ id=" + document.getData());
                                    successCB.onSuccess(collection);
                                }
                                else
                                {
                                    Log.d (
                                            TAG,
                                            "getCollection invalid permissions "
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
     * Update Item.
     *
     * @param item Item to update.
     * @param successCB On Success.
     * @param failureCB On Failure.
     */
    public void updateItem (
            Item item,
            OnSuccessListener<Boolean> successCB,
            OnFailureListener failureCB
    )
    {
        db.collection("items").document(item.getDocID())
            .set(item)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                        successCB.onSuccess(true);
                    else
                        failureCB.onFailure(task.getException());
                }
            });
    }

    /**
     * Delete Item Based on ID.
     * @param itemID Item ID.
     * @param successCB On Success.
     * @param failureCB On Failure.
     */
    public void deleteItem (
            String itemID,
            OnSuccessListener<Boolean> successCB,
            OnFailureListener failureCB
    )
    {
        db.collection("items")
                .document(itemID)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                            successCB.onSuccess(true);
                        else
                            failureCB.onFailure(task.getException());
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
     * @param parentCollectionOwnerID Parent collection owner, document id.
     * @param successCB Callback for successful creation.
     * @param failureCB Callback for any creation related failures.
     */
    public void createCollection (
            Collection collection,
            String parentCollectionID,
            String parentCollectionOwnerID,
            OnSuccessListener<String> successCB,
            OnFailureListener failureCB
    )
    {
        if (auth.getUid() == null)
        {
            Log.d(TAG, "createCollection: user not logged in!");
            return;
        }

        collection.setOwner(db.collection("users").document(parentCollectionOwnerID));
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
                        if (!auth.getUid().equals(parentCollectionOwnerID))
                        {
                            documentReference.update (
                                    "authUsers",
                                    FieldValue.arrayUnion (
                                            db.document("/users/" + auth.getUid())
                                    )
                            );
                        }
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

    private void deleteAllItemsForCollection (
            String collectionID,
            OnSuccessListener<Boolean> successCB,
            OnFailureListener failureCB
    )
    {
        DocumentReference parentCollectionDR =
                db.collection("collections").document(collectionID);
        db.collection("items")
                .whereEqualTo("parentCollection", parentCollectionDR)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            Log.d ( TAG,
                                    "deleteAllItemsForCollection task successful... deleting now"
                            );
                            for (QueryDocumentSnapshot document : task.getResult())
                                document.getReference().delete();
                        }
                        else
                        {
                            Log.d ( TAG,
                                    "deleteAllItemsForCollection task unsuccessful " +
                                            task.getException()
                            );
                            failureCB.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Helper for Collection Deletion.
     *
     * Deletes all items in collection, and collection itself.
     * Then called recursively on all childCollections.
     *
     * @param collection Collection to delete.
     * @param successCB On Success.
     * @param failureCB On Failure.
     */
    private void deleteCollectionHelper (
            Collection collection,
            OnSuccessListener<Boolean> successCB,
            OnFailureListener failureCB
    )
    {
        Log.d(TAG, "deleteCollectionHelper: on collection id=" + collection.getDocID());
        // Delete Items
        deleteAllItemsForCollection(collection.getDocID(), new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {

            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                failureCB.onFailure(new Exception("ItemsDeletionFailure"));
            }
        });
        // Delete Collection
        db.collection("collections").document(collection.getDocID()).delete();
        // Call Deletion on Children Collections
        for (DocumentReference childCollectionDR : collection.getChildrenCollections())
        {
            // deleteCollectionHelper(childCollectionDR.getId(), successCB, failureCB);
            childCollectionDR.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful())
                    {
                        deleteCollectionHelper(task.getResult().toObject(Collection.class), successCB, failureCB);
                    }
                }
            });
        }
    }

    /**
     * Delete Collection.
     *
     * Adjusts childCollections of parentCollection if applicable
     * (not so for root collection), then calls deleteCollectionHelper.
     *
     * @param collectionID Collection ID.
     * @param successCB On Success. Passed dummy true Boolean.
     * @param failureCB On Failure. Passed exception.
     */
    public void deleteCollection (
            String collectionID,
            OnSuccessListener<Boolean> successCB,
            OnFailureListener failureCB
    )
    {
        getCollection(collectionID, new OnSuccessListener<Collection>() {
            @Override
            public void onSuccess(Collection collection) {
                if (!(collection.getOwner().getId()).equals(auth.getUid()))
                    failureCB.onFailure(new Exception("InvalidPermissionToDelete"));

                DocumentReference collectionDR =
                        db.collection("collections").document(collectionID);

                // Adjust parent
                if (collection.getParentCollection() != null)
                {
                    collection.getParentCollection()
                            .update (
                                    "childCollections",
                                    FieldValue.arrayRemove(collectionDR)
                            );
                }
                // Deletion
                deleteCollectionHelper(collection, new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "getCollection: failure trying to retrieve collection for deletion");
                failureCB.onFailure(e);
            }
        });
    }

    /* Collaboration */

    public void getCollabs (
            Collection collection,
            OnSuccessListener<ArrayList<User>> successCB,
            OnFailureListener failureCB
    )
    {
        ArrayList<String> authUserIds = new ArrayList<>();
        for (DocumentReference user : collection.getAuthUsers())
            authUserIds.add(user.getId());

        if (authUserIds.isEmpty())
        {
            successCB.onSuccess(new ArrayList<User>());
            return;
        }

        db.collection("users")
                .whereIn("uid", authUserIds)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            ArrayList<User> users = new ArrayList<>();
                            Log.d(TAG, "getCollabs: Collabs retrieved.");
                            for (QueryDocumentSnapshot document : task.getResult())
                                users.add(document.toObject(User.class));
                            successCB.onSuccess(users);
                        }
                        else
                        {
                            Log.d(TAG, "getCollabs: task failed getting collabs " + task.getException());
                            failureCB.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Add Collab to Collection.
     *
     * User is added as a collab on $collection, and all parent collections upstream up to root.
     *
     * @param collection Collection to add collab on.
     * @param userID UID of user to add as collab.
     * @param successCB
     * @param failureCB
     */
    public void addCollab (
            Collection collection,
            String userID,
            OnSuccessListener<Boolean> successCB,
            OnFailureListener failureCB
    )
    {
        DocumentReference userDR = db.collection("users").document(userID);
        if (!collection.getAuthUsers().contains(userDR))
        {
            // Add user as authorized user
            DocumentReference collectionDR =
                    db.collection("collections").document(collection.getDocID());
            collectionDR.update("authUsers", FieldValue.arrayUnion(userDR));
            // Climb upstream
            if (collection.getParentCollection() == null) return;
            collection.getParentCollection().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    addCollab (
                            task.getResult().toObject(Collection.class),
                            userID,
                            successCB,
                            failureCB
                    );
                }
            });
        }
    }

}

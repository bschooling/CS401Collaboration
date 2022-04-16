package com.example.cs401collaboration;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

    public static DatabaseService getInstance()
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
     * Create User
     * @param uid uid of authenticated user
     * @param name name of user
     */
    public void createUser(String uid, String name)
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
                }
            });
    }

}

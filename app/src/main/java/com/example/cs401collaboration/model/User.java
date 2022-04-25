package com.example.cs401collaboration.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

/**
 * @author Arshdeep Padda
 *
 * Represents application User. Correlates to database "users" collection.
 *
 * Complies with firebase document toObject().
 */
public class User
{
    /** Document ID */
    @DocumentId
    private String docID;

    /** User ID */
    private String uid;

    /** User Name */
    private String name;

    /** User Email */
    private String email;

    /** Document References to User's Collections. */
    private ArrayList<DocumentReference> collections;

    /** Document References to User's Collaborating Users. */
    private ArrayList<DocumentReference> collabs;

    /**
     * User - Empty Constructor.
     */
    public User ()
    {

    }

    /**
     * Get Document ID.
     * @return Document ID.
     */
    public String getDocID()
    {
        return docID;
    }

    /**
     * Get ID.
     * @return User ID.
     */
    public String getUid()
    {
        return uid;
    }

    /**
     * Get Name.
     * @return User Name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get Email.
     * @return User Email.
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Get User Collections.
     * @return Get list of User's collection as document references.
     */
    public ArrayList<DocumentReference> getCollections()
    {
        return collections;
    }

    /**
     * Get User Collaborators.
     * @return Get list of User's collaborators as document references to user documents.
     */
    public ArrayList<DocumentReference> getCollabs()
    {
        return collabs;
    }

};

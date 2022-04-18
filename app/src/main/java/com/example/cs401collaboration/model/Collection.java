package com.example.cs401collaboration.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

/**
 * @author Arshdeep Padda
 *
 * Represents Collection Entity. Correlates to database "collections" collection.
 *
 * Complies with firebase document toObject().
 */
public class Collection
{
    /** Document ID */
    @DocumentId
    private String docID;

    /** Collection Name */
    private String name;

    /** Description */
    private String description;

    /** Location */
    private String location;

    /** List of contained items as DocumentReferences. */
    ArrayList<DocumentReference> items;

    /** User owner of collection, as DocumentReference. */
    DocumentReference owner;

    /**
     * Empty Constructor.
     */
    public Collection ()
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
     * Get User Name.
     * @return Name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get Collection Description.
     * @return Description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Get Collection Location Field.
     * @return Location Field.
     */
    public String getLocation()
    {
        return location;
    }

    /**
     * Get Collection Item List, as DocumentReferences.
     * @return Item DocumentReference List.
     */
    public ArrayList<DocumentReference> getItems()
    {
        return items;
    }

    /**
     * Get Collection Owner, as DocumentReference.
     * @return Collection Owner DocumentReference.
     */
    public DocumentReference getOwner()
    {
        return owner;
    }

}

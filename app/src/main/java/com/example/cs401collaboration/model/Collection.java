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

    /** Image URL */
    private String imageResourceID = "placeholder.png";

    /** User owner of collection, as DocumentReference. */
    private DocumentReference owner;

    /** Collection authorized users, as DocumentReference. */
    private ArrayList<DocumentReference> authUsers;

    /** Parent Collection, as DocumentReference */
    private DocumentReference parentCollection;

    /** Children Collections, as DocumentReference */
    private ArrayList<DocumentReference> childrenCollections;

    /** List of contained items as DocumentReferences. */
    private ArrayList<DocumentReference> items;

    /**
     * Empty Constructor.
     */
    public Collection ()
    {

    }

    public Collection (String name)
    {
        this.name = name;
    }

    public Collection (String name, String location)
    {
        this(name);
        this.location = location;
    }

    public Collection (String name, String location, String description)
    {
        this(name, location);
        this.description = description;
    }

    public void setOwner (DocumentReference owner)
    {
        this.owner = owner;
    }

    public void setParentCollection (DocumentReference parentCollection)
    {
        this.parentCollection = parentCollection;
    }

    public void setArrayFieldsEmpty ()
    {
        this.items = new ArrayList<>();
        this.authUsers = new ArrayList<>();
        this.childrenCollections = new ArrayList<>();
    }

    // Copy method
    public void copyOther(Collection otherCollection) {
        docID = otherCollection.getDocID();
        name = otherCollection.getName();
        description = otherCollection.getDescription();
        location = otherCollection.getLocation();
        imageResourceID = otherCollection.getImageResourceID();
        owner = otherCollection.getOwner();
        authUsers = otherCollection.getAuthUsers();
        parentCollection = otherCollection.getParentCollection();
        childrenCollections = otherCollection.getChildrenCollections();
        items = otherCollection.getItems();
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
     * Set Name.
     * @param name Name.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Set Location.
     * @param location Location.
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * Set Description.
     * @param description Description.
     */
    public void setDescription(String description)
    {
        this.description = description;
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
     * Get Image Resource ID.
     * @return Image Resource ID.
     */
    public String getImageResourceID ()
    {
        return imageResourceID;
    }

    /**
     * Set Image Resource ID.
     * @param imageResourceID Image Resource ID.
     * @return
     */
    public void setImageResourceID (String imageResourceID)
    {
        this.imageResourceID = imageResourceID;
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

    public ArrayList<DocumentReference> getAuthUsers()
    {
        return authUsers;
    }

    public DocumentReference getParentCollection()
    {
        return parentCollection;
    }

    public ArrayList<DocumentReference> getChildrenCollections()
    {
        return childrenCollections;
    }

}

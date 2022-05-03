package com.example.cs401collaboration.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

/**
 * @author Arshdeep Padda
 *
 * Represents Collection Item. Correlates to database "items" collection.
 *
 * Complies with firebase document toObject().
 */
public class Item
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

    /** Image Resource ID */
    private String imageResourceID = "placeholder.png";

    /** User owner of collection, as DocumentReference. */
    private DocumentReference parentCollection;

    /**
     * Empty Constructor.
     */
    public Item ()
    {

    }

    public Item (String name)
    {
        this.name = name;
    }

    public Item (String name, String location)
    {
        this(name);
        this.location = location;
    }

    public Item (String name, String location, String description)
    {
        this(name, location);
        this.description = description;
    }

    public void copyOther (Item otherItem) {
        docID = otherItem.getDocID();
        name = otherItem.getName();
        description = otherItem.getDescription();
        location = otherItem.getLocation();
        imageResourceID = otherItem.getImageResourceID();
        parentCollection = otherItem.getParentCollection();
    }

    public void setParentCollection (DocumentReference parentCollection)
    {
        this.parentCollection = parentCollection;
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
     * Get Item Name.
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
     * Get Item Description.
     * @return Description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Get Item Location Field.
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
        return this.imageResourceID;
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
     * Get Parent Collection.
     * @return Parent Collection, as Document Reference.
     */
    public DocumentReference getParentCollection()
    {
        return this.parentCollection;
    }

}

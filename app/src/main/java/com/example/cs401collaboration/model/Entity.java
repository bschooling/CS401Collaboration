package com.example.cs401collaboration.model;

import com.google.firebase.firestore.DocumentReference;

/**
 * Entity represents a object that mimics Collection or Item for RecyclerView.
 *
 * It has fields such as image resource identifier, first line, second line,
 * and document id and collection / item specifier.
 *
 * @author Arshdeep Padda
 */
public class Entity
{
    /** First Line to Display */
    private String firstLine = null;

    /** Second Line to Display */
    private String secondLine = null;

    /** Image Resource ID */
    private String imageResourceID = null;

    /**
     * Type of entity.
     * Check against TYPE_COLLECTION or TYPE_ITEM.
     */
    private Integer type = 0;

    /** Code for if entity is "collection". */
    public static Integer TYPE_COLLECTION = 1;
    /** Code for if entity is "item". */
    public static Integer TYPE_ITEM = 2;

    /** Document ID of Entity */
    private String docID = null;

    /**
     * Construct entity with all 5 fields.
     *
     * @param fl First line of display.
     * @param sl Second line of display.
     * @param img Image URL.
     * @param docid Document ID.
     * @param type Type of Entity.
     */
    public Entity (
            String fl,
            String sl,
            String img,
            String docid,
            Integer type
    )
    {
        this.firstLine = fl;
        this.secondLine = sl;
        this.imageResourceID = img;
        this.docID = docid;
        this.type = type;
    }

    /**
     * Get First Line.
     * @return First Line.
     */
    public String getFirstLine ()
    {
        return firstLine;
    }

    /**
     * Get Second Line.
     * @return Second Line.
     */
    public String getSecondLine ()
    {
        return secondLine;
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
     * Get Document ID.
     * @return Document ID.
     */
    public String getDocID ()
    {
        return docID;
    }

    /**
     * Get Type of Entity.
     *
     * Check against Entity.
     *
     * @return Type of Entity.TYPE_COLLECTION or Entity.TYPE_ITEM.
     */
    public Integer getType ()
    {
        return type;
    }

}

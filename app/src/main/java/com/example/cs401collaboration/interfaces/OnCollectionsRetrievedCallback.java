package com.example.cs401collaboration.interfaces;

import com.example.cs401collaboration.model.Collection;

import java.util.ArrayList;

/**
 * @author Arshdeep Padda
 *
 * OnCollectionsRetrievedCallback.OnCollectionsRetrieved(ArrayList<Collection> collections)
 * is called when a database query for collections is received and ready for use.
 */
public interface OnCollectionsRetrievedCallback
{
    /**
     * Callback to operate on collections once received.
     * @param collections ArrayList of Collections.
     */
    void OnCollectionsRetrieved(ArrayList<Collection> collections);
}

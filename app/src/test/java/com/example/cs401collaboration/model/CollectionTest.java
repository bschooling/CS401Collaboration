package com.example.cs401collaboration.model;

import static org.junit.Assert.*;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

public class CollectionTest {
    @Test
    public void setOwner() {
        Collection c = new Collection();
        c.setOwner(null);
        assertEquals("Test", null, c.getOwner());

    }

    @Test
    public void setArrayFieldsEmpty() {
        Collection c = new Collection();
        c.setArrayFieldsEmpty();

        assertEquals("c's items Array List length should be 0",0,c.getItems().size());
        assertEquals("c's authUsers Array List length should be 0", 0, c.getAuthUsers().size());
        assertEquals("c's childCollections Array List length should be 0", 0, c.getChildrenCollections().size());
    }

    @Test
    public void setName() {
        Collection c = new Collection();
        c.setName("Test");
        assertEquals("Name of Collection c should be Test", "Test", c.getName());
    }

    @Test
    public void setLocation() {
        Collection c = new Collection();
        c.setLocation("Uranus");
        assertEquals("Location of Collection c should be Uranus", "Uranus", c.getLocation());
    }

    @Test
    public void setDescription() {
        Collection c = new Collection();
        c.setDescription("Filler Desc");
        assertEquals("Description of Collection c should be Filler Desc", "Filler Desc", c.getDescription());
    }

    @Test
    public void setImageResourceID() {
        Collection c = new Collection();
        c.setImageResourceID("asdf1234");
        assertEquals("ImageResourceID for Collection c should be asdf1234", "asdf1234", c.getImageResourceID());
    }
}
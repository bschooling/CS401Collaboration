package com.example.cs401collaboration.model;

import static org.junit.Assert.*;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;


import org.junit.Test;

public class CollectionTest {
    /**
     * Test method for {@link Collection#Collection(String)}.
     */
    @Test
    public void testCollection() {
        //Collection obj c(String)
        Collection c = new Collection ("Lord of the Rings");
        assertEquals("Lord of the Rings", c.getName());
    }

    /**
     * Test method for {@link Collection#Collection(String, String)}.
     */
    @Test
    public void testCollection2() {
        //Collection obj c(String name, String location)
        Collection c = new Collection("Lord of the Blings", "Jason's House");
        assertEquals("Lord of the Blings", c.getName());
        assertEquals("Jason's House", c.getLocation());
    }

    /**
     * Test method for {@link Collection#Collection(String, String, String)}
     */
    @Test
    public void testCollection3() {
        //Collection obj c(String name, String location, String description)
        Collection c = new Collection("Book Case 1","Jason's House", "Books that Jason has");
        assertEquals("Book Case 1", c.getName());
        assertEquals("Jason's House", c.getLocation());
        assertEquals("Books that Jason has", c.getDescription());
    }

    /**
     * Test method for {@link Collection#setOwner(DocumentReference)}.
     */
    @Test
    public void testSetOwner() {
        Collection c = new Collection();
        c.setOwner(null);
        assertNull(c.getOwner());

    }

    /**
     * Test method for {@link Collection#setArrayFieldsEmpty()}.
     */
    @Test
    public void testSetArrayFieldsEmpty() {
        Collection c = new Collection();
        c.setArrayFieldsEmpty();
        
        assertEquals(0, c.getItems().size());
        assertEquals(0, c.getAuthUsers().size());
        assertEquals(0, c.getChildrenCollections().size());
    }

    /**
     * Test method for {@link Collection#setName(String)}
     */
    @Test
    public void testSetName() {
        Collection c = new Collection();
        c.setName("Test");
        assertEquals("Name of Collection c should be Test", "Test", c.getName());
    }

    /**
     * Test method for {@link Collection#setLocation(String)}.
     */
    @Test
    public void testSetLocation() {
        Collection c = new Collection();
        c.setLocation("Uranus");
        assertEquals("Location of Collection c should be Uranus", "Uranus", c.getLocation());
    }

    /**
     * Test method for {@link Collection#setDescription(String)}.
     */
    @Test
    public void testSetDescription() {
        Collection c = new Collection();
        c.setDescription("Filler Desc");
        assertEquals("Description of Collection c should be Filler Desc", "Filler Desc", c.getDescription());
    }

    /**
     * Test method for {@link Collection#setImageResourceID(String)}.
     */
    @Test
    public void testSetImageResourceID() {
        Collection c = new Collection();
        c.setImageResourceID("placeholder.png");
        assertEquals("placeholder.png", c.getImageResourceID());
    }
}
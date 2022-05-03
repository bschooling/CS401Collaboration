package com.example.cs401collaboration.model;

import static org.junit.Assert.*;

import com.google.firebase.firestore.DocumentReference;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jason Lim
 */
public class ItemTest {
    //Each portion (ParentCollection, Name, Location, etc) will be individually portioned, thus not requiring a @before method.
    Item mockItem = new Item();

    /**
     * Test method for {@link Item#setParentCollection(DocumentReference)}.
     */
    @Test
    public void testSetParentCollection() {
        mockItem.setParentCollection(null);
        assertNull(mockItem.getParentCollection());
    }

    /**
     * Test method for {@link Item#setName(String)}.
     */
    @Test
    public void testSetName() {
        mockItem.setName("Abyssal Whip");
        assertEquals("Abyssal Whip", mockItem.getName());
    }

    /**
     * Test method for {@link Item#setLocation(String)}
     */
    @Test
    public void testSetLocation() {
        mockItem.setLocation("Ver Shinhaza");
        assertEquals("Ver Shinhaza", mockItem.getLocation());
    }

    /**
     * Test method for {@link Item#setDescription(String)}
     */
    @Test
    public void testSetDescription() {
        String mockDesc = "An iconic and powerful one-handed melee weapon requiring an Attack level of 70 to wield.";
        mockItem.setDescription(mockDesc);
        assertEquals(mockDesc,mockItem.getDescription());
    }

    /**
     * Test method for {@link Item#setImageResourceID(String)}
     */
    @Test
    public void testSetImageResourceID() {
        mockItem.setImageResourceID("0pSAWfU");
        assertEquals("0pSAWfU",mockItem.getImageResourceID());
    }
}
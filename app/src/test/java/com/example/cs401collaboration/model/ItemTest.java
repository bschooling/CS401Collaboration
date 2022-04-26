package com.example.cs401collaboration.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ItemTest {
    Item mockItem = new Item();
    @Test
    public void setParentCollection() {
        mockItem.setParentCollection(null);
        assertEquals("mockItem's parent collection should be null", null, mockItem.getParentCollection());
    }

    @Test
    public void setName() {
        mockItem.setName("Abyssal Whip");
        assertEquals("mockItem's name should be Abyssal Whip", "Abyssal Whip", mockItem.getName());
    }

    @Test
    public void setLocation() {
        mockItem.setLocation("Ver Shinhaza");
        assertEquals("mockItem's location should be Ver Shinhaza", "Ver Shinhaza", mockItem.getLocation());
    }

    @Test
    public void setDescription() {
        String mockDesc = "An iconic and powerful one-handed melee weapon requiring an Attack level of 70 to wield.";
        mockItem.setDescription(mockDesc);
        assertEquals("Desc should match", mockDesc,mockItem.getDescription());
    }

    @Test
    public void setImageResourceID() {
        mockItem.setImageResourceID("0pSAWfU");
        assertEquals("Image Resource ID should match", "0pSAWfU",mockItem.getImageResourceID());
    }
}
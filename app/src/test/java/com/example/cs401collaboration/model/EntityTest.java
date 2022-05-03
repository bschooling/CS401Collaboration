package com.example.cs401collaboration.model;


import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Jason Lim
 */
public class EntityTest {
    Entity testEntity = new Entity(     "firstLine",
                                        "secondLine",
                                        "image",
                                        "documentID",
                                        0);

    /**
     * Test method for {@link Entity#setFirstLine(String)}
     */
    @Test
    public void testSetFirstLine() {
        assertEquals("firstLine",testEntity.getFirstLine());
    }

    /**
     * Test method for {@link Entity#setSecondLine(String)}
     */
    @Test
    public void testSetSecondLine() {
        assertEquals("secondLine", testEntity.getSecondLine());
    }

    /**
     * Test method for {@link Entity#setImageResourceID(String)}
     */
    @Test
    public void testSetImageResourceID() {
        assertEquals("image", testEntity.getImageResourceID());
    }

    /**
     * Test method for {@link Entity#setDocID(String)}
     */
    @Test
    public void testSetDocID() {
        assertEquals("documentID", testEntity.getDocID());
    }

    /**
     * Test method for {@link Entity#setType(Integer)}
     */
    @Test
    public void testSetType() {

        assertEquals((Integer)0,testEntity.getType());
    }
}
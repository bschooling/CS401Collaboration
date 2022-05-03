package com.example.cs401collaboration.model;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Jason Lim
 */
public class UserTest {
    //@BeforeEach not required as each method calls for a different portion of data from testUser.
    User testUser = new User();
    /**
     * Test method for {@link User#getDocID()}.
     */
    @Test
    public void testGetDocID() {
        assertNull(testUser.getDocID());
    }

    /**
     * Test method for {@link User#getUid()}.
     */
    @Test
    public void testGetUid() {
        assertNull(testUser.getUid());
    }

    /**
     * Test method for {@link User#getName()}.
     */
    @Test
    public void testGetName() {
        assertNull(testUser.getName());
    }

    /**
     * Test method for {@link User#getEmail()}.
     */
    @Test
    public void testGetEmail() {
        assertNull(testUser.getEmail());
    }

    /**
     * Test method for {@link User#getCollections()}
     */
    @Test
    public void testGetCollections() {
        assertNull(testUser.getCollections());
    }

    /**
     * Test method for {@link User#getCollabs()}
     */
    @Test
    public void testGetCollabs() {
        assertNull(testUser.getCollabs());
    }
}
package com.example.cs401collaboration.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class EntityTest {
    Entity testEntity = new Entity(     "1stLineA",
                                        "2ndLineB",
                                        "imgStringC",
                                        "docidD",
                                        0);
    @Test
    public void setFirstLine() {
        assertEquals(   "testEntity's First Line should be 1stLineA",
                        "1stLineA",
                        testEntity.getFirstLine());


    }

    @Test
    public void setSecondLine() {
        assertEquals(   "testEntity's Second Line should be 2ndLineB",
                "2ndLineB",
                testEntity.getSecondLine());
    }

    @Test
    public void setImageResourceID() {
        assertEquals(   "testEntity's ImageResourceID should be imgStringC",
                "imgStringC",
                testEntity.getImageResourceID());
    }

    @Test
    public void setDocID() {
        assertEquals(   "testEntity's DocID should be docidD",
                "docidD",
                testEntity.getDocID());
    }

    @Test
    public void setType() {
        fail();
        /*
        assertEquals("testEntity's type should be 0",
                0,
                testEntity.getType());

         */
    }
}
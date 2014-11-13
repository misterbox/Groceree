package com.example.skybox.groceree;

/**
 * Created by skybox on 11/12/14.
 */
public class Item {
    private long id;
    private String item;

    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem( String item ) {
        this.item = item;
    }

    // Will be used by the ArrayAdapter in the ListView
    public String toString() {
        return item;
    }
}

package com.example.skybox.groceree;

/**
 * Created by skybox on 11/12/14.
 */
public class Item {
    private long id; // Column index 0
    private String item; //Column index 1
    private boolean isDeleted; // Column index 2
    private int timeStamp; // Column index 3


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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted( boolean isDeleted ) {
        this.isDeleted = isDeleted;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    // TODO: getTimeStampAsString

    public void setTimeStamp( int timeStamp ) {
        this.timeStamp = timeStamp;
    }
}
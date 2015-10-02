package com.theskyegriffin.groceree;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by skybox on 11/12/14.
 */
public class Item implements Comparable< Item > {
    private String id; // Column index 0
    private String item; //Column index 1
    private boolean isMarked;   // Column index 2
    private boolean isDeleted; // Column index 3
    private long timestamp; // Column index 4
    private long version;   // Column index 5
    private boolean isPending; // Column index 6

    // Implemented to support the 'indexOf()' method for an Item list
    @Override
    public boolean equals( Object o ) {
        if( ( o != null ) && ( o.getClass().equals( this.getClass() ) ) ) {
            return o.toString().equals( this.toString() );
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public int compareTo( Item another ) {
        return this.toString().compareTo( another.toString() );
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
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
        return id;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked( boolean isMarked ) {
        this.isMarked = isMarked;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted( boolean isDeleted ) {
        this.isDeleted = isDeleted;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    // Helper function to get item TimeStamp as human-readable string.
    // Source: http://tips.androidhive.info/2013/10/android-insert-datetime-value-in-sqlite-database/
    public String getTimeStampString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault() );
        return dateFormat.format( timestamp * 1000 );
    }

    public void setTimeStamp( long timeStamp ) {
        this.timestamp = timeStamp;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion( long version ) {
        this.version = version;
    }

    public void setPending( boolean isPending ) {
        this.isPending = isPending;
    }

    public boolean isPending() {
        return isPending;
    }
}

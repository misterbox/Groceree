package com.theskyegriffin.groceree;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by skybox on 12/14/14.
 */
public class ItemTable {
    public static final String TABLE_ITEM = "item";
    public static final String COLUMN_ITEM_ID = "_id";
    public static final String COLUMN_ITEM = "item";
    public static final String COLUMN_ISMARKED = "isMarked";    // Represents if an item has been striked off the list, but not deleted.
    public static final String COLUMN_ISDELETED = "isDeleted";  // Represents if an item has been removed from the list.
    public static final String COLUMN_ITEM_TIMESTAMP = "timestamp";  // Timestamp of last update to the row.
    public static final String COLUMN_VERSION = "version";
    public static final String COLUMN_ISPENDING = "isPending";

    private static final String DATABASE_NAME = "item.db";
    private static final int DATABASE_VERSION = 3;

    // Item table creation statement
    private static final String ITEM_TABLE_CREATE = "create table " + TABLE_ITEM
            + "(" + COLUMN_ITEM_ID + " text unique primary key, "
            + COLUMN_ITEM + " text not null, "
            + COLUMN_ISDELETED + " boolean not null default 0, " // default is false
            + COLUMN_ISMARKED + " boolean not null default 0, " // default is false
            + COLUMN_ITEM_TIMESTAMP  + " datetime default current_timestamp, "
            + COLUMN_VERSION + " integer not null, "
            + COLUMN_ISPENDING + " boolean not null default 0 );";

    public static void onCreate( SQLiteDatabase database ) {
        database.execSQL( ITEM_TABLE_CREATE );
    }

    public static void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        Log.w(ItemTable.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion);
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_ITEM );
        onCreate( db );
    }
}

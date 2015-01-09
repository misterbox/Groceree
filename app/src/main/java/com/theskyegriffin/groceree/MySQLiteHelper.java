package com.theskyegriffin.groceree;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by skybox on 11/11/14.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    // Item table
    public static final String TABLE_ITEM = "item";
    public static final String COLUMN_ITEM_ID = "_id";
    public static final String COLUMN_ITEM = "item";
    public static final String COLUMN_ISMARKED = "isMarked";    // Represents if an item has been striked off the list, but not deleted.
    public static final String COLUMN_ISDELETED = "isDeleted";  // Represents if an item has been removed from the list.
    public static final String COLUMN_ITEM_TIMESTAMP = "timestamp";  // Timestamp of last update to the row.

    private static final String DATABASE_NAME = "item.db";
    private static final int DATABASE_VERSION = 1;

    // Item table creation statement
    private static final String ITEM_TABLE_CREATE = "create table " + TABLE_ITEM
            + "(" + COLUMN_ITEM_ID + " integer primary key autoincrement, "
            + COLUMN_ITEM + " text not null, "
            + COLUMN_ISDELETED + " boolean not null default 0, " // default is false
            + COLUMN_ISMARKED + " boolean not null default 0, " // default is false
            + COLUMN_ITEM_TIMESTAMP  + " datetime default current_timestamp);";

   public MySQLiteHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    public void onCreate( SQLiteDatabase database ) {
        database.execSQL( ITEM_TABLE_CREATE );
    }

    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        Log.w( MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                + newVersion );
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_ITEM );
        onCreate( db );
    }
}

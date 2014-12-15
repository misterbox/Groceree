package com.example.skybox.groceree;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by skybox on 12/14/14.
 */
public class ItemDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "item.db";
    private static final int DATABASE_VERSION = 1;

    public ItemDatabaseHelper( Context context ) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onCreate( SQLiteDatabase database ) {
        ItemTable.onCreate( database );
    }

    public void onUpgrade( SQLiteDatabase database, int oldVersion, int newVersion ) {
        ItemTable.onUpgrade( database, oldVersion, newVersion );
    }
}

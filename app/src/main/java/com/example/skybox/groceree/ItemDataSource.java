package com.example.skybox.groceree;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by skybox on 11/12/14.
 */
public class ItemDataSource {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.TABLE_ITEM,
        MySQLiteHelper.COLUMN_DELETED, MySQLiteHelper.COLUMN_TIMESTAMP };

    public ItemDataSource( Context context ) {
        dbHelper = new MySQLiteHelper( context );
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Item createItem( String item ) {
        ContentValues values = new ContentValues();
        values.put( MySQLiteHelper.COLUMN_ITEM, item );
        values.put( MySQLiteHelper.COLUMN_DELETED, 0 );
        values.put( MySQLiteHelper.COLUMN_TIMESTAMP, getDateTime() );
        long insertId = database.insert( MySQLiteHelper.TABLE_ITEM, null, values );

        Cursor cursor = database.query( MySQLiteHelper.TABLE_ITEM, allColumns, MySQLiteHelper.COLUMN_ID
         + " = " + insertId, null, null, null, null );
        cursor.moveToFirst();
        Item newItem = cursorToItem( cursor );
        cursor.close();

        return newItem;
    }

    // TODO: Update item

    public void deleteItem( Item item ) {
        long id = item.getId();

        System.out.println( "Entry deleted with id: " + id );
        database.delete( MySQLiteHelper.TABLE_ITEM, MySQLiteHelper.COLUMN_ID + " = "
         + id, null);
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<Item>();

        Cursor cursor = database.query( MySQLiteHelper.TABLE_ITEM, allColumns,
                null, null, null, null, null );

        cursor.moveToFirst();

        while( !cursor.isAfterLast() ) {
            Item item = cursorToItem( cursor );
            items.add( item );
            cursor.moveToNext();
        }

        cursor.close();

        return items;
    }

    private Item cursorToItem( Cursor cursor ){
        Item item = new Item();
        int isDeletedInt;
        item.setId( cursor.getLong( 0 ) );
        item.setItem( cursor.getString( 1 ) );

        isDeletedInt = cursor.getInt( 2 );
        if( isDeletedInt != 0 )
            item.setDeleted( true );
        else
            item.setDeleted( false );

        item.setTimeStamp( cursor.getInt( 3 ) );

        return item;
    }

    // Helper function for createItem() to get current DateTime
    // Source: http://tips.androidhive.info/2013/10/android-insert-datetime-value-in-sqlite-database/
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault() );
        Date date = new Date();
        return dateFormat.format( date );
    }
}
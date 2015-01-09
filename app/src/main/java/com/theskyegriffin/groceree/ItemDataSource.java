package com.theskyegriffin.groceree;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by skybox on 11/12/14.
 */
public class ItemDataSource {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ITEM_ID, MySQLiteHelper.COLUMN_ITEM,
        MySQLiteHelper.COLUMN_ISMARKED, MySQLiteHelper.COLUMN_ISDELETED, MySQLiteHelper.COLUMN_ITEM_TIMESTAMP };

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
        values.put( MySQLiteHelper.COLUMN_ISMARKED, 0 );    // default is false
        values.put( MySQLiteHelper.COLUMN_ISDELETED, 0 );   // default is false
        values.put( MySQLiteHelper.COLUMN_ITEM_TIMESTAMP, getCurrentTime() );
        long insertId = database.insert( MySQLiteHelper.TABLE_ITEM, null, values );

        Cursor cursor = database.query( MySQLiteHelper.TABLE_ITEM, allColumns, MySQLiteHelper.COLUMN_ITEM_ID
         + " = " + insertId, null, null, null, null );
        cursor.moveToFirst();
        Item newItem = cursorToItem( cursor );
        cursor.close();

        String message = String.format( "Item created with id %d, name %s and timeStamp %s", insertId, item, newItem.getTimeStampString() );
        Log.w( MySQLiteHelper.class.getName(), message );

        return newItem;
    }

    public long insertItem( Item item ) {
        if( item == null ) {
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put( MySQLiteHelper.COLUMN_ITEM, item.toString() );
        values.put( MySQLiteHelper.COLUMN_ISMARKED, item.isMarked() );    // default is false
        values.put( MySQLiteHelper.COLUMN_ISDELETED, item.isDeleted() );   // default is false
        values.put( MySQLiteHelper.COLUMN_ITEM_TIMESTAMP, item.getTimeStamp() );
        long insertId = database.insert( MySQLiteHelper.TABLE_ITEM, null, values );

        item.setId( insertId );

        return insertId;
    }

    // TODO: Update item
    public int updateItem( Item item, long timestamp ) {
        ContentValues values = new ContentValues();
        values.put( MySQLiteHelper.COLUMN_ITEM, item.toString() );
        values.put( MySQLiteHelper.COLUMN_ISMARKED, item.isMarked() );
        values.put( MySQLiteHelper.COLUMN_ISDELETED, item.isDeleted() );
        values.put( MySQLiteHelper.COLUMN_ITEM_TIMESTAMP, timestamp );

        return database.update( MySQLiteHelper.TABLE_ITEM, values, MySQLiteHelper.COLUMN_ITEM_ID + " =?",
                new String[] { String.valueOf( item.getId() ) } );
    }

    public void deleteItem( Item item ) {
        long id = item.getId();

        database.delete( MySQLiteHelper.TABLE_ITEM, MySQLiteHelper.COLUMN_ITEM_ID + " = "
         + id, null);
    }

    // Method used by 'MainActivity' to get all items with 'isDeleted' set to false.
    // Basically gets all items on the list that haven't been deleted.
    public List<Item> getAllActiveItems() {
        Cursor cursor = database.query( MySQLiteHelper.TABLE_ITEM, allColumns,
                MySQLiteHelper.COLUMN_ISDELETED + " =?", new String[] { String.valueOf( 0 ) }, null, null, null );

        return cursorToList( cursor );
    }

    public List<Item> getAllItems() {
        Cursor cursor = database.query( MySQLiteHelper.TABLE_ITEM, allColumns,
                null, null, null, null, null );

        return cursorToList( cursor );
    }

    public List<Item> getItemsSinceTimestamp( long timestamp ) {
        Cursor cursor = database.query( MySQLiteHelper.TABLE_ITEM, allColumns,
                MySQLiteHelper.COLUMN_ITEM_TIMESTAMP + " >?", new String[] { String.valueOf( timestamp ) }, null, null, null );

        return cursorToList( cursor );
    }

    private List<Item> cursorToList( Cursor cursor ) {
        List<Item> items = new ArrayList<Item>();

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
        item.setId( cursor.getLong( 0 ) );
        item.setItem( cursor.getString( 1 ) );

        int isMarkedInt = cursor.getInt( 2 );
        if( isMarkedInt != 0 ) {
            item.setMarked( true );
        } else {
            item.setMarked( false );
        }

        int isDeletedInt = cursor.getInt( 3 );
        if( isDeletedInt != 0 ) {
            item.setDeleted(true);
        } else {
            item.setDeleted(false);
        }

        item.setTimeStamp( cursor.getInt( 4 ) );

        return item;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

}

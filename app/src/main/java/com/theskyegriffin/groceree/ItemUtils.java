package com.theskyegriffin.groceree;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skybox on 6/4/15.
 */
public class ItemUtils {
    // Convert a cursor of Items in to an ArrayList
    public static List<Item> cursorToList( Cursor cursor ) {
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

    // Convert an individual Cursor row in to an Item
    private static Item cursorToItem( Cursor cursor ){
        Item item = new Item();
        int idColIdx = cursor.getColumnIndex( ItemTable.COLUMN_ITEM_ID );
        item.setId( cursor.getString( idColIdx ) );

        int itemColIdx = cursor.getColumnIndex( ItemTable.COLUMN_ITEM );
        item.setItem( cursor.getString( itemColIdx ) );

        int isMarkedColIdx = cursor.getColumnIndex( ItemTable.COLUMN_ISMARKED );
        int isMarkedInt = cursor.getInt( isMarkedColIdx );

        if( isMarkedInt != 0 ) {
            item.setMarked( true );
        } else {
            item.setMarked( false );
        }

        int isDeletedColIdx = cursor.getColumnIndex( ItemTable.COLUMN_ISDELETED );
        int isDeletedInt = cursor.getInt( isDeletedColIdx );
        if( isDeletedInt != 0 ) {
            item.setDeleted( true );
        } else {
            item.setDeleted( false );
        }

        int timeColIdx = cursor.getColumnIndex( ItemTable.COLUMN_ITEM_TIMESTAMP );
        item.setTimeStamp( cursor.getInt( timeColIdx ) );
        int versionColIdx = cursor.getColumnIndex( ItemTable.COLUMN_VERSION );
        item.setVersion( cursor.getInt( versionColIdx ) );
        int isPendingColIdx = cursor.getColumnIndex( ItemTable.COLUMN_ISPENDING );
        int isPendingInt = cursor.getInt( isPendingColIdx );

        if( isPendingInt != 0 ) {
            item.setPending( true );
        } else {
            item.setPending( false );
        }

        return item;
    }

}

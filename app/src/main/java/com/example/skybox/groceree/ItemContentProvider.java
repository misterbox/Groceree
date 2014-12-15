package com.example.skybox.groceree;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by skybox on 12/14/14.
 * Credit: http://www.vogella.com/tutorials/AndroidSQLite/article.html#todo
 */
public class ItemContentProvider extends ContentProvider {
    private ItemDatabaseHelper database;

    private static final int ITEMS = 10;
    private static final int ITEM_ID = 20;

    private static final String AUTHORITY = "com.example.skybox.groceree.contentprovider";

    private static final String BASE_PATH = "items";
    public static final Uri CONTENT_URI = Uri.parse( "content://" + AUTHORITY + "/" + BASE_PATH );
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/items";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/item";

    private static final UriMatcher sURIMatcher = new UriMatcher( UriMatcher.NO_MATCH );
    static {
        sURIMatcher.addURI( AUTHORITY, BASE_PATH, ITEMS );
        sURIMatcher.addURI( AUTHORITY, BASE_PATH + "/#", ITEM_ID );
    }

    @Override
    public boolean onCreate() {
        database = new ItemDatabaseHelper( getContext() );
        return false;
    }

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs,
                         String sortOrder ) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        checkColumns( projection );

        queryBuilder.setTables( ItemTable.TABLE_ITEM );

        int uriType = sURIMatcher.match( uri );

        switch( uriType ) {
            case ITEMS:
                break;
            case ITEM_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere( ItemTable.COLUMN_ITEM_ID + "=" + uri.getLastPathSegment() );
                break;
            default:
                throw new IllegalArgumentException( "Unknown URI: " + uri );
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query( db, projection, selection, selectionArgs, null, null, sortOrder );

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri( getContext().getContentResolver(), uri );

        return cursor;
    }

    @Override
    public String getType( Uri uri ) {
        return null;
    }

    @Override
    public Uri insert( Uri uri, ContentValues values ) {
        int uriType = sURIMatcher.match( uri );
        SQLiteDatabase sqlDB = database.getWritableDatabase();

        int rowsDeleted = 0;
        long id = 0;

        switch( uriType ) {
            case ITEMS:
                id = sqlDB.insert( ItemTable.TABLE_ITEM, null, values );
                break;
            default:
                throw new IllegalArgumentException( "Unknown URI: " + uri );
        }

        getContext().getContentResolver().notifyChange( uri, null );

        return Uri.parse( BASE_PATH + "/" + id );
    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs ) {
        int uriType = sURIMatcher.match( uri );
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;

        switch( uriType ) {
            case ITEMS:
                rowsDeleted = sqlDB.delete( ItemTable.TABLE_ITEM, selection, selectionArgs );
                break;
            case ITEM_ID:
                String id = uri.getLastPathSegment();

                if(TextUtils.isEmpty( selection ) ) {
                    rowsDeleted = sqlDB.delete( ItemTable.TABLE_ITEM, ItemTable.COLUMN_ITEM_ID +
                        "=" + id, null );
                } else {
                    rowsDeleted = sqlDB.delete( ItemTable.TABLE_ITEM, ItemTable.COLUMN_ITEM_ID + "="
                        + id + " and " + selection, selectionArgs );
                }
                break;
            default:
                throw new IllegalArgumentException( "Unknown URI: " + uri );
        }

        getContext().getContentResolver().notifyChange( uri, null );
        return rowsDeleted;
    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs ) {
        int uriType = sURIMatcher.match( uri );
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;

        switch( uriType ) {
            case ITEMS:
                rowsUpdated = sqlDB.update( ItemTable.TABLE_ITEM, values, selection, selectionArgs );
                break;
            case ITEM_ID:
                String id = uri.getLastPathSegment();

                if( TextUtils.isEmpty( selection ) ) {
                    rowsUpdated = sqlDB.update( ItemTable.TABLE_ITEM, values, ItemTable.COLUMN_ITEM_ID
                        + "=" + id, null );
                } else {
                    rowsUpdated = sqlDB.update( ItemTable.TABLE_ITEM, values, ItemTable.COLUMN_ITEM_ID
                        + "=" + id + " and " + selection, selectionArgs );
                }
                break;
            default:
                throw new IllegalArgumentException( "Unknown URI: " + uri );
        }

        getContext().getContentResolver().notifyChange( uri, null );
        return rowsUpdated;
    }

    private void checkColumns( String[] projection ) {
        String[] available = { ItemTable.COLUMN_ITEM_ID, ItemTable.COLUMN_ITEM,
            ItemTable.COLUMN_ISMARKED, ItemTable.COLUMN_ISDELETED, ItemTable.COLUMN_ITEM_TIMESTAMP };

        if( projection != null ) {
            HashSet<String> requestedColumns = new HashSet<String>( Arrays.asList( projection ) );
            HashSet<String> availableColumns = new HashSet<String>( Arrays.asList( available ) );

            // Check if all requested columns are available
            if( !availableColumns.containsAll( requestedColumns ) ) {
                throw new IllegalArgumentException( "Unknown columns in projection" );
            }
        }
    }
}

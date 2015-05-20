package com.theskyegriffin.groceree;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by skybox on 11/24/14.
 * Abstracts data transfer to/from the Groceree server
 * Handles tracking the most recent check-in, polling for list updates,
 * and updating our database adapters with recent item changes.
 */
public class ServerDataSource {
    private String TAG = this.getClass().getSimpleName();
    private long lastCheckIn;
    private RequestQueue mRequestQueue;

    private String serverURL = "http://theskyegriffin.com:8000";
    private String listEndPoint = "/list";
    private String syncEndPoint = "/syncItems";

    private List<Item> itemsReceived;         // List of Item objects received from server
    private List<Item> itemsStored;           // List of Item objects we currently know about

    // Allows us to store the most recent check-in timestamp
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor prefEditor;
    private String TIMESTAMP_PREF = "last_checkin_timestamp";

    private ContentProviderClient providerClient;

    String[] allColumns = { ItemTable.COLUMN_ITEM_ID, ItemTable.COLUMN_ITEM, ItemTable.COLUMN_ISMARKED,
            ItemTable.COLUMN_ISDELETED, ItemTable.COLUMN_ITEM_TIMESTAMP, ItemTable.COLUMN_VERSION,
            ItemTable.COLUMN_ISPENDING };


    public ServerDataSource( Context context ) {
        sharedPref = context.getSharedPreferences( TAG, Context.MODE_PRIVATE );
        prefEditor = sharedPref.edit();

        this.lastCheckIn = getLastCheckIn();
        mRequestQueue = Volley.newRequestQueue( context );

        itemsReceived = new ArrayList<Item>();
    }

    // TODO: Should we use a ConnectionMananger here to determine if the network is available?
    public void serverSync( ContentProviderClient client ) {
        providerClient = client;

        System.out.println( "last check-in: " + lastCheckIn );

        // Clear all items received before fetching a new list
        itemsReceived.clear();

        // Check last check-in timestamp
        // If timestamp is 0, we've never checked in, so we get all active items from the server
        // GET request to '/list' endpoint
        if( lastCheckIn == 0 ) {
            JsonObjectRequest jsonReq = new JsonObjectRequest( Request.Method.GET, serverURL + listEndPoint, null, new jsonRespListener(), new jsonRespErrListener() );

            mRequestQueue.add( jsonReq );
        } else { // POST request to '/syncItems' endpoint

            // Get items that are set to pending
            List<Item> updatedItems = getPendingItems();

            if( updatedItems != null ) {
                // Convert 'updatedItems' to JSON array
                JSONArray itemsAry = itemsToJSONAry( updatedItems );
                JSONObject postObj = new JSONObject();

                // Build our JSON object to post
                try {
                    postObj.put( "timestamp", lastCheckIn );
                    postObj.put( "items", itemsAry );
                } catch( Exception e ) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonReq = new JsonObjectRequest( Request.Method.POST, serverURL + syncEndPoint, postObj, new jsonRespListener(), new jsonRespErrListener() );

                mRequestQueue.add( jsonReq );
            }
        }
    }

    private JSONArray itemsToJSONAry( List<Item> itemsList ) {
        JSONArray itemsJSONAry = new JSONArray();

        try {
            for( Iterator<Item> i = itemsList.iterator(); i.hasNext(); ) {
                Item item = i.next();
                JSONObject itemObj = new JSONObject();
                itemObj.put( "id", item.getId() );
                itemObj.put( "item", item.toString() );
                itemObj.put( "isMarked", item.isMarked() );
                itemObj.put( "isDeleted", item.isDeleted() );
                itemObj.put( "timestamp", item.getTimeStamp() );
                itemObj.put( "version", item.getVersion() );

                itemsJSONAry.put( itemObj );
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }

        return itemsJSONAry;
    }

    private class jsonRespListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse( JSONObject response ) {
            Log.i( TAG, response.toString() );
            List<Item> servItems = parseJSON( response );

            updateItemList( servItems );
        }
    }

    private class jsonRespErrListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse( VolleyError error ) {
            Log.i( TAG, error.getMessage() );
        }
    }

    private List<Item> parseJSON( JSONObject json ) {
        List<Item> items = new ArrayList<Item>();

        try {
            long timestamp = json.optLong( "timestamp" );
            JSONArray itemsAry = json.getJSONArray( "items" );

            for( int i = 0; i < itemsAry.length(); i++ ) {
                JSONObject item = itemsAry.getJSONObject( i );
                Item newItem = new Item();
                newItem.setItem( item.optString( "item" ) );
                newItem.setMarked( item.optBoolean( "isMarked" ) );
                newItem.setDeleted( item.optBoolean( "isDeleted" ) );
                newItem.setTimeStamp( item.optLong( "timestamp" ) );

                String message = String.format( "i: %d, item: %s", i, newItem );
                Log.i( TAG, message );

                items.add( newItem );
            }

            // Update lastCheckIn timestamp
            lastCheckIn = setLastCheckIn( timestamp );
            System.out.println( "TIMESTAMP SET: " + lastCheckIn );
        } catch( Exception e ) {
            e.printStackTrace();
        }

        return items;
    }

    /*
        This is where we actually consider the updates sent by the server, and update our Item list
        where appropriate.

        Takes items from server (servItems) and compares them to items we currently have (itemsStored).
        For each item received:
            Check if item exists in 'itemsStored'
                If false
                    Insert in to db and 'itemsStored'
                If true,
                    replace with item received
    */
    private void updateItemList( List<Item> servItems ) {
        // If items were received
        if( !servItems.isEmpty() ) {

            // Get a list of all the Items we know about
            itemsStored = getAllItems();

            // Iterate over the Items sent by the server
            for( Iterator<Item> i = servItems.iterator(); i.hasNext(); ) {
                Item newItem = i.next();

                int index = itemsStored.indexOf( newItem );
                // If Item is not found in 'itemsStored'
                if( index == -1 ){
                    // Add Item to the db and 'itemsStored'
                    ContentValues values = new ContentValues();
                    values.put( ItemTable.COLUMN_ITEM_ID, newItem.getId() );
                    values.put( ItemTable.COLUMN_ITEM, newItem.getItem() );
                    values.put( ItemTable.COLUMN_ISMARKED, newItem.isMarked() );
                    values.put( ItemTable.COLUMN_ISDELETED, newItem.isDeleted() );
                    values.put( ItemTable.COLUMN_ITEM_TIMESTAMP, newItem.getTimeStamp() );
                    values.put( ItemTable.COLUMN_VERSION, newItem.getVersion() );
                    values.put( ItemTable.COLUMN_ISPENDING, false );    // Because this is a new Item provided by the server, we do not need to set to pending

                    try {
                        providerClient.insert( ItemContentProvider.CONTENT_URI, values );
                    } catch ( RemoteException e ) {
                        e.printStackTrace();
                    }

                    itemsStored.add( newItem );
                } else {
                    //Else we update an existing Item with values from 'newItem'
                    Item existingItem = itemsStored.get( index );

                    // Update 'existingItem' with values from 'newItem'
                    existingItem.setMarked( newItem.isMarked() );
                    existingItem.setDeleted(newItem.isDeleted());
                    existingItem.setTimeStamp(newItem.getTimeStamp());
                    existingItem.setVersion( newItem.getVersion() );

                    // Update db with our changes
                    ContentValues values = new ContentValues();
                    values.put( ItemTable.COLUMN_ISMARKED, newItem.isMarked() );
                    values.put( ItemTable.COLUMN_ISDELETED, newItem.isDeleted() );
                    values.put( ItemTable.COLUMN_ITEM_TIMESTAMP, newItem.getTimeStamp() );
                    values.put( ItemTable.COLUMN_VERSION, newItem.getVersion() );
                    values.put( ItemTable.COLUMN_ISPENDING, false );
                    String itemId = existingItem.getId();

                    Uri uri = Uri.parse( ItemContentProvider.CONTENT_URI + "/" + itemId );

                    try {
                        providerClient.update( uri, values, null, null );
                    } catch ( RemoteException e ) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private long getLastCheckIn() {
        return sharedPref.getLong( TIMESTAMP_PREF, 0 );
    }

    private long setLastCheckIn( long timestamp ) {
        prefEditor.putLong( TIMESTAMP_PREF, timestamp );
        prefEditor.commit();

        return timestamp;
    }

    private long getCurTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    private List<Item> getAllItems() {
    try {
        Cursor results = providerClient.query( ItemContentProvider.CONTENT_URI, allColumns, null, null, null );

        return cursorToList(results);
    } catch ( RemoteException e ) {
        e.printStackTrace();
    }

    // By default, return an empty list
    return null;
    }

    private List<Item> getPendingItems() {
        try {
            Cursor results = providerClient.query( ItemContentProvider.CONTENT_URI, allColumns, ItemTable.COLUMN_ISPENDING
                    + "=?", new String[] { "1" }, null );

            return cursorToList( results );
        } catch ( RemoteException e ) {
            e.printStackTrace();
        }

        // By default, return an empty list
        return null;
    }

    // Convert a cursor of Items in to an ArrayList
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

    // Convert an individual Cursor row in to an Item
    private Item cursorToItem( Cursor cursor ){
        Item item = new Item();
        item.setId( cursor.getString( 0 ) );
        item.setItem( cursor.getString( 1 ) );

        int isMarkedInt = cursor.getInt( 2 );
        if( isMarkedInt != 0 ) {
            item.setMarked( true );
        } else {
            item.setMarked( false );
        }

        int isDeletedInt = cursor.getInt( 3 );
        if( isDeletedInt != 0 ) {
            item.setDeleted( true );
        } else {
            item.setDeleted( false );
        }

        item.setTimeStamp( cursor.getInt( 4 ) );
        item.setVersion( cursor.getInt( 5 ) );

        return item;
    }

}

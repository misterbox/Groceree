package com.example.skybox.groceree;

import android.content.Context;
import android.content.SharedPreferences;
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
 *
 * TODO: How often should we poll? How do we schedule them efficiently?
 */
public class ServerDataSource {
    private String TAG = this.getClass().getSimpleName();
    private SelectionAdapter adapter;
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

    private ItemDataSource itemDataSource;

    public ServerDataSource( Context context, List<Item> items, SelectionAdapter adapter ) {
        this.adapter = adapter;
        this.itemsStored = items;

        sharedPref = context.getSharedPreferences( TAG, Context.MODE_PRIVATE );
        prefEditor = sharedPref.edit();

        this.lastCheckIn = getLastCheckIn();
        mRequestQueue = Volley.newRequestQueue( context );

        itemsReceived = new ArrayList<Item>();
        itemDataSource = new ItemDataSource( context );
        itemDataSource.open();
    }

    // TODO: Should we use a ConnectionMananger here to determine if the network is available?
    public void serverSync() {
        System.out.println( "last check-in: " + lastCheckIn );

        // Clear all items received before fetching a new list
        itemsReceived.clear();

        // Check last check-in timestamp
        // If timestamp is 0, we've never checked in, so we get all active items from the server
        // GET request to '/list' endpoint
        if( lastCheckIn == 0 ) {
            JsonObjectRequest jsonReq = new JsonObjectRequest( Request.Method.GET, serverURL + listEndPoint, null, new jsonRespListener(), new jsonRespErrListener() );

            mRequestQueue.add( jsonReq );
        } else {
            // POST request to '/syncItems' endpoint
            List<Item> updatedItems = new ArrayList<Item>();

            // Go through the items we have and select those that have been updated since 'lastCheckIn'
            for( Iterator<Item> i = itemsStored.iterator(); i.hasNext(); ){
                Item item = i.next();

                // Add item to 'postItems' list if it has been updated since our last check-in
                System.out.println( "item considered: " + item.toString() );
                System.out.println( "item timestamp: " + item.getTimeStamp() );
                if( item.getTimeStamp() > lastCheckIn ) {
                    System.out.println( "Adding updated item: " + item.toString() );
                    updatedItems.add( item );
                }
            }
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

    // TODO: I'm have to build each JSON object myself consisting of all data within each Item
    private JSONArray itemsToJSONAry( List<Item> itemsList ) {
        JSONArray itemsJSONAry = new JSONArray();

        for( Iterator<Item> i = itemsList.iterator(); i.hasNext(); ) {
            Item item = i.next();
            itemsJSONAry.put( item );
        }

        return itemsJSONAry;
    }

    private class jsonRespListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse( JSONObject response ) {
            Log.i(TAG, response.toString());
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
                newItem.setItem( item.optString( "name" ) );
                newItem.setMarked( item.optBoolean( "isMarked" ) );
                newItem.setDeleted( item.optBoolean( "isDeleted" ) );
                newItem.setTimeStamp( item.optLong( "timestamp" ) );

                String message = String.format( "i: %d, item: %s", i, newItem );
                Log.i( TAG, message );

                items.add( newItem );
            }

            // Update lastCheckIn timestamp
            lastCheckIn = setLastCheckIn( timestamp );
        } catch( Exception e ) {
            e.printStackTrace();
        }

        return items;
    }

    /*
        Takes items from server (servItems) and compares them to items we currently have (itemsStored).
        For each item received:
            Check if item exists in 'itemsStored'
                If true, replace with item received
            If false
                Insert in to db and 'itemsStored'
    */
    private void updateItemList( List<Item> servItems ) {
        // If items were received
        if( !servItems.isEmpty() ) {
            for( Iterator<Item> i = servItems.iterator(); i.hasNext(); ) {
                Item newItem = i.next();

                int index = itemsStored.indexOf( newItem );
                // If Item is not found in 'itemsStored'
                if( index == -1 ){
                    // Attempt to insert newItem in to db.
                    long insertId = itemDataSource.insertItem( newItem );

                    if( insertId == -1 ) {
                        Log.w( TAG, "Error inserting new Item in to database" );
                    } else {
                        itemsStored.add( newItem );
                    }

                } else {
                    // Replace Item with newItem received
                    itemsStored.set( index, newItem );
                }
            }

            adapter.notifyDataSetChanged();
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

    private long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

}

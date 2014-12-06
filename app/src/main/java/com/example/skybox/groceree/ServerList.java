package com.example.skybox.groceree;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Credit: https://github.com/kpbird/volley-example/

public class ServerList extends ListActivity {
    private String TAG = this.getClass().getSimpleName();
    private RequestQueue mRequestQueue;
    private List<Item> itemsReceived;
    private ItemAdapter adapter;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);

        mRequestQueue = Volley.newRequestQueue( this );
        String url = "http://theskyegriffin.com:8000/list";

        JsonObjectRequest jsonReq = new JsonObjectRequest( Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse( JSONObject response ) {
                Log.i(TAG, response.toString() );
                itemsReceived = parseJSON( response );
                adapter = new ItemAdapter( context, R.layout.listview_row_item, itemsReceived );
                setListAdapter( adapter );

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse( VolleyError error ) {
                Log.i( TAG, error.getMessage() );
            }
        });

        mRequestQueue.add( jsonReq );

    }

    private List<Item> parseJSON( JSONObject json ) {
        List<Item> items = new ArrayList<Item>();

        try {
            long timestamp = json.optLong( "timestamp" );
            System.out.println( "timestamp object: " + timestamp );
            JSONArray itemsAry = json.getJSONArray( "items" );

            for( int i = 0; i < itemsAry.length(); i++ ) {
                JSONObject item = itemsAry.getJSONObject( i );
                Item newItem = new Item();
                newItem.setItem( item.optString( "name" ) );
                newItem.setMarked( item.optBoolean( "isMarked" ) );
                newItem.setDeleted( item.optBoolean( "isDeleted" ) );
                newItem.setTimeStamp( item.optLong( "timestamp", 0 ) );

                String message = String.format( "i: %d, item: %s", i, newItem );
                Log.i( TAG, message );

                items.add( newItem );
            }
        } catch( Exception e ) {
            e.printStackTrace();
        }

        return items;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the cab_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

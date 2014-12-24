package com.example.skybox.groceree;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.net.URI;
import java.util.List;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //private ItemDataSource itemDataSource;
    //private ServerDataSource servDataSource;

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private SelectionAdapter adapter;

    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        final ListView listView = getListView();

        // Populate our 'listView'
        fillData();

        EditText etEnterString = ( EditText ) findViewById( R.id.enter_string );

        //final List<Item> items = itemDataSource.getAllActiveItems();

        //final SelectionAdapter mAdapter = new SelectionAdapter( this, android.R.layout.simple_list_item_1, items );

        //setListAdapter( mAdapter );

        // Establish our server check-ins
        //servDataSource = new ServerDataSource( this, items, mAdapter );

        // Setup our MultiChoiceModeListener for the CAB
        listView.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE_MODAL );
        listView.setMultiChoiceModeListener( new AbsListView.MultiChoiceModeListener() {
            private int numRows = 0;

            @Override
            public void onItemCheckedStateChanged( ActionMode actionMode, int position, long id, boolean checked ) {
                if ( checked ) {
                    numRows++;
                    adapter.setNewSelection( position, checked );
                } else {
                    numRows--;
                    adapter.toggleSelection( position );
                }

                actionMode.setTitle( numRows + " selected" );
            }

            @Override
            public boolean onCreateActionMode( ActionMode actionMode, Menu menu ) {
                numRows = 0;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate( R.menu.cab_menu, menu );

                return true;
            }

            @Override
            public boolean onPrepareActionMode( ActionMode actionMode, Menu menu ) {
                return false;
            }

            @Override
            public boolean onActionItemClicked( ActionMode actionMode, MenuItem menuItem ) {
                switch( menuItem.getItemId() ) {
                    case R.id.delete_item:
                        // Mark items at position as deleted
                        SparseBooleanArray selected = adapter.getmSelectedItems();

                        for ( int i = ( selected.size() - 1 ); i >= 0; i-- ) {
                            if( selected.valueAt( i ) ) {
                                markedItemDeleted( selected.keyAt( i ) );
                            }
                        }

                        numRows = 0;
                        adapter.clearSelection();
                        actionMode.finish();
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode( ActionMode actionMode ) {
                adapter.clearSelection();
            }
        });
        listView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick( AdapterView<?> adapterView, View view, int position, long l ) {
                listView.setItemChecked( position, !adapter.isPositionChecked( position ) );
                return false;
            }
        });

        // onClick listener to stikethrough text and set 'isMarked' for the item in the database
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> adapterView, View view, int position, long l ) {
                boolean isMarked = false;
                TextView tvItem = ( TextView ) view.findViewById( android.R.id.text1 );

                // TODO: I could really extend TextView here to toggle the stikethrough more elegantly.
                // If item is marked with a strikethrough, remove
                if ( ( tvItem.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG ) > 0 ) {
                    tvItem.setPaintFlags( tvItem.getPaintFlags() & ( ~Paint.STRIKE_THRU_TEXT_FLAG ) );

                // Else remove the strikethrough
                } else {
                    tvItem.setPaintFlags( tvItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG );

                    isMarked = true;
                }

                // Update our item in the database
                Cursor markedItem = ( Cursor ) adapter.getItem( position );
                int itemId = markedItem.getInt( 0 );
                Uri uri = Uri.parse( ItemContentProvider.CONTENT_URI + "/" + itemId );

                ContentValues values = new ContentValues();
                values.put( ItemTable.COLUMN_ISMARKED, isMarked );
                String itemName = markedItem.getString( 1 );
                String message = String.format( "%s updated, isMarked: %b", itemName, isMarked );
                Log.w( this.getClass().getName(), message );

                getContentResolver().update( uri, values, null, null );
            }
        });

        // Set up listener for 'Done' input from the keyboard
        etEnterString.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction( TextView textView, int actionId, KeyEvent keyEvent ) {
                boolean handled = false;

                if( actionId == EditorInfo.IME_ACTION_DONE ) {
                    insertItem();
                    handled = true;
                }

                return handled;
            }
        });
    }

    public void insertItem() {
        // Get item name from editText
        EditText editText = ( EditText ) findViewById( R.id.enter_string );
        String itemName = editText.getText().toString();

        // Clear editText
        editText.setText( "" );

        ContentValues values = new ContentValues();
        values.put( ItemTable.COLUMN_ITEM, itemName );

        Uri itemURI = getContentResolver().insert( ItemContentProvider.CONTENT_URI, values );

        String message = String.format( "%s entered, uri: %s", itemName, itemURI );
        Log.w( MySQLiteHelper.class.getName(), message );
    }

    // Set item as 'deleted' and therefore no longer needed on the list.
    public void markedItemDeleted( int position ) {
        Cursor selectedItem = ( Cursor ) adapter.getItem( position );

        int itemId = selectedItem.getInt( 0 );
        Uri uri = Uri.parse( ItemContentProvider.CONTENT_URI + "/" + itemId );
        getContentResolver().delete( uri, null, null );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the CAB_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch ( id ) {
            case R.id.action_settings:
                return true;

            case R.id.action_showdb:
                Intent intent = new Intent( this, ShowDB.class );
                startActivity( intent );
                break;

            case R.id.action_server_list:
                intent = new Intent( this, ServerList.class );
                startActivity(intent);
                break;

            case R.id.action_server_sync:
                //servDataSource.serverSync();
                break;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onStop() {
        //itemDataSource.close();

        super.onStop();
    }

    private void fillData() {
        // Fields from the db
        String[] from = new String[] { ItemTable.COLUMN_ITEM };
        // Fields on the UI to which we map
        int[] to = new int[] { android.R.id.text1 };

        getLoaderManager().initLoader( 0, null, this );
        adapter = new SelectionAdapter( this, android.R.layout.simple_list_item_1, null, from, to, 0 );

        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader( int id, Bundle args ) {
        // Get all columns from all items in the database
        String[] projection = { ItemTable.COLUMN_ITEM_ID, ItemTable.COLUMN_ITEM, ItemTable.COLUMN_ISMARKED,
            ItemTable.COLUMN_ISDELETED, ItemTable.COLUMN_ITEM_TIMESTAMP };
        CursorLoader cursorLoader = new CursorLoader( this, ItemContentProvider.CONTENT_URI, projection,
                null, null, null );

        return cursorLoader;
    }

    @Override
    public void onLoadFinished( Loader<Cursor> loader, Cursor data ) {
        adapter.swapCursor( data );
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader ) {
        // data is not available anymore, delete reference
        adapter.swapCursor( null );
    }
}

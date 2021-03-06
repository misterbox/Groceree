package com.theskyegriffin.groceree;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Loader;
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

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks< List< Item > > {
    private SelectionAdapter adapter;
    private SparseBooleanArray markedItems;

    ContentResolver resolver;

    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        markedItems = new SparseBooleanArray();

        // Create account, if needed
        // Sets up and registers our syncAdapter
        SyncUtils.CreateSyncAccount( this );

        // Get a handle on our content resolver, and configure the URI to point to our content provider
        resolver = getContentResolver();

        setContentView( R.layout.activity_main );

        final ListView listView = getListView();

        // Populate our 'listView' from our loader
        fillData();

        EditText etEnterString = ( EditText ) findViewById( R.id.enter_string );

        // Setup our MultiChoiceModeListener for the CAB
        listView.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE_MODAL );
        listView.setMultiChoiceModeListener( new AbsListView.MultiChoiceModeListener() {
            private int numRows = 0;

            @Override
            public void onItemCheckedStateChanged( ActionMode actionMode, int position, long id, boolean checked ) {
                if( checked ) {
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

            // Listener to delete items that have been selected by the CAB
            @Override
            public boolean onActionItemClicked( ActionMode actionMode, MenuItem menuItem ) {
                switch( menuItem.getItemId() ) {
                    case R.id.delete_item:
                        // Mark items at position as deleted
                        SparseBooleanArray selected = adapter.getmSelectedItems();

                        for( int i = ( selected.size() - 1 ); i >= 0; i-- ) {
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
        } );
        listView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick( AdapterView< ? > adapterView, View view, int position, long l ) {
                listView.setItemChecked( position, ! adapter.isPositionChecked( position ) );
                return false;
            }
        } );

        // onClick listener to stikethrough text and set 'isMarked' for the item in the database
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView< ? > adapterView, View view, int position, long l ) {
                boolean isMarked = false;
                TextView tvItem = ( TextView ) view.findViewById( R.id.tvItemName );

                // TODO: I could really extend TextView here to toggle the stikethrough more elegantly.
                // If item is marked with a strikethrough, remove
                if( ( tvItem.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG ) > 0 ) {
                    tvItem.setPaintFlags( tvItem.getPaintFlags() & ( ~ Paint.STRIKE_THRU_TEXT_FLAG ) );
                    // Else remove the strikethrough
                } else {
                    tvItem.setPaintFlags( tvItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG );

                    isMarked = true;
                }

                // Update our item in the database
                toggleItemIsMarked( position, isMarked );
            }
        } );

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
        } );
    }

    public void insertItem() {
        // Get item name from editText
        EditText editText = ( EditText ) findViewById( R.id.enter_string );
        String itemName = editText.getText().toString();

        // Clear editText
        editText.setText( "" );

        ContentValues values = new ContentValues();

        UUID newItemUUID = UUID.randomUUID();
        values.put( ItemTable.COLUMN_ITEM_ID, newItemUUID.toString() );
        values.put( ItemTable.COLUMN_ITEM, itemName );
        values.put( ItemTable.COLUMN_ISPENDING, true );
        values.put( ItemTable.COLUMN_VERSION, 1 );

        Uri itemURI = resolver.insert( ItemContentProvider.CONTENT_URI, values );

        String message = String.format( "%s entered, uri: %s", itemName, itemURI );
        Log.w( this.getClass().getName(), message );
    }

    public void toggleItemIsMarked( int position, boolean isMarked ) {
        Item markedItem = adapter.getItem( position );
        String itemId = markedItem.getId();
        long itemVersion = markedItem.getVersion();
        Uri uri = Uri.parse( ItemContentProvider.CONTENT_URI + "/" + itemId );

        setIsMarked( position, ! markedItems.get( position ) );

        ContentValues values = new ContentValues();
        values.put( ItemTable.COLUMN_ISMARKED, isMarked );
        values.put( ItemTable.COLUMN_ISPENDING, true );
        values.put( ItemTable.COLUMN_VERSION, itemVersion + 1 );

        String itemName = markedItem.getItem();
        String message = String.format( "%s updated, isMarked: %b", itemName, isMarked );
        Log.w( this.getClass().getName(), message );

        resolver.update( uri, values, null, null );
    }

    // Set item as 'deleted' and therefore no longer needed on the list.
    public void markedItemDeleted( int position ) {
        Item selectedItem = adapter.getItem( position );
        String itemId = selectedItem.getId();
        long itemVersion = selectedItem.getVersion();
        Uri uri = Uri.parse( ItemContentProvider.CONTENT_URI + "/" + itemId );

        ContentValues values = new ContentValues();
        values.put( ItemTable.COLUMN_ISDELETED, true );
        values.put( ItemTable.COLUMN_ISPENDING, true );
        values.put( ItemTable.COLUMN_VERSION, itemVersion + 1 );

        resolver.update( uri, values, null, null );
    }

    // Set marked items on the list to isDeleted = true
    public void deleteMarkedItems() {
        for( int i = ( markedItems.size() - 1 ); i >= 0; i-- ) {
            if( markedItems.valueAt( i ) ) {
                markedItemDeleted( markedItems.keyAt( i ) );
            }
        }
    }

    private void setIsMarked( int position, boolean value ) {
        if( value )
            markedItems.put( position, value );
        else
            markedItems.delete( position );
    }

    // Helper method to create boolean array of Items where isMarked=true
    // This will be called after our Loader is finished
    private void setMarkedItems( List< Item > items ) {
        // Clear 'markedItems' before determining the new positions of marked Items.
        markedItems = new SparseBooleanArray();
        int position = 0;

        for( Iterator< Item > iterator = items.iterator(); iterator.hasNext(); ) {
            Item next = iterator.next();

            if( next.isMarked() )
                setIsMarked( position, true );

            position++;
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the CAB_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch( id ) {
            case R.id.action_settings:
                return true;

            case R.id.action_deleteMarked:
                deleteMarkedItems();
                break;

            case R.id.action_server_sync:
                SyncUtils.TriggerRefresh();
                break;
        }

        return super.onOptionsItemSelected( item );
    }

    private void fillData() {
        getLoaderManager().initLoader( 0, null, this );
        adapter = new SelectionAdapter( this, R.layout.listview_row_item );

        setListAdapter( adapter );
    }

    @Override
    public Loader< List< Item > > onCreateLoader( int id, Bundle args ) {
        return new ItemLoader( this );
    }

    @Override
    public void onLoadFinished( Loader< List< Item > > loader, List< Item > data ) {
        // Create boolean array of marked items
        setMarkedItems( data );
        adapter.swapData( data );
    }

    @Override
    public void onLoaderReset( Loader< List< Item > > loader ) {
        // data is not available anymore, delete reference
        adapter.swapData( null );
    }
}

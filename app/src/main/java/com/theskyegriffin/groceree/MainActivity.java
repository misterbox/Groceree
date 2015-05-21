package com.theskyegriffin.groceree;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.UUID;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SelectionAdapter adapter;

    public static final String SCHEME = "content://";
    public static final String AUTHORITY = ItemContentProvider.AUTHORITY;
    public static final String TABLE_PATH = "items";
    Uri uri;
    ContentResolver resolver;

    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // Create account, if needed
        // Sets up and registers our syncAdapter
        SyncUtils.CreateSyncAccount( this );

        // Get a handle on our content resolver, and configure the URI to point to our content provider
        resolver = getContentResolver();
        uri = new Uri.Builder()
                .scheme( SCHEME )
                .authority( AUTHORITY )
                .path( TABLE_PATH )
                .build();

        // Setup our content observer to watch for changes in data
        ItemObserver observer = new ItemObserver( null );
        resolver.registerContentObserver( uri, true, observer );

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
                toggleItemIsMarked(position, isMarked);
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
        editText.setText("");

        ContentValues values = new ContentValues();

        UUID newItemUUID = UUID.randomUUID();
        //TODO: I need to create an item object id here
        values.put( ItemTable.COLUMN_ITEM_ID, newItemUUID.toString() );
        values.put( ItemTable.COLUMN_ITEM, itemName );
        values.put( ItemTable.COLUMN_ISPENDING, true );
        values.put( ItemTable.COLUMN_VERSION, 1 );

        Uri itemURI = getContentResolver().insert( ItemContentProvider.CONTENT_URI, values );

        String message = String.format( "%s entered, uri: %s", itemName, itemURI );
        Log.w( this.getClass().getName(), message );
    }

    public void toggleItemIsMarked( int position, boolean isMarked ) {
        Cursor markedItem = ( Cursor ) adapter.getItem( position );
        String itemId = markedItem.getString( 0 );
        Uri uri = Uri.parse( ItemContentProvider.CONTENT_URI + "/" + itemId );

        ContentValues values = new ContentValues();
        values.put( ItemTable.COLUMN_ISMARKED, isMarked );
        values.put( ItemTable.COLUMN_ISPENDING, true );
        values.put( ItemTable.COLUMN_VERSION, ItemTable.COLUMN_VERSION + "+1" );

        String itemName = markedItem.getString( 1 );
        String message = String.format( "%s updated, isMarked: %b", itemName, isMarked );
        Log.w( this.getClass().getName(), message );

        getContentResolver().update( uri, values, null, null );
    }

    // Set item as 'deleted' and therefore no longer needed on the list.
    public void markedItemDeleted( int position ) {
        Cursor selectedItem = ( Cursor ) adapter.getItem( position );
        int itemId = selectedItem.getInt( 0 );
        Uri uri = Uri.parse(ItemContentProvider.CONTENT_URI + "/" + itemId);

        ContentValues values = new ContentValues();
        values.put( ItemTable.COLUMN_ISDELETED, true );
        values.put( ItemTable.COLUMN_ISPENDING, true );
        values.put( ItemTable.COLUMN_VERSION, ItemTable.COLUMN_VERSION + "+1" );

        getContentResolver().update(uri, values, null, null);
    }

    // Set marked items on the list to isDeleted = true
    public void deleteMarkedItems() {
        // Uri is going to be the base path, since we are updating multiple items
        Uri uri = ItemContentProvider.CONTENT_URI;

        // Items selected will have their 'isDeleted' column set to 'true'
        ContentValues values = new ContentValues();
        values.put( ItemTable.COLUMN_ISDELETED, true );
        values.put( ItemTable.COLUMN_ISPENDING, true );
        values.put( ItemTable.COLUMN_VERSION, ItemTable.COLUMN_VERSION + "+1" );

        // Update all items setting 'isDeleted' = true where 'isMarked' = true
        getContentResolver().update( uri, values, ItemTable.COLUMN_ISMARKED + "=?",
                new String[] { String.valueOf( 1 ) } );
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

        switch ( id ) {
            case R.id.action_settings:
                return true;

            case R.id.action_deleteMarked:
                deleteMarkedItems();
                break;

            case R.id.action_showdb:
                Intent intent = new Intent( this, ShowDB.class );
                startActivity( intent );
                break;

            case R.id.action_server_list:
                intent = new Intent( this, ServerList.class );
                startActivity( intent );
                break;

            case R.id.action_server_sync:
                SyncUtils.TriggerRefresh();
                break;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void fillData() {
        // Fields from the db
        String[] from = new String[] { ItemTable.COLUMN_ITEM };
        // Fields on the UI to which we map
        int[] to = new int[] { android.R.id.text1 };

        getLoaderManager().initLoader( 0, null, this );
        adapter = new SelectionAdapter( this, android.R.layout.simple_list_item_1, null, from, to, 0 );

        setListAdapter( adapter );
    }

    @Override
    public Loader<Cursor> onCreateLoader( int id, Bundle args ) {
        // Get all columns from all items in the database with the 'isDeleted' column set to 0 (false)
        String[] projection = { ItemTable.COLUMN_ITEM_ID, ItemTable.COLUMN_ITEM, ItemTable.COLUMN_ISMARKED,
            ItemTable.COLUMN_ISDELETED, ItemTable.COLUMN_ITEM_TIMESTAMP, ItemTable.COLUMN_VERSION,
            ItemTable.COLUMN_ISPENDING };

        return new CursorLoader( this, ItemContentProvider.CONTENT_URI, projection,
                "isDeleted=?", new String[] { "0" }, ItemTable.COLUMN_ITEM + " ASC" );
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

    public class ItemObserver extends ContentObserver {
        public ItemObserver( Handler handler ) {
            super( handler );
        }

        @Override
        public void onChange( boolean selfChange ) {
            onChange( selfChange, null );
        }

        @Override
        public void onChange( boolean selfChange, Uri changeUri ) {
            SyncUtils.TriggerRefresh();
        }
    }
}

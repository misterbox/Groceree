package com.example.skybox.groceree;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Paint;
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

import java.util.List;

public class MainActivity extends ListActivity {
    private ItemDataSource itemDataSource;
    private ServerDataSource servDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listView = getListView();

        EditText etEnterString = ( EditText ) findViewById( R.id.enter_string );

        // First get all items from database and populate our ListView
        itemDataSource = new ItemDataSource( this );
        itemDataSource.open();

        final List<Item> items = itemDataSource.getAllActiveItems();

        final SelectionAdapter mAdapter = new SelectionAdapter( this, android.R.layout.simple_list_item_1, items );

        setListAdapter( mAdapter );

        // Establish our server check-ins
        servDataSource = new ServerDataSource( this, items, mAdapter );

        // Setup our MultiChoiceModeListener for the CAB
        listView.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE_MODAL );
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private int numRows = 0;

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                if (checked) {
                    numRows++;
                    mAdapter.setNewSelection(position, checked);
                } else {
                    numRows--;
                    mAdapter.toggleSelection(position);
                }

                actionMode.setTitle(numRows + " selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                numRows = 0;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.cab_menu, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch( menuItem.getItemId() ) {
                    case R.id.delete_item:
                        // Mark items at position as deleted
                        SparseBooleanArray selected = mAdapter.getmSelectedItems();

                        for ( int i = (selected.size() - 1); i >= 0; i-- ) {
                            if( selected.valueAt( i ) ) {
                                markedItemDeleted( i );
                            }
                        }

                        numRows = 0;
                        mAdapter.clearSelection();
                        actionMode.finish();
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                mAdapter.clearSelection();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                listView.setItemChecked(position, !mAdapter.isPositionChecked(position));
                return false;
            }
        });

        // onClick listener to stikethrough text and set 'isMarked' for the item in the database
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Item item = items.get( position );
                boolean isMarked = false;
                TextView tvItem = (TextView) view.findViewById(android.R.id.text1);

                // TODO: I could really extend TextView here to toggle the stikethrough more elegantly.
                // If item is marked with a strikethrough, remove
                if ((tvItem.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
                    tvItem.setPaintFlags(tvItem.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

                // Else remove the strikethrough
                } else {
                    tvItem.setPaintFlags(tvItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    isMarked = true;
                }

                // Update our item in the database
                item.setMarked( isMarked );
                itemDataSource.updateItem( item );

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
        SelectionAdapter adapter = ( SelectionAdapter ) getListAdapter();

        // Get item name from editText
        EditText editText = ( EditText ) findViewById( R.id.enter_string );
        String itemName = editText.getText().toString();

        // Clear editText
        editText.setText( "" );

        // insert new item in to the database
        Item item = itemDataSource.createItem( itemName );
        adapter.add( item );
        String message = String.format( "%s entered", item );
        Log.w(MySQLiteHelper.class.getName(), message);
    }

    // Set item as 'deleted' and therefore no longer needed on the list.
    public void markedItemDeleted( int itemID ) {
        SelectionAdapter adapter = ( SelectionAdapter ) getListAdapter();
        Item item = adapter.getItem( itemID );

        // Update item in the database
        item.setDeleted( true );
        itemDataSource.updateItem( item );

        // Remove item from the adapter
        adapter.remove(item);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the CAB_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                servDataSource.serverSync();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        //itemDataSource.close();

        super.onStop();
    }
}

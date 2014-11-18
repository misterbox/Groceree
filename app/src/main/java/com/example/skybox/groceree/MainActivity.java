package com.example.skybox.groceree;

import android.app.ListActivity;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends ListActivity {
    private ItemDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView listView = getListView();

        // First get all items from database and populate our ListView
        dataSource = new ItemDataSource( this );
        dataSource.open();

        List<Item> items = dataSource.getAllItems();
        System.out.println( "items.size: " + items.size() );

        // TODO: replace 'simple_list_item_1' with something to include the TimeStamp column (for debugging)
        final SelectionAdapter mAdapter = new SelectionAdapter( this, R.layout.listview_row_item, items );

        setListAdapter( mAdapter );

        // Setup our MultiChoiceModeListener for the CAB
        listView.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE_MODAL );
        listView.setMultiChoiceModeListener( new AbsListView.MultiChoiceModeListener() {
            private int numRows = 0;

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
                if( checked ) {
                    numRows++;
                    mAdapter.setNewSelection( position, checked );
                } else {
                    numRows--;
                    mAdapter.toggleSelection( position);
                }

                actionMode.setTitle( numRows + " selected" );
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                numRows = 0;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate( R.menu.menu, menu );

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch ( menuItem.getItemId() ) {
                    case R.id.delete_item:
                        // TODO: mark item as deleted
                        // Mark items at position as deleted
                        SparseBooleanArray selected = mAdapter.getmSelectedItems();

                        for( int i = ( selected.size() - 1 ); i >= 0; i-- ) {
                            if( selected.valueAt( i ) ) {
                                // Do something here
                            }
                        }

                        numRows = 0;
                        mAdapter.clearSelection();;
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
        listView.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                listView.setItemChecked( position, !mAdapter.isPositionChecked( position ) );
                return false;
            }
        });

        // onClick listener to stikethrough text and 'mark' the item in the database
        // TODO: Figure out how to properly test for flags
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                TextView tvItem = ( TextView ) view.findViewById( R.id.textViewItem );

                if( ( tvItem.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG ) > 0 ) {
                    tvItem.setPaintFlags( tvItem.getPaintFlags() & ( ~ Paint.STRIKE_THRU_TEXT_FLAG ) );
                } else {
                    tvItem.setPaintFlags( tvItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG );
                }
            }
        });
    }

    public void insertItem( View view ) {
        ArrayAdapter<Item> adapter = ( ArrayAdapter<Item> ) getListAdapter();

        // Get item name from editText
        EditText editText = ( EditText ) findViewById( R.id.enter_string );
        String itemName = editText.getText().toString();

        // insert new item in to the database
        Item item = dataSource.createItem( itemName );
        adapter.add( item );
        String message = String.format( "%s entered", item );
        Log.w(MySQLiteHelper.class.getName(), message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
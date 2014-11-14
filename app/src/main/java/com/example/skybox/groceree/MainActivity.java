package com.example.skybox.groceree;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

        List<Item> values = dataSource.getAllItems();
        System.out.println( "values.size" + values.size() );

        // TODO: replace 'simple_list_item_1' with something to include the TimeStamp column (for debugging)
        final SelectionAdapter mAdapter = new SelectionAdapter( this, android.R.layout.simple_list_item_1, values );

        setListAdapter( mAdapter );
        mAdapter.addAll( values );

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
    }

    private class SelectionAdapter extends ArrayAdapter<Item> {
        private SparseBooleanArray mSelectedItems;

        public SelectionAdapter( Context context, int resource, List<Item> items ) {
            super( context, resource, items );
            mSelectedItems = new SparseBooleanArray();
        }

        public void toggleSelection( int position ) {
            setNewSelection( position, !mSelectedItems.get( position ) );
        }

        public void setNewSelection( int position, boolean value ) {
            if( value )
                mSelectedItems.put( position, value );
            else
                mSelectedItems.delete( position );

            notifyDataSetChanged();
        }

        public boolean isPositionChecked( int position ) {
            Boolean result = mSelectedItems.get( position );
            return result == null ? false : result;
        }

        public SparseBooleanArray getmSelectedItems() {
            return mSelectedItems;
        }

        public void clearSelection() {
            mSelectedItems = new SparseBooleanArray();
            notifyDataSetChanged();
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            View v = super.getView( position, convertView, parent ); // Let the adapter handle setting up the row views

            if( mSelectedItems.get( position ) != false ) {
                v.setBackgroundColor( getResources().getColor( android.R.color.holo_blue_light ) );
            }

            return v;
        }
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
package com.theskyegriffin.groceree;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class ShowDB extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;

    private String[] allColumns = { ItemTable.COLUMN_ITEM_ID, ItemTable.COLUMN_ITEM, ItemTable.COLUMN_ISMARKED,
            ItemTable.COLUMN_ISDELETED, ItemTable.COLUMN_ITEM_TIMESTAMP, ItemTable.COLUMN_VERSION,
            ItemTable.COLUMN_ISPENDING };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        displayListView();
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(0, null, this);
    }

    private void displayListView() {
        int[] to = new int[] {
                R.id.textViewID,
                R.id.textViewItem,
                R.id.tvMarked,
                R.id.tvDeleted,
                R.id.textViewTimeStamp,
                R.id.tvVersion,
                R.id.tvPending,
        };

        adapter = new SimpleCursorAdapter( getActivity(), R.layout.listview_row_item, null,
                allColumns, to, 0 );

        setListAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader( int id, Bundle args ) {
        return new CursorLoader( getActivity(), ItemContentProvider.CONTENT_URI,
                allColumns, null, null, null );
    }

    @Override
    public void onLoadFinished( Loader<Cursor> loader, Cursor data ) {
        adapter.swapCursor(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader ) {
        adapter.swapCursor( null );
    }
}

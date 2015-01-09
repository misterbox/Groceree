package com.example.skybox.groceree;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by skybox on 11/15/14.
 */
public class SelectionAdapter extends SimpleCursorAdapter {
    private SparseBooleanArray mSelectedItems;
    private Context context;
    private int layout;

    public SelectionAdapter( Context context, int layout, Cursor cursor, String[] from, int[] to, int flags ) {
        super( context, layout, cursor, from, to, flags );
        this.context = context;
        this.layout = layout;
        mSelectedItems = new SparseBooleanArray();
    }

    @Override
    public View newView( Context context, Cursor cursor, ViewGroup parent ) {
        LayoutInflater inflater = LayoutInflater.from( context );
        View v = inflater.inflate( layout, parent, false );

        return v;
    }

    @Override
    public void bindView( View v, Context context, Cursor cursor ) {
        int itemCol = cursor.getColumnIndex( ItemTable.COLUMN_ITEM );
        String item = cursor.getString( itemCol );

        TextView tvItem = ( TextView ) v.findViewById( android.R.id.text1 );

        // If the Item actually has a name to display
        if( item != null ) {
            tvItem.setText( item );

            // Get the column number and value of this Item's 'isMarked' column
            int isMarkedCol = cursor.getColumnIndex( ItemTable.COLUMN_ISMARKED );
            int isMarkedInt = cursor.getInt( isMarkedCol );

            // Determine if Item 'isMarked' to set strikethrough
            boolean isMarked = false;
            if( isMarkedInt != 0 ) {
                isMarked = true;
            }
            if( isMarked ){
                tvItem.setPaintFlags( tvItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG );
            } else {
                tvItem.setPaintFlags( tvItem.getPaintFlags() & ( ~Paint.STRIKE_THRU_TEXT_FLAG ) );
            }

        }
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        // Let the adapter handle setting up the row views
        View v = super.getView( position, convertView, parent );
        v.setBackgroundColor( context.getResources().getColor( android.R.color.background_light ) );

        if( mSelectedItems.get( position ) ) {
            v.setBackgroundColor( context.getResources().getColor( android.R.color.holo_blue_light ) );
        }

        return v;
    }

    // Used to toggle a boolean representing if a particular view within our listView is selected
    public void toggleSelection( int position ) {
        setNewSelection( position, !mSelectedItems.get( position ) );
    }

    // Sets new boolean when a view within our listView is first selected
    public void setNewSelection( int position, boolean value ) {
        if( value )
            mSelectedItems.put( position, value );
        else
            mSelectedItems.delete( position );

        notifyDataSetChanged();
    }

    // Returns true if an Item within our listView is selected
    public boolean isPositionChecked( int position ) {
        Boolean result = mSelectedItems.get( position );
        return result == null ? false : result;
    }

    // Return a spareBooleanArray of all Items within our listView that are currently selected
    public SparseBooleanArray getmSelectedItems() {
        return mSelectedItems;
    }

    // Clear all selected Items within our listView
    public void clearSelection() {
        mSelectedItems = new SparseBooleanArray();
        notifyDataSetChanged();
    }
}

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
        Cursor c = getCursor();

        final LayoutInflater inflater = LayoutInflater.from( context );
        View v = inflater.inflate( layout, parent, false );

        int itemCol = c.getColumnIndex( ItemTable.COLUMN_ITEM );
        String item = c.getString( itemCol );

        TextView tvItem = ( TextView ) v.findViewById( android.R.id.text1 );

        if( item != null ) {
            tvItem.setText( item );

            // Determine if Item 'isMarked' to set strikethrough
            int isMarkedCol = c.getColumnIndex( ItemTable.COLUMN_ISMARKED );
            int isMarkedInt = c.getInt( isMarkedCol );

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

        return v;
    }

    @Override
    public void bindView( View v, Context context, Cursor cursor ) {
        int itemCol = cursor.getColumnIndex( ItemTable.COLUMN_ITEM );
        String item = cursor.getString( itemCol );

        TextView tvItem = ( TextView ) v.findViewById( android.R.id.text1 );

        if( item != null ) {
            tvItem.setText( item );

            // Determine if Item 'isMarked' to set strikethrough
            int isMarkedCol = cursor.getColumnIndex( ItemTable.COLUMN_ISMARKED );
            int isMarkedInt = cursor.getInt( isMarkedCol );

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
        //Item item = items.get( position );
        System.out.println( "getView: position is: " + position );

        if( convertView == null ) {
            System.out.println( "getView: convertView is null" );
        }
        if( parent == null ) {
            System.out.println( "getView: parent is null" );
        }

        View v = super.getView( position, convertView, parent ); // Let the adapter handle setting up the row views
        v.setBackgroundColor( context.getResources().getColor( android.R.color.background_light ) );

        if( mSelectedItems.get( position ) ) {
            v.setBackgroundColor( context.getResources().getColor( android.R.color.holo_blue_light ) );
        }

        return v;
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

    // Creates an Item object from a cursor
    private Item cursorToItem( Cursor cursor ){
        Item item = new Item();
        item.setId( cursor.getLong( 0 ) );
        item.setItem( cursor.getString( 1 ) );

        int isMarkedInt = cursor.getInt( 2 );
        if( isMarkedInt != 0 ) {
            item.setMarked( true );
        } else {
            item.setMarked( false );
        }

        int isDeletedInt = cursor.getInt( 3 );
        if( isDeletedInt != 0 ) {
            item.setDeleted(true);
        } else {
            item.setDeleted(false);
        }

        item.setTimeStamp( cursor.getInt( 4 ) );

        return item;
    }

}

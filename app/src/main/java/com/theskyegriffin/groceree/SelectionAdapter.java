package com.theskyegriffin.groceree;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by skybox on 11/15/14.
 */
public class SelectionAdapter extends ArrayAdapter< Item > {
    private SparseBooleanArray mSelectedItems;
    private Context context;
    private List< Item > items;

    public SelectionAdapter( Context context, int resource ) {
        super( context, 0 );
        this.context = context;
        mSelectedItems = new SparseBooleanArray();
    }

    public void toggleSelection( int position ) {
        setNewSelection( position, ! mSelectedItems.get( position ) );
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

    // Used by the ItemLoader to replace current data set with updates
    public void swapData( List< Item > data ) {
        if( data != null )
            items = data;

        clear();
        addAll( items );
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        Item item = items.get( position );

        View v;

        if( convertView == null ) {
            v = LayoutInflater.from( context ).inflate( R.layout.listview_row_item, parent, false );
        } else {
            v = convertView;
        }

        TextView tvItem = ( TextView ) v.findViewById( R.id.tvItemName );
        TextView tvRowNum = ( TextView ) v.findViewById( R.id.tvRowNum );

        tvItem.setText( item.getItem() );
        tvRowNum.setText( Integer.toString( position + 1 ) + "." );

        // Determine if Item 'isMarked' to set strikethrough
        if( item.isMarked() ) {
            tvItem.setPaintFlags( tvItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG );
            tvItem.setTextColor( Color.parseColor( "#E8E8E8" ) );
        } else {
            tvItem.setPaintFlags( tvItem.getPaintFlags() & ( ~ Paint.STRIKE_THRU_TEXT_FLAG ) );
            tvItem.setTextColor( Color.BLACK );
            v.setBackgroundColor( context.getResources().getColor( android.R.color.background_light ) );
        }

        if( mSelectedItems.get( position ) != false ) {
            v.setBackgroundColor( context.getResources().getColor( android.R.color.holo_blue_light ) );
        }

        return v;
    }
}

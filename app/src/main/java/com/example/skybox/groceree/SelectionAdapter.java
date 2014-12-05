package com.example.skybox.groceree;

import android.content.Context;
import android.graphics.Paint;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by skybox on 11/15/14.
 */
public class SelectionAdapter extends ArrayAdapter<Item> {
    private SparseBooleanArray mSelectedItems;
    private Context context;
    private List<Item> items;

    public SelectionAdapter( Context context, int resource, List<Item> items ) {
        super( context, resource, items );
        this.context = context;
        this.items = items;
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
    public void add( Item item ) {
        super.add( item );

        Collections.sort( items );
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        Item item = items.get( position );
        View v = super.getView( position, convertView, parent ); // Let the adapter handle setting up the row views
        TextView tvItem = ( TextView ) v.findViewById( android.R.id.text1 );

        // Determine if Item 'isMarked' to set strikethrough
        if( item.isMarked() ){
            tvItem.setPaintFlags(tvItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvItem.setPaintFlags(tvItem.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        v.setBackgroundColor( context.getResources().getColor( android.R.color.background_light ) );

        if( mSelectedItems.get( position ) ) {
            v.setBackgroundColor( context.getResources().getColor( android.R.color.holo_blue_light ) );
        }

        return v;
    }
}

package com.example.skybox.groceree;

import android.content.Context;
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
public class SelectionAdapter extends ArrayAdapter<Item> {
    private SparseBooleanArray mSelectedItems;
    private Context context;
    private List<Item> items;

    public SelectionAdapter( Context context, int resource, List<Item> items ) {
        super( context, 0, items );
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
    public View getView( int position, View convertView, ViewGroup parent ) {
        Item item = items.get( position );

        if( convertView == null ) {
            convertView = LayoutInflater.from( context ).inflate( R.layout.listview_row_item, parent, false );
        }

        convertView.setBackgroundColor( context.getResources().getColor( android.R.color.background_light ) );

        TextView tvItem = ( TextView ) convertView.findViewById( R.id.textViewItem );
        TextView tvTimeStamp = ( TextView ) convertView.findViewById( R.id.textViewTimeStamp );

        tvItem.setText( item.toString() );
        tvTimeStamp.setText( Integer.toString( item.getTimeStamp() ) );

        if( mSelectedItems.get( position ) != false ) {
            convertView.setBackgroundColor( context.getResources().getColor( android.R.color.holo_blue_light ) );
        }

        return convertView;
    }
}
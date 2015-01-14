package com.theskyegriffin.groceree;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by griffinsb on 11/18/14.
 */
public class ItemAdapter extends ArrayAdapter<Item> {
    private Context context;
    private List<Item> items;

    public ItemAdapter( Context context, int resource, List<Item> items ) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {

        Item item = items.get( position );

        if( convertView == null ) {
            convertView = LayoutInflater.from(context).inflate( R.layout.listview_row_item, parent, false );
        }

        convertView.setBackgroundColor( context.getResources().getColor( android.R.color.background_light ) );

        TextView tvId = ( TextView ) convertView.findViewById( R.id.textViewID );
        TextView tvItem = ( TextView ) convertView.findViewById( R.id.textViewItem );
        TextView tvTimeStamp = (TextView) convertView.findViewById( R.id.textViewTimeStamp );
        TextView tvMarked = ( TextView ) convertView.findViewById( R.id.tvMarked );
        TextView tvDeleted = ( TextView ) convertView.findViewById( R.id.tvDeleted );

        tvId.setText( String.valueOf( item.getId() ) );
        tvItem.setText( item.toString() );
        tvTimeStamp.setText( Long.toString( item.getTimeStamp() ) );
        tvMarked.setText( String.valueOf( item.isMarked() ) );
        tvDeleted.setText( String.valueOf( item.isDeleted() ) );

        return convertView;
    }
}

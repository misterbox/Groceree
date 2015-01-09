package com.theskyegriffin.groceree;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class ShowDB extends ListActivity {
    private ItemDataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_db);

        dataSource = new ItemDataSource( this );
        dataSource.open();

        List<Item> items = dataSource.getAllItems();

        final ItemAdapter itemAdapter = new ItemAdapter( this, R.layout.listview_row_item, items );

        setListAdapter( itemAdapter );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the CAB_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_db, menu);
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

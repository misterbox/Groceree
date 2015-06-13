package com.theskyegriffin.groceree;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import java.util.List;

/**
 * Created by skybox on 6/4/15.
 *
 * Much of the inspiration here can be credited back to Alex Lockwood (great guy).
 * http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
 */
public class ItemLoader extends AsyncTaskLoader<List<Item>> {
    String[] projection = { ItemTable.COLUMN_ITEM_ID, ItemTable.COLUMN_ITEM, ItemTable.COLUMN_ISMARKED,
            ItemTable.COLUMN_ISDELETED, ItemTable.COLUMN_ITEM_TIMESTAMP, ItemTable.COLUMN_VERSION,
            ItemTable.COLUMN_ISPENDING };

    public static final String SCHEME = "content://";
    public static final String AUTHORITY = ItemContentProvider.AUTHORITY;
    public static final String TABLE_PATH = "items";
    Uri uri;
    ContentResolver resolver;
    ItemObserver mObserver;

    private List<Item> mData;

    public ItemLoader( Context context ) {
        super( context );

        resolver = context.getContentResolver();

        uri = new Uri.Builder()
                .scheme( SCHEME )
                .authority( AUTHORITY )
                .path( TABLE_PATH )
                .build();
    }

    @Override
    public List<Item> loadInBackground() {
        // Get all active items from provider
        Cursor cursor = resolver.query(ItemContentProvider.CONTENT_URI, projection, "isDeleted=?",
                new String[]{"0"}, ItemTable.COLUMN_ITEM + " ASC");

        //TODO: Check if cursor actually contains data
        // Also close it
        return ItemUtils.cursorToList( cursor );
    }

    @Override
    public void deliverResult( List<Item> data ) {
        if( isReset() ) {
            releaseResources( data );
            return;
        }

        List<Item> oldData = mData;
        mData = data;

        if( isStarted() ) {
            super.deliverResult( data );
        }

        if( oldData != null && oldData != data ) {
            releaseResources( oldData );
        }
    }

    @Override
    protected void onStartLoading() {
        if( mData != null ) {
            // If we have a result available, deliver it immediately
            deliverResult(mData);
        }

        // Start watching for changes in the Item data
        if( mObserver == null ) {
            mObserver = new ItemObserver( null );
            resolver.registerContentObserver( uri, true, mObserver );
        }

        if( takeContentChanged() || mData == null ) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        // Ensure the loader has been stopped
        onStopLoading();

        // At this point we can release resources
        if( mData != null ) {
            releaseResources( mData );
            mData = null;
        }

        // The loader is being reset, so we should stop monitoring for changes
        if( mObserver != null ) {
            resolver.unregisterContentObserver( mObserver );
            mObserver = null;
        }
    }

    @Override
    public void onCanceled( List<Item> data ) {
        // Attempt to cancel the current asynchronous load
        super.onCanceled(data);
        releaseResources( mData );
    }

    private void releaseResources( List<Item> data ) {
        data = null;
    }

    public class ItemObserver extends ContentObserver {
        public ItemObserver( Handler handler ) {
            super( handler );
        }

        @Override
        public void onChange( boolean selfChange ) {
            onChange( selfChange, null );
        }

        @Override
        public void onChange( boolean selfChange, Uri changeUri ) {
            SyncUtils.TriggerRefresh();
            onContentChanged();
        }
    }
}

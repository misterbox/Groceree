package com.theskyegriffin.groceree;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by skybox on 12/30/14.
 */
public class ItemSyncAdapter extends AbstractThreadedSyncAdapter {
    ServerDataSource dataSource;

    public ItemSyncAdapter( Context context, boolean autoInitialize ) {
        super(context, autoInitialize);

        dataSource = new ServerDataSource( context );
    }

    @Override
    public void onPerformSync( Account account, Bundle extras, String authority, ContentProviderClient providerClient,
                               SyncResult syncResult ) {
        try {
            dataSource.serverSync( providerClient );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

package com.example.skybox.groceree;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.example.skybox.groceree.ServerDataSource;

import java.util.List;

/**
 * Created by skybox on 12/30/14.
 */
public class ItemSyncAdapter extends AbstractThreadedSyncAdapter {
    private final AccountManager accountManager;

    public ItemSyncAdapter( Context context, boolean autoInitialize ) {
        super( context, autoInitialize );
        System.out.println( "SyncAdapter: constructor" );
        accountManager = AccountManager.get( context );
    }

    @Override
    public void onPerformSync( Account account, Bundle extras, String authority, ContentProviderClient providerClient,
                               SyncResult syncResult ) {
        try {
            // TODO: Modify ServerDataSource to work here
            //ServerDataSource dataSource = new ServerDataSource();
            // Use provider to get a list of items that have changed since our last check-in
            System.out.println( "In onPerformSync" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

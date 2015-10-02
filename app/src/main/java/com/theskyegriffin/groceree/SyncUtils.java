package com.theskyegriffin.groceree;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Created by skybox on 1/5/15.
 */
public class SyncUtils {
    public static final Account account = AuthenticatorService.GetAccount( "theskyegriffin.com" );
    private static final long SYNC_FREQUENCY = 3 * 60 * 60; // 3 hours in seconds
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    private static final String CONTENT_AUTHORITY = ItemContentProvider.AUTHORITY;

    public static void CreateSyncAccount( Context context ) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager.getDefaultSharedPreferences( context )
                .getBoolean( PREF_SETUP_COMPLETE, false );

        // Create account if it's missing. (Either first run, or user has deleted account. )
        AccountManager accountManager = ( AccountManager ) context.getSystemService( Context.ACCOUNT_SERVICE );

        if( accountManager.addAccountExplicitly( account, null, null ) ) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable( account, CONTENT_AUTHORITY, 1 );

            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically( account, CONTENT_AUTHORITY, true );

            // Recommend a schedule for automatic syncs. The system may modify this based on other
            // scheduled syncs and network utilization
            ContentResolver.addPeriodicSync( account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY );
            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. ( Note that it's possible to clear app data without affecting
        // the account list, so we need to check both. )
        if( newAccount || ! setupComplete ) {
            TriggerRefresh();
            PreferenceManager.getDefaultSharedPreferences( context ).edit()
                    .putBoolean( PREF_SETUP_COMPLETE, true ).commit();
        }
    }

    public static void TriggerRefresh() {
        Bundle bundle = new Bundle();

        bundle.putBoolean( ContentResolver.SYNC_EXTRAS_MANUAL, true );
        bundle.putBoolean( ContentResolver.SYNC_EXTRAS_EXPEDITED, true );
        ContentResolver.requestSync( account, CONTENT_AUTHORITY, bundle );
    }
}

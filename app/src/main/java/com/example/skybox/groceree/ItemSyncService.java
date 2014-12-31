package com.example.skybox.groceree;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by skybox on 12/30/14.
 */
public class ItemSyncService extends Service {

    private final Object syncAdapterLock = new Object();
    private static ItemSyncAdapter syncAdapter = null;

    @Override
    public void onCreate() {
        synchronized ( syncAdapterLock ) {
            if( syncAdapter == null )
                syncAdapter = new ItemSyncAdapter( getApplicationContext(), true );
        }
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return syncAdapter.getSyncAdapterBinder();
    }
}

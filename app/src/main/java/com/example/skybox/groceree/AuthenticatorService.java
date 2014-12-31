package com.example.skybox.groceree;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by skybox on 12/30/14.
 */
public class AuthenticatorService extends Service {
    private Authenticator authenticator;

    @Override
    public void onCreate() {
        authenticator = new Authenticator( this );
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return authenticator.getIBinder();
    }
}

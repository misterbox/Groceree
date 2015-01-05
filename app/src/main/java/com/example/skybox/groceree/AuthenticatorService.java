package com.example.skybox.groceree;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by skybox on 12/30/14.
 */
public class AuthenticatorService extends Service {
    private static final String ACCOUNT_NAME = "default_account";
    private Authenticator authenticator;

    @Override
    public void onCreate() {
        System.out.println( "AuthenticatorService: onCreate()" );
        authenticator = new Authenticator( this );
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return authenticator.getIBinder();
    }

    public static Account GetAccount( String accountType ) {
        final String accountName = ACCOUNT_NAME;
        return new Account( accountName, accountType );
    }
}

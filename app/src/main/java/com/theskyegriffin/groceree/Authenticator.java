package com.theskyegriffin.groceree;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by skybox on 12/30/14.
 */
public class Authenticator extends AbstractAccountAuthenticator {
    public Authenticator( Context context ) {
        super( context );
        System.out.println( "Authenticator: Constructor()" );
    }

    @Override
    public Bundle editProperties( AccountAuthenticatorResponse response, String s ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount( AccountAuthenticatorResponse response, String s,
                              String s2, String[] strings, Bundle bundle ) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle confirmCredentials( AccountAuthenticatorResponse response, Account account, Bundle bundle ) {
        return null;
    }

    @Override
    public Bundle getAuthToken( AccountAuthenticatorResponse response, Account account,
                                String s, Bundle bundle ) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel( String s ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials( AccountAuthenticatorResponse response, Account account,
                                     String s, Bundle bundle ) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures( AccountAuthenticatorResponse response, Account account,
                               String[] strings ) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}

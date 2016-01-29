package com.theskyegriffin.groceree;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by skybox on 1/28/16.
 */
public class GrocereeService extends IntentService {
    public GrocereeService() {
        super( "GrocereeService" );
    }

    @Override
    protected void onHandleIntent( Intent intent ) {
    }
}

package com.blinnikka.android.qremote;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;

/**
 * Activity that starts the service. This is used when the application is started in the
 * traditional manner instead of being triggered by the broadcast intents.
 */
public class StartServiceActivity extends Activity {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".StartServiceActivity";
	
	// **************************************** //
	// Public Methods
	// **************************************** //
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate");
        
        startService(new Intent(getApplicationContext(), ReceiverService.class));
		
        this.finish();
    }
    
}

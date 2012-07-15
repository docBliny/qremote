package com.blinnikka.android.qremote;

import org.apache.http.protocol.HttpRequestHandlerRegistry;

import com.blinnikka.android.net.HttpServer;
import com.blinnikka.android.qremote.handlers.LedHandler;
import com.blinnikka.android.qremote.handlers.MediaControlsHandler;
import com.blinnikka.android.qremote.handlers.MediaPlaylistHandler;
import com.blinnikka.android.qremote.handlers.MediaSettingsHandler;
import com.blinnikka.android.qremote.handlers.StaticFileHandler;
import com.blinnikka.android.qremote.handlers.TextToSpeechHandler;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Listens to commands from the network and translates them into local media playback commands.
 */
public class ReceiverService extends Service {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".ReceiverService";

	// **************************************** //
	// Private Fields
	// **************************************** //
	/** The HTTP server instance used to serve requests. */
	private HttpServer server = null;
	
	// **************************************** //
	// Public Methods
	// **************************************** //
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();
		
		Toast.makeText(this, R.string.msg_started, Toast.LENGTH_SHORT).show();
		
		try {
			server = new HttpServer(this, 8080);
			
			// Register all supported paths (this should be a factory or use DI)
			HttpRequestHandlerRegistry registry = server.getRequestHandlerRegistry();
			
			// Handle transport controls and playback state
			registry.register(Shared.URL_MEDIA_CONTROLS, new MediaControlsHandler(this));
			
			// Handle player settings
			registry.register(Shared.URL_MEDIA_SETTINGS + "*", new MediaSettingsHandler(this));
			
			// Handle playlist
			registry.register(Shared.URL_MEDIA_PLAYLIST + "*", new MediaPlaylistHandler(this));
			
			// Handle LEDs
			registry.register(Shared.URL_LED + "*", new LedHandler(this));
			
			// Handle text-to-speech
			registry.register(Shared.URL_SPEAK + "*", new TextToSpeechHandler(this));
			
			// Handle web content as the default
			registry.register(Shared.URL_HTDOCS, new StaticFileHandler(this));
			
		} catch (Exception ex) {
			Log.d(TAG, "Error starting server (1): " + ex.toString());
			Toast.makeText(this,"Error starting server (1).",  Toast.LENGTH_LONG).show();
			ex.printStackTrace();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		super.onStartCommand(intent, flags, startId);
		
		// Start the web server
		try {
			server.startServer();
		} catch (Exception ex) {
			Log.d(TAG, "Error starting server (2): " + ex.toString());
			Toast.makeText(this,"Error starting server (2).",  Toast.LENGTH_LONG).show();
			ex.printStackTrace();
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		
		// Stop the web server
		try {
			server.stopServer();
		} catch (Exception ex) {
			Log.d(TAG, "Error stopping server: " + ex.toString());
			ex.printStackTrace();
		}
		
		server = null;
		
		super.onDestroy();
	}
}



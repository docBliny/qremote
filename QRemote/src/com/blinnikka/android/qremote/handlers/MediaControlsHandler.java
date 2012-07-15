package com.blinnikka.android.qremote.handlers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import com.blinnikka.android.qremote.Shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Handles HTTP requests for media playback controls.
 */
public class MediaControlsHandler extends HandlerBase {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".MediaControlsHandler";

	// **************************************** //
	// Private Fields
	// **************************************** //
	private final static HashMap<String, String> control = new HashMap<String, String>();
	
	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Instantiates a new instance of the MediaControlsHandler object.
	 * 
	 * @param context The application context.
	 */
	public MediaControlsHandler(Context context) {
		super(context);
		Log.d(TAG, ".ctor");

		// Initialize current state
		MediaControlsHandler.control.put(Shared.CONTROL_STATUS, Shared.CONTROL_STATUS_STOP);
		
		// Register for updates from the player
		IntentFilter intentFilter = new IntentFilter();
		
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "onReceive: " + intent.getAction());
				//Utility.dumpIntent(TAG, intent);
				
				if(Shared.ACTION_PLAY_STATUS_RESPONSE.equals(intent.getAction())
						|| Shared.ACTION_PLAY_STATE_CHANGED.equals(intent.getAction())) {
					
					if(intent.getBooleanExtra("preparing", false) 
						&& intent.getBooleanExtra("streaming", false)) {
						MediaControlsHandler.control.put(
								Shared.CONTROL_STATUS, Shared.CONTROL_STATUS_BUFFERING);
					} else if(intent.getBooleanExtra("playing", false)) {
						MediaControlsHandler.control.put(
								Shared.CONTROL_STATUS, Shared.CONTROL_STATUS_PLAY);
					} else {
						Log.d(TAG, "has position=" + intent.hasExtra("position"));
						// Check if not playing and position is zero (= stop)
						Long position = intent.getLongExtra("position", -1);
						if(position == 0) {
							MediaControlsHandler.control.put(
									Shared.CONTROL_STATUS, Shared.CONTROL_STATUS_STOP);
						} else if(position != -1) {
							// Don't update status if we didn't get a value
							MediaControlsHandler.control.put(
									Shared.CONTROL_STATUS, Shared.CONTROL_STATUS_PAUSE);
						}
					}
				} else if(Shared.ACTION_PLAYBACK_FAILED.equals(intent.getAction())) {
					MediaControlsHandler.control.put(
							Shared.CONTROL_STATUS, Shared.CONTROL_STATUS_ERROR);
					
				}
			}
		};
		
		// Filter messages
		intentFilter.addAction(Shared.ACTION_PLAY_STATUS_RESPONSE);
		intentFilter.addAction(Shared.ACTION_PLAY_STATE_CHANGED);
		intentFilter.addAction(Shared.ACTION_PLAYBACK_FAILED);
		context.registerReceiver(receiver, intentFilter);
		
		// Request initial status
		requestPlayStatus();
	}

	// **************************************** //
	// Public Methods
	// **************************************** //
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) 
			throws HttpException, IOException {
		Log.d(TAG, "handle, uri=" + request.getRequestLine().getUri());

		// Make sure we've got a supported method
		String method = assertMethod(request, response, new String[] { "OPTIONS", "GET", "PUT" });
		
		// Set CORS permissions
		setCorsHeaders(response, "GET, PUT");

		// Check the method
		if ("GET".equals(method)) {
			doControlsGet(request, response);
		} else if ("PUT".equals(method)) {
			doControlsPut(request, response);
		} else if ("OPTIONS".equals(method)) {
			// CORS headers set, return 200
			response.setStatusCode(HttpURLConnection.HTTP_OK);
		}
	}
	
	// **************************************** //
	// Private Methods
	// **************************************** //
	/**
	 * Handles a GET request for the controls resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doControlsGet(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doControlsGet");
		
		// Trigger status update - NOTE: This is asynchronous, and won't impact this response!
		requestPlayStatus();

		// Send the response
		respondOK(response, MediaControlsHandler.control);
	}

	/**
	 * Handles a PUT request for the controls resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doControlsPut(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doControlsPut");
		
		String status = assertJsonStringProperty(assertJsonObjectResponse(request, response), 
				response, Shared.CONTROL_STATUS,
				new String[] {Shared.CONTROL_STATUS_PAUSE, Shared.CONTROL_STATUS_PLAY,
				Shared.CONTROL_STATUS_STOP});

		if(Shared.CONTROL_STATUS_PAUSE.equals(status)) {
			doSetPause();
		} else if(Shared.CONTROL_STATUS_PLAY.equals(status)) {
			doSetPlay();
		} else if(Shared.CONTROL_STATUS_STOP.equals(status)) {
			doSetStop();
		} else if(Shared.CONTROL_STATUS_TOGGLE_PAUSE.equals(status)) {
			doSetTogglePause();
		}

		// Attempt to update internal status after sending command
		requestPlayStatus();
	}

	/**
	 * Handles pause resource requests.
	 */
	private void doSetPause() {
		Intent intent = new Intent(Shared.ACTION_MUSIC_COMMAND);
		intent.putExtra(Shared.EXTRA_MUSIC_COMMAND, Shared.EXTRA_MUSIC_COMMAND_PAUSE);
		this.getContext().sendBroadcast(intent);
	}

	/**
	 * Handles play resource requests.
	 */
	private void doSetPlay() {
		Intent intent = new Intent(Shared.ACTION_MUSIC_COMMAND);
		intent.putExtra(Shared.EXTRA_MUSIC_COMMAND, Shared.EXTRA_MUSIC_COMMAND_PLAY);
		this.getContext().sendBroadcast(intent);
	}

	/**
	 * Handles stop resource requests.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doSetStop() {
		Intent intent = new Intent(Shared.ACTION_MUSIC_COMMAND);
		intent.putExtra(Shared.EXTRA_MUSIC_COMMAND, Shared.EXTRA_MUSIC_COMMAND_STOP);
		this.getContext().sendBroadcast(intent);
		MediaControlsHandler.control.put(Shared.CONTROL_STATUS, Shared.CONTROL_STATUS_STOP);
	}

	/**
	 * Handles togglePause resource requests.
	 */
	private void doSetTogglePause() {
		Intent intent = new Intent(Shared.ACTION_MUSIC_COMMAND_TOGGLE_PAUSE);
		this.getContext().sendBroadcast(intent);
	}

	/**
	 * Requests a play status update from the device.
	 */
	private void requestPlayStatus() {
		
		this.getContext().sendBroadcast(new Intent(Shared.ACTION_PLAY_STATUS_REQUEST));
	}

}

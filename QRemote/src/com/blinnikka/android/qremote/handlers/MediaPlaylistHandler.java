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
 * Handles HTTP requests for the active playlist.
 */
public class MediaPlaylistHandler extends HandlerBase {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".MediaPlaylistHandler";

	// **************************************** //
	// Private Fields
	// **************************************** //
	private final static HashMap<String, String> playlistItem = new HashMap<String, String>();
	
	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Instantiates a new instance of the MediaPlaylistHandler object.
	 * 
	 * @param context The application context.
	 */
	public MediaPlaylistHandler(Context context) {
		super(context);
		Log.d(TAG, ".ctor");

		// Initialize current state
		MediaPlaylistHandler.playlistItem.put(Shared.MEDIA_PLAYLISTITEM_URI, null);
		MediaPlaylistHandler.playlistItem.put(Shared.MEDIA_PLAYLISTITEM_ARTISTNAME, null);
		MediaPlaylistHandler.playlistItem.put(Shared.MEDIA_PLAYLISTITEM_ALBUMNAME, null);
		MediaPlaylistHandler.playlistItem.put(Shared.MEDIA_PLAYLISTITEM_TITLENAME, null);
		
		// Register for updates from the player
		IntentFilter intentFilter = new IntentFilter();
		
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "onReceive: " + intent.getAction());

				if(Shared.ACTION_PLAY_STATUS_RESPONSE.equals(intent.getAction())
						|| Shared.ACTION_PLAY_STATE_CHANGED.equals(intent.getAction())) {

					Long itemValue = intent.getLongExtra("id", -1);
					
					MediaPlaylistHandler.playlistItem.put(Shared.MEDIA_PLAYLISTITEM_URI,
							(itemValue != -1) 
								? Shared.MEDIA_PLAYLISTITEM_URN_GOOGLE_PLAY_PREFIX + itemValue
								: "");
					MediaPlaylistHandler.playlistItem.put(Shared.MEDIA_PLAYLISTITEM_ARTISTNAME,
							intent.getStringExtra("artist"));
					MediaPlaylistHandler.playlistItem.put(Shared.MEDIA_PLAYLISTITEM_ALBUMNAME,
							intent.getStringExtra("album"));
					MediaPlaylistHandler.playlistItem.put(Shared.MEDIA_PLAYLISTITEM_TITLENAME,
							intent.getStringExtra("track"));
					
				} else if(Shared.ACTION_QUEUE_CHANGED.equals(intent.getAction())) {
					// TODO: 
				}
			}
		};
		
		// Filter messages
		intentFilter.addAction(Shared.ACTION_PLAY_STATUS_RESPONSE);
		intentFilter.addAction(Shared.ACTION_PLAY_STATE_CHANGED);
		intentFilter.addAction(Shared.ACTION_QUEUE_CHANGED);
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
		String method = assertMethod(request, response, new String[] { "OPTIONS", "GET", "POST" });
		
		// Check resource
		if (Shared.MEDIA_PLAYLISTITEM_CURRENT.equals(request.getRequestLine().getUri().substring(
				Shared.URL_MEDIA_PLAYLIST.length()))) {
			
			// Set CORS permissions
			setCorsHeaders(response, "GET, POST");
			
			// Check the method
			if ("GET".equals(method)) {
				doCurrentItemGet(request, response);
			} else if ("POST".equals(method)) {
				doCurrentItemPut(request, response);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			}
		} else {
			// Return not found
			super.handle(request, response, httpContext);
		}
		
	}
	
	// **************************************** //
	// Private Methods
	// **************************************** //
	/**
	 * Handles a GET request for the current playlist resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doCurrentItemGet(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doCurrentItemGet");
		
		// Trigger status update - NOTE: This is asynchronous, and won't impact this response!
		requestPlayStatus();

		// Send the response
		respondOK(response, MediaPlaylistHandler.playlistItem);
	}

	/**
	 * Handles a PUT request for the playlist resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doCurrentItemPut(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doCurrentItemPut");
		
		String status = assertJsonStringProperty(assertJsonObjectResponse(request, response), response,
				Shared.MEDIA_PLAYLISTITEM_URI,
				new String[] {Shared.MEDIA_PLAYLISTITEM_URN_PREVIOUS,
				Shared.MEDIA_PLAYLISTITEM_URN_NEXT});

		if(Shared.MEDIA_PLAYLISTITEM_URN_PREVIOUS.equals(status)) {
			doSetPreviousItem();
		} else if(Shared.MEDIA_PLAYLISTITEM_URN_NEXT.equals(status)) {
			doSetNextItem();
		}
		
		// Attempt to update internal status after sending command
		requestPlayStatus();
	}

	/**
	 * Handles previous playlist item set requests.
	 */
	private void doSetPreviousItem() {
		Intent intent = new Intent(Shared.ACTION_MUSIC_COMMAND_PREVIOUS);
		this.getContext().sendBroadcast(intent);
	}

	/**
	 * Handles next playlist item set requests.
	 */
	private void doSetNextItem() {
		Intent intent = new Intent(Shared.ACTION_MUSIC_COMMAND_NEXT);
		this.getContext().sendBroadcast(intent);
	}

	/**
	 * Requests a play status update from the device.
	 */
	private void requestPlayStatus() {
		
		this.getContext().sendBroadcast(new Intent(Shared.ACTION_PLAY_STATUS_REQUEST));
	}
}

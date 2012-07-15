package com.blinnikka.android.qremote.handlers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import com.blinnikka.android.qremote.R;
import com.blinnikka.android.qremote.Shared;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

/**
 * Handles HTTP requests for media playback settings.
 */
public class MediaSettingsHandler extends HandlerBase {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".MediaSettingsHandler";

	// **************************************** //
	// Private Fields
	// **************************************** //
	private final static HashMap<String, Integer> volume = new HashMap<String, Integer>();
	private final static HashMap<String, String> mute = new HashMap<String, String>();
	private final static HashMap<String, String> repeat = new HashMap<String, String>();
	private final static HashMap<String, String> shuffle = new HashMap<String, String>();
	
	private AudioManager audioManager;
	
    private static final Method setMasterMuteMethod = getSetMasterMuteMethod();
    
	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Instantiates a new instance of the MediaSettingsHandler object.
	 * 
	 * @param context The application context.
	 */
	public MediaSettingsHandler(Context context) {
		super(context);
		Log.d(TAG, ".ctor");

		// Get AudioManager instance
		this.audioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));

		// Initialize current state
        int volume = this.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		MediaSettingsHandler.volume.put(Shared.MEDIA_SETTINGS_VOLUME_VALUE, volume); 

		MediaSettingsHandler.mute.put(Shared.MEDIA_SETTINGS_MUTE_VALUE,
				Shared.MEDIA_SETTINGS_MUTE_VALUE_UNKNOWN); 
		
		MediaSettingsHandler.repeat.put(Shared.MEDIA_SETTINGS_REPEAT_VALUE,
				Shared.MEDIA_SETTINGS_REPEAT_VALUE_UNKNOWN); 
		
		MediaSettingsHandler.shuffle.put(Shared.MEDIA_SETTINGS_SHUFFLE_VALUE,
				Shared.MEDIA_SETTINGS_SHUFFLE_VALUE_UNKNOWN); 
		
		// Register for updates from the player
		IntentFilter intentFilter = new IntentFilter();
		
		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG, "onReceive: " + intent.getAction());
				//Utility.dumpIntent(TAG, intent);
				
				if(Shared.ACTION_MASTER_VOLUME_CHANGED.equals(intent.getAction())) {
					MediaSettingsHandler.volume.put(Shared.MEDIA_SETTINGS_VOLUME_VALUE, 
							intent.getExtras().getInt(
									Shared.EXTRA_MASTER_VOLUME_VALUE, 
									Shared.MEDIA_SETTINGS_VOLUME_VALUE_UNKNOWN));
					
				} else if(Shared.ACTION_MASTER_MUTE_CHANGED.equals(intent.getAction())) {
					if (intent.getBooleanExtra(Shared.EXTRA_MASTER_MUTE_VALUE, false)) {
						MediaSettingsHandler.mute.put(Shared.MEDIA_SETTINGS_MUTE_VALUE,
								Shared.MEDIA_SETTINGS_MUTE_VALUE_ON);
					} else {
						MediaSettingsHandler.mute.put(Shared.MEDIA_SETTINGS_MUTE_VALUE,
								Shared.MEDIA_SETTINGS_MUTE_VALUE_OFF);
					}
					
				} else if(Shared.ACTION_REPEAT_CHANGED.equals(intent.getAction())) {
					int repeatMode = intent.getExtras().getInt("repeat", -1);

					if (repeatMode == 0) {
						MediaSettingsHandler.repeat.put(Shared.MEDIA_SETTINGS_REPEAT_VALUE,
								Shared.MEDIA_SETTINGS_REPEAT_VALUE_OFF);
					} else if (repeatMode == 1) {
						MediaSettingsHandler.repeat.put(Shared.MEDIA_SETTINGS_REPEAT_VALUE,
								Shared.MEDIA_SETTINGS_REPEAT_VALUE_SINGLE);
					} else if (repeatMode == 2) {
						MediaSettingsHandler.repeat.put(Shared.MEDIA_SETTINGS_REPEAT_VALUE,
								Shared.MEDIA_SETTINGS_REPEAT_VALUE_ALL);
					} else {
						Log.d(TAG, "Unknown repeat mode received.");
						MediaSettingsHandler.repeat.put(Shared.MEDIA_SETTINGS_REPEAT_VALUE,
								Shared.MEDIA_SETTINGS_REPEAT_VALUE_UNKNOWN);
					}
					
				} else if(Shared.ACTION_SHUFFLE_CHANGED.equals(intent.getAction())) {
					int shuffleMode = intent.getExtras().getInt("shuffle", -1);

					if (shuffleMode == 0) {
						MediaSettingsHandler.shuffle.put(Shared.MEDIA_SETTINGS_SHUFFLE_VALUE,
								Shared.MEDIA_SETTINGS_SHUFFLE_VALUE_OFF);
					} else if (shuffleMode == 1) {
						MediaSettingsHandler.shuffle.put(Shared.MEDIA_SETTINGS_SHUFFLE_VALUE,
								Shared.MEDIA_SETTINGS_SHUFFLE_VALUE_ALL);
					} else {
						Log.d(TAG, "Unknown shuffle mode received.");
						MediaSettingsHandler.shuffle.put(Shared.MEDIA_SETTINGS_SHUFFLE_VALUE,
								Shared.MEDIA_SETTINGS_SHUFFLE_VALUE_UNKNOWN);
					}
				}
			}
		};
		
		// Filter messages
		intentFilter.addAction(Shared.ACTION_MASTER_VOLUME_CHANGED);
		intentFilter.addAction(Shared.ACTION_MASTER_MUTE_CHANGED);
		intentFilter.addAction(Shared.ACTION_REPEAT_CHANGED);
		intentFilter.addAction(Shared.ACTION_SHUFFLE_CHANGED);
		context.registerReceiver(receiver, intentFilter);
	}

	// **************************************** //
	// Public Methods
	// **************************************** //
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) 
			throws HttpException, IOException {
		Log.d(TAG, "handle, uri=" + request.getRequestLine().getUri());
		
		// Make sure we've got a supported method
		String method = assertMethod(request, response, new String[] { "OPTIONS", "GET", "PUT" });

		String subResource = request.getRequestLine().getUri().substring(
				Shared.URL_MEDIA_SETTINGS.length());
		
		// Check resource
		if (Shared.MEDIA_SETTINGS_VOLUME.equals(subResource)) {
		
			// Set CORS permissions
			setCorsHeaders(response, "GET, PUT");
			
			// Check the method
			if ("GET".equals(method)) {
				doVolumeGet(request, response);
			} else if ("PUT".equals(method)) {
				doVolumePut(request, response);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			}
			
			return;
		} else if (Shared.MEDIA_SETTINGS_MUTE.equals(subResource)) {
			
			// Set CORS permissions
			setCorsHeaders(response, "GET, PUT");
			
			// Check the method
			if ("GET".equals(method)) {
				doMuteGet(request, response);
			} else if ("PUT".equals(method)) {
				doMutePut(request, response);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			}
			
			return;
		}
		
		// The device doesn't currently respect external setting of repeat and shuffle
		method = assertMethod(request, response, new String[] { "OPTIONS", "GET" });
		
		if (Shared.MEDIA_SETTINGS_REPEAT.equals(subResource)) {
			
			// Set CORS permissions
			setCorsHeaders(response, "GET");
			
			// Check the method
			if ("GET".equals(method)) {
				doRepeatGet(request, response);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			} /* else if ("PUT".equals(method)) {
				doRepeatPut(request, response);
			}*/
		} else if (Shared.MEDIA_SETTINGS_SHUFFLE.equals(subResource)) {
			
			// Set CORS permissions
			setCorsHeaders(response, "GET");
			
			// Check the method
			if ("GET".equals(method)) {
				doShuffleGet(request, response);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			} /*else if ("PUT".equals(method)) {
				doShufflePut(request, response);
			}*/
		} else {
			// Return not found
			super.handle(request, response, httpContext);
		}
	}

	// **************************************** //
	// Private Methods
	// **************************************** //
	/**
	 * Handles a GET request for the volume resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doVolumeGet(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doVolumeGet");
		
		// Send the response
		respondOK(response, MediaSettingsHandler.volume);
	}

	/**
	 * Handles a PUT request for the volume resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doVolumePut(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doVolumePut");
		
		// Attempt to get the given volume value
		int volume = assertJsonIntegerProperty(assertJsonObjectResponse(request, response), response,
				Shared.MEDIA_SETTINGS_VOLUME_VALUE, null);

		// Make sure we're within a valid range
		if(volume >=0 && volume <= this.audioManager.getStreamMaxVolume(
				AudioManager.STREAM_MUSIC)) {
			this.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
					volume, AudioManager.FLAG_SHOW_UI);
		} else {
			String message = String.format(this.getContext().getResources().getString(
					R.string.invalid_value), volume, Shared.MEDIA_SETTINGS_VOLUME_VALUE);
			respondBadRequest(response, message);
		}
	}

	/**
	 * Handles a GET request for the mute resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doMuteGet(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doMuteGet");
		
		respondOK(response, MediaSettingsHandler.mute);
	}

	/**
	 * Handles a PUT request for the mute resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doMutePut(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doMutePut");
		
		// Attempt to get the given mute value
		String mute = assertJsonStringProperty(assertJsonObjectResponse(request, response), response,
				Shared.MEDIA_SETTINGS_MUTE_VALUE, new String[] {
				Shared.MEDIA_SETTINGS_MUTE_VALUE_OFF, Shared.MEDIA_SETTINGS_MUTE_VALUE_ON });

		if(Shared.MEDIA_SETTINGS_MUTE_VALUE_OFF.equals(mute)) {
			setMasterMute(false);
		} else if(Shared.MEDIA_SETTINGS_MUTE_VALUE_ON.equals(mute)) {
			setMasterMute(true);
		} else {
			Log.d(TAG,"Unknown mute value.");
		}
	}

	/**
	 * Handles a GET request for the repeat resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doRepeatGet(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doRepeatGet");
		
		respondOK(response, MediaSettingsHandler.repeat);
	}

//	/**
//	 * Handles a PUT request for the repeat resource.
//	 * 
//	 * @param request The original HTTP request.
//	 * @param response The response object to return data.
//	 */
//	private void doRepeatPut(HttpRequest request, HttpResponse response) {
//		Log.d(TAG, "doRepeatPut");
//		
//		Intent intent = new Intent(Shared.INTENT_REPEAT_CHANGED);
//
//		// Attempt to get the given repeat value
//		String mute = assertJsonStringProperty(request, response, 
//				Shared.MEDIA_SETTINGS_REPEAT_VALUE, new String[] {
//				Shared.MEDIA_SETTINGS_REPEAT_VALUE_OFF, Shared.MEDIA_SETTINGS_REPEAT_VALUE_ALL,
//				Shared.MEDIA_SETTINGS_REPEAT_VALUE_SINGLE });
//
//		if(Shared.MEDIA_SETTINGS_REPEAT_VALUE_OFF.equals(mute)) {
//			intent.putExtra("repeat", 0);
//			this.getContext().sendBroadcast(intent);
//		} else if(Shared.MEDIA_SETTINGS_REPEAT_VALUE_SINGLE.equals(mute)) {
//			intent.putExtra("repeat", 1);
//			this.getContext().sendBroadcast(intent);
//		} else if(Shared.MEDIA_SETTINGS_REPEAT_VALUE_ALL.equals(mute)) {
//			intent.putExtra("repeat", 2);
//			this.getContext().sendBroadcast(intent);
//		} else {
//			Log.d(TAG,"Unknown repeat value.");
//		}
//	}

	/**
	 * Handles a GET request for the shuffle resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doShuffleGet(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doShuffleGet");
		
		respondOK(response, MediaSettingsHandler.shuffle);
	}

//	/**
//	 * Handles a PUT request for the shuffle resource.
//	 * 
//	 * @param request The original HTTP request.
//	 * @param response The response object to return data.
//	 */
//	private void doShufflePut(HttpRequest request, HttpResponse response) {
//		Log.d(TAG, "doShufflePut");
//		
//		Intent intent = new Intent(Shared.INTENT_SHUFFLE_CHANGED);
//
//		// Attempt to get the given shuffle value
//		String mute = assertJsonStringProperty(request, response, 
//				Shared.MEDIA_SETTINGS_SHUFFLE_VALUE, new String[] {
//				Shared.MEDIA_SETTINGS_SHUFFLE_VALUE_OFF, Shared.MEDIA_SETTINGS_SHUFFLE_VALUE_ALL });
//
//		if(Shared.MEDIA_SETTINGS_SHUFFLE_VALUE_OFF.equals(mute)) {
//			intent.putExtra("shuffle", 0);
//			this.getContext().sendBroadcast(intent);
//		} else if(Shared.MEDIA_SETTINGS_SHUFFLE_VALUE_ALL.equals(mute)) {
//			intent.putExtra("shuffle", 1);
//			this.getContext().sendBroadcast(intent);
//		}
//	}

	/**
	 * Gets a reference to a non-public method named setMasterMute();
	 * 
	 * @return The Method instance or null if an error occurred.
	 */
    private static Method getSetMasterMuteMethod()
    {
        Method result = null;
        @SuppressWarnings("rawtypes")
		Class aclass[] = new Class[2];
        aclass[0] = Boolean.TYPE;
        aclass[1] = Integer.TYPE;
        
        try {
            result = AudioManager.class.getMethod("setMasterMute", aclass);
        } catch (NoSuchMethodException ex) {
            Log.d(TAG, "Unable to find setMasterMute() method.");
        }

        return result;
    }

    /**
     * Sets the master mute status for the device.
     * 
     * @param mute true to turn mute on, false to turn mute off.
     */
    private void setMasterMute(boolean mute)
    {
        if(setMasterMuteMethod == null) {
        	return;
        }
            
        Method method = setMasterMuteMethod;
        Object aobj[] = new Object[2];
        aobj[0] = Boolean.valueOf(mute);
        aobj[1] = Integer.valueOf(1);
        
        try {
            method.invoke(this.audioManager, aobj);
        } catch (Exception ex) {
            Log.d(TAG, "Bad method invocation calling setMasterMute().");
        }
    }
}

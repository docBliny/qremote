package com.blinnikka.android.qremote.handlers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import com.blinnikka.android.qremote.R;
import com.blinnikka.android.qremote.Shared;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

/**
 * Handles HTTP requests for the active playlist.
 */
public class TextToSpeechHandler extends HandlerBase {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".TextToSpeechHandler";

	// **************************************** //
	// Private Fields
	// **************************************** //
	/** The text-to-speech engine. */
	TextToSpeech engine;
	
	/** A value indicating whether the speech engine is ready. */
	Boolean isConnected = false;
	
	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Instantiates a new instance of the TextToSpeechHandler object.
	 * 
	 * @param context The application context.
	 */
	public TextToSpeechHandler(Context context) {
		super(context);
		Log.d(TAG, ".ctor");

		this.engine = new TextToSpeech(context, new OnInitListener() {

			public void onInit(int status) {
				isConnected = true;
			}
			
		});
	}

	// **************************************** //
	// Public Methods
	// **************************************** //
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) 
			throws HttpException, IOException {
		Log.d(TAG, "handle, uri=" + request.getRequestLine().getUri());

		// Make sure we've got a supported method
		String method = assertMethod(request, response, new String[] { "OPTIONS", "PUT" });
		String subResource = request.getRequestLine().getUri().substring(Shared.URL_SPEAK.length());
		
		// Set CORS permissions
		setCorsHeaders(response, "PUT");
		
		// Check resource
		if ("".equals(subResource)) {
			// Check the method
			if ("PUT".equals(method)) {
				doSpeakPut(request, response);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			}
		} else  if (("/" + Shared.SPEAK_SENTENCE).equals(subResource)) {
			// Check the method
			if ("PUT".equals(method)) {
				doSentencePut(request, response);
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
	 * Handles a PUT request for the sentence resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doSpeakPut(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doSpeakPut");
		
		// Sanity status check
		if (!this.isConnected) {
			Log.d(TAG, "Not connected to tts, bailing.");
			
			respondServiceUnavailable(response,this.getContext().getResources().getString(
					R.string.service_unavailable));
			
			// NOTE: Function exit!
			return;
		}
		
		try {
			// Get data
			JSONObject data = assertJsonObjectResponse(request, response);
			
			Double pitch = data.optDouble(Shared.SPEAK_SENTENCE_PITCH);
			if(pitch != null && !Double.isNaN(pitch)) {
				this.engine.setPitch(pitch.floatValue());
			} else {
				respondBadRequest(response, String.format(
						this.getContext().getResources().getString(R.string.invalid_value), 
						pitch, Shared.SPEAK_SENTENCE_RATE));
			}
			
			Double rate = data.optDouble(Shared.SPEAK_SENTENCE_RATE);
			if(rate != null && !Double.isNaN(rate)) {
				this.engine.setSpeechRate(rate.floatValue());
			} else {
				respondBadRequest(response, String.format(
						this.getContext().getResources().getString(R.string.invalid_value), 
						rate, Shared.SPEAK_SENTENCE_RATE));
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
			ex.printStackTrace();
		}
	}
	
	/**
	 * Handles a PUT request for the sentence resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doSentencePut(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doSentencePut");
		
		// Sanity status check
		if (!this.isConnected) {
			Log.d(TAG, "Not connected to tts, bailing.");
			
			respondServiceUnavailable(response,this.getContext().getResources().getString(
					R.string.service_unavailable));

			// NOTE: Function exit!
			return;
		}
		
		// Verify required fields
		JSONObject data = assertJsonObjectResponse(request, response);
		String text = assertJsonStringProperty(data, response, Shared.SPEAK_SENTENCE_TEXT, null);
		
		if (text != null) {
			// Get optional fields and use default values if missing
			Double tempDouble;
			HashMap<String, String> params = new HashMap<String, String>();

			// Get queue mode or use default
			int queueMode = data.optInt(Shared.SPEAK_SENTENCE_QUEUEMODE, TextToSpeech.QUEUE_ADD);
			
			// Get volume if available
			tempDouble = data.optDouble(Shared.SPEAK_SENTENCE_VOLUME);
			if(tempDouble != null && !Double.isNaN(tempDouble)) {
				Log.d(TAG, "Volume=" + tempDouble);
				params.put(Engine.KEY_PARAM_VOLUME, tempDouble.toString());
			}
			
			// Get pan if available
			tempDouble = data.optDouble(Shared.SPEAK_SENTENCE_PAN);
			if(tempDouble != null && !Double.isNaN(tempDouble)) {
				Log.d(TAG, "Pan=" + tempDouble);
				params.put(Engine.KEY_PARAM_PAN, tempDouble.toString());
			}
			
			if(params.isEmpty()) {
				params = null;
			}
			
			engine.speak(text, queueMode, params);
		} else {
			respondBadRequest(response, String.format(this.getContext().getResources().getString(
					R.string.missing_field), Shared.SPEAK_SENTENCE_TEXT));
		}
	}
}

package com.blinnikka.android.qremote.handlers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blinnikka.android.qremote.LedClient;
import com.blinnikka.android.qremote.R;
import com.blinnikka.android.qremote.Shared;
import com.blinnikka.android.qremote.LedClient.Callbacks;
import com.google.tungsten.ledcommon.LedAnimation;
import com.google.tungsten.ledcommon.LedAnimation.LED;

import android.content.Context;
import android.util.Log;

/**
 * Handles HTTP requests for controlling and reading LED states.
 */
public class LedHandler extends HandlerBase {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".MediaSettingsHandler";
	
	/** The default priority for our application for LED access. */
	private static final int DEFAULT_PRIORITY = 7; 

	// **************************************** //
	// Private Fields
	// **************************************** //
	/** The JSON object containing the current animation resource. */
	private final static HashMap<String, Object> animation = new HashMap<String, Object>();
	
	/** The LED client class to communicate with the device LEDs. */
	private final LedClient ledClient;
	
	/** A value indicating whether the LED service is connected. */
	Boolean isConnected = false;
	
	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Instantiates a new instance of the LedHandler object.
	 * 
	 * @param context The application context.
	 */
	public LedHandler(Context context) {
		super(context);
		Log.d(TAG, ".ctor");

		LedHandler.animation.put(Shared.LED_ANIMATION_TYPE, Shared.LED_ANIMATION_TYPE_UNKNOWN);
		LedHandler.animation.put(Shared.LED_ANIMATION_FIELDS, new JSONArray());
		
		this.ledClient = new LedClient(context, "LedHandler", new Callbacks() {

			public void ledClientConnected(LedClient ledClient) {
				Log.d(TAG, "ledClientConnected");
				
				ledClient.enable(DEFAULT_PRIORITY);
				isConnected = true;
			}

			public void ledClientDisconnected(LedClient ledClient) {
				Log.d(TAG, "ledClientDisconnected");
				isConnected = false;
			}
		});
		
		// Connect to the LED service
		ledClient.connect();
	}

	// **************************************** //
	// Public Methods
	// **************************************** //
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) 
			throws HttpException, IOException {
		Log.d(TAG, "handle, uri=" + request.getRequestLine().getUri());
		
		if (!this.isConnected) {
			Log.d(TAG, "Not connected to LED service, bailing.");
			
			respondServiceUnavailable(response,this.getContext().getResources().getString(
					R.string.service_unavailable));
			
			// NOTE: Function exit!
			return;
		}
		
		// Make sure we've got a supported method
		String method = assertMethod(request, response, new String[] { "OPTIONS", /*"GET",*/ "PUT" });
		String subResource = request.getRequestLine().getUri().substring(Shared.URL_LED.length());
		
		// Check resource
		if (Shared.LED_ANIMATION.equals(subResource)) {
		
			// Set CORS permissions
			//setCorsHeaders(response, "GET, PUT");
			setCorsHeaders(response, "PUT");
			
			// Check the method
			if ("GET".equals(method)) {
				doAnimationGet(request, response);
			} else if ("PUT".equals(method)) {
				doAnimationPut(request, response);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			}
			
		} else if (Shared.LED_LEDS_ALL.equals(subResource)) {
			
			// Set CORS permissions
			setCorsHeaders(response, "PUT");

			// Reassert PUT only since we can't read from the device
			method = assertMethod(request, response, new String[] { "OPTIONS", "PUT" });
			if ("PUT".equals(method)) {
				doLedAllPut(request, response);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			}
			
		} else if (Shared.LED_LEDS_STATUS.equals(subResource)) {

			// Set CORS permissions
			setCorsHeaders(response, "PUT");

			// Reassert PUT only since we can't read from the device
			method = assertMethod(request, response, new String[] { "OPTIONS", "PUT" });
			if ("PUT".equals(method)) {
				doLedPut(request, response, true);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			}
			
		} else if (Shared.LED_LEDS.equals(subResource)) {
			// This block is for setting a range of LEDs to specific values
			
			// Set CORS permissions
			setCorsHeaders(response, "PUT");

			// Reassert PUT only since we can't read from the device
			method = assertMethod(request, response, new String[] { "OPTIONS", "PUT" });
			if ("PUT".equals(method)) {
				doLedRangePut(request, response);
			} else if ("OPTIONS".equals(method)) {
				// CORS headers set, return 200
				response.setStatusCode(HttpURLConnection.HTTP_OK);
			}
			
		} else if (subResource != null && subResource.startsWith(Shared.LED_LEDS + "/")) {
			
			// Reassert PUT only since we can't read from the device
			method = assertMethod(request, response, new String[] { "OPTIONS", "PUT" });
			
			// Set CORS permissions
			setCorsHeaders(response, "PUT");

			if ("PUT".equals(method)) {
				
				// Get and verify LED ID
				String resourceId = subResource.substring(Shared.LED_LEDS.length() + 1);
				Boolean isValid = false;
				
				try {
					Integer ledId = Integer.parseInt(resourceId);
					
					if(ledId != null && (ledId >= 0 && ledId <= ledClient.getLedCount())) {
						isValid = true;
						doLedPut(request, response, false);
					}					
				} catch(NumberFormatException ex) {
					// NO-OP
				}

				if(!isValid) {
					respondBadRequest(response, String.format(
							this.getContext().getResources().getString(R.string.invalid_value), 
							resourceId, Shared.LED_ANIMATION_ID));
				}
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
	 * Handles a GET request for the animation resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doAnimationGet(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doAnimationGet");
		
		// Send the response
		respondOK(response, LedHandler.animation);
	}

	/**
	 * Handles a PUT request for the animation resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doAnimationPut(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doAnimationPut");
		
		JSONObject data = assertJsonObjectResponse(request, response);
		
		// Get the type and validate it's supported
		String type = assertJsonStringProperty(data, response, 
				Shared.LED_ANIMATION_TYPE, new String[] {
				Shared.LED_ANIMATION_TYPE_NONE,
				Shared.LED_ANIMATION_TYPE_BUILTIN, Shared.LED_ANIMATION_TYPE_CUSTOM });
		
		if(Shared.LED_ANIMATION_TYPE_NONE.equals(type)) {
			ledClient.cancelAnimation();
		} else if(Shared.LED_ANIMATION_TYPE_BUILTIN.equals(type)) {
			// Validate the ID
			int id = assertJsonIntegerProperty(data, response, Shared.LED_ANIMATION_ID, null);

			if(id != Integer.MIN_VALUE) {
				// Start the animation
				ledClient.setBuiltInAnimation(id);
			} else {
				respondBadRequest(response, this.getContext().getResources().getString(
						R.string.malformed_data));
			}
		} else if(Shared.LED_ANIMATION_TYPE_CUSTOM.equals(type)) {
			// Validate that fields exist
			JSONArray fields = assertJsonArrayProperty(data, response, Shared.LED_ANIMATION_FIELDS);
			Boolean repeat = assertJsonBooleanProperty(data, response, Shared.LED_ANIMATION_REPEAT, 
					false);

			// Transform each entry into LED items for an animation, bail on error
			if (fields != null && fields.length() > 0) {
				LedAnimation animation = null;
				LED led;
				JSONObject field;
				try {
					animation = new LedAnimation(fields.length());
					for (int index = 0; index < fields.length(); index +=1) {
						field = fields.getJSONObject(index);
						
						led = new LED(field.getInt(Shared.ANIMATION_FRAME_LEDID),
								field.getInt(Shared.ANIMATION_FRAME_RED),
								field.getInt(Shared.ANIMATION_FRAME_GREEN),
								field.getInt(Shared.ANIMATION_FRAME_BLUE),
								field.getInt(Shared.ANIMATION_FRAME_START));
						
						animation.addLed(led);
					}
					
					// Start the animation
					ledClient.setAnimation(animation, repeat);
				} catch (JSONException e) {
					e.printStackTrace();
					
					respondBadRequest(response, this.getContext().getResources().getString(
							R.string.malformed_data));
				}
			} else if (fields == null || fields.length() == 0) {
				respondBadRequest(response, String.format(
						this.getContext().getResources().getString(R.string.invalid_value), "", 
						Shared.LED_ANIMATION_FIELDS));
			}
		} else {
			Log.d(TAG,"Unknown animation type value.");
		}
	}

	/**
	 * Handles a PUT request for the all LEDs resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doLedAllPut(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doLedAllPut");
		
		JSONObject data = assertJsonObjectResponse(request, response);

		if (data != null) {
			try {
				ledClient.setAllLeds(data.getInt(Shared.ANIMATION_FRAME_RED),
						data.getInt(Shared.ANIMATION_FRAME_GREEN),
						data.getInt(Shared.ANIMATION_FRAME_BLUE));
			} catch (JSONException e) {
				e.printStackTrace();
				respondBadRequest(response, this.getContext().getResources().getString(
						R.string.malformed_data));
			}
		}
	}

	/**
	 * Handles a PUT request for the all LEDs resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doLedPut(HttpRequest request, HttpResponse response, Boolean statusLed) {
		Log.d(TAG, "doLedPut");
		
		JSONObject data = assertJsonObjectResponse(request, response);

		if (data != null) {
			try {
				if (statusLed) {
					ledClient.setLed(Shared.LED_STATUS_ID,
							data.getInt(Shared.ANIMATION_FRAME_RED),
							data.getInt(Shared.ANIMATION_FRAME_GREEN),
							data.getInt(Shared.ANIMATION_FRAME_BLUE));
				} else {
					ledClient.setLed(data.getInt(Shared.LED_ANIMATION_ID),
							data.getInt(Shared.ANIMATION_FRAME_RED),
							data.getInt(Shared.ANIMATION_FRAME_GREEN),
							data.getInt(Shared.ANIMATION_FRAME_BLUE));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				respondBadRequest(response, this.getContext().getResources().getString(
						R.string.malformed_data));
			}
		}
	}

	/**
	 * Handles a PUT request for the leds parent resource.
	 * 
	 * @param request The original HTTP request.
	 * @param response The response object to return data.
	 */
	private void doLedRangePut(HttpRequest request, HttpResponse response) {
		Log.d(TAG, "doLedRangePut");
		
		JSONObject data = assertJsonObjectResponse(request, response);
		
		// Validate that fields exist
		JSONArray leds = assertJsonArrayProperty(data, response, Shared.LED_LEDS);
		
		if (leds != null && leds.length() > 0) {
			
			int start = assertJsonIntegerProperty(data, response, Shared.LED_LEDS_START, null);
			int count  = assertJsonIntegerProperty(data, response, Shared.LED_LEDS_COUNT, null);
			
			if(start == Integer.MIN_VALUE || count == Integer.MIN_VALUE) {
				respondBadRequest(response, this.getContext().getResources().getString(
						R.string.malformed_data));
				
				// NOTE: Function exit!
				return;
			}
			
			JSONObject item;
			int[] rgbValues = new int[leds.length() * 3];
			
			if (count > leds.length() || (start + count > ledClient.getLedCount())) {
				respondBadRequest(response, String.format(
						this.getContext().getResources().getString(R.string.invalid_value), count, 
						Shared.LED_LEDS_COUNT));
				
				// NOTE: Function exit!
				return;
			}
			
			try {
				
				// Loop items in the array
				int outIndex = 0;
				for (int index = 0; index < leds.length(); index +=1) {
					item = leds.getJSONObject(index);
					
					rgbValues[outIndex++] = item.getInt(Shared.ANIMATION_FRAME_RED);
					rgbValues[outIndex++] = item.getInt(Shared.ANIMATION_FRAME_GREEN);
					rgbValues[outIndex++] = item.getInt(Shared.ANIMATION_FRAME_BLUE);
				}

				// Set the range
				ledClient.setLedRange(start, count, rgbValues);
			} catch (JSONException e) {
				e.printStackTrace();
				
				respondBadRequest(response, this.getContext().getResources().getString(
						R.string.malformed_data));
			}
		} else if (leds == null || leds.length() == 0) {
			respondBadRequest(response, String.format(
					this.getContext().getResources().getString(R.string.invalid_value), "", 
					Shared.LED_LEDS));
		}
	}

}

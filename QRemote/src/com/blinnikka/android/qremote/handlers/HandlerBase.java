package com.blinnikka.android.qremote.handlers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blinnikka.android.net.JsonResponse;
import com.blinnikka.android.qremote.R;
import com.blinnikka.android.qremote.Shared;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

/**
 * Handles HTTP requests for media playback controls.
 */
public class HandlerBase implements HttpRequestHandler {

	// **************************************** //
	// Constants
	// **************************************** //
	private static final String TAG = Shared.APP_NAME + ".HandlerBase";

	// **************************************** //
	// Private Fields
	// **************************************** //
	private Context context = null;

	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Initializes a new instance of the HandlerBase object.
	 * 
	 * @param context The application context.
	 */
	public HandlerBase(Context context) {
		Log.d(TAG, ".ctor");
		this.context = context;
	}

	// **************************************** //
	// Public Properties
	// **************************************** //
	/**
	 * Gets the application context.
	 * 
	 * @return The application context object.
	 */
	public Context getContext() {
		return this.context;
	}
	
	// **************************************** //
	// Public Methods
	// **************************************** //
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) 
			throws HttpException, IOException {
		Log.d(TAG, "handle, uri=" + request.getRequestLine().getUri());
		
		respondNotFound(response, context.getResources().getString(R.string.not_found));
	}

	// **************************************** //
	// Protected Methods
	// **************************************** //
	/**
	 * Checks if the given request is either a method in the given string array.
	 * If the method is not in the array, a Not Implemented response is sent.
	 * 
	 * Additionally checks for matches in the method override header.
	 * 
	 * @param request The HTTP request to check.
	 * @param response The response object to use.
	 * @param validMethods An array of strings that contains valid method names. 
	 * 
	 * @return A string containing a method name or an empty string if no method matched
	 * the allowed list.
	 */
	protected String assertMethod(HttpRequest request, HttpResponse response, 
			String[] validMethods) {

		String method = request.getRequestLine().getMethod();
		String result = "";

		// Check for method override
		if(request.containsHeader(Shared.HEADER_METHOD_OVERRIDE)) {
			method = request.getFirstHeader(Shared.HEADER_METHOD_OVERRIDE).getValue();;
		}

		if(Arrays.asList(validMethods).contains(method)) {
			result = method;
		} else {
			// Return a "not allowed" response
			respondNotAllowed(response, TextUtils.join(", ", validMethods));
		}
		
		return result;
	}
	
	/**
	 * Checks if the request body exists and contains a JSON object.
	 * 
	 * A Bad Request will be populated in the response if the body is missing.
	 * 
	 * @param request The HTTP request to check.
	 * @param response The response object to use.
	 * 
	 * @return The JSONObject containing the response body or null if content not valid.
	 */
	protected JSONObject assertJsonObjectResponse(HttpRequest request, HttpResponse response) {
		
		Boolean isValid = false;
		String message = "";
		JSONObject result = null;
		
		// Make sure we have a body
		if(request instanceof HttpEntityEnclosingRequest) {
			HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
			
			// Attempt to parse the body as JSON
			try {
				result = new JSONObject(EntityUtils.toString(entity));
				
				if(result != null) {
					isValid = true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(isValid) {
			response.setStatusCode(HttpURLConnection.HTTP_OK);
			setCacheHeaders(response);
		} else {
			message = this.getContext().getResources().getString(R.string.malformed_data);
			respondBadRequest(response, message);
		}
		
		return result;
	}
	
	/**
	 * Checks if the request body exists and contains a JSON array.
	 * 
	 * A Bad Request will be populated in the response if the body is missing.
	 * 
	 * @param request The HTTP request to check.
	 * @param response The response object to use.
	 * 
	 * @return The JSONObject containing the response body or null if content not valid.
	 */
	protected JSONArray assertJsonArrayResponse(HttpRequest request, HttpResponse response) {
		
		Boolean isValid = false;
		String message = "";
		JSONArray result = null;
		
		// Make sure we have a body
		if(request instanceof HttpEntityEnclosingRequest) {
			HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
			
			// Attempt to parse the body as JSON
			try {
				result = new JSONArray(EntityUtils.toString(entity));
				
				if(result != null) {
					isValid = true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(isValid) {
			response.setStatusCode(HttpURLConnection.HTTP_OK);
			setCacheHeaders(response);
		} else {
			message = this.getContext().getResources().getString(R.string.malformed_data);
			respondBadRequest(response, message);
		}
		
		return result;
	}
	
	/**
	 * Checks if the given JSON object contains the specified property with an array value within.
	 * 
	 * A Bad Request will be populated in the response if the field is not valid.
	 * 
	 * @param data The parent JSON object.
	 * @param response The response object to use.
	 * @param propertyName The top-level property to check.
	 * 
	 * @return The array value from the requested field or null if field or value was invalid.
	 */
	protected JSONArray assertJsonArrayProperty(JSONObject data, HttpResponse response,
			String propertyName) {
		
		Boolean isValid = false;
		String message = "";
		JSONArray result = null;
		
		if(data != null && !data.isNull(propertyName)) {
			// Attempt to get JSON array
			result = data.optJSONArray(propertyName);
			if (result != null) {
				isValid = true;
			}
		}
		
		if(isValid) {
			response.setStatusCode(HttpURLConnection.HTTP_OK);
			setCacheHeaders(response);
		} else {
			message = String.format(this.getContext().getResources().getString(
					R.string.missing_field), propertyName);
			respondBadRequest(response, message);
		}
		
		return result;
	}
	
	/**
	 * Checks if the given JSON object contains the specified property with a string value. 
	 * Optionally checks the values of the property against an array of valid field values.
	 * 
	 * A Bad Request will be populated in the response if the field is not valid, 
	 * has malformed data, or fails the optional data value check. 
	 * 
	 * @param data The parent JSON object.
	 * @param response The response object to use.
	 * @param propertyName The top-level property to check.
	 * @param validValues An optional array of strings that contains valid values for the property.
	 * No value check is performed if this is null. 
	 * 
	 * @return The string value from the requested field or null if field or value was invalid.
	 */
	protected String assertJsonStringProperty(JSONObject data, HttpResponse response,
			String propertyName, String[] validValues) {
		
		Boolean isValid = false;
		String message = "";
		String result = null;
		
		try {
			if(data != null && !data.isNull(propertyName)) {
				// Check if we should validate field values
				if (validValues != null) {
					// Make sure a supported value was given
					if(Arrays.asList(validValues).contains(data.getString(propertyName))) {
						result = data.getString(propertyName);
						isValid = true;
					} else {
						message = String.format(this.getContext().getResources().getString(
								R.string.invalid_value), data.getString(propertyName),
								propertyName);
					}
				} else {
					result = data.getString(propertyName);
					isValid = true;
				}
			} else {
				message = String.format(this.getContext().getResources().getString(
						R.string.missing_field), propertyName);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			message = this.getContext().getResources().getString(R.string.malformed_data);
		}
		
		if(isValid) {
			response.setStatusCode(HttpURLConnection.HTTP_OK);
			setCacheHeaders(response);
		} else {
			respondBadRequest(response, message);
		}
		
		return result;
	}
	
	/**
	 * Checks if the given JSON object contains the specified property with an integer value. 
	 * Optionally checks the values of the property against an array of valid field values.
	 * 
	 * A Bad Request will be populated in the response if the field is not valid, 
	 * has malformed data, or fails the optional data value check. 
	 * 
	 * @param data The parent JSON object.
	 * @param response The response object to use.
	 * @param propertyName The top-level property to check.
	 * @param validValues An optional array of integers that contains valid values for the property.
	 * No value check is performed if this is null. 
	 * 
	 * @return The integer value from the requested field or Integer.MIN_VALUE if the field
	 * or value was invalid. Note that type coercion can occur on the value.
	 */
	protected int assertJsonIntegerProperty(JSONObject data, HttpResponse response,
			String propertyName, Integer[] validValues) {
		
		Boolean isValid = false;
		String message = "";
		int result = Integer.MIN_VALUE;
		
		try {
			if(data != null && !data.isNull(propertyName)) {
				// Check if we should validate field values
				if (validValues != null) {
					// Make sure a supported value was given
					if(Arrays.asList(validValues).contains(data.getInt(propertyName))) {
						result = data.getInt(propertyName);
						isValid = true;
					} else {
						message = String.format(this.getContext().getResources().getString(
								R.string.invalid_value), data.getString(propertyName),
								propertyName);
					}
				} else {
					result = data.getInt(propertyName);
					isValid = true;
				}
			} else {
				message = String.format(this.getContext().getResources().getString(
						R.string.missing_field), propertyName);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			message = this.getContext().getResources().getString(R.string.malformed_data);
		}
		
		if(isValid) {
			response.setStatusCode(HttpURLConnection.HTTP_OK);
			setCacheHeaders(response);
		} else {
			respondBadRequest(response, message);
		}
		
		return result;
	}
	
	/**
	 * Checks if the given JSON object contains the specified property with an boolean value. 
	 * 
	 * A Bad Request will be populated in the response if the field is not valid or
	 * has malformed data. 
	 * 
	 * @param data The parent JSON object.
	 * @param response The response object to use.
	 * @param propertyName The top-level property to check.
	 * @param defaultValue The default value to return if the data is invalid. 
	 * 
	 * @return The boolean value from the requested field or the given default value if the field
	 * or value was invalid. Note that type coercion can occur on the value.
	 */
	protected Boolean assertJsonBooleanProperty(JSONObject data, HttpResponse response,
			String propertyName, Boolean defaultValue) {
		
		Boolean isValid = false;
		String message = "";
		Boolean result = defaultValue;
		
		try {
			if(data != null && !data.isNull(propertyName)) {
				result = data.getBoolean(propertyName);
				
				// Consider valid if no exception was raised
				isValid = true;
			} else {
				message = String.format(this.getContext().getResources().getString(
						R.string.missing_field), propertyName);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			message = this.getContext().getResources().getString(R.string.malformed_data);
		}
		
		if(isValid) {
			response.setStatusCode(HttpURLConnection.HTTP_OK);
			setCacheHeaders(response);
		} else {
			respondBadRequest(response, message);
		}
		
		return result;
	}
	
	/**
	 * Sets cache headers that prevent client-side caching of the resource.
	 * 
	 * @param response The response object to use.
	 */
	protected void setCacheHeaders(HttpResponse response) {

		response.setHeader(Shared.HEADER_CACHE_CONTROL,
				"private, max-age=0, must-revalidate, no-transform");

		response.setHeader(Shared.HEADER_EXPIRES, "Thu, 01 Dec 1994 16:00:00 GMT");

		// DEBUG: Do we really want to allow this?
		response.setHeader(Shared.HEADER_CORS, "*");
	}
	
	/**
	 * Sets cross-origin permissions for this call.
	 * 
	 * @param response The response object to use.
	 */
	protected void setCorsHeaders(HttpResponse response, String allowList) {

		// DEBUG: Do we really want to allow this?
		response.setHeader(Shared.HEADER_CORS, "*");

		// Set the list of allowed HTTP methods for this call
		response.setHeader(Shared.HEADER_CORS_METHODS, allowList);
		
		// Allow Content-Type headers
		response.setHeader(Shared.HEADER_CORS_HEADERS, 
				Shared.HEADER_CONTENT_TYPE + ", " + Shared.HEADER_METHOD_OVERRIDE);
	}
	
	/**
	 * Response a 200 OK to the client with the optional JSON object as the body.
	 * 
	 * @param response The response object to use.
	 * @param jsonData The optional JSON object to respond as the body, or null if no body is
	 * required.
	 */
	protected void respondOK(HttpResponse response, 
			@SuppressWarnings("rawtypes") Map jsonData) {
		
		// Check if body data was given
		if (jsonData != null) {
			JSONObject body = new JSONObject(jsonData);
			response.setEntity(new JsonResponse(body));
		}
		
		response.setStatusCode(HttpURLConnection.HTTP_OK);
		setCacheHeaders(response);
	}
	
	/**
	 * Sends a 400 to the client.
	 * 
	 * @param response The response object to use.
	 * @param message The user-readable error message.
	 */
	protected void respondBadRequest(HttpResponse response, String message) {
		
		try {
			JSONObject body = new JSONObject("{\"error\":"
												+ "{\"errors\": [ {"
														+ "\"domain\": \"global\","
														+ "\"reason\": \"badRequest\","
														+ "\"message\": \"" + message + "\""
												+ "} ] }"
											+ "}");
			response.setEntity(new JsonResponse(body));
			response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
			setCacheHeaders(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a 404 to the client.
	 * 
	 * @param response The response object to use.
	 * @param message The user-readable error message.
	 */
	protected void respondNotFound(HttpResponse response, String message) {
		
		try {
			JSONObject body = new JSONObject("{\"error\":"
												+ "{\"errors\": [ {"
														+ "\"domain\": \"global\","
														+ "\"reason\": \"notFound\","
														+ "\"message\": \"" + message + "\""
												+ "} ] }"
											+ "}");
			response.setEntity(new JsonResponse(body));
			response.setStatusCode(HttpURLConnection.HTTP_NOT_FOUND);
			setCacheHeaders(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a 404 to the client.
	 * 
	 * @param response The response object to use.
	 * @param message The user-readable error message.
	 */
	protected void respondNotImplemented(HttpResponse response, String message) {
		
		try {
			JSONObject body = new JSONObject("{\"error\":"
												+ "{\"errors\": [ {"
														+ "\"domain\": \"global\","
														+ "\"reason\": \"notImplemented\","
														+ "\"message\": \"" + message + "\""
												+ "} ] }"
											+ "}");
			response.setEntity(new JsonResponse(body));
			response.setStatusCode(HttpURLConnection.HTTP_NOT_IMPLEMENTED);
			setCacheHeaders(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a 405 to the client.
	 * 
	 * @param response The response object to use.
	 * @param allowList A comma separated list of allowed HTTP methods for this resource.
	 */
	protected void respondNotAllowed(HttpResponse response, String allowList) {
		
		try {
			JSONObject body = new JSONObject("{\"error\":"
												+ "{\"errors\": [ {"
												+ "\"domain\": \"global\","
												+ "\"reason\": \"notAllowed\","
												+ "\"message\": \"" 
												+ String.format(
														this.getContext().getResources().getString(
														R.string.allow_method), allowList)
												+ "\""
												+ "} ] }"
											+ "}");
			response.setEntity(new JsonResponse(body));
			response.setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
			setCacheHeaders(response);
			response.setHeader(Shared.HEADER_ALLOW, allowList);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a 503 to the client.
	 * 
	 * @param response The response object to use.
	 * @param message The user-readable error message.
	 */
	protected void respondServiceUnavailable(HttpResponse response, String message) {
		
		try {
			JSONObject body = new JSONObject("{\"error\":"
												+ "{\"errors\": [ {"
														+ "\"domain\": \"global\","
														+ "\"reason\": \"serviceUnavailable\","
														+ "\"message\": \"" + message + "\""
												+ "} ] }"
											+ "}");
			response.setEntity(new JsonResponse(body));
			response.setStatusCode(HttpURLConnection.HTTP_UNAVAILABLE);
			setCacheHeaders(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}

package com.blinnikka.android.qremote.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import com.blinnikka.android.qremote.R;
import com.blinnikka.android.qremote.Shared;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * Handles HTTP requests for static web content.
 */
public class StaticFileHandler extends HandlerBase {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".StaticFileHandler";

	// **************************************** //
	// Private Fields
	// **************************************** //
	private AssetManager assets;
	private MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
	
	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Instantiates a new instance of the StaticFileHandler object.
	 * 
	 * @param context The application context.
	 */
	public StaticFileHandler(Context context) {
		super(context);
		Log.d(TAG, ".ctor");

		this.assets = context.getResources().getAssets();
	}

	// **************************************** //
	// Public Methods
	// **************************************** //
	public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext) 
			throws HttpException, IOException {
		Log.d(TAG, "handle, uri=" + request.getRequestLine().getUri());

		// Make sure this wasn't a failed API call
		if(request.getRequestLine().getUri().startsWith(Shared.URL_API + "/")) {
			respondNotFound(response, getContext().getResources().getString(R.string.not_found));
			
			// NOTE: Function exit!
			return;
		}
		
		// Make sure we've got a supported method
		String method = assertMethod(request, response, new String[] { "OPTIONS", "GET" });
		
		// Check the method
		if ("GET".equals(method) || "OPTIONS".equals(method)) {
			sendFile(method, request.getRequestLine().getUri(), response);
		}
	}

	// **************************************** //
	// Private Methods
	// **************************************** //
	/**
	 * Attempts to send the file from the assets folder with the given path and filename.
	 * 
	 * @param fileName The file to send.
	 * @param response The response object to return data.
	 */
	private void sendFile(String method, String fileName, HttpResponse response) {
		
		InputStream stream = null;
			
		if(fileName.endsWith("/")) {
			fileName += Shared.HTML_HTDOCS_DEFAULT;
		}
		
		// Make sure the file exists
		try {
			stream = this.assets.open(Shared.HTML_HTDOCS_ROOT + fileName);
			response.setStatusCode(HttpURLConnection.HTTP_OK);
			
			if("OPTIONS".equals(method)) {
				// Set CORS permissions
				setCorsHeaders(response, "GET");
				
				try {
					stream.close();
					stream = null;
				} catch(Exception ex) {
					// NO-OP
				}
				
				// NOTE: Function exit!
				return;
			}
			
		} catch (Exception e) {
			Log.d(TAG, "Requested file not found or other error occurred.");
			//e.printStackTrace();
			
			// Return the 404 page
			try {
				Log.d(TAG, "Attempting to open default 404 page.");
				fileName = Shared.HTML_NOT_FOUND;
				stream = this.assets.open(fileName);
				response.setStatusCode(HttpURLConnection.HTTP_NOT_FOUND);
			} catch (Exception e1) {
				Log.d(TAG, "Unable to open 404 page.");
				//e1.printStackTrace();
			}
		}
		
		if (stream != null) {
			Log.d(TAG, "Have stream, getting content type, fileName=" + fileName);
			String contentType = getContentType(fileName);
			
			// Set CORS permissions
			setCorsHeaders(response, "GET");
			
			// Attempt to send the file to the client
			try {
				Log.d(TAG, "Send stream. Content-type=" + contentType);
				InputStreamEntity entity = new InputStreamEntity(stream, stream.available());
				entity.setContentType(contentType);
				response.setEntity(entity);
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			// Send static content as fallback
			try {
				StringEntity entity = new StringEntity(
						this.getContext().getResources().getString(R.string.not_found));
				entity.setContentType(Shared.CONTENT_TYPE_PLAINTEXT);
				response.setEntity(entity);
				response.setStatusCode(HttpURLConnection.HTTP_NOT_FOUND);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		setCacheHeaders(response);
	}
	
	/**
	 * Attempts to return a content type value for the given file.
	 * 
	 * @return A value suitable for the Content-Type header if a match is found based on the file
	 * extension, otherwise returns "application/octet-stream".
	 */
	private String getContentType(String fileName) {
		
		String result = Shared.CONTENT_TYPE_OCTET_STREAM;
		
		if (fileName != null && !"".equals(fileName) && fileName.contains(".")
				&& !fileName.endsWith(".")) {
			String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			
			// Try patching for a few extra types
			if(Shared.EXTENSION_WOFF.equals(extension)) {
				return Shared.CONTENT_TYPE_WOFF;
			} else if(Shared.EXTENSION_JAVASCRIPT.equals(extension)) {
				return Shared.CONTENT_TYPE_JAVASCRIPT;
			}
			
			Log.d(TAG, "extension=" + extension);
			String contentType = this.mimeTypeMap.getMimeTypeFromExtension(extension);
			
			if (contentType != null) {
				result = contentType;
			}
		}
		
		return result;
	}
}

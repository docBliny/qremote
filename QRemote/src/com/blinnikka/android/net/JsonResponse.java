package com.blinnikka.android.net;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import com.blinnikka.android.qremote.Shared;

/**
 * A basic JSON response that can be sent as an HTTP response body.
 */
public class JsonResponse extends EntityTemplate {

	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Instantiates an instance of the JsonResponse object with the given data.
	 * 
	 * @param data The JSON data to place in the response body.
	 */
	public JsonResponse(final JSONObject data) {
		super(new ContentProducer() {
			public void writeTo(final OutputStream outstream) throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
				writer.write(data.toString());
				writer.flush();
			}
		});
		
		this.contentType = new BasicHeader(Shared.HEADER_CONTENT_TYPE, Shared.CONTENT_TYPE_JSON);
	}

}

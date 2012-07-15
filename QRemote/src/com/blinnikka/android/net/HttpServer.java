package com.blinnikka.android.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import com.blinnikka.android.qremote.Shared;

import android.content.Context;
import android.util.Log;

public class HttpServer extends Thread {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".HttpServer";

	// **************************************** //
	// Private Fields
	// **************************************** //
	private boolean isActive = false;
	private Context context = null;
	private int serverPort = 8080;
	private BasicHttpProcessor httpproc = null;
	private BasicHttpContext httpContext = null;
	private HttpService httpService = null;
	private HttpRequestHandlerRegistry registry = null;

	private ServerSocket serverSocket;

	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Initializes a new instance of the HttpServer object.
	 * 
	 * @param serverPort The port to listen on.
	 */
	public HttpServer(Context context, int serverPort) {
		Log.d(TAG, ".ctor");
		
		this.context = context;
		this.serverPort = serverPort;
		
		httpproc = new BasicHttpProcessor();
		httpContext = new BasicHttpContext();

		httpproc.addInterceptor(new ResponseDate());
		httpproc.addInterceptor(new ResponseServer());
		httpproc.addInterceptor(new ResponseContent());
		httpproc.addInterceptor(new ResponseConnControl());

		httpService = new HttpService(httpproc,
		    new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());

		registry = new HttpRequestHandlerRegistry();
		httpService.setHandlerResolver(registry);
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
	
	/**
	 * Gets the request handler registry, so that consumers can add handlers for requests.
	 * 
	 * @return The HttpRequestHandlerRegistry.
	 */
	public HttpRequestHandlerRegistry getRequestHandlerRegistry() {
		return this.registry;
	}
	
	// **************************************** //
	// Public Methods
	// **************************************** //
	/**
	 * Start the HTTP server and begin handling requests on a separate thread.
	 * 
	 * NOTE: Do not call this method directly. Use the startThread method instead.
	 */
	@Override
	public void run() {
		super.run();
		
		try {
			serverSocket = new ServerSocket(serverPort);

			serverSocket.setReuseAddress(true);

			while (isActive) {
				try {
					final Socket socket = serverSocket.accept();

					DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();

					serverConnection.bind(socket, new BasicHttpParams());

					httpService.handleRequest(serverConnection, httpContext);

					serverConnection.shutdown();
				} catch (IOException e) {
					Log.d(TAG, "Error handling requests.");
					e.printStackTrace();
				} catch (HttpException e) {
					Log.d(TAG, "Error handling requests.");
					e.printStackTrace();
				}
			}

			serverSocket.close();
			serverSocket = null;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		isActive = false;
	}

	/**
	 * Starts the HTTP server.
	 */
	public synchronized void startServer() {
		isActive = true;
		super.start();
	}

	/**
	 * Stops the HTTP server.
	 */
	public synchronized void stopServer() {
		isActive = false;
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
package com.blinnikka.android.qremote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

public class NetworkListener implements Runnable {

	// **************************************** //
	// Constants
	// **************************************** //
	private static final String TAG = Shared.APP_NAME + ".NetworkListener";

	// **************************************** //
	// Private Fields
	// **************************************** //
	/** The IP address of this device. */
	private String serverIP = null;
	
	/** The port to listen on. */
	private int serverPort = 8080;

	/** The Handler to communicate with the main thread. */
	private Handler handler = new Handler();

	/** The socket used to listen to network activity. */
	private ServerSocket serverSocket;

	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Initializes a new instance of the NetworkListener object.
	 * 
	 * @param serverIP The IP address to listen on.
	 * @param serverPort The TCP port to listen on.
	 */
	public NetworkListener(String serverIP, int serverPort) {
		
		this.serverIP = serverIP;
		this.serverPort = serverPort;
	}
	
	// **************************************** //
	// Public Methods
	// **************************************** //
	public void run() {
		try {
			if (serverIP != null) {
				
				// Send startup message
				handler.post(new Runnable() {
					public void run() {
						Log.d(TAG, "Listening on IP: " + serverIP);
					}
				});

				// Create a new socket and start listening
				serverSocket = new ServerSocket(serverPort);
				while (true) {
					// Listen for incoming connections
					Socket client = serverSocket.accept();
					
					// Send connection message
					handler.post(new Runnable() {
						public void run() {
							Log.d(TAG, "Connected.");
						}
					});

					try {
						// Attempt to read data from the socket
						String line = null;
						BufferedReader in = new BufferedReader(
								new InputStreamReader(client.getInputStream()));

						while ((line = in.readLine()) != null) {
                            Log.d(TAG, line);
                            
                            if ("".equals(line)) { break; }
                        }
						
//						// Read a line
//						line = in.readLine();
//						Log.d(TAG, "Received '" + line + "'");
//						
						client.close();
						client = null;
						
					} catch (Exception e) {
						final String errorMessage = e.toString();
						
						// TODO: Handle error
						handler.post(new Runnable() {
							public void run() {
								Log.d(TAG, "Lost connection: " + errorMessage);
							}
						});
						e.printStackTrace();
					} finally {
						if(client != null) {
							Log.d(TAG, "Closing client in finally.");
							client.close();
						}
					}
				}
			} else {
				// TODO: No network connection available
				handler.post(new Runnable() {
					public void run() {
						Log.d(TAG, "No internet connection available.");
					}
				});
			}
		} catch (Exception e) {
			final String errorMessage = e.toString();
			handler.post(new Runnable() {
				public void run() {
					Log.d(TAG, errorMessage.toString());
				}
			});
			e.printStackTrace();
		}
	}

}

package com.blinnikka.android.qremote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.tungsten.ledcommon.ILedService;
import com.google.tungsten.ledcommon.LedAnimation;

public class LedClient {

	// **************************************** //
	// Constants
	// **************************************** //
	/** Class name for debugging purposes. */
	private static final String TAG = Shared.APP_NAME + ".LedClient";

	// **************************************** //
	// Private Fields
	// **************************************** //
    private final Binder mBinder;
    private final Callbacks mCallbacks;
    private final Context mContext;
    private ILedService mLedService;
    private final String mName;
    private ServiceConnection mServiceConnection;

	private boolean isConnected = false;
    
	// **************************************** //
	// Public Interfaces
	// **************************************** //
    public static interface Callbacks
    {
        public abstract void ledClientConnected(LedClient ledclient);

        public abstract void ledClientDisconnected(LedClient ledclient);
    }

	// **************************************** //
	// Constructors
	// **************************************** //
	/**
	 * Instantiates a new instance of the LedClient object.
	 * 
	 * @param context The application context.
	 * @param callbacks The service callbacks.
	 */
    public LedClient(Context context, String name, Callbacks callbacks)
    {
		Log.d(TAG, ".ctor");

		this.mBinder = new Binder();
		this.mContext = context;
        this.mName = name;
        this.mCallbacks = callbacks;
        
        mServiceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName componentname, IBinder iBinder)
            {
            	Log.d(TAG, "onServiceConnected");
                mLedService = ILedService.Stub.asInterface(iBinder);
                try {
                    if(mCallbacks != null) {
                        mCallbacks.ledClientConnected(LedClient.this);
                    }
                } catch (Exception e) {
            		Log.d(TAG, "Error connecting to service: " + e.toString());
            		e.printStackTrace();
                	// NO-OP
                }
            }

            public void onServiceDisconnected(ComponentName componentname)
            {
            	Log.d(TAG, "onServiceDisconnected");
                
            	mLedService = null;
                
                try {
                    if(mCallbacks != null) {
                        mCallbacks.ledClientDisconnected(LedClient.this);
                    }
                } catch (Exception e) {
            		Log.d(TAG, "Error disconnecting from service: " + e.toString());
            		e.printStackTrace();
                	// NO-OP
                }
            }
        };
    }

	// **************************************** //
	// Public Properties
	// **************************************** //
    /**
     * Gets the number of LEDs available.
     * 
     * @return The number of LEDs available.
     */
    public int getLedCount()
    {
        int result = -1;

        // Verify service is connected
        assertService();
        
        try {
            result = mLedService.getLedCount();
        } catch (RemoteException e) {
            Log.e(TAG, "getLedCount failed");
        }
        
        return result;
    }
    
	/**
	 * Gets the global brightness value set in the preferences.
	 *
	 * @return A value between 0 and 100.
	 */
    public int getGlobalBrightness()
    {
        int result = -1;

        // Verify service is connected
        assertService();
        
        try {
            result = mLedService.getGlobalBrightness();
        } catch (RemoteException e) {
            Log.e(TAG, "getGlobalBrightness failed");
        }
        
        return result;
    }
    
	/**
	 * Sets the global brightness value.
	 *
	 * @param brightness A value between 0 and 100.
	 */
    public void setGlobalBrightness(int brightness)
    {
        // Verify service is connected
        assertService();
        
        try {
            mLedService.setGlobalBrightness(brightness);
        } catch (RemoteException e) {
            Log.e(TAG, "setGlobalBrightness failed");
        }
    }
    
	// **************************************** //
	// Public Methods
	// **************************************** //
    /**
     * Connects to the LED service if not already connected.
     */
    public void connect()
    {
    	Log.d(TAG, "connect");
    	
    	if(!this.isConnected ) {
            mContext.bindService(new Intent("com.google.tungsten.LedService"), mServiceConnection, 
            		Context.BIND_AUTO_CREATE);
    	}
    }

    /**
     * Disconnects from the LED service if connected and unbinds the service.
     */
    public void disconnect()
    {
    	Log.d(TAG, "disconnect");

    	if(this.isConnected) {
    		// Disable the client connection if have service
    		if(mLedService != null) {
    			this.disable();
    		}
    		
    		// Unbind service
            mContext.unbindService(mServiceConnection);
            
            this.isConnected = false;
    	}
    }

	/**
	 * Enables the client with the given priority.
	 *
	 * @param priority The client application's priority. Higher numbers represent higher priority.
	 */
    public void enable(int priority)
    {
        // Verify service is connected
        assertService();
        
        try {
        	mLedService.enable(mBinder, mName, priority);
        } catch (RemoteException e) {
            Log.e(TAG, "enable failed");
        }
    }
    
    /**
     * Stops the LED client.
     */
    public void disable()
    {
        // Verify service is connected
        assertService();
        
        try {
        	mLedService.disable(mBinder);
        } catch (RemoteException e) {
            Log.e(TAG, "disable failed");
        }
    }
    
    /**
     * Cancels a previously started animation.
     */
    public void cancelAnimation()
    {
        // Verify service is connected
        assertService();
        
        try {
        	mLedService.cancelAnimation(mBinder);
        } catch (RemoteException e) {
            Log.e(TAG, "cancelAnimation failed");
        }
    }
    
    /**
     * Sets all LEDs to the specified color values.
     * 
     * @param red The red amount. A value between 0 and 255.
     * @param green The green amount. A value between 0 and 255.
     * @param blue The blue amount. A value between 0 and 255.
     */
    public void setAllLeds(int red, int green, int blue)
    {
        // Verify service is connected
        assertService();
        
        try {
        	mLedService.setAllLeds(mBinder, red, green, blue);
        } catch (RemoteException e) {
            Log.e(TAG, "setAllLeds failed");
        }
    }
    
    /**
     * Sets a range of LEDs to the given values.
     * 
     * @param iBinder The binder.
     * @param start The start LED ID.
     * @param count The number of LEDs to modify.
     * @param rgbValues An array of RGB values for each LED in the range.
     */
    public void setLedRange(int start, int count, int[] rgbValues)
    {
        // Verify service is connected
        assertService();
        
        try {
        	mLedService.setLedRange(mBinder, start, count, rgbValues);
        } catch (RemoteException e) {
            Log.e(TAG, "setLedRange failed");
        }
    }
    
	/**
	 * Starts a custom animation.
	 *
	 * @param ledAnimation The animation to start.
	 * @param repeat true if the animation should be repeated, otherwise false.
	 */
    public void setAnimation(LedAnimation ledAnimation, boolean repeat)
    {
        // Verify service is connected
        assertService();
        
        try {
        	mLedService.setAnimation(mBinder, ledAnimation, repeat);
        } catch (RemoteException e) {
            Log.e(TAG, "setAnimation failed");
        }
    }
    
	/**
	 * Starts a built-in animation.
	 *
	 * @param animationId The ID of the the built-in animation to start.
	 */
    public void setBuiltInAnimation(int animationIndex)
    {
        // Verify service is connected
        assertService();
        
        try {
        	mLedService.setBuiltinAnimation(mBinder, animationIndex);
        } catch (RemoteException e) {
            Log.e(TAG, "setBuiltInAnimation failed");
        }
    }
    
	/**
	 * Sets the given color values to the specified LED.
	 *
	 * @param ledId The ID of the LED to set. IDs start from 0. 1000 is the status (mute) LED, 
	 * 1001 is all LEDs.
     * @param red The red amount. A value between 0 and 255.
     * @param green The green amount. A value between 0 and 255.
     * @param blue The blue amount. A value between 0 and 255.
	 */
    public void setLed(int ledId, int red, int green, int blue)
    {
        // Verify service is connected
        assertService();
        
        try {
        	mLedService.setLed(mBinder, ledId, red, green, blue);
        } catch (RemoteException e) {
            Log.e(TAG, "setBuiltInAnimation failed");
        }
    }
    
	// **************************************** //
	// Private Methods
	// **************************************** //
    /**
     * Throws an IllegalStateException exception if the service is not connected.
     */
    private void assertService() {
    	
        if(mLedService == null) {
            throw new IllegalStateException("LedClient not connected");
        }
    }
}

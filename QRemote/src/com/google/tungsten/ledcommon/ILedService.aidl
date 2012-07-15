package com.google.tungsten.ledcommon;

import android.os.IBinder;
import com.google.tungsten.ledcommon.LedAnimation;

interface ILedService {

	/**
	 * Gets the number of LEDs available.
	 *
	 * @return The number of LEDs available.
	 */
    int getLedCount();

	/**
	 * Enables the client with the given priority.
	 *
	 * @param iBinder The binder used to track the client.
	 * @param name The client application name.
	 * @param priority The client application's priority. Higher numbers represent higher priority.
	 */
    void enable(IBinder iBinder, String name, int priority);

    /**
     * Stops the LED client.
	 *
	 * @param iBinder The binder used to track the client.
     */
    void disable(IBinder iBinder);

	/**
	 * Sets the specified LED to the given values.
	 *
	 * @param iBinder The binder used to track the client.
	 * @param ledId The ID of the LED to set. IDs start from 0. 1000 is the status (mute) LED, 
	 * 1001 is all LEDs.
     * @param red The red amount. A value between 0 and 255.
     * @param green The green amount. A value between 0 and 255.
     * @param blue The blue amount. A value between 0 and 255.
	 */
    void setLed(IBinder iBinder, int ledId, int red, int green, int blue);

    /**
     * Sets all LEDs to the specified color values.
     * 
	 * @param iBinder The binder used to track the client.
     * @param red The red amount. A value between 0 and 255.
     * @param green The green amount. A value between 0 and 255.
     * @param blue The blue amount. A value between 0 and 255.
     */
    void setAllLeds(IBinder iBinder, int red, int green, int blue);

    /**
     * Sets a range of LEDs to the given values.
     * 
	 * @param iBinder The binder used to track the client.
     * @param start The start LED ID.
     * @param count The number of LEDs to modify.
     * @param rgbValues An array of RGB values for each LED in the range. The array should
     * contain at a minimum three times the number of integers than count. Example:
     * [255, 0, 0, 255, 0, 0] contains values for two LEDs.
     */
    void setLedRange(IBinder iBinder, int start, int count, in int[] rgbValues);

	/**
	 * Starts a custom animation.
	 *
	 * @param iBinder The binder used to track the client.
	 * @param ledAnimation The animation to start.
	 * @param repeat true if the animation should be repeated, otherwise false.
	 */
    void setAnimation(IBinder iBinder, in LedAnimation ledAnimation, boolean repeat);

	/**
	 * Starts a built-in animation.
	 *
	 * @param iBinder The binder used to track the client.
	 * @param animationId The ID of the the built-in animation to start.
	 */
    void setBuiltinAnimation(IBinder iBinder, int animationId);

    /**
     * Cancels a previously started animation.
     */
    void cancelAnimation(IBinder iBinder);

	/**
	 * Sets the client priority.
	 *
	 * @param iBinder The binder used to track the client.
	 * @param priority The client priority.
	 */
    void setPriority(IBinder iBinder, int priority);
    
	/**
	 * Sets the global brightness value set in the preferences.
	 *
	 * @param brightness A value between 0 and 100.
	 */
    void setGlobalBrightness(int brightness);

	/**
	 * Gets the global brightness value set in the preferences.
	 *
	 * @return A value between 0 and 100.
	 */
    int getGlobalBrightness();

}

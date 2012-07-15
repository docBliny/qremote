package com.blinnikka.android.utility;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Android utility methods.
 */
public final class Utility {

	// **************************************** //
	// Public Methods
	// **************************************** //
	/**
	 * Dumps the intent to the log as a debug item.
	 * 
	 * @param tag The Log tag to use.
	 * @param intent The intent to log.
	 */
	public static void dumpIntent(String tag, Intent intent) {
		
		if(intent != null) {
			int index = 0;
			StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("Intent={Action='%s'", intent.getAction()));
			
			if(intent.getCategories() != null) {
				sb.append(String.format(", Categories=[%s]", intent.getCategories().toString()));
			}
			
			Bundle extras = intent.getExtras();
			
			if (extras != null) {
				sb.append(", Extras=[");
				
				for (String key : extras.keySet()) {
					Object item = extras.get(key);
					
					if(index > 0) { sb.append(","); }
					
					if (item == null) {
						sb.append(String.format("{%s='%s', type='null'}", key, item));
					} else {
						sb.append(String.format("{%s='%s', type='%s'}", key, item, 
								item.getClass().getCanonicalName()));
					}
					index++;
				}
				sb.append("]");
			}
			
			sb.append(String.format("}%n"));
			Log.d(tag, sb.toString());
		} else {
			Log.d(tag, "Intent is null");
		}
	}
}

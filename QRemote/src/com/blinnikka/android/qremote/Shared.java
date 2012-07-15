package com.blinnikka.android.qremote;

/**
 * Shared application constants.
 */
public final class Shared {

	// **************************************** //
	// Debugging Constants
	// **************************************** //
	/** Application name used in debugging logs. */
	public static final String APP_NAME = "QRemote";

	// **************************************** //
	// Common
	// **************************************** //
	/** String used for unknown values. */
	public static final String STRING_UNKNOWN = "unknown";
	
	/** Integer used for unknown values. */
	public static final int INTEGER_UNKNOWN = -1;
	
	// **************************************** //
	// API URIs
	// **************************************** //
	/** Base URI for player transport controls. */
	public static final String URL_API = "/api";
	
	/** Base URI for player transport controls. */
	public static final String URL_API_VERSION = "/V1/";
	
	/** Base URI for all API calls. */
	public static final String URL_API_PREFIX = URL_API + URL_API_VERSION;
	
	/** Base URI for player transport controls. */
	public static final String URL_MEDIA_CONTROLS = URL_API_PREFIX + "media/controls";
	
	/** Base URI for player settings. */
	public static final String URL_MEDIA_SETTINGS = URL_API_PREFIX + "media/settings/";
	
	/** Base URI for player playlist manipulation. */
	public static final String URL_MEDIA_PLAYLIST = URL_API_PREFIX + "media/playlist/current/items/";
	
	/** Base URI for LED manipulation. */
	public static final String URL_LED = URL_API_PREFIX + "led/";
	
	/** Base URI for text to speech. */
	public static final String URL_SPEAK = URL_API_PREFIX + "speak";
	
	/** Base URI for static web content. */
	public static final String URL_HTDOCS = "*";
	
	// **************************************** //
	// HTTP Header Names
	// **************************************** //
	/** Allow header name. */
	public static final String HEADER_ALLOW = "Allow";

	/** Cache-Control header name. */
	public static final String HEADER_CACHE_CONTROL = "Cache-Control";
	
	/** Content-Type header name. */
	public static final String HEADER_CONTENT_TYPE = "Content-Type";

	/** Expires header name. */
	public static final String HEADER_EXPIRES = "Expires";
	
	/** X-HTTP-Method-Override header name. */
	public static final String HEADER_METHOD_OVERRIDE = "X-HTTP-Method-Override";

	/** The CORS header. */
	public static final String HEADER_CORS = "Access-Control-Allow-Origin";

	/** The HTTP methods allowed for the cross-origin call. */
	public static final String HEADER_CORS_METHODS = "Access-Control-Allow-Methods";
	
	/** The headers allowed for the cross-origin call. */
	public static final String HEADER_CORS_HEADERS = "Access-Control-Allow-Headers";
	
	// **************************************** //
	// HTTP Header Values
	// **************************************** //
	/** The Content-Type value for JavaScript requests/responses. */
	public static final String CONTENT_TYPE_JAVASCRIPT = "text/javascript";
	
	/** The file extension value for JavaScript requests/responses. */
	public static final String EXTENSION_JAVASCRIPT = "js";
	
	/** The Content-Type value for JSON requests/responses. */
	public static final String CONTENT_TYPE_JSON = "application/json";
	
	/** The fallback content type for unknown file types. */
	public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
	
	/** The Content-Type value for JSON requests/responses. */
	public static final String CONTENT_TYPE_PLAINTEXT = "text/plain;charset=utf-8";
	
	/** The Content-Type value for Web Open Font Format requests/responses. */
	public static final String CONTENT_TYPE_WOFF = "application/x-font-woff";
	
	/** The file extension value for Web Open Font Format requests/responses. */
	public static final String EXTENSION_WOFF = "woff";
	
	// **************************************** //
	// Intents: Controls (Playback State)
	// **************************************** //
	/** The generic music command intent. */
	public static final String ACTION_MUSIC_COMMAND = "com.android.music.musicservicecommand";

	/** The generic music command intent extra key name. */
	public static final String EXTRA_MUSIC_COMMAND = "command";

	/** The generic music command intent value for pause. */
	public static final String EXTRA_MUSIC_COMMAND_PAUSE = "pause";

	/** The generic music command intent value for play. */
	public static final String EXTRA_MUSIC_COMMAND_PLAY = "play";

	/** The generic music command intent value for stop. */
	public static final String EXTRA_MUSIC_COMMAND_STOP = "stop";

	/** The music toggle play/pause command intent. */
	public static final String ACTION_MUSIC_COMMAND_TOGGLE_PAUSE = "com.android.music.musicservicecommand.togglepause";

	/** The music previous playlist item command intent. */
	public static final String ACTION_MUSIC_COMMAND_PREVIOUS = "com.android.music.musicservicecommand.previous";

	/** The music next playlist item command intent. */
	public static final String ACTION_MUSIC_COMMAND_NEXT = "com.android.music.musicservicecommand.next";

	/** The play status request intent. */
	public static final String ACTION_PLAY_STATUS_REQUEST = "com.android.music.playstatusrequest";

	/** The play status response intent. */
	public static final String ACTION_PLAY_STATUS_RESPONSE = "com.android.music.playstatusresponse";

	/** The play state change intent. */
	public static final String ACTION_PLAY_STATE_CHANGED = "com.android.music.playstatechanged";

	/** The playback failed intent. */
	public static final String ACTION_PLAYBACK_FAILED = "com.android.music.playbackfailed";

	/** The queue change intent. */
	public static final String ACTION_QUEUE_CHANGED = "com.android.music.queuechanged";

	// **************************************** //
	// Intents: Settings
	// **************************************** //
	/** The repeat mode change intent. */
	public static final String ACTION_REPEAT_CHANGED = "com.google.android.music.repeatmodechanged";

	/** The shuffle mode change intent. */
	public static final String ACTION_SHUFFLE_CHANGED = "com.google.android.music.shufflemodechanged";

	/** The master volume change intent. */
	public static final String ACTION_MASTER_VOLUME_CHANGED = "android.media.MASTER_VOLUME_CHANGED_ACTION";

	/** The master volume value extra key name. */
	public static final String EXTRA_MASTER_VOLUME_VALUE = "android.media.EXTRA_MASTER_VOLUME_VALUE";

	/** The master mute change intent. */
	public static final String ACTION_MASTER_MUTE_CHANGED = "android.media.MASTER_MUTE_CHANGED_ACTION";

	/** The master mute value extra key name. */
	public static final String EXTRA_MASTER_MUTE_VALUE = "android.media.EXTRA_MASTER_VOLUME_MUTED";

	// **************************************** //
	// Control Resource Key Names & Values
	// **************************************** //
	/** The property name for the current playback status. */ 
	public static final String CONTROL_STATUS = "status";

	/** The control property value when the device is buffering. */ 
	public static final String CONTROL_STATUS_BUFFERING = "buffering";
	
	/** The control property value when the device playback failed. */ 
	public static final String CONTROL_STATUS_ERROR = "error";
	
	/** The control property value when the device is paused. */ 
	public static final String CONTROL_STATUS_PAUSE = "pause";
	
	/** The control property value when the device is playing. */ 
	public static final String CONTROL_STATUS_PLAY = "play";
	
	/** The control property value when the device playback is stopped. */ 
	public static final String CONTROL_STATUS_STOP = "stop";

	/** The control property value when the device play/pause mode is toggled. */ 
	public static final String CONTROL_STATUS_TOGGLE_PAUSE = "togglePause";

	// **************************************** //
	// Settings Resource Key Names & Values
	// **************************************** //
	/** The volume resource name. */
	public static final String MEDIA_SETTINGS_VOLUME = "volume";
	
	/** The volume resource value property name. */
	public static final String MEDIA_SETTINGS_VOLUME_VALUE = "value";

	/** The volume resource unknown value. */
	public static final int MEDIA_SETTINGS_VOLUME_VALUE_UNKNOWN = INTEGER_UNKNOWN;

	/** The mute resource name. */
	public static final String MEDIA_SETTINGS_MUTE = "mute";
	
	/** The mute resource value property name. */
	public static final String MEDIA_SETTINGS_MUTE_VALUE = "value";

	// Not supported
	///** The mute resource value when mute is partially on. */
	//public static final String MEDIA_SETTINGS_MUTE_VALUE_ATTENUATE = "attenuate";

	/** The mute resource value when mute is fully on. */
	public static final String MEDIA_SETTINGS_MUTE_VALUE_ON = "on";

	/** The mute resource value when mute is off. */
	public static final String MEDIA_SETTINGS_MUTE_VALUE_OFF = "off";

	/** The mute resource value when mute is unknown. */
	public static final String MEDIA_SETTINGS_MUTE_VALUE_UNKNOWN = STRING_UNKNOWN;

	/** The repeat resource name. */
	public static final String MEDIA_SETTINGS_REPEAT = "repeat";
	
	/** The repeat resource value property name. */
	public static final String MEDIA_SETTINGS_REPEAT_VALUE = "value";

	/** The repeat resource value when repeat all is active. */
	public static final String MEDIA_SETTINGS_REPEAT_VALUE_ALL = "all";

	/** The repeat resource value when repeat one is on. */
	public static final String MEDIA_SETTINGS_REPEAT_VALUE_SINGLE = "single";

	/** The repeat resource value when repeat is off. */
	public static final String MEDIA_SETTINGS_REPEAT_VALUE_OFF = "off";

	/** The repeat resource value when repeat state is unknown. */
	public static final String MEDIA_SETTINGS_REPEAT_VALUE_UNKNOWN = STRING_UNKNOWN;

	/** The shuffle resource name. */
	public static final String MEDIA_SETTINGS_SHUFFLE = "shuffle";
	
	/** The shuffle resource value property name. */
	public static final String MEDIA_SETTINGS_SHUFFLE_VALUE = "value";

	/** The shuffle resource value when shuffle all is active. */
	public static final String MEDIA_SETTINGS_SHUFFLE_VALUE_ALL = "all";

// Not supported
//	/** The shuffle resource value when album shuffle on. */
//	public static final String MEDIA_SETTINGS_SHUFFLE_VALUE_ALBUM = "album";

// Not supported
//	/** The shuffle resource value when artist shuffle on. */
//	public static final String MEDIA_SETTINGS_SHUFFLE_VALUE_ARTIST = "artist";

// Not supported
//	/** The shuffle resource value when playlist shuffle on. */
//	public static final String MEDIA_SETTINGS_SHUFFLE_VALUE_PLAYLIST = "playlist";

	/** The shuffle resource value when shuffle is off. */
	public static final String MEDIA_SETTINGS_SHUFFLE_VALUE_OFF = "off";

	/** The mute resource value when repeat state is unknown. */
	public static final String MEDIA_SETTINGS_SHUFFLE_VALUE_UNKNOWN = STRING_UNKNOWN;

	// **************************************** //
	// Playlist Resource Key Names & Values
	// **************************************** //
	/** The playlist resource name. */
	public static final String MEDIA_PLAYLISTITEM_CURRENT = "current";
	
	/** The playlist resource URI property name. */
	public static final String MEDIA_PLAYLISTITEM_URI = "uri";

	/** The playlist resource artist property name. */
	public static final String MEDIA_PLAYLISTITEM_ARTISTNAME = "artistName";

	/** The playlist resource album property name. */
	public static final String MEDIA_PLAYLISTITEM_ALBUMNAME = "albumName";

	/** The playlist resource title property name. */
	public static final String MEDIA_PLAYLISTITEM_TITLENAME = "titleName";

	/** The playlist item URN prefix for Google Play items. */
	public static final String MEDIA_PLAYLISTITEM_URN_GOOGLE_PLAY_PREFIX = "urn:google:play:";
	
	/** The playlist item URN for virtual previous item. */
	public static final String MEDIA_PLAYLISTITEM_URN_PREVIOUS = "urn:media:item:previous";

	/** The playlist item URN for virtual next item. */
	public static final String MEDIA_PLAYLISTITEM_URN_NEXT = "urn:media:item:next";

	// **************************************** //
	// LED Resource Key Names & Values
	// **************************************** //
	/** The status LED ID. */
	public static final int LED_STATUS_ID = 1000;
	
	/** The LED animation resource name. */
	public static final String LED_ANIMATION = "animation";
	
	/** The LEDS collection resource name. */
	public static final String LED_LEDS = "leds";
	
	/** The LEDS collection all resource name. */
	public static final String LED_LEDS_ALL = "leds/all";
	
	/** The status/mute LED resource name. */
	public static final String LED_LEDS_STATUS = "leds/status";
	
	/** The animation resource type property name. */
	public static final String LED_ANIMATION_TYPE = "type";
	
	/** The animation type for a built-in animation. */
	public static final String LED_ANIMATION_TYPE_NONE = "none";

	/** The animation type for a built-in animation. */
	public static final String LED_ANIMATION_TYPE_BUILTIN = "builtIn";

	/** The animation type for a custom animation. */
	public static final String LED_ANIMATION_TYPE_CUSTOM = "custom";

	/** The animation type for an unknown animation. */
	public static final String LED_ANIMATION_TYPE_UNKNOWN = STRING_UNKNOWN;

	/** The animation resource ID property name. */
	public static final String LED_ANIMATION_ID = "id";
	
	/** The animation resource repeat property name. */
	public static final String LED_ANIMATION_REPEAT = "repeat";
	
	/** The animation resource fields property name. */
	public static final String LED_ANIMATION_FIELDS = "fields";
	
	/** The animation frame resource ledId property name. */
	public static final String ANIMATION_FRAME_LEDID = "ledId";
	
	/** The animation frame resource red property name. */
	public static final String ANIMATION_FRAME_RED = "red";

	/** The animation frame resource green property name. */
	public static final String ANIMATION_FRAME_GREEN = "green";

	/** The animation frame resource blue property name. */
	public static final String ANIMATION_FRAME_BLUE = "blue";

	/** The animation frame resource start property name. */
	public static final String ANIMATION_FRAME_START = "start";

	/** The LEDs start property name. */
	public static final String LED_LEDS_START = "start";
	
	/** The LEDs count property name. */
	public static final String LED_LEDS_COUNT = "count";
	
	// **************************************** //
	// Text-to-Speech Resource Key Names & Values
	// **************************************** //
	/** The sentence resource name. */
	public static final String SPEAK_SENTENCE = "sentence";
	
	/** The text property name. */
	public static final String SPEAK_SENTENCE_TEXT = "text";
	
	/** The pitch property name. */
	public static final String SPEAK_SENTENCE_PITCH = "pitch";
	
	/** The rate property name. */
	public static final String SPEAK_SENTENCE_RATE = "rate";
	
	/** The queueMode property name. */
	public static final String SPEAK_SENTENCE_QUEUEMODE = "queueMode";
	
	/** The pan property name. */
	public static final String SPEAK_SENTENCE_PAN = "pan";
	
	/** The volume property name. */
	public static final String SPEAK_SENTENCE_VOLUME = "volume";

	// **************************************** //
	// Static HTTP Server
	// **************************************** //
	/** The path to static local assets. */
	public static final String HTML_HTDOCS_ROOT = "htdocs";
	
	/** The name of the default document. */
	public static final String HTML_HTDOCS_DEFAULT = "index.html";
	
	/** The default 404 Not Found file to serve for static content (not /api). */
	public static final String HTML_NOT_FOUND = HTML_HTDOCS_ROOT + "/404.html";
	
}

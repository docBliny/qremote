var QRemote = {
	/**
	 * Calls the given API with the specified options.
	 *
	 * @param method The HTTP method for the request.
	 * @param url The URL to request.
	 * @param data The JSON data as a string to send as the body, or null.
	 * @param successCallback The method to call after a successful call.
	 */
	callApi: function(method, url, data, successCallback) {
		$.ajax({
			type: method,
			url: url,
			contentType: "application/json",
			data: data,
			dataType: "json",
			success: successCallback || function(data) { },
			error: QRemote.apiError
		});
	},

	/**
	 * Handles API call errors.
	 *
	 * @param jqXhr jQuery XHR object.
	 * @param textStatus One of "timeout", "error", "abort", or "parsererror".
	 * @param errorThrown A string containing the HTTP status text.
	 */
	apiError: function(jqXhr, textStatus, errorThrown) {
		console.error("Error calling API: Reason=" + textStatus + ", status=" + errorThrown);

		$("#messageContainer").html('<div class="alert alert-block alert-error fade in">'
			+ '<a class="close" data-dismiss="alert" href="javascript:;">&times;</a>'
			+ 'There was an error calling the API.'
			+ '</div>');
	},

	/**
	 * Updates the status of all controls.
	 */
	updateAll: function() {
		QRemote.getMediaInfo();
		QRemote.getControlStatus();
		QRemote.getVolume();
		QRemote.getMute();
		QRemote.getRepeat();
		QRemote.getShuffle();
	},

	/**
	 * Performs an update if the "Live update" is turned on and kicks off a timer to poll again.
	 */
	pollUpdate: function() {

		if($("#liveUpdate").attr("checked")) {
			QRemote.updateAll();
		}

		// Restart the timer
		setTimeout(QRemote.pollUpdate, QRemote.Config.UPDATE_INTERVAL);
	},

	/**
	 * Gets the volume level.
	 */
	getMediaInfo: function() {
		QRemote.callApi(
			"GET",
			QRemote.Shared.URL_MEDIA_PLAYLIST_CURRENT_ITEM,
			null,
			function(data) {
				$("#titleName").html(data.titleName || "&nbsp;");
				$("#artistName").html(data.artistName || "&nbsp;");
				$("#albumName").html(data.albumName || "&nbsp;");
			}
		);
	},

	/**
	 * Gets the volume level.
	 */
	getControlStatus: function() {
		QRemote.callApi(
			"GET",
			QRemote.Shared.URL_MEDIA_CONTROLS,
			null,
			function(data) {
				$("#play").removeClass("active");
				$("#pause").removeClass("active");
				switch(data.status) {
					case "play":
						$("#play").addClass("active");
						break;
					case "pause":
						$("#pause").addClass("active");
						break;
					default:
						break;
				}
			}
		);
	},

	/**
	 * Sets the transport control status.
	 *
	 * @param status The status to set.
	 */
	setControls: function(status) {
		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_MEDIA_CONTROLS, 
			'{"status": "' + status + '"}',
			QRemote.getControlStatus
		);
	},

	/**
	 * Gets the volume level.
	 */
	getVolume: function() {
		QRemote.callApi(
			"GET",
			QRemote.Shared.URL_MEDIA_SETTINGS + "volume",
			null,
			function(data) {
				$("#volume").slider("option", "value", data.value);
			}
		);
	},

	/**
	 * Sets the volume level.
	 *
	 * @param volume The volume level between 0 and 100.
	 */
	setVolume: function(volume) {
		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_MEDIA_SETTINGS + "volume",
			'{"value": "' + volume + '"}',
			QRemote.getVolume
		);
	},

	/**
	 * Gets the mute status.
	 */
	getMute: function() {
		QRemote.callApi(
			"GET",
			QRemote.Shared.URL_MEDIA_SETTINGS + "mute",
			null,
			function(data) {
				switch(data.value) {
					case "on":
						$("#mute").addClass("active");
						break;
					default:
						$("#mute").removeClass("active");
						break;
				}

				QRemote.setMuteButton();
			}
		);
	},

	/**
	 * Sets the mute status.
	 *
	 * @param mute The mute status to set, either "on" or "off".
	 */
	setMute: function(mute) {
		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_MEDIA_SETTINGS + "mute",
			'{"value": "' + mute + '"}',
			QRemote.getMute
		);
	},

	/**
	 * Sets the mute button to match the current state based on the "active" class name.
	 *
	 * @param mute The mute status to set, either "on" or "off".
	 */
	setMuteButton: function() {
		if($("#mute").hasClass("active")) {
			$("#mute").children("i").first().removeClass("icon-volume-up");
			$("#mute").children("i").first().addClass("icon-volume-off");
		} else {
			$("#mute").children("i").first().removeClass("icon-volume-off");
			$("#mute").children("i").first().addClass("icon-volume-up");
		}
	},

	/**
	 * Sets the active playlist item.
	 *
	 * @param itemUri The item to play. Only special URIs 
	 * "urn:media:item:previous" and "urn:media:item:next" are 
	 * supported which cause a navigation within the currently active playlist.
	 */
	setPlaylistItem: function(itemUri) {
		QRemote.callApi(
			"POST", 
			QRemote.Shared.URL_MEDIA_PLAYLIST_CURRENT_ITEM,
			'{"uri": "' + itemUri + '"}',
			QRemote.getMediaInfo
		);
	},

	/**
	 * Gets the repeat status.
	 */
	getRepeat: function() {
		QRemote.callApi(
			"GET",
			QRemote.Shared.URL_MEDIA_SETTINGS + "repeat",
			null,
			function(data) {
				// Remove current style
				$("#repeat").children("i").first().removeClass("icon-refresh");
				$("#repeat").children("i").first().removeClass("icon-repeat");

				switch(data.value) {
					case "single":
						$("#repeat").addClass("active");
						$("#repeat").children("i").first().addClass("icon-repeat");
						break;
					case "all":
						$("#repeat").addClass("active");
						$("#repeat").children("i").first().addClass("icon-refresh");
						break;
					default:
						$("#repeat").removeClass("active");
						$("#repeat").children("i").first().addClass("icon-refresh");
						break;
				}
			}
		);
	},

	/**
	 * Sets the repeat status.
	 *
	 * @param value The repeat status to set. Valid values are "off", "single", and "all".
	 */
	setRepeat: function(value) {
		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_MEDIA_SETTINGS + "repeat",
			'{"value": "' + value + '"}'
		);
	},

	/**
	 * Gets the repeat status.
	 */
	getShuffle: function() {
		QRemote.callApi(
			"GET",
			QRemote.Shared.URL_MEDIA_SETTINGS + "shuffle",
			null,
			function(data) {
				switch(data.value) {
					case "all":
						$("#shuffle").addClass("active");
						break;
					default:
						$("#shuffle").removeClass("active");
						break;
				}
			}
		);
	},

	/**
	 * Sets the shuffle status.
	 *
	 * @param value The repeat status to set. Valid values are "off" and "all".
	 */
	setShuffle: function(value) {
		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_MEDIA_SETTINGS + "shuffle",
			'{"value": "' + value + '"}'
		);
	},

	/**
	 * Rotates the shuffle status.
	 */
	rotateShuffle: function() {
		// Check if active
		if(!$("#shuffle").hasClass("active")) {
			QRemote.setShuffle("all");
		} else {
			// Flip through available modes
			if ($("#shuffle").children("i").first().hasClass("icon-refresh")) {
				QRemote.setShuffle("single");
			} else {
				QRemote.setShuffle("off");
			}
		}

		// Update the status
		QRemote.getShuffle();
	},

	/**
	 * Speaks the given sentence.
	 *
	 * @param sentence The sentence to speak using text-to-speech.
	 */
	speak: function(sentence) {
		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_SPEAK_SENTENCE,
			'{"text": "' + sentence.replace(/"/g, "") + '",'
			+ '"pan":' + ($("#speakPan").slider("value") / 100) + ','
			+ '"volume":' + ($("#speakVolume").slider("value") / 100)
			+ '}'
		);
	},

	/**
	 * Sets a built-in animation.
	 *
	 * @param id The animation ID to start. Valid values are 0 and 1.
	 */
	setBuiltInAnimation: function(id) {
		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_LED_ANIMATION,
			'{"type": "builtIn",'
			+ '"id": "' + id + '"}'
		);
	},

	/**
	 * Cancels animation.
	 */
	cancelAnimation: function() {
		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_LED_ANIMATION,
			'{"type": "none"}'
		);
	},

	/**
	 * Sets all LEDs to the specified color.
	 *
	 * @param hex The hexadecimal value for the color.
	 * @param rgb The RGB value for the color.
	 */
	setAllLeds: function(hex, rgb) {

		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_LED_LEDS_ALL,
			'{"red":' + rgb.r +','
			+ '"green":' + rgb.g + ','
			+ '"blue":' + rgb.b
			+ '}'
		);
	},

	/**
	 * Sets status LED to the specified color.
	 *
	 * @param hex The hexadecimal value for the color.
	 * @param rgb The RGB value for the color.
	 */
	setStatusLed: function(hex, rgb) {

		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_LED_LEDS_STATUS,
			'{"red":' + rgb.r +','
			+ '"green":' + rgb.g + ','
			+ '"blue":' + rgb.b
			+ '}'
		);
	},

	/**
	 * Sets the selected range of LEDs to the specified color.
	 */
	setLedRange: function() {

		// Get the start and count of LEDs to change
		var start = $("#ledRange").slider("values", 0);
		var end = $("#ledRange").slider("values", 1);
		var count = end - start;

		// Get color setting
		var rgb = $("#ledRangeColor").miniColors("rgb");

		// Create an item for each color
		var leds = "";

		for(var index = 0; index < count; index +=1) {
			if(index > 0) {
				leds += ",";
			}
			leds += '{"red":' + rgb.r + ','
					+ '"green":' + rgb.g + ','
					+ '"blue":' + rgb.b + '}';
		}

		QRemote.callApi(
			"PUT", 
			QRemote.Shared.URL_LED_LEDS,
			'{"start":' + start +','
			+ '"count":' + count + ','
			+ '"leds":[' + leds + ']'
			+ '}'
		);
	}
};

/** The configuration options for the remote. */
QRemote.Config = {
	/*"URL_ROOT": "http://192.168.147.152:8080",*/
	"URL_ROOT": "",
	"UPDATE_INTERVAL": 5000
};

/** Shared constants. */
QRemote.Shared = {
		"URL_MEDIA_CONTROLS": QRemote.Config.URL_ROOT + "/api/V1/media/controls",
		"URL_MEDIA_PLAYLIST_CURRENT_ITEM": QRemote.Config.URL_ROOT + "/api/V1/media/playlist/current/items/current",
		"URL_MEDIA_SETTINGS": QRemote.Config.URL_ROOT + "/api/V1/media/settings/",
		"URL_SPEAK": QRemote.Config.URL_ROOT + "/api/V1/speak",
		"URL_SPEAK_SENTENCE": QRemote.Config.URL_ROOT + "/api/V1/speak/sentence",
		"URL_LED_ANIMATION": QRemote.Config.URL_ROOT + "/api/V1/led/animation",
		"URL_LED_LEDS_STATUS": QRemote.Config.URL_ROOT + "/api/V1/led/leds/status",
		"URL_LED_LEDS_ALL": QRemote.Config.URL_ROOT + "/api/V1/led/leds/all",
		"URL_LED_LEDS": QRemote.Config.URL_ROOT + "/api/V1/led/leds"
},

$(document).ready(function() { 

	/**
	 * Handles click events for the mute button.
	 *
	 * @param event The click event.
	 */
	$("#mute").on("click", function(event){

		if($("#mute").hasClass("active")) {
			QRemote.setMute("off");
		} else {
			QRemote.setMute("on");
		}
	});

	// Initialize all volume sliders
	$(".volume").slider({
		value: 50
	});

	// Set speech volume to 100%
	$("#speakVolume").slider("option", "value", 100);

	// Set speech pan values
	$("#speakPan").slider("option", { "min": -100, "max": 100 });

	/**
	 * Handles volume slider change events.
	 *
	 * @param event The click event.
	 * @param ui The UI object data.
	 */
	$("#volume").slider({
		value: 50,
		change: function(event, ui) {
			if(event.originalEvent) {
				QRemote.setVolume(ui.value);
			}
		}
	});

	// Create all LED color picker
	$("#ledColor").miniColors({
		letterCase: "uppercase",
		change: QRemote.setAllLeds
	});

	// Create status color picker
	$("#statusLedColor").miniColors({
		letterCase: "uppercase",
		change: QRemote.setStatusLed
	});

	// Create other color pickers
	$(".color-picker").miniColors({
		letterCase: "uppercase"
	});

	// Initialize all volume sliders
	$("#ledRange").slider({
		min: 0,
		max: 32,
		range: true,
		values: [0, 32]
	});

	// Start polling for updates
	QRemote.pollUpdate();
});

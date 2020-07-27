var source

function start(search_term) {

    var hostandport = window.location.hostname + ':' + window.location.port;

	/**
	 * Create a custom-styled Google Map with no labels and custom color scheme.
	 */
	var CreateMap = function() {
		var map_style = [ {
			elementType : "labels",
			stylers : [ {
				visibility : "on"
			} ]
		}, {
			stylers : [ {
				saturation : -69
			} ]
		} ];
		var map_canvas = document.getElementById("map_canvas");
		var myOptions = {
			center : new google.maps.LatLng(-27.4985696,152.9236058),
			zoom : 13,
			styles : map_style,
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};
		return new google.maps.Map(map_canvas, myOptions);
	};
	
	var map = CreateMap();

	/**
	 * We want to assign a random color to each bus in our visualization. We
	 * pick from the HSV color-space since it gives more natural colors.
	 */
	var HsvToRgb = function(h, s, v) {
		h_int = parseInt(h * 6);
		f = h * 6 - h_int;
		var a = v * (1 - s);
		var b = v * (1 - f * s);
		var c = v * (1 - (1 - f) * s);
		switch (h_int) {
		case 0:
			return [ v, c, a ];
		case 1:
			return [ b, v, a ];
		case 2:
			return [ a, v, c ];
		case 3:
			return [ a, b, v ];
		case 4:
			return [ c, a, v ];
		case 5:
			return [ v, a, b ];
		}
	};

	var HsvToRgbString = function(h, s, v) {
		var rgb = HsvToRgb(h, s, v);
		for ( var i = 0; i < rgb.length; ++i) {
			rgb[i] = parseInt(rgb[i] * 256)
		}
		return 'rgb(' + rgb[0] + ',' + rgb[1] + ',' + rgb[2] + ')';
	};

	var h = Math.random();
	var golden_ratio_conjugate = 0.618033988749895;

	var NextRandomColor = function() {
		h = (h + golden_ratio_conjugate) % 1;
		return HsvToRgbString(h, 0.90, 0.90)
	};

	var icon = new google.maps.MarkerImage(
			'http://' + hostandport + '/WhiteCircle8.png', null, null,
			new google.maps.Point(4, 4));

	var CreateVehicle = function(v_data) {
		var point = new google.maps.LatLng(v_data.lat, v_data.lon);
		var path = new google.maps.MVCArray();
		path.push(point);
		var marker_opts = {
			clickable : true,
			draggable : false,
			flat : false,
			icon : icon,
			map : map,
			position : point,
			title : 'id=' + v_data.vid,
			label: v_data.label
		};
		var polyline_opts = {
			clickable : false,
			editable : false,
			map : map,
			path : path,
			strokeColor : NextRandomColor(),
			strokeOpacity : 0.8,
			strokeWeight : 4
		};
		return {
			id : v_data.vid,
			label: v_data.label,
			marker : new google.maps.Marker(marker_opts),
			polyline : new google.maps.Polyline(polyline_opts),
			path : path,
			lastUpdate : v_data.lastUpdate
		};
	};

	function CreateVehicleUpdateOperation(vehicle, lat, lon) {
		return function() {
			var point = new google.maps.LatLng(lat, lon);
			vehicle.marker.setPosition(point);
			var path = vehicle.path;
			var index = path.getLength() - 1;
			path.setAt(index, point);
		};
	};
	
	var vehicles_by_id = {};
	var animation_steps = 20;

	function UpdateVehicle(v_data, updates) {
	    //console.log(v_data)
		var id = v_data.vid;
		if (!(id in vehicles_by_id)) {
			vehicles_by_id[id] = CreateVehicle(v_data);
		}
		var vehicle = vehicles_by_id[id];
		if (vehicle.lastUpdate >= v_data.lastUpdate) {
			return;
		}
		vehicle.lastUpdate = v_data.lastUpdate

		var path = vehicle.path;
		var last = path.getAt(path.getLength() - 1);
		path.push(last);

		var lat_delta = (v_data.lat - last.lat()) / animation_steps;
		var lon_delta = (v_data.lon - last.lng()) / animation_steps;

		if (lat_delta != 0 && lon_delta != 0) {
			for ( var i = 0; i < animation_steps; ++i) {
				var lat = last.lat() + lat_delta * (i + 1);
				var lon = last.lng() + lon_delta * (i + 1);
				var op = CreateVehicleUpdateOperation(vehicle, lat, lon);
				updates[i].push(op);
			}
		}
	};
	
	var first_update = true;
	
	var ProcessVehicleData = function(data) {
		var vehicle = jQuery.parseJSON(data);
		var updates = [];
		var bounds = new google.maps.LatLngBounds();
		for ( var i = 0; i < animation_steps; ++i) {
			updates.push(new Array());
		}
		//jQuery.each(vehicles, function() {
  	    UpdateVehicle(vehicle, updates);
	    bounds.extend(new google.maps.LatLng(this.lat, this.lon));
		//});
		if (first_update && ! bounds.isEmpty()) {
		    //var myLatLng = new google.maps.LatLng(-27.4985696,152.9236058);
		    //bounds.extend(myLatLng)
			//map.fitBounds(bounds);
			first_update = false;
		}
		var applyUpdates = function() {
			if (updates.length == 0) {
				return;
			}
			var fs = updates.shift();
			for ( var i = 0; i < fs.length; i++) {
				fs[i]();
			}
			setTimeout(applyUpdates, 1);
		};
		setTimeout(applyUpdates, 1);	
	};

	/**
	 * We create a WebSocket to listen for vehicle position updates from our
	 * webserver.

	if ("WebSocket" in window) {
		var ws = new WebSocket("ws://" + hostandport + "/gtfs/server");
		ws.onopen = function() {
			console.log("WebSockets connection opened");
		}
		ws.onmessage = function(e) {
			console.log("Got WebSockets message");
			ProcessVehicleData(e.data);
		}
		ws.onclose = function() {
			console.log("WebSockets connection closed");
		}
	} else {
		alert("No WebSockets support");
	}
   */

    var host = location.hostname

    if (host == 'localhost')  {
        console.log("SSE connection opened for " + search_term);
        source = new EventSource("http://localhost:8080/query/stream/" + search_term);

    } else {
        source = new EventSource("https://" + location.hostname + "/query/stream/" + search_term);
    }
    source.onmessage = eventHandler;

    function eventHandler (e) {
        //console.log(e.data);
        if ("" != e.data) {
            ProcessVehicleData(e.data);
        }
    }
}

function myClickFunctionSearch() {
    var search_term = document.getElementById('select_route').value ? document.getElementById('select_route').value : 'all';
    start(search_term)
    // disable button
    $(this).prop("disabled", true);
    $(this).html(
      `<div id="spinnerSearch"><span class="spinner-border spinner-border-sm float-right" role="status" aria-hidden="true"></span> Loading...</div>`
    );
}

function myCancelFunctionSearch() {
    source.close();
    var spinner = document.getElementById("spinnerSearch");
    spinner.parentNode.removeChild(spinner);
    var newElement = document.createElement('div');
    newElement.innerHTML = '<button type="button" id="btnFetchSearch" class="btn btn-primary mb-2"></button>';
    newElement.addEventListener("click", myClickFunctionSearch, true);
    newElement.text = "Submit";
    $(this).insertBefore(this, newElement);
    document.getElementById("btnFetchSearch").disabled = false;
    document.getElementById("btnFetchSearch").innerHTML = "Submit"
}

function loadDropDown() {
    var host = location.hostname
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var obj = JSON.parse(this.response)
            var html = '<option value="all">ALL</option>';
            for (var key in obj) {
                html += '<option value="' + obj[key] + '">' + obj[key] + '</option>';
            }
            $("#select_route").append(html);
        }
    };
    xhttp.open("GET", "/query/routes", true);
    xhttp.send();
}

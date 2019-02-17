var routes = $('#routes');

function updateResults() {
	$.getJSON('http://localhost:45000/stats', function(result) {
		var sizes = result.sizes;
		var times = result.times;

		Object.keys(sizes).forEach(function(path) {
			var route = sizes[path];
			var id = path.replace('/', '_');
			var routeElement = $('#' + id);
			if (!routeElement.length) {
				routes.append(
					'<tr id="' +
						id +
						'"><th scope="row">' +
						path +
						'</th><td id="' +
						id +
						'time_min"></td><td id="' +
						id +
						'time_max"></td><td id="' +
						id +
						'time_avg"></td><td id="' +
						id +
						'size_min"></td><td id="' +
						id +
						'size_max"></td><td id="' +
						id +
						'size_avg"></td></tr>'
				);
			}

			$('#' + id + 'size_min').text(route.min);
			$('#' + id + 'size_max').text(route.max);
			$('#' + id + 'size_avg').text(route.avg);
		});

		Object.keys(times).forEach(function(path) {
			var route = times[path];
			var id = path.replace('/', '_');
			var routeElement = $('#' + id);
			if (!routeElement.length) {
				routes.append(
					'<tr id="' +
						id +
						'"><th scope="row">' +
						path +
						'</th><td id="' +
						id +
						'time_min"></td><td id="' +
						id +
						'time_max"></td><td id="' +
						id +
						'time_avg"></td><td id="' +
						id +
						'size_min"></td><td id="' +
						id +
						'size_max"></td><td id="' +
						id +
						'size_avg"></td></tr>'
				);
			}

			$('#' + id + 'time_min').text(route.min);
			$('#' + id + 'time_max').text(route.max);
			$('#' + id + 'time_avg').text(route.avg);
		});
	});
	setTimeout(updateResults, 3000);
}
updateResults();

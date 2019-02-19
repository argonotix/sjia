/*
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Christopher Kies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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

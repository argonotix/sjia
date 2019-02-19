var lorem_text = $('#lorem_text');

function loadLorem() {
	$.getJSON('http://localhost:4567/lorem', function(result) {});

	$.ajax({
		type: 'GET',
		url: 'http://localhost:4567/lorem',
		success: function(data, textStatus, request) {
			lorem_text.text(data.lorem);
		},
		error: function(request, textStatus, errorThrown) {
			lorem_text.text('An error occurred: ' + errorThrown);
		}
	});
}
window.onload = function() {
	loadLorem();
};

$('#lorem_button').click(function() {
	loadLorem();
});

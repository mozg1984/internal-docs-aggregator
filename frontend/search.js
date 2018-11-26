var App = {};

App.httpRequest = function (url, options) {
	options = $.extend({
		url: url
	}, options || {});
	
	return new Promise(function (resolve, reject) {
		options = $.extend({
			success: function (response) {
				resolve(response);
			},
			error: function (response) {
				reject(response.responseText);
			}
		}, options);
		
		$.ajax(options);	
	});
};

App.reset = function () {
  $('#query').val('');
  $('#result').val('');
};

App.execute = function () {
  var service = $('#service').val();
  var query = $('#query').val();

  App.httpRequest('http://localhost:8080/doc-api/search/' + service + '/' + query)
		.then(function (queryResult) {
			$('#result').val(JSON.stringify(queryResult));
		})
		.catch(function (response) {
      $('#result').val('Error: ' + response);
		});
};
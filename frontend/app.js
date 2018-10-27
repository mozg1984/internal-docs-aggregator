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
				//reject(JSON.parse(response.responseText));
			}
		}, options);
		
		$.ajax(options);	
	});
};

App.isSuccess = function (response) {
	return response && response.status === 'OK';
};

App.success = function (message) {
	$('.success').show();
	$('.success .message').text(message);
};

App.isError = function (response) {
	return response && response.status === 'ERROR';
};

App.error = function (message) {
	$('.error').show();
	$('.error .message').text(message);
};

App.modal = function (view) {
	$modal = $(view);
	
	$modal.on('hidden.bs.modal', function () {
		$(this).remove();
	});
	
	$modal.modal('show');
};

App.documents = (function () {	
	function closeModal() {
		$('.employee.modal').modal('hide');
	}
	
	function showControls() {
		$('.entities-container .btn.commit-control, .btn.delete-control').show();
	}

	function hideControls() {
		$('.entities-container .btn.commit-control, .btn.delete-control').hide();
	}
	
	function emptyRowView() {
		return '<tr>' +
							'<td colspan="2" class="empty">Empty list</td>' +
						'</tr>';
	}

	function checkboxView() {
		return '<input type="checkbox" class="form-check-input">';
	}

	function inputDocView() {
		return '<input type="file" class="form-control-file border">';
	}

	function rowView() {
		return '<tr>' +
						'<td>' + checkboxView() + '</td>' +
						'<td class="document">' + inputDocView() + '</td>' +
					'</tr>';
	}

	function isTableEmpty() {
		return $('.documents.entities-container .empty').length > 0 ||
			$('.documents.entities-container > .list > table > tbody > tr').length === 0;
	}

	function addToTable($row) {
		var $list = $('.documents.entities-container > .list > table > tbody');
		isTableEmpty() ? $list.html($row) : $list.append($row);
		showControls();
	}

	function removeFromTable() {		
		$rows = $('.documents.entities-container > .list > table > tbody > tr');

		$rows.each(function () {
			if ($(this).find('[type="checkbox"]').is(':checked')) {
				$(this).remove();
			}
		});

		if (isTableEmpty()) {
			addToTable(emptyRowView());
		}
	}

	function add() {
		$row = $(rowView());

		$docField = $row.find('[type="file"]');
		$nameField = $row.find('td.document');

		
		addToTable($row);
		
		$docField.click();

		$docField.change(function () {
			var n = $(this).val();
			$nameField.append(n);
		});
	}
	
	function commit() {
		var formData = new FormData();

		$('[type="file"]').each(function () {
			if ($(this)[0].files[0] != undefined) {
				formData.append("document", $(this)[0].files[0]);
			}
		});

		var requestOptions = {
			type: 'POST', 
			data: formData,
			processData: false,
    	contentType: false
		};	

		App.httpRequest('http://localhost:8080/doc-api/save', requestOptions)
		.then(function (response) {
			console.log('success', response);	
		})
		.catch(function (response) {
			App.error('Something went wrong: ' + response);
		});
	}
	
	function init() {
		$(document).on('click', '.documents.entities-container .add-control', function () {
			add();	
		});

		$(document).on('click', '.documents.entities-container .delete-control', function () {
			removeFromTable();	
		});

		$(document).on('click', '.documents.entities-container .commit-control', function () {
			commit();	
		});
	}
	
	// public API
	return {
		init: init
	};
})();

$(document).ready(function () {
	App.documents.init();
});
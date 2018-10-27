var App = {};
	
// Current service name
App.SERVICE = 'dispatchers';

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

App.DotNotation = {
	validate: function (string) {
		if (/[^a-zA-Zа-яА-ЯёЁ0-9._-]/.test(string)) {
			throw new Error("The given string <" + string + "> contains not supported chars.");
		}
		
		if (/\.{2,}/.test(string) || /^\./.test(string) || /\.$/.test(string)) {
			throw new Error("The given string <" + string + "> has a dotted notation violation.");
		}
	}
};

App.documents = (function () {
	var ROOT = 'root';

	function getAttributes(category, catalog) {
		try {
			App.DotNotation.validate(category);
			App.DotNotation.validate(catalog);
		} catch (e) {
			App.error(e.message);
			return;
		}

		return {
			service: App.SERVICE,
			category: ROOT + '.' + category,
			catalog: ROOT + '.' + catalog
		};
	}
	
	function closeModal() {
		$('.employee.modal').modal('hide');
	}
	
	function showControls() {
		$('.entities-container .btn.commit-control, .btn.delete-control').show();
	}

	function hideControls() {
		$('.entities-container .btn.commit-control, .btn.delete-control').hide();
	}
	
	function resetTable() {
		addToTable(emptyRowView());
		hideControls();
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

	function fileDownloadLinkRow(id) {
		var link = 'http://localhost:8080/doc-api/get/' + App.SERVICE + '/' + id;
		return '<a href="' + link + '">' +
							'<span class="glyphicon glyphicon-file"></span>' +
						'</a>';
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
			resetTable();
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
		var formData = new FormData(),
				order = 0;

		formData.append(
			'attributes', 
			JSON.stringify(
				getAttributes(
					'orders', 
					'catalog1.catalog2.catalog3'
				)
			)
		);

		$('[type="file"]').each(function () {
			if ($(this)[0].files[0] != undefined) {
				formData.append("document", $(this)[0].files[0]);
				var $cell = $(this).parent();
				$cell.append('<img src="images/loader.gif" class="loader">');
				$cell.attr('order', order++);		
			} else {
				$(this).parent().parent().remove();
			}
		});

		if (formData.getAll("document").length === 0) {
			resetTable();
			return;
		}

		var requestOptions = {
			type: 'POST', 
			data: formData,
			processData: false,
    	contentType: false
		};	

		App.httpRequest('http://localhost:8080/doc-api/save', requestOptions)
		.then(function (response) {
			console.log('success', response);
			setIdsInTable(response);
		})
		.catch(function (response) {
			App.error('Something went wrong: ' + response);
		});
	}
	
	function setIdsInTable(response) {
		for (var i = 0, n = response.length; i < n; i++) {
			var $cell = $('[order="' + i + '"]');
			$cell.find('input, img').remove();
			$cell.append(fileDownloadLinkRow(response[i].id));
		}
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
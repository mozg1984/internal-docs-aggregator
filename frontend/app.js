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

App.reset_errors = function () {
	$('.error').hide();
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

	function getAttributes(service, category, catalog) {
		if (service == '') {
			App.error('service is undefined');
			return null;
		}
		
		if (category == '') {
			App.error('category is undefined');
			return null;
		}

		if (catalog == '') {
			App.error('catalog is undefined');
			return null;
		}

		try {
			App.DotNotation.validate(category);
			App.DotNotation.validate(catalog);
		} catch (e) {
			App.error(e.message);
			return null;
		}

		return {
			service: service,
			category: ROOT + '.' + category,
			catalog: ROOT + '.' + catalog
		};
	}

	function getAttributesFromFilter() {
		return {
			service: getSelectedService(),
			category: getSelectedCategory(),
			catalog: getSelectedCatalog()
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

	function markTableAsSerching() {
		$row = '<tr>' +
							'<td colspan="2" class="empty">Searching...</td>' +
						'</tr>';
		$('.documents.entities-container > .list > table > tbody').html($row);
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

	function fileDownloadLinkRow(id, service) {
		var link = 'http://localhost:8080/doc-api/get/' + (service || getSelectedService()) + '/' + id;
		return '<a href="' + link + '">' +
							'<span class="glyphicon glyphicon-file"></span>' +
						'</a>';
	}

	
	function tableRowView(id, name, service) {
		return '<tr>' +
						'<td>' + checkboxView() + '</td>' +
						'<td>' + name + fileDownloadLinkRow(id, service) + '</td>' +
					'</tr>';
	}

	function addTableBy(documents) {
		$('.documents.entities-container > .list > table > tbody').html('');
		resetTable();
		
		for (var i = 0, n = documents.length; i < n; i++) {
			addToTable(tableRowView(documents[i].id, documents[i].name, documents[i].service)); 
		}
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

	function getJoinedTextValuesBy($tree) {
		var nodes = $tree.treeview('getSelected');

		if (nodes.length === 0) {
			return '';
		}
		
		var node = nodes[0];
		var textValues = [node.text];
		
		var parentNode = $tree.treeview('getParent', node.nodeId);
		while(parentNode.hasOwnProperty('parentId')) {
			textValues.push(parentNode.text);
			parentNode = $tree.treeview('getParent', parentNode.nodeId);
		}
			
		return textValues.reverse().join('.');	
	}

	function getSelectedCategory() {
		return getJoinedTextValuesBy($('#category-tree'));
	}

	function getSelectedCatalog() {
		return getJoinedTextValuesBy($('#catalog-tree'));
	}

	function getSelectedService() {
		return $("#services-list").val();
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

		var attributes = getAttributes(
			getSelectedService(),
			getSelectedCategory(), 
			getSelectedCatalog()
		);

		if (attributes == null) {
			return;
		}

		formData.append(
			'attributes', 
			JSON.stringify(attributes)
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


	function search() {
		var attributes = getAttributes(
			getSelectedService(),
			getSelectedCategory(), 
			getSelectedCatalog()
		);

		if (attributes == null) {
			return;
		}

		var query = 'category:' + attributes.category + ' AND ' + 'catalog:' + attributes.catalog;

		markTableAsSerching();

		App.httpRequest('http://localhost:8080/doc-api/search/' + attributes.service + '/' + query)
		.then(function (response) {
			addTableBy(response);
		})
		.catch(function (response) {
			App.error('Something went wrong: ' + response);
		});	
	}

	function getCategoryTree() {
		return [
			{
				text: "category1",
				state: {
					expanded: false,
				},
				nodes: [
					{
						text: "category11",
						state: {
							expanded: false,
						},
						nodes: [
							{
								text: "category111"
							},
							{
								text: "category112"
							}
						]
					},
					{
						text: "category12"
					}
				]
			},
			{
				text: "category2",
				state: {
					expanded: false,
				},
				nodes: [
					{
						text: "category21",
						state: {
							expanded: false,
						},
						nodes: [
							{
								text: "category211"
							},
							{
								text: "category212",
								state: {
									expanded: false,
								},
								nodes: [
									{
										text: "category2121"
									}
								]
							}
						]
					}
				]
			},
			{
				text: "category3"
			}
		];
	}

	function getCatalogTree() {
		return [
			{
				text: "catalog1",
				state: {
					expanded: false,
				},
				nodes: [
					{
						text: "catalog11",
						state: {
							expanded: false,
						},
						nodes: [
							{
								text: "catalog111"
							},
							{
								text: "catalog112"
							}
						]
					},
					{
						text: "catalog12"
					}
				]
			},
			{
				text: "catalog2",
				state: {
					expanded: false,
				},
				nodes: [
					{
						text: "catalog21",
						state: {
							expanded: false,
						},
						nodes: [
							{
								text: "catalog211"
							},
							{
								text: "catalog212",
								state: {
									expanded: false,
								},
								nodes: [
									{
										text: "catalog2121"
									}
								]
							}
						]
					}
				]
			},
			{
				text: "catalog3"
			}
		];
	}

	function filterChanged() {
		var $list = $('.documents.entities-container > .list > table > tbody').html('');
		resetTable();
		App.reset_errors();
	}

	function init() {		
		$('#category-tree').treeview({data: getCategoryTree()});
		$('#catalog-tree').treeview({data: getCatalogTree()});

		$('#services-list').on('change', filterChanged);

		$('#category-tree')
			.on('nodeSelected', filterChanged)
			.on('nodeUnselected', filterChanged);

		$('#catalog-tree')
			.on('nodeSelected', filterChanged)
			.on('nodeUnselected', filterChanged);
		
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
		search: search,
		init: init
	};
})();

$(document).ready(function () {
	App.documents.init();
});
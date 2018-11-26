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

App.setProgressBar = function (percent) {
  $('.progress-bar').css('width', percent + '%');
  $('.progress-bar').html(percent + '%');
};

App.increaseProgressBar = function (percent) {
  var prevValue = parseInt($('.progress-bar').text());
  $('.progress-bar').css('width', prevValue + percent + '%');
  $('.progress-bar').html(prevValue + percent + '%');
};

App.select = function (id) {
  $block = $('#' + id);
  $block.removeClass('expected');
  $result = $block.find('.result');
  $result.html('ВЫПОЛНЯЕТСЯ');
};

App.estimate = function (id, value) {
  $block = $('#' + id);
  $block.removeClass('expected');
  $result = $block.find('.result');
  $result.html('РЕЗУЛЬТАТ: ' + value);
  App.increaseProgressBar(4);
};

App.getMetadataForTest = function () {
  return {
    service: 'dispatchers', 
    catalog: 'root.catalog3',
    category: 'root.category3'
  };
};

App.getFormDataForTest = function () {
  var formData = new FormData();

  formData.append(
    'attributes', 
    JSON.stringify(App.getMetadataForTest())
  );

  $('[type="file"]').each(function () {
    if ($(this)[0].files[0] != undefined) {
      formData.append("document", $(this)[0].files[0]);
      console.log($(this)[0].files[0]);	
    }
  });

  return formData;
};

App.searchTestData = function (query) {
  App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + query)
  .then(function (response) {
    result = response;
  });
};

App.createTestDocuments = function () {
  var formData = App.getFormDataForTest();

  if (formData.getAll("document").length === 0) {
    // error
  }

  var requestOptions = {
    type: 'POST', 
    data: formData,
    processData: false,
    contentType: false
  };
  
  App.httpRequest('http://localhost:8080/doc-api/create', requestOptions)
  .then(function (response) {
    console.log(response);

    if (response.length == 0) {
      // Didn't added
    }

    var searchInIndex = function () {
      App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + 'id:' + response[0].id)
      .then(function (response) {
        if (response.length > 0 || ((new Date()).getTime() - begin) > 5000) {
          console.log('exists');
          return;
        }

        console.log('not exists');
        searchInIndex();
      });
    };

    var begin = (new Date()).getTime();
    searchInIndex();

    console.log('exit');
  })
  .catch(function (response) {
    consle.log('Error', response);
  });
};

var tests = { external: {}, internal: {} };

tests.external.K1 = {
  action: function () {    
    var promise = new Promise(function(resolve, reject) {
      App.select(1);
      App.estimate(1, 1);
      resolve();
    });
    return promise;
  }
};

tests.external.K2 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(2);
      App.estimate(2, 1);
      resolve();
    });
    return promise;
  }
};

tests.external.K3 = {
  action: function () { 
    var promise = new Promise(function(resolve) {
      App.select(3);
      App.estimate(3, 1);
      resolve();
    });
    return promise;
  }
};

tests.external.K4 = {// Пробуем добавить два одинаковых документа
  action: function () {
    var promise = new Promise(function(resolve, reject) {
      App.select(4);
      var formData = new FormData();

      formData.append(
        'attributes', 
        JSON.stringify(App.getMetadataForTest())
      );
      
      if ($('[type="file"]')[0].files[0] != undefined) {
        formData.append("document", $('[type="file"]')[0].files[0]);
      }

      if (formData.getAll("document").length == 0) {
        reject();
      }

      // Дублируем
      formData.append(
        "document",
        formData.getAll("document")[0]
      );

      var documentsCount = formData.getAll("document").length;

      var requestOptions = {
        type: 'POST', 
        data: formData,
        processData: false,
        contentType: false
      };      
     
      var start = (new Date()).getTime();
      App.httpRequest('http://localhost:8080/doc-api/create', requestOptions)
        .then(function (response) {
          if (response.length + 1 == documentsCount) {
            var searchInIndex = function () {
              App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + 'id:' + response[0].id)
              .then(function (response) {
                if (response.length > 0) {
                  var speed = (new Date()).getTime() - start;
                  App.estimate(4, 1 + " (" + speed + "ms)");
                  resolve();
                } else {
                  searchInIndex();
                }
              });
            };       
            
            searchInIndex();
          } else {
            reject();
          }
        });      
    });
    return promise;
  }
};

tests.external.K5 = {
  action: function () {
    var promise = new Promise(function(resolve, reject) {
      App.select(5);
      var formData = new FormData();

      formData.append(
        'attributes', 
        JSON.stringify(App.getMetadataForTest())
      );

      if ($('[type="file"]')[1].files[0] != undefined) {
        formData.append("document", $('[type="file"]')[1].files[0]);
      }

      if ($('[type="file"]')[2].files[0] != undefined) {
        formData.append("document", $('[type="file"]')[2].files[0]);
      }

      if (formData.getAll("document").length == 0) {
        reject();
      }
      
      var requestOptions = {
        type: 'POST', 
        data: formData,
        processData: false,
        contentType: false
      };

      var start = (new Date()).getTime();
      App.httpRequest('http://localhost:8080/doc-api/create', requestOptions)
         .then(function (response) {
          if (response.length > 0) {
            var searchInIndex = function () {
              App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + 'id:' + response[0].id + ' OR ' + 'id:' + response[1].id)
              .then(function (response) {
                if (response.length >= 2) {
                  var speed = (new Date()).getTime() - start;
                  App.estimate(5, 1 + " (" + speed + "ms)");
                  resolve();             
                }
                else {
                  searchInIndex();
                }
              });
            };        
            
            searchInIndex();
          } else {
            reject();
          }
        })
    });
    return promise;
  }
};

tests.external.K6 = {
  action: function () {
    var promise = new Promise(function(resolve, reject) {
      App.select(6);
      var wordsForFullSearching = 'проектирование Рекомендации analyses of Socrates';

      var start = (new Date()).getTime();
      App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + wordsForFullSearching)
      .then(function (response) {
        console.log(response);
        
        if (response.length >= 3) {
          var speed = (new Date()).getTime() - start;
          App.estimate(6, 1 + " (" + speed + "ms)");
          resolve();
        } else {
          reject();
        }
      });
    });
    return promise;
  }
};

tests.external.K7 = {
  action: function () {
    var promise = new Promise(function(resolve, reject) {
      App.select(7);
      var wordsForSearchingByMetadata = 'name:Тестовый_файл_1 OR name:Тестовый_файл_2 OR name:Тестовый_файл_3';
      
      var start = (new Date()).getTime();
      App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + wordsForSearchingByMetadata)
      .then(function (response) {
        if (response.length >= 3) {
          var speed = (new Date()).getTime() - start;
          App.estimate(7, 1 + " (" + speed + "ms)");
          resolve();
        } else {
          reject();
        }
      });
    });
    return promise;
  }
};

tests.external.K8 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(8);
      App.estimate(8, 1);
      resolve();
    });
    return promise;
  }
};

tests.external.K9 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(9);
      App.estimate(9, 1);
      resolve();
    });
    return promise;
  }
};

tests.external.K10 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(10);
      App.estimate(10, 1);
      resolve();
    });
    return promise;
  }
};

tests.internal.K1 = {
  action: function () {// Пробуем породить ошибку путем удаления несуществующего документа
    var promise = new Promise(function(resolve, reject) {
      App.select(11);
      
      var requestOptions = {
        type: 'POST', 
        data: JSON.stringify({service: 'dispatchers', ids: '-1'}),
        processData: false,
        contentType: false,
        contentType: 'application/json;charset=utf-8'
      };
  
      App.httpRequest('http://localhost:8080/doc-api/delete', requestOptions)
      .then(function (response) {
        reject();
      })
      .catch(function (response) {
        App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/id:0')
          .then(function (response) {
            App.estimate(11, 1);
            resolve();
          })
          .catch(function (response) {
            reject();
          });
      });
    });
    return promise;
  }
};

tests.internal.K2 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(12);
      App.estimate(12, 1);
      resolve();
    });
    return promise;
  }
};

tests.internal.K3 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(13);
      App.estimate(13, 1);
      resolve();
    });
    return promise;
  }
};

tests.internal.K4 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(14);
      App.estimate(14, 1);
      resolve();
    });
    return promise;
  }
};

tests.internal.K5 = {
  action: function () {
    var promise = new Promise(function(resolve, reject) {
      App.select(15);
      var wordsForFullSearching = 'проектирование Рекомендации analyses of Socrates';

      var start = (new Date()).getTime();
      App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + wordsForFullSearching)
      .then(function (response) {
        console.log(response);
        
        if (response.length >= 3) {
          var speed = (new Date()).getTime() - start;
          App.estimate(15, 1 + " (" + speed + "ms)");
          resolve();
        } else {
          reject();
        }
      });
    });
    return promise;
  }
};

tests.internal.K6 = {
  action: function () {
    var promise = new Promise(function(resolve, reject) {
      App.select(16);
      var wordsForSearchingByMetadata = 'name:Тестовый_файл_1 OR name:Тестовый_файл_2 OR name:Тестовый_файл_3';
      
      var start = (new Date()).getTime();
      App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + wordsForSearchingByMetadata)
      .then(function (response) {
        if (response.length >= 3) {
          var speed = (new Date()).getTime() - start;
          App.estimate(16, 1 + " (" + speed + "ms)");
          resolve();
        } else {
          reject();
        }
      });
    });
    return promise;
  }
};

tests.internal.K7 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(17);
      App.estimate(17, 1);
      resolve();
    });
    return promise;
  }
};

tests.internal.K8 = {
  action: function () {
    var promise = new Promise(function(resolve, reject) {
      App.select(18);
      var differentFileTypes = 'name:.txt OR name:.pdf OR name:.doc';
      
      App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + differentFileTypes)
      .then(function (response) {
        if (response.length >= 3) {
          App.estimate(18, 1);
          resolve();
        } else {

          reject();
        }
      });
    });
    return promise;
  }
};

tests.internal.K9 = {
  action: function () {// Валидация отсутствия обязательных метаданных
    var promise = new Promise(function(resolve, reject) {
      App.select(19);
      var formData = new FormData();

      formData.append(
        'attributes', 
        JSON.stringify({})// Пустые метаданные
      );
      
      if ($('[type="file"]')[0].files[0] != undefined) {
        formData.append("document", $('[type="file"]')[0].files[0]);
      }

      if (formData.getAll("document").length == 0) {
        reject();
      }

      // Дублируем
      formData.append(
        "document",
        formData.getAll("document")[0]
      );

      var documentsCount = formData.getAll("document").length;

      var requestOptions = {
        type: 'POST', 
        data: formData,
        processData: false,
        contentType: false
      };      
     
      App.httpRequest('http://localhost:8080/doc-api/create', requestOptions)
        .then(function (response) {
          reject();
        })
        .catch(function (response) {
          App.estimate(19, 1);
          resolve();
        }); 
    });
    return promise;
  }
};

tests.internal.K10 = {
  action: function () {
    var promise = new Promise(function(resolve, reject) {
      App.select(20);
      var differentFileTypes = 'name:.txt OR name:.pdf OR name:.doc';
      
      App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + differentFileTypes)
      .then(function (response) {
        if (response.length >= 3) {
          App.estimate(20, 1);
          resolve();
        } else {
          reject();
        }
      });
    });
    return promise;
  }
};

tests.internal.K11 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(21);
      App.estimate(21, 1);
      resolve();
    });
    return promise;
  }
};

tests.internal.K12 = {
  action: function (resolve, reject) {
    var promise = new Promise(function(resolve) {
      App.select(22);
      var wordsForFullSearching = 'проектирование Рекомендации analyses of Socrates';

      var start = (new Date()).getTime();
      App.httpRequest('http://localhost:8080/doc-api/search/dispatchers/' + wordsForFullSearching)
        .then(function (response) {
          if (response.length >= 3) {
            var speed = (new Date()).getTime() - start;
            App.estimate(22, speed + 'ms');
            resolve();
          } else {
            reject();
          }
        });
    });
    return promise;
  }
};

tests.internal.K13 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(23);
      App.estimate(23, 1);
      resolve();
    });
    return promise;
  }
};

tests.internal.K14 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(24);
      App.estimate(24, 1);
      resolve();
    });
    return promise;
  }
};

tests.internal.K15 = {
  action: function () {
    var promise = new Promise(function(resolve) {
      App.select(25);
      App.estimate(25, 1);
      resolve();
    });
    return promise;
  }
};

App.runTest = function () {
  App.setProgressBar(0);

  tests.external.K1.action()
    .then(tests.external.K2.action)
    .then(tests.external.K3.action)
    .then(tests.external.K4.action)
    .then(tests.external.K5.action)
    .then(tests.external.K6.action)
    .then(tests.external.K7.action)
    .then(tests.external.K8.action)
    .then(tests.external.K9.action)
    .then(tests.external.K10.action)
    .then(tests.internal.K1.action)
    .then(tests.internal.K2.action)
    .then(tests.internal.K3.action)
    .then(tests.internal.K4.action)
    .then(tests.internal.K5.action)
    .then(tests.internal.K6.action)
    .then(tests.internal.K7.action)
    .then(tests.internal.K8.action)
    .then(tests.internal.K9.action)
    .then(tests.internal.K10.action)
    .then(tests.internal.K11.action)
    .then(tests.internal.K12.action)
    .then(tests.internal.K13.action)
    .then(tests.internal.K14.action)
    .then(tests.internal.K15.action);
};


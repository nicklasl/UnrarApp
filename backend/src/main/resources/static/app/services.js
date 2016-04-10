(function(angular) {
  var ArchiveFactory = function($resource) {
    return $resource('/api/:id', {
      id: '@id'
    }, {
      update: {
        method: "PUT"
      },
      remove: {
        method: "DELETE"
      }
    });
  };

  ArchiveFactory.$inject = ['$resource'];
  angular.module("myApp.services").factory("Archive", ArchiveFactory);
}(angular));
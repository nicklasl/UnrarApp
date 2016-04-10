(function(angular) {
  var AppController = function($scope, Archive) {
    Archive.query(function(response) {
      $scope.archives = response ? response : [];
    });

    $scope.handleClick = function(item) {
        console.log("item="+item);
        if(item.hasSubDirs) {
            navigate(item);
        } else {
            unrar(item);
        }
    };

    function navigate(folder) {
        console.log("item is a folder. navigate!");
    }

    function unrar(archive) {
        console.log("item is an archive. unrar!")
    }

//    $scope.addItem = function(description) {
//      new Item({
//        description: description,
//        checked: false
//      }).$save(function(item) {
//        $scope.items.push(item);
//      });
//      $scope.newItem = "";
//    };
  };

  AppController.$inject = ['$scope', 'Archive'];
  angular.module("myApp.controllers").controller("AppController", AppController);
}(angular));
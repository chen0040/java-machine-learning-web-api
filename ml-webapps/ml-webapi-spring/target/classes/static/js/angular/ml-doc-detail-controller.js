(function(){
    var controller = function($scope, $log, $modalInstance, doc){
        $scope.doc = doc;

        $scope.ok = function(){
            $modalInstance.dismiss('ok');
        };
    };

    var app = angular.module('ml-app');
    app.controller('mlDocDetailCtrl', ['$scope', '$log', '$modalInstance', 'doc', controller]);
}());
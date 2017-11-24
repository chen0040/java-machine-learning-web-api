(function(){
    var controller = function($scope, $log, $modalInstance){
        $scope.f = {};
        $scope.f.keywords = 'error; systemd-logind';
        $scope.f.timeWindowSize = 900000;
        $scope.f.url = 'http://172.16.2.111:9200/syslog/syslog/_search';
        $scope.f.size = 1000;

        $scope.ok = function(){
            $modalInstance.close($scope.f);
        };

        $scope.cancel = function(){
            $modalInstance.dismiss('cancel');
        };
    };

    var app = angular.module('ml-app');
    app.controller('mlBatchCreateESCtrl', ['$scope', '$log', '$modalInstance', controller]);
}());
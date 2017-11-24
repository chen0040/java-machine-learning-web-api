(function(){
    var controller = function($scope, $log, $location, $modalInstance, mlModuleService, moduleId){
        $scope.moduleId = moduleId;

        var onGetModule = function(response){
            $scope.module = response.data;

            $scope.attributes = [];
            for(var attrname in $scope.module.attributes){
                var attr = {};
                attr.name = attrname;
                attr.value = $scope.module.attributes[attrname];
                $scope.attributes.push(attr);
            }
        };

        var getModule = function(){
            mlModuleService.getModule($scope.moduleId).then(onGetModule);
        }

        $scope.onOK = function(){
            for(var i = 0; i < $scope.attributes.length; ++i){
                var attr = $scope.attributes[i];
                $scope.module.attributes[attr.name] = attr.value;
                //$log.info(attr.name+'='+attr.value);
            }
            mlModuleService.updateModuleShell($scope.module).then(function(){
                $modalInstance.close($scope.module);
            });
        };

        $scope.onCancel = function(){
            $modalInstance.dismiss('cancel');
        };

        getModule();
    };

    var app = angular.module('ml-app');
    app.controller('mlModuleTuneCtrl', ['$scope', '$log', '$location', '$modalInstance', 'mlModuleService', 'moduleId', controller]);
}());
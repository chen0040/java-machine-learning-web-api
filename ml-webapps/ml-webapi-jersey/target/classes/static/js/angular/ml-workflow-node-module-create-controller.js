(function(){
    var controller = function($scope, $log, $modalInstance, modules){
        $scope.modules = modules;


        $scope.ok = function(){
            $modalInstance.close($scope.node);
        };

        $scope.onCancel = function(){
            $modalInstance.dismiss('cancel');
        };

        $scope.node = {};
        $scope.node.nodeName = 'Algorithm Name';
        $scope.node.nodeWidth = 120;
        $scope.node.nodeModelId = '';

        $scope.onModuleSelectionChanged = function(){
            var selectedIndex = -1;
            for(var i=0; i < $scope.modules.length; ++i){
                if($scope.modules[i].id == $scope.node.nodeModelId){
                    selectedIndex = i;
                    break;
                }
            }
            if(selectedIndex != -1){
                $scope.node.nodeName = $scope.modules[selectedIndex].name;
            }
        };

        if($scope.modules != null && $scope.modules.length > 0){
            $scope.node.nodeModelId = $scope.modules[0].id;
            $scope.node.nodeName = $scope.modules[0].name;
        }
    };

    var app = angular.module('ml-app');
    app.controller('mlWorkflowNodeModuleCreateCtrl', ['$scope', '$log', '$modalInstance', 'modules', controller]);


}());
(function(){
    var controller = function($scope, $log, $modalInstance, batches){
        $scope.batches = batches;


        $scope.ok = function(){
            $modalInstance.close($scope.node);
        };

        $scope.onCancel = function(){
            $modalInstance.dismiss('cancel');
        };

        $scope.node = {};
        $scope.node.nodeName = 'Data Module Name';
        $scope.node.nodeWidth = 120;
        $scope.node.nodeModelId = '';

        $scope.onModuleSelectionChanged = function(){
            var selectedIndex = -1;
            for(var i=0; i < $scope.batches.length; ++i){
                if($scope.batches[i].id == $scope.node.nodeModelId){
                    selectedIndex = i;
                    break;
                }
            }
            if(selectedIndex != -1){
                $scope.node.nodeName = $scope.batches[selectedIndex].name;
            }
        };

        if($scope.batches != null && $scope.batches.length > 0){
            $scope.node.nodeModelId = $scope.batches[0].id;
            $scope.node.nodeName = $scope.batches[0].name;
        }
    };

    var app = angular.module('ml-app');
    app.controller('mlWorkflowNodeBatchCreateCtrl', ['$scope', '$log', '$modalInstance', 'batches', controller]);


}());
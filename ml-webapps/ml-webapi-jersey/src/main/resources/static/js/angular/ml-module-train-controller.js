(function(){
    var controller = function($scope, $log, $location, $modalInstance, selectedModule, batches, project, mlModuleService, mlBatchService){


        var onTrainModule = function(response){
            var trainedModule = response.data;
             $modalInstance.close(trainedModule);
        };

        $scope.cancel = function(){
            $modalInstance.dismiss('cancel');
        };

        $scope.trainModule = function(){
            mlModuleService.trainModule($scope.module.id, $scope.batch.id).then(onTrainModule);
        };

        $scope.module = selectedModule;
        $scope.batches = batches;
        $scope.project = project;


        var getBatches = function(){
            mlBatchService.listBatches(project.id).then(function(response){
                $scope.batches = response.data;
                $scope.batch = $scope.batches[0];
            });
        };

        if($scope.batches == null){
            getBatches();
        } else{
            $scope.batch = $scope.batches[0];
        }
    };
    var app = angular.module('ml-app');
    app.controller('mlModuleTrainCtrl', ['$scope', '$log', '$location', '$modalInstance', 'selectedModule', 'batches', 'project', 'mlModuleService', 'mlBatchService', controller]);
}());

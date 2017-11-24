(function(){
    var controller = function($scope, $log, $location, $modalInstance, $modal, selectedBatch, modules, project, mlModuleService){


        var onPredictLabels = function(response){
            var predictedBatch = response.data;
             //$location.path('/projects/'+$scope.project.id);
             $modalInstance.close(predictedBatch);
        };

        function filter(modules2){
            var accepted_modules = [];
            for(var i=0; i < modules2.length; ++i){
                if($scope.isActive(modules2[i])){
                    accepted_modules.push(modules2[i]);
                }
            }

            return accepted_modules;
        }

        $scope.isActive = function(m){
            return m.modelSource.id != null && m.modelSource.id != undefined;
        };

        $scope.cancel = function(){
            //$location.path('/projects/'+$scope.project.id);
            $modalInstance.dismiss('cancel');
        };

        $scope.predictLabels = function(){
            mlModuleService.runModule($scope.module.id, $scope.batch.id).then(onPredictLabels);
        };

        $scope.tuneModule = function(){
            var moduleId2 = $scope.module.id;

            var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/module.tune.html',
              controller: 'mlModuleTuneCtrl',
              resolve: {
                moduleId: function () {
                  return moduleId2;
                }
              }
            });

            modalInstance.result.then(function (updatedModule) {
                alert('Algorithm Updated!');
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };

        $scope.trainModule = function(){
            var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/module.train.html',
              controller: 'mlModuleTrainCtrl',
              resolve: {
                selectedModule: function () {
                  return $scope.module;
                },
                batches: function() {
                  return null;
                },
                project: function() {
                  return $scope.project;
                }
              }
            });

            modalInstance.result.then(function (trainedModule) {
                if(trainedModule != undefined && trainedModule != null){
                    $scope.module.modelSource.id = trainedModule.modelSource.id;
                    $log.log('modelSource.id: '+$scope.module.modelSource.id);
                    alert("training completed!");

                }
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };

        $scope.modules = modules; //filter(modules);
        $scope.batch = selectedBatch;
        $scope.project = project;
        $scope.module = $scope.modules[0];
    };
    var app = angular.module('ml-app');
    app.controller('mlBatchPredictLabelsCtrl', ['$scope', '$log', '$location', '$modalInstance', '$modal', 'selectedBatch', 'modules', 'project', 'mlModuleService', controller]);
}());

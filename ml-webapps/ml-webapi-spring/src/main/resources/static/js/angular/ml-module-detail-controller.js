(function(){
    var controller = function($scope, $log, $location, $routeParams, $modal, mlTextService, mlProjectService, mlBatchService, mlModuleService){
        $scope.moduleId = $routeParams.moduleId;

        var onGetModule = function(response){
            $scope.module = response.data;
            $scope.attributes = [];
            for(var attrname in $scope.module.attributes){
                var attr = {};
                attr.name = attrname;
                attr.value = $scope.module.attributes[attrname];
                $scope.attributes.push(attr);
            }
            getProject($scope.module.projectId);
        };

        var onGetProject = function(response){
            $scope.project = response.data;
        };

        var getProject = function(projectId){
            mlProjectService.getProject(projectId).then(onGetProject);
            mlBatchService.listBatches(projectId).then(function(response){
                $scope.batches = response.data;
            });
        };

        var getModule = function(){
            mlModuleService.getModule($scope.moduleId).then(onGetModule);
        };

        $scope.isTitleEditing = function(){
            return $scope.titleEditingOn;
        };

        $scope.editTitle = function(){
            $scope.titleEditingOn = true;
        };

        $scope.cancelEditingTitle = function(){
            $scope.titleEditingOn = false;
        };

        $scope.commitEditingTitle = function(){
            $scope.titleEditingOn = false;
            mlModuleService.updateModuleShell($scope.module);
        };

         var onDeleteModule = function(response){
            $location.path('/projects/'+$scope.module.projectId);
        };

        $scope.deleteModule = function(){
            mlModuleService.deleteModule($scope.module.id).then(onDeleteModule);
        }

        $scope.beautifyString = function(text){
            return mlTextService.beautifyString(text);
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
                  return $scope.batches;
                },
                project: function() {
                  return $scope.project;
                }
              }
            });

            modalInstance.result.then(function (trainedModule) {
                if(trainedModule != undefined && trainedModule != null){
                    $scope.module = trainedModule;
                }
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };

        $scope.saveModule = function(){
            for(var i = 0; i < $scope.attributes.length; ++i){
                var attr = $scope.attributes[i];
                $scope.module.attributes[attr.name] = attr.value;
                //$log.info(attr.name+'='+attr.value);
            }
            mlModuleService.updateModuleShell($scope.module).then(function(){
                alert("Updated!");
            });
        };

        $scope.titleEditingOn = false;
        getModule();
    };

    var app = angular.module('ml-app');
    app.controller('mlModuleDetailCtrl', ['$scope', '$log', '$location', '$routeParams', '$modal', 'mlTextService', 'mlProjectService', 'mlBatchService', 'mlModuleService', controller]);
}());

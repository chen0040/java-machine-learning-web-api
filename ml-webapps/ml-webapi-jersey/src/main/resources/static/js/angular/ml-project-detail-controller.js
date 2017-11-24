(function(){
    var controller = function($scope, $routeParams, $log, $location, $modal, mlTextService, mlProjectService, mlModuleService, mlBatchService) {
        $scope.projectId = $routeParams.projectId;

        var onGetProject = function(response){
            $scope.project = response.data;
            listModules($scope.projectId);
            listBatches($scope.projectId);
        };

        var getProject = function(projectId){
            mlProjectService.getProject(projectId).then(onGetProject);
        };

        var onListModules = function(response){
            $scope.modules = response.data;
            for(var i =0; i < $scope.modules.length; ++i){
                var moduleId = $scope.modules[i].id;
                var moduleTitle = $scope.modules[i].title;
                $scope.modulenames[moduleId] = moduleTitle;
            }
        };

        var listModules = function(projectId){
            mlModuleService.listModules(projectId).then(onListModules);
        };

        var onListBatches = function(response){
            $scope.batches = response.data;
            for(var i =0; i < $scope.batches.length; ++i){
                var batchId = $scope.batches[i].id;
                var batchTitle = $scope.batches[i].title;
                $scope.batchnames[batchId] = batchTitle;
            }
        };

        var listBatches = function(projectId){
            mlBatchService.listBatches(projectId).then(onListBatches);
        };

        $scope.editTitle = function(){
            $scope.titleEditingOn = true;
        };

        $scope.commitEditingTitle = function(){
            $scope.titleEditingOn = false;
            mlProjectService.updateProject($scope.project);
        };

        $scope.cancelEditingTitle = function(){
            $scope.titleEditingOn = false;
        };

        $scope.isTitleEditing = function(){
            return $scope.titleEditingOn;
        };

        $scope.editDescription = function(){
            $scope.descriptionEditingOn = true;
        };

        $scope.commitEditingDescription = function(){
            $scope.descriptionEditingOn = false;
            mlProjectService.updateProject($scope.project);
        };

        $scope.cancelEditingDescription = function(){
            $scope.descriptionEditingOn = false;
        };

        $scope.isDescriptionEditing = function(){
            return $scope.descriptionEditingOn;
        };

        $scope.editModuleTitle = function(module){
            $scope.moduleWithTitleEditing = module;
        };

        $scope.commitEditingModuleTitle = function(module){
            $scope.moduleWithTitleEditing = null;
            $scope.modulenames[module.id] = module.title;
            mlModuleService.updateModuleShell(module);
        };

        $scope.cancelEditingModuleTitle = function(module){
            $scope.moduleWithTitleEditing = null;
        };

        $scope.isModuleTitleEditing = function(module){
            return $scope.moduleWithTitleEditing == module;
        };

        $scope.beautifyString = function(text){
            return mlTextService.beautifyString(text);
        };

        $scope.createModule = function(){

            var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/module.create.html',
              controller: 'mlModuleCreateCtrl',
              size: 'lg',
              resolve: {
                project: function () {
                  return $scope.project;
                }
              }
            });

            modalInstance.result.then(function (selectedItem) {
                if(selectedItem != undefined && selectedItem != null){
                    $scope.modules.push(selectedItem);
                }
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };

        var onDeleteModule = function(response){
            var deletedModule = response.data;
            var deletedModuleIndex = -1;
            for(var i=0; i < $scope.modules.length; ++i){
                if($scope.modules[i].id == deletedModule.id){
                    deletedModuleIndex = i;
                    break;
                }
            }

            if(deletedModuleIndex != -1){
                if($scope.moduleWithDescriptionEditing != null && $scope.moduleWithDescriptionEditing.id == deletedModule.id){
                    $scope.moduleWithDescriptionEditing = null;
                }
                if($scope.moduleWithTitleEditing != null && $scope.moduleWithTitleEditing.id == deletedModule.id){
                    $scope.moduleWithTitleEditing = null;
                }

                $scope.modules.splice(deletedModuleIndex, 1);
            }
        };

        $scope.deleteModule = function(module){
            mlModuleService.deleteModule(module.id).then(onDeleteModule);
        }

        $scope.viewModuleDetail = function(module){
            $location.path('/modules/'+module.id);
        }
        
        /////////////////////////////////////
        
        $scope.editBatchTitle = function(batch){
            $scope.batchWithTitleEditing = batch;
        };

        $scope.commitEditingBatchTitle = function(batch){
            $scope.batchWithTitleEditing = null;
            $scope.batchnames[batch.id] = batch.title;
            mlBatchService.updateBatchShell(batch);
        };

        $scope.cancelEditingBatchTitle = function(batch){
            $scope.batchWithTitleEditing = null;
        };

        $scope.isBatchTitleEditing = function(batch){
            return $scope.batchWithTitleEditing == batch;
        };

        $scope.editBatchDescription = function(batch){
            $scope.batchWithDescriptionEditing = batch;
        };

        $scope.commitEditingBatchDescription = function(batch){
            $scope.batchWithDescriptionEditing = null;
            $scope.batchnames[batch.id] = batch.title;
            mlBatchService.updateBatchShell(batch);
        };

        $scope.cancelEditingBatchDescription = function(batch){
            $scope.batchWithDescriptionEditing = null;
        };

        $scope.isBatchDescriptionEditing = function(batch){
            return $scope.batchWithDescriptionEditing == batch;
        };

        $scope.createBatchES = function(){
            var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/batch.create.es.html',
              controller: 'mlBatchCreateESCtrl',
              size: 'lg'
            });

            modalInstance.result.then(function (f) {
                var url = f.url+'?size='+f.size;
                var timeWindowSize = f.timeWindowSize;
                var keywords = f.keywords;
                mlBatchService.createBatchES(url, $scope.projectId, keywords, timeWindowSize).then(function(response){
                    $scope.batches.push(response.data);
                });
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };

        $scope.createBatch = function(){

            var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/batch.create.html',
              controller: 'mlBatchCreateCtrl',
              size: 'lg',
              resolve: {
                projectId: function () {
                  return $scope.projectId;
                }
              }
            });

            modalInstance.result.then(function (selectedItem) {
                if(selectedItem != undefined && selectedItem != null){
                    $scope.batches.push(selectedItem);
                }
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };

        var onDeleteBatch = function(response){
            var deletedBatch = response.data;
            var deletedBatchIndex = -1;
            for(var i=0; i < $scope.batches.length; ++i){
                if($scope.batches[i].id == deletedBatch.id){
                    deletedBatchIndex = i;
                    break;
                }
            }

            if(deletedBatchIndex != -1){
                if($scope.batchWithDescriptionEditing != null && $scope.batchWithDescriptionEditing.id == deletedBatch.id){
                    $scope.batchWithDescriptionEditing = null;
                }
                if($scope.batchWithTitleEditing != null && $scope.batchWithTitleEditing.id == deletedBatch.id){
                    $scope.batchWithTitleEditing = null;
                }

                $scope.batches.splice(deletedBatchIndex, 1);
            }
        };

        $scope.deleteBatch = function(batch){
            mlBatchService.deleteBatch(batch.id).then(onDeleteBatch);
        }

        $scope.viewBatchDetail = function(batch){
            $location.path('/batches/'+batch.id);
        }
        
        ///////////////////////



        $scope.trainModule = function(module){
            var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/module.train.html',
              controller: 'mlModuleTrainCtrl',
              resolve: {
                selectedModule: function () {
                  return module;
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
                    var trainedIndex = -1;
                    for(var i=0; i < $scope.modules.length; ++i){
                        if($scope.modules[i].id == trainedModule.id){
                            trainedIndex = i;
                            break;
                        }
                    }
                    if(trainedIndex != -1){
                        $scope.modules[trainedIndex] = trainedModule;
                        //alert("Training complete for " + trainedModule.title);
                    }

                }
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };

        var onPredictLabels = function(response){
            var updatedBatch = response.data;
            var updatedIndex = -1;
            for(var i = 0; i < $scope.batches.length; ++i){
                if($scope.batches[i].id == updatedBatch.id){
                    updatedIndex = i;
                    break;
                }
            }

            if(updatedIndex != -1){
                $scope.batches[updatedIndex] = updatedBatch;
            }
            alert("Classification completed");
        };

        $scope.predictLabels = function(batch){
            var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/batch.predict.labels.html',
              controller: 'mlBatchPredictLabelsCtrl',
              resolve: {
                selectedBatch: function () {
                  return batch;
                },
                modules: function() {
                  return $scope.modules;
                },
                project: function() {
                  return $scope.project;
                }
              }
            });

            modalInstance.result.then(function (predictedBatch) {
                if(predictedBatch != undefined && predictedBatch != null){
                    var predictedIndex = -1;
                    for(var i=0; i < $scope.batches.length; ++i){
                        if($scope.batches[i].id == predictedBatch.id){
                            predictedIndex = i;
                            break;
                        }
                    }
                    if(predictedIndex != -1){
                        $scope.batches[predictedIndex] = predictedBatch;
                        //alert("Prediction complete for " + predictedBatch.title);
                    }

                }
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };

        $scope.titleEditingOn = false;
        $scope.descriptionEditingOn = false;
        $scope.moduleWithTitleEditing = null;
        $scope.moduleWithDescriptionEditing = null;
        $scope.batchWithTitleEditing = null;
        $scope.batchWithDescriptionEditing = null;
        $scope.batchnames = [];
        $scope.modulenames = [];
        getProject($scope.projectId);
    };

    var app = angular.module('ml-app');
    app.controller('mlProjectDetailCtrl', ['$scope', '$routeParams', '$log', '$location', '$modal', 'mlTextService', 'mlProjectService', 'mlModuleService', 'mlBatchService', controller]);
}());

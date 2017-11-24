(function(){
    var controller = function($scope, $q, $log, $location, $modal, $routeParams, mlRandomService, mlNvd3Service, mlProjectService, mlBatchService, mlBatchChartService, mlModuleService){
        $scope.batchId = $routeParams.batchId;
        $scope.batchPage = null;

        $scope.f = {};
        $scope.f.colStart = 1;
        $scope.f.colEnd = 7;
        $scope.f.displayCols = [];

        var onGetBatch = function(response){
            updateBatchInfo(response.data);
            getProject($scope.batch.projectId);
        };

        var updateBatchInfo = function(batch){
            $scope.batch = batch;
            var tuples = $scope.batch.tuples;
            $scope.cols = [];
            if(tuples.length > 0){
                var tuple = tuples[0];
                var attributeNames = null;
                if($scope.batch.attributeLevelSource){
                    attributeNames = $scope.batch.attributeLevelSource.attributeNames;
                }
                $scope.attributeNames = {};
                for(var key in tuple.model){
                    var key2 = key;
                    if(attributeNames != null && attributeNames != undefined && key < attributeNames.length){
                        key2 = attributeNames[key];
                    }
                    $scope.attributeNames[key] = key2;
                    $scope.cols.push(key);
                }
                if($scope.cols.length > 0){
                    $scope.sequenceResult.col = $scope.cols[0];
                }
            }

            var deferred = $q.defer();
            var promise = deferred.promise;

            promise.then(function(){
                updateClassifierChart($scope.batch);
            }).then(function(){
                updateSequenceChart($scope.batch);
            }).then(function(){
                if($scope.cols.length > 1){
                    $scope.clusteringResult1.xAxis = $scope.cols[mlRandomService.nextInt($scope.cols.length)];
                    $scope.clusteringResult1.yAxis = $scope.cols[mlRandomService.nextInt($scope.cols.length)];

                    $scope.clusteringResult2.xAxis = $scope.cols[mlRandomService.nextInt($scope.cols.length)];
                    $scope.clusteringResult2.yAxis = $scope.cols[mlRandomService.nextInt($scope.cols.length)];

                    updateClusteringChart($scope.batch, $scope.clusteringResult1.xAxis, $scope.clusteringResult1.yAxis, $scope.clusteringResult1);
                    updateClusteringChart($scope.batch, $scope.clusteringResult2.xAxis, $scope.clusteringResult2.yAxis, $scope.clusteringResult2);
                }
            });

            updateDisplayCols();
            updatePage();

            deferred.resolve();
        };

        var updatePage = function(){
            $scope.pageInfo.batchSize = $scope.batch.tuples.length;
            $scope.pageInfo.updatePageSize();
            $scope.pageInfo.updatePage($scope.batch, function(batchPage){
                $scope.batchPage = batchPage;
            });
        };

        var updateDisplayCols = function(){
            $scope.f.displayCols = [];
            $log.log($scope.f.colStart);
            $log.log($scope.f.colEnd);
            for(var i = $scope.f.colStart; i <= $scope.f.colEnd; ++i){
                if(i >= 1 && i <= $scope.cols.length){
                    $scope.f.displayCols.push($scope.cols[i-1]);
                }
            }
        }

        $scope.updateColumns = function(){
            updateDisplayCols();
            updatePage();
        };

        var updateSequenceChart = function(batch){
            mlBatchChartService.getSequenceChart(batch, $scope.sequenceResult.col, function(data){
                $scope.sequenceResult.data1 = data;
            });
            mlBatchChartService.getPredictedLabelSequenceChart(batch, function(data){
                 $scope.sequenceResult.data2 = data;
            });
            mlBatchChartService.getLabelSequenceChart(batch, function(data){
                 $scope.sequenceResult.data3 = data;
            });
        };

        var updateClusteringChart = function(batch, col1, col2, clusteringResult) {
           clusteringResult.options.chart.xAxis.axisLabel = col1;
           clusteringResult.options.chart.yAxis.axisLabel = col2;
           mlBatchChartService.getClusteringChart(batch, col1, col2, function(data){
               clusteringResult.data = data;
           });
       }

        var updateClassifierChart = function(batch){
            mlBatchChartService.getClassifierChart(batch, function(classifierResult){
                $scope.classifierResult.data1 = classifierResult.data1;
                $scope.classifierResult.data2 = classifierResult.data2;
                $scope.classifierResult.data3 = classifierResult.data3;

                $scope.classifierResult.correctness_count = classifierResult.correctness_count;
                $scope.classifierResult.incorrectness_count = classifierResult.incorrectness_count;
            });
        };


        var onGetProject = function(response){
            $scope.project = response.data;
            mlModuleService.listModules($scope.project.id).then(function(response){
                $scope.modules = response.data;
            });
        };

        var getProject = function(projectId){
            mlProjectService.getProject(projectId).then(onGetProject);
        };

        var getBatch = function(){
            mlBatchService.getBatch($scope.batchId).then(onGetBatch);
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
            mlBatchService.updateBatchShell($scope.batch);
        };

        $scope.isDescriptionEditing = function(){
            return $scope.descriptionEditingOn;
        };

        $scope.editDescription = function(){
            $scope.descriptionEditingOn = true;
        };

        $scope.cancelEditingDescription = function(){
            $scope.descriptionEditingOn = false;
        };

        $scope.commitEditingDescription = function(){
            $scope.descriptionEditingOn = false;
            mlBatchService.updateBatchShell($scope.batch);
        };

        $scope.titleEditingOn = false;
        $scope.descriptionEditingOn = false;

        $scope.pageInfo = new BatchPaginationModel();

        $scope.pageChanged = function() {
            $log.log('Page changed to: ' + $scope.pageInfo.currentPage);
            $scope.pageInfo.updatePage($scope.batch, function(batchPage){
                 $scope.batchPage = batchPage;
            });
        };

        $scope.pageSizeChanged = function(){
            $scope.pageInfo.updatePageSize();
            $scope.pageInfo.updatePage($scope.batch, function(batchPage){
                 $scope.batchPage = batchPage;
            });
        };

        var onDeleteBatch = function(response){
            $location.path('/projects/'+$scope.batch.projectId);
        };

        $scope.deleteBatch = function(){
            mlBatchService.deleteBatch($scope.batch.id).then(onDeleteBatch);
        }

        $scope.predictLabels = function(){
            var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/batch.predict.labels.html',
              controller: 'mlBatchPredictLabelsCtrl',
              resolve: {
                selectedBatch: function () {
                  return $scope.batch;
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
                    updateBatchInfo(predictedBatch);
                }
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };


        $scope.getDoc = function(index){

            if(index < $scope.batch.docBag.length){
                var selectedDoc = $scope.batch.docBag[index];
                var modalInstance = $modal.open({
                  animation: true,
                  templateUrl: 'partials/doc.detail.html',
                  controller: 'mlDocDetailCtrl',
                  resolve: {
                    doc: function () {
                      return selectedDoc;
                    }
                  }
                });

                modalInstance.result.then(function (predictedBatch) {

                }, function () {
                  $log.info('Modal dismissed at: ' + new Date());
                });

            }

        };

        $scope.updateTupleLabel = function(tuple){
            var oldLabel = tuple.labelOutput;
            if(oldLabel == null) oldLabel == "";
            var newLabel = prompt('Update the label of the tuple here:', oldLabel);
            if(newLabel != oldLabel){
                tuple.labelOutput = newLabel;
                //$log.log(tuple);
                mlBatchService.updateTuple(tuple, $scope.batchId).then(function(response){
                    //var tuple = response.data;
                    //$log.log(tuple);
                    updatePage();
                });
            }
        };

        $scope.classifierResult = {};
        $scope.classifierResult.data1 = null;
        $scope.classifierResult.data2 = null;
        $scope.classifierResult.data3 = null;
        $scope.classifierResult.data4 = null;
        $scope.classifierResult.options1 = mlNvd3Service.getOptionsForPie(500, 400, true);
        $scope.classifierResult.options2 = mlNvd3Service.getOptionsForDiscreteBar(500, 400, 'Class Labels', 'Count');
        $scope.classifierResult.options3 = mlNvd3Service.getOptionsForDiscreteBar(500, 400, 'Predicted Class Labels', 'Count');
        $scope.classifierResult.options4 = mlNvd3Service.getOptionsForDiscreteBar(500, 400, 'Predicted Class Labels', 'Count');


        $scope.clusteringResult1 = {};
        $scope.clusteringResult1.data = null;
        $scope.clusteringResult1.options = mlNvd3Service.getOptionsForScatter(500, 400, 'X', 'Y');
        $scope.clusteringResult2 = {};
        $scope.clusteringResult2.data = null;
        $scope.clusteringResult2.options = mlNvd3Service.getOptionsForScatter(500, 400, 'X', 'Y');

        $scope.onXAxisClusteringResultChanged = function(clusteringResult){
            if($scope.cols.length > 1){
                updateClusteringChart($scope.batch, clusteringResult.xAxis, clusteringResult.yAxis, clusteringResult);
            }
        };
        $scope.onYAxisClusteringResultChanged = function(clusteringResult){
            if($scope.cols.length > 1){
                updateClusteringChart($scope.batch, clusteringResult.xAxis, clusteringResult.yAxis, clusteringResult);
            }
        };
        $scope.onSequenceResultAxisChanged = function(sequenceResult){
            mlBatchChartService.getSequenceChart($scope.batch, sequenceResult.col, function(data){
                $scope.sequenceResult.data1 = data;
            });
        };

        $scope.sequenceResult = {};
        $scope.sequenceResult.data1 = null;
        $scope.sequenceResult.data2 = null;
        $scope.sequenceResult.data3 = null;

        $scope.sequenceResult.options1 = mlNvd3Service.getOptionsForLineWithFocus(500, 400, 'Index', 'Values', 'Data Analysis');
        $scope.sequenceResult.options2 = mlNvd3Service.getOptionsForLineWithFocus(500, 400, 'Index', 'Values', 'Predicted Label Analysis');
        $scope.sequenceResult.options3 = mlNvd3Service.getOptionsForLineWithFocus(500, 400, 'Index', 'Values', 'Label Analysis');

        $scope.getArray = function(){
            return mlBatchService.getArray($scope.batch);
        };

        $scope.getHeader = function(){
           return mlBatchService.getHeader($scope.batch);
        };

        getBatch();
        $scope.pageInfo.updatePageSize();
    };

    var app = angular.module('ml-app');
    app.controller('mlBatchDetailCtrl', ['$scope', '$q', '$log', '$location', '$modal', '$routeParams', 'mlRandomService', 'mlNvd3Service', 'mlProjectService', 'mlBatchService', 'mlBatchChartService', 'mlModuleService', controller]);
}());

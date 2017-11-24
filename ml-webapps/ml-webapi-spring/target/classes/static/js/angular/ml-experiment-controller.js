(function(){
    var controller = function($scope, $log, $location, $modal, mlProjectService, mlNvd3Service){
        var onFindAllOverviews = function(response){
            $scope.projects = response.data;
            if($scope.projects.length > 0){
                $scope.project = $scope.projects[0];
                $scope.project.testing = $scope.project.testingModes[0];
                getDetail($scope.project);
            }

        };

        var onFindDetail = function(response){
            var proj = response.data;
            $scope.modules = proj.modules;
            $scope.batches = proj.batches;

            for(var m = 0; m < $scope.modules.length; ++m){
                $scope.modules[m].selected = true;
            }
            for(var b = 0; b < $scope.batches.length; ++b){
                $scope.batches[b].selected = true;
            }

        };

        var getOverviews = function(){
            mlProjectService.findAllOverviews().then(onFindAllOverviews);
        };

        var getDetail = function(proj){
            mlProjectService.findOverview(proj.id).then(onFindDetail);
        };

        $scope.onProjectSelectionChanged = function(){
            getDetail($scope.project);
        };

        $scope.isFoldCountFieldVisible = function(){
            return $scope.project && $scope.project.testing && $scope.project.testing.mode == 'CrossValidation';
        };

        $scope.isLeaveOutCountFieldVisible = function(){
            return $scope.project && $scope.project.testing && $scope.project.testing.mode == 'CrossValidation';
        };

        $scope.isTrainingPortionFieldVisible = function(){
            return $scope.project && $scope.project.testing && $scope.project.testing.mode == 'ByTrainingPortion';
        };

        $scope.trainAndEvaluate = function(){
            $scope.selectedModules = [];
            $scope.selectedBatches = [];
            for(var i =0; i < $scope.modules.length; ++i){
                if($scope.modules[i].selected){
                    $scope.selectedModules.push($scope.modules[i]);
                }
            }
            for(var i =0; i < $scope.batches.length; ++i){
                if($scope.batches[i].selected){
                    $scope.selectedBatches.push($scope.batches[i]);
                }
            }

            if($scope.selectedModules.length==0 || $scope.selectedBatches.length==0){
                alert('please select at least one algorithm and at least one data source');
                return;
            }

            $scope.clusteringResults = [];
            $scope.classifierResults = [];
            $scope.anomalyResults = [];
            $scope.testCases = {};
            $scope.testCases.totalCount = $scope.selectedModules.length * $scope.selectedBatches.length;
            $scope.testCases.currentCount = 0;
            $scope.testing_progress = 0;
            for(var i=0; i < $scope.selectedModules.length; ++i){
                var selectedModule = $scope.selectedModules[i];
                $scope.testCases[selectedModule.id] = {};

                for(var j=0; j < $scope.selectedBatches.length; ++j){
                    var testCase = {};
                    var selectedBatch = $scope.selectedBatches[j];
                    testCase.moduleId = selectedModule.id;
                    testCase.batchId = selectedBatch.id;
                    testCase.testingModel = $scope.project.testing;
                    testCase.moduleName = selectedModule.name;
                    testCase.batchName = selectedBatch.name;

                    $scope.testCases[selectedModule.id][selectedBatch.id] = testCase;

                    mlProjectService.runTestCase(testCase).then(function(response){
                        var testCaseResult = response.data;
                        $log.info(testCaseResult);
                        $scope.testCases[testCaseResult.moduleId][testCaseResult.batchId].predictionAccuracy = testCaseResult.predictionAccuracy;
                        $scope.testCases.currentCount++;
                        $scope.testing_progress = parseInt(Math.floor($scope.testCases.currentCount * 100 / $scope.testCases.totalCount));

                        if(testCaseResult.moduleType == 'Clustering'){
                            $scope.clusteringResults.push(testCaseResult);
                        } else if(testCaseResult.moduleType == 'Classifier' || testCaseResult.moduleType == 'BinaryClassifier'){
                            $scope.classifierResults.push(testCaseResult);
                        } else if(testCaseResult.moduleType == 'AnomalyDetection'){
                            $scope.anomalyResults.push(testCaseResult);
                        }
                        if($scope.testCases.currentCount >= $scope.testCases.totalCount){
                            $scope.testing_progress = 0;

                            updateClassifierResultChart();
                            updateClusteringResultChart();
                            updateAnomalyResultChart();
                        }
                    });
                }
            }
        };
        
        var updateClassifierResultChart = function(){
            if($scope.classifierResults.length > 0){
                var byBatch = {};
                var dataKeys = [];

                for(var i=0; i < $scope.classifierResults.length; ++i){
                    var cr = $scope.classifierResults[i];
                    if(!(cr.batchName in byBatch)){
                        byBatch[cr.batchName] = [];
                        dataKeys.push(cr.batchName);
                    }

                    byBatch[cr.batchName].push({'label': cr.moduleName, 'value': cr.attributes.predictionAccuracy});
                }

                $scope.classifierResultChart.dataStore = byBatch;
                $scope.classifierResultChart.dataKeys = dataKeys;

                if(dataKeys.length > 0){
                    $scope.classifierResultChart.dataKey = dataKeys[0];
                    $scope.classifierResultChart.data = [ {
                        'key': $scope.classifierResultChart.dataKey,
                        'values': $scope.classifierResultChart.dataStore[$scope.classifierResultChart.dataKey] } ];
                }
            }
        };
        
        var updateClusteringResultChart = function(){
            if($scope.clusteringResults.length > 0){
                var byBatch = {};
                var dataKeys = [];

                for(var i=0; i < $scope.clusteringResults.length; ++i){
                    var cr = $scope.clusteringResults[i];
                    if(!(cr.batchName in byBatch)){
                        byBatch[cr.batchName] = [];
                        dataKeys.push(cr.batchName);
                    }

                    byBatch[cr.batchName].push({'label': cr.moduleName, 'value': cr.attributes.DBI});
                }

                $scope.clusteringResultChart.dataStore = byBatch;
                $scope.clusteringResultChart.dataKeys = dataKeys;

                if(dataKeys.length > 0){
                    $scope.clusteringResultChart.dataKey = dataKeys[0];
                    $scope.clusteringResultChart.data = [ {
                        'key': $scope.clusteringResultChart.dataKey,
                        'values': $scope.clusteringResultChart.dataStore[$scope.clusteringResultChart.dataKey] } ];
                }
            }
        };
        
        var updateAnomalyResultChart = function(){
            if($scope.anomalyResults.length > 0){
                var byBatch = {};
                var dataKeys = [];

                for(var i=0; i < $scope.anomalyResults.length; ++i){
                    var cr = $scope.anomalyResults[i];
                    if(!(cr.batchName in byBatch)){
                        byBatch[cr.batchName] = [];
                        dataKeys.push(cr.batchName);
                    }

                    byBatch[cr.batchName].push({'label': cr.moduleName, 'value': cr.attributes.anomalyRatio});
                }

                $scope.anomalyResultChart.dataStore = byBatch;
                $scope.anomalyResultChart.dataKeys = dataKeys;

                if(dataKeys.length > 0){
                    $scope.anomalyResultChart.dataKey = dataKeys[0];
                    $scope.anomalyResultChart.data = [ {
                        'key': $scope.anomalyResultChart.dataKey,
                        'values': $scope.anomalyResultChart.dataStore[$scope.anomalyResultChart.dataKey] } ];
                }
            }
        };

        $scope.onClassifierResultChartDataKeyChanged = function(){
            $scope.classifierResultChart.data = [ {
                'key': $scope.classifierResultChart.dataKey,
                'values': $scope.classifierResultChart.dataStore[$scope.classifierResultChart.dataKey] } ];
        };

        $scope.onClusteringResultChartDataKeyChanged = function(){
            $scope.clusteringResultChart.data = [ {
                'key': $scope.clusteringResultChart.dataKey,
                'values': $scope.clusteringResultChart.dataStore[$scope.clusteringResultChart.dataKey] } ];
        };

        $scope.onAnomalyResultChartDataKeyChanged = function(){
            $scope.anomalyResultChart.data = [ {
                'key': $scope.anomalyResultChart.dataKey,
                'values': $scope.anomalyResultChart.dataStore[$scope.anomalyResultChart.dataKey] } ];
        };

        $scope.onModuleClicked = function(moduleId2){
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

        $scope.classifierResultChart = {};
        $scope.classifierResultChart.options = mlNvd3Service.getOptionsForDiscreteBar(500, 400, 'Algorithm', 'Prediction Accuracy');
        $scope.clusteringResultChart = {};
        $scope.clusteringResultChart.options = mlNvd3Service.getOptionsForDiscreteBar(500, 400, 'Algorithm', 'DBI');
        $scope.anomalyResultChart = {};
        $scope.anomalyResultChart.options = mlNvd3Service.getOptionsForDiscreteBar(500, 400, 'Algorithm', 'Anomaly Ratio');

        $scope.testing_progress = 0;
        getOverviews();
    };


    var app = angular.module('ml-app');
    app.controller('mlExperimentCtrl', ['$scope', '$log', '$location', '$modal', 'mlProjectService', 'mlNvd3Service', controller]);
}());

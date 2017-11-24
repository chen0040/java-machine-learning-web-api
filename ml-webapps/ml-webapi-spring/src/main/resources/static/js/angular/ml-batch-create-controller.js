(function(){
    var controller = function($scope, $log, $location, $modalInstance, mlAjaxService, mlProjectService, mlBatchService, projectId){
        $scope.projectId = projectId;

        var onGetProject = function(response){
            $scope.project = response.data;
        };

        var getProject = function(){
            mlProjectService.getProject($scope.projectId).then(onGetProject);
        };

        var onCreateBatch = function(response){
            $location.path('/projects/'+$scope.projectId);
            $modalInstance.close(response.data);
        };

        $scope.createBatch = function(){

            mlBatchService.createBatch($scope.projectId,
                $scope.f.version,
                $scope.f.has_header,
                $scope.input_data,
                $scope.title,
                $scope.description,
                $scope.f.separator,
                $scope.f.output_column_label,
                $scope.f.output_column_numeric,
                $scope.f.columns_to_add,
                $scope.f.columns_to_ignore).then(onCreateBatch);
        };

        $scope.cancel = function(){
            $location.path('/projects/'+$scope.projectId);
            $modalInstance.dismiss('cancel');
        };


        var initialize = function(){
            mlAjaxService.get('randname?count=1').then(function(response){
                var names = response.data;
                $scope.title = names[0];
                $scope.description = 'This data source '+names[0]+' is generated';
            });
            mlAjaxService.get('batches/formats').then(function(response){
                var formats = response.data;

                $scope.f.formatDescriptions = {};
                for(f_key in formats){
                    $scope.f.formats.push(f_key);
                    $scope.f.formatDescriptions[f_key] = formats[f_key];
                }

                $scope.f.formatDescription = $scope.f.formatDescriptions[$scope.f.version];

                updatePlaceHolders();
            });
        };

        $scope.onFormatSelectionChanged = function(){
            $scope.f.formatDescription = $scope.f.formatDescriptions[$scope.f.version];
            updatePlaceHolders();
        };

        $scope.VERSION_CSV_DATA_TABLE = 'CSV_DATA_TABLE';
        $scope.VERSION_CSV_HEART_SCALE = 'CSV_HEART_SCALE';
        $scope.VERSION_JSON_ELASTIC_SEARCH = 'JSON_ELASTIC_SEARCH';

        $scope.f = {};
        $scope.f.version = $scope.VERSION_CSV_HEART_SCALE;
        $scope.f.formats = [];
        $scope.f.formatDescription = '';
        $scope.f.separator = ',';

        $scope.f.has_header = false;
        $scope.f.output_column_label = '-1';
        $scope.f.output_column_numeric = '-1';
        $scope.f.columns_to_add = '';
        $scope.f.columns_to_ignore = '';

        $scope.f.isHeaderVisible = function(){
            return $scope.f.version == $scope.VERSION_CSV_DATA_TABLE;
        };
        $scope.f.isSeparatorVisible = function(){
            return $scope.f.version == $scope.VERSION_CSV_DATA_TABLE || $scope.f.version == $scope.VERSION_JSON_ELASTIC_SEARCH;
        };
        $scope.f.isOutputLabelColumnIndexVisible = function(){
            return $scope.f.version == $scope.VERSION_CSV_DATA_TABLE || $scope.f.version == $scope.VERSION_JSON_ELASTIC_SEARCH;
        };
        $scope.f.isOutputValueColumnIndexVisible = function(){
            return $scope.f.version == $scope.VERSION_CSV_DATA_TABLE;
        };
        $scope.f.isColumnsToAddVisible = function(){
            return $scope.f.version == $scope.VERSION_CSV_DATA_TABLE;
        };
        $scope.f.isColumnsToIgnoreVisible = function(){
            return $scope.f.version == $scope.VERSION_CSV_DATA_TABLE;
        };

        var updatePlaceHolders = function(){
            if($scope.f.version == $scope.VERSION_JSON_ELASTIC_SEARCH){
                $scope.placeholder3 = 'Time Window Size (milliseconds)';
                $scope.placeholder2 = 'Keywords (separated by \';\')';
                $scope.f.separator = 'error; systemd-logind';
                $scope.f.output_column_label = '900000';
            }else{
                $scope.placeholder3 = 'Output Label Column Index';
                $scope.placeholder2 = 'Field Separator';
                $scope.f.separator = ',';
                $scope.f.output_column_label = '-1';
            }
        };

        $scope.input_data = null;
        $scope.title = 'heart_scale';
        $scope.description = 'heart_scale Description';

        getProject();
        initialize();
    };

    var app = angular.module('ml-app');
    app.controller('mlBatchCreateCtrl', ['$scope', '$log', '$location', '$modalInstance', 'mlAjaxService', 'mlProjectService', 'mlBatchService', 'projectId', controller]);

}());

(function(){
    var service = function($log, mlAjaxService){
        var listBatches = function(projectId, myAppName){
        
            return mlAjaxService.get('batches/project?projectId='+projectId+'&light=1', myAppName);
        };

        var createBatchES = function(url, projectId, keywords, timeWindowSize, myAppName){
            return mlAjaxService.postJson('batches/project?projectId='+projectId, {'url':url, 'source':'URL_ES_V0', 'keywords':keywords, 'timeWindowSize':timeWindowSize}, myAppName);
        };

        var updateTuple = function(tuple, batchId, myAppName){
            return mlAjaxService.postJson('batches/tuples?batchId='+batchId, tuple, myAppName);
        };

        var createBatch = function(projectId, version, has_header, input_data, title, description, separator, output_column_label, output_column_numeric, columns_to_add, columns_to_ignore, myAppName){

            var has_header_text = '0';
            if(has_header) {
                has_header_text = '1';
            }

            if(output_column_label==undefined){
                output_column_label = '-1';
            }
            if(output_column_numeric==undefined){
                output_column_numeric = '-1';
            }

            if(columns_to_add == undefined){
                columns_to_add = '';
            }
            if(columns_to_ignore == undefined){
                columns_to_ignore = '';
            }

            var params = {
                'projectId' : projectId,
                'version' : version,
                'hasHeader': has_header_text,
                'input_data': input_data,
                'title' : title,
                'description' : description,
                'splitBy' : separator,
                'output_column_label': output_column_label,
                'output_column_numeric': output_column_numeric,
                'columns_to_add': columns_to_add,
                'columns_to_ignore': columns_to_ignore
            };

            return mlAjaxService.uploadFile("batches/csv", params, myAppName);
        };

        var getBatch = function(moduleId, myAppName){
            return mlAjaxService.get("batches/"+moduleId, myAppName);
        };

        var deleteBatch = function(moduleId, myAppName){
            return mlAjaxService.del("batches/"+moduleId, myAppName);
        };

        var updateBatchShell = function(batch, myAppName){
            batch.isShell = true;
            return mlAjaxService.postJson("batches", batch, myAppName);
        };

        var getArray = function(batch){
            var data = [];
            var tuples = batch.tuples;
            for(var i = 0; i < tuples.length; ++i){
                var tuple = tuples[i];
                var line = {};
                line.label = tuple.label;
                line.labelp = tuple.predictedLabelOutput;
                line.output = tuple.output;
                line.outputp = tuple.predictedNumericOutput;
                for(var col in $scope.cols){
                    line[col] = tuple.model[col];
                }
                data.push(line);
            }
            return data;
        };

        var getHeader = function(batch){
            var tuples = batch.tuples;
            var cols = [];
            if(tuples.length > 0){
                var tuple = tuples[0];
                for(var key in tuple.model){
                    cols.push(key);
                }
            }

             var header = [];

            for(var col in $cols){
                header.push(col);
            }

            header.push('label');
            header.push('labelp');
            header.push('output');
            header.push('outputp');
            return header;
        };

        return {
            listBatches: listBatches,
            createBatch: createBatch,
            createBatchES: createBatchES,
            getBatch: getBatch,
            deleteBatch: deleteBatch,
            updateBatchShell: updateBatchShell,
            updateTuple: updateTuple,
            getArray: getArray,
            getHeader: getHeader
        };
    };

    var app = angular.module('ml-app');
    app.factory('mlBatchService', ['$log', 'mlAjaxService', service]);
}());

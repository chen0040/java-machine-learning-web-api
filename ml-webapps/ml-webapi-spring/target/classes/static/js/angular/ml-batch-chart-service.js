(function(){
    var service = function($log){
         var getLabelSequenceChart = function(batch, callback){
            var classHistogramCountSet = {};
            var labels = [];

            var tuples = batch.tuples;
            for(var i=0; i < tuples.length; ++i){
                var label = tuples[i].labelOutput;

                if(!(label in classHistogramCountSet)){
                    classHistogramCountSet[label] = 1;
                    labels.push(label);
                }
            }

            var data = [];
            for(var i=0; i < labels.length; ++i){
                var label = labels[i];
                var series = {'key': label};
                var values = [];
                for(var j=0; j < tuples.length; ++j){
                    var tuple_label = tuples[j].labelOutput;
                    if(label == tuple_label){
                        values.push({x: j, y: 1});
                    }else{
                        values.push({x: j, y: 0});
                    }
                }
                series.values = values;
                data.push(series);
            }

            callback(data);
        };

        var getPredictedLabelSequenceChart = function(batch, callback){
            var classHistogramCountSet = {};
            var labels = [];

            var tuples = batch.tuples;
            for(var i=0; i < tuples.length; ++i){
                var label = tuples[i].predictedLabelOutput;

                if(!(label in classHistogramCountSet)){
                    classHistogramCountSet[label] = 1;
                    labels.push(label);
                }
            }

            var data = [];
            for(var i=0; i < labels.length; ++i){
                var label = labels[i];
                var series = {'key': label};
                var values = [];
                for(var j=0; j < tuples.length; ++j){
                    var tuple_label = tuples[j].predictedLabelOutput;
                    if(label == tuple_label){
                        values.push({x: j, y: 1});
                    }else{
                        values.push({x: j, y: 0});
                    }
                }
                series.values = values;
                data.push(series);
            }

            callback(data);
        };

        var getSequenceChart = function(batch, col, callback){
            var data = [];
            var cols = [];
            var tuples = batch.tuples;

            var series = { 'key': 'field-'+col };
            var values = [];
            for(var j=0; j < tuples.length; ++j) {
               var tuple = tuples[j];
               var yVal = tuple.model[col];
               if(yVal == undefined){
                   yVal = '0';
               }
               var point = { x : j,
                   y: parseFloat(yVal)
               }
               values.push(point);
           }
           series.values = values;
           data.push(series);


            callback(data);
        };

        var getClusteringChart = function(batch, col1, col2, callback){
            var data = [],
                          shapes = ['circle', 'cross', 'triangle-up', 'triangle-down', 'diamond', 'square'],
                          random = d3.random.normal();

           var tuples = batch.tuples;
           var clusters = [];
           for(var i=0; i < tuples.length; ++i){
               if(clusters.indexOf(tuples[i].predictedLabelOutput) == -1){
                    clusters.push(tuples[i].predictedLabelOutput);
               }
           }

           for (var i = 0; i < clusters.length; i++) {
               data.push({
                   key: clusters[i],
                   values: [],
                   slope: Math.random() - .01,
                   intercept: Math.random() - .5
               });

               for (var j = 0; j < tuples.length; j++) {
                   if(tuples[j].predictedLabelOutput == clusters[i]){
                        var xVal = 0;
                        var yVal = 0;

                        xVal = parseFloat(tuples[j].model[col1]);
                        yVal = parseFloat(tuples[j].model[col2]);

                        if(xVal == undefined || isNaN(xVal)){
                            xVal = 0;
                        }
                        if(yVal == undefined || isNaN(yVal)){
                            yVal = 0;
                        }

                        data[i].values.push({
                           x: xVal,
                           y: yVal,
                           size: 0.5,
                           shape: shapes[j % 6]
                       });
                   }

               }
           }

           callback(data);
        };

        var getClassifierChart = function(batch, callback){
            var correct_count = 0;
            var incorrect_count = 0;

            var classHistogramCountMap2 = {};
            var classHistogramCountMap3 = {};

            var tuples = batch.tuples;
            for(var i=0; i < tuples.length; ++i){
                var label = tuples[i].labelOutput;
                var label_p = tuples[i].predictedLabelOutput;

                if(label in classHistogramCountMap2){
                    classHistogramCountMap2[label] = classHistogramCountMap2[label] + 1;
                } else{
                    classHistogramCountMap2[label] = 1;
                }

                if(label_p in classHistogramCountMap3){
                    classHistogramCountMap3[label_p] = classHistogramCountMap3[label_p] + 1;
                } else{
                    classHistogramCountMap3[label_p] = 1;
                }

                if(label == label_p){
                    correct_count++;
                }else{
                    incorrect_count++;
                }
            }

            var classHistogramCounts2 = [];
            var classHistogramCounts3 = [];
            for(var label in classHistogramCountMap2) {
                var dataPoint = {
                    'label': label,
                    'value': classHistogramCountMap2[label]
                };
                classHistogramCounts2.push(dataPoint);
            }
            for(var label in classHistogramCountMap3) {
                var dataPoint = {
                    'label': label,
                    'value': classHistogramCountMap3[label]
                };
                classHistogramCounts3.push(dataPoint);
            }

            var classifierResult = {};
             classifierResult.data1 = [
                {
                    key: "Correct Predictions",
                    y: correct_count
                },
                {
                    key: "Incorrect Predictions",
                    y: incorrect_count
                }
            ];

            classifierResult.data2 = [{'key':'Class Labels', 'values': classHistogramCounts2}];
            classifierResult.data3 = [{'key':'Predicted Class Labels', 'values': classHistogramCounts3}];


            classifierResult.correctness_count = correct_count;
            classifierResult.incorrectness_count = incorrect_count;

            callback(classifierResult);
        };

        return {
            getClusteringChart: getClusteringChart,
            getClassifierChart: getClassifierChart,
            getSequenceChart: getSequenceChart,
            getLabelSequenceChart: getLabelSequenceChart,
            getPredictedLabelSequenceChart: getPredictedLabelSequenceChart,
        };
    };

    var app = angular.module('ml-app');
    app.factory('mlBatchChartService', ['$log', service]);
}());

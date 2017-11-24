(function(){
    var controller = function($scope, $log, $location, $modal, mlAjaxService, mlProjectService, mlNvd3Service){
        var onFindAllOverviews = function(response){
            $scope.projects = response.data;
            if($scope.projects.length > 0){
                $scope.project = $scope.projects[0];
                $scope.project.testing = $scope.project.testingModes[0];
                getDetail($scope.project);
                getWorkflow($scope.project);
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
            getWorkflow($scope.project);
        };

        var getWorkflow = function(proj){
            //
            // Setup the data-model for the chart.
            //
            $scope.chartDataModel = proj.workflow;
            //
            // Create the view-model for the chart and attach to the scope.
            //
            $scope.chartViewModel = new flowchart.ChartViewModel($scope.chartDataModel);
        };

        $scope.saveWorkflow = function(){

            mlProjectService.saveWorkflow($scope.chartDataModel, $scope.project.id).then(function(response){
                $scope.project.workflow = response.data;
                alert("Workflow Saved!");
                $log.log(response.data);
            });
        };

        $scope.runWorkflow = function(){

            mlProjectService.runWorkflow($scope.chartDataModel, $scope.project.id).then(function(response){
                $scope.project.workflow = response.data;
                alert("Workflow Running Completed!");
                $log.log(response.data);
            });
        };

        //
        // Code for the delete key.
        //
        var deleteKeyCode = 46;

        //
        // Code for control key.
        //
        var ctrlKeyCode = 65;

        //
        // Set to true when the ctrl key is down.
        //
        var ctrlDown = false;

        //
        // Code for A key.
        //
        var aKeyCode = 17;

        //
        // Code for esc key.
        //
        var escKeyCode = 27;

        //
        // Selects the next node id.
        //
        var nextNodeID = 10;



        //
        // Event handler for key-down on the flowchart.
        //
        $scope.keyDown = function (evt) {

            if (evt.keyCode === ctrlKeyCode) {

                ctrlDown = true;
                evt.stopPropagation();
                evt.preventDefault();
            }
        };

        //
        // Event handler for key-up on the flowchart.
        //
        $scope.keyUp = function (evt) {

            if (evt.keyCode === deleteKeyCode) {
                //
                // Delete key.
                //
                $scope.chartViewModel.deleteSelected();
            }

            if (evt.keyCode == aKeyCode && ctrlDown) {
                //
                // Ctrl + A
                //
                $scope.chartViewModel.selectAll();
            }

            if (evt.keyCode == escKeyCode) {
                // Escape.
                $scope.chartViewModel.deselectAll();
            }

            if (evt.keyCode === ctrlKeyCode) {
                ctrlDown = false;

                evt.stopPropagation();
                evt.preventDefault();
            }
        };

        $scope.addModuleNode = function(){
            var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/workflow.node.module.create.html',
              controller: 'mlWorkflowNodeModuleCreateCtrl',
              resolve: {
                modules: function () {
                  return $scope.modules;
                }
              }
            });

            modalInstance.result.then(function (nodeInfo) {
                addNewNode(nodeInfo.nodeName, nodeInfo.nodeModelId, 'Module');
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });

        };

        $scope.addInputStreamNode = function(){
             var modalInstance = $modal.open({
              animation: true,
              templateUrl: 'partials/workflow.node.batch.create.html',
              controller: 'mlWorkflowNodeBatchCreateCtrl',
              resolve: {
                batches: function () {
                  return $scope.batches;
                }
              }
            });

            modalInstance.result.then(function (nodeInfo) {
                addNewNode(nodeInfo.nodeName, nodeInfo.nodeModelId, 'Input Stream');
            }, function () {
              $log.info('Modal dismissed at: ' + new Date());
            });
        };

        $scope.addOutputStreamNode = function(){
            mlAjaxService.get('randname?count=1').then(function(response){
                var names = response.data;
                var nodeName = names[0];
                nodeName = prompt('Enter the node name: ', nodeName);

                var nodeModelId = null;
                addNewNode(nodeName, nodeModelId, 'Output Stream');
            });
        };

        $scope.addIntermediateStreamNode = function(){
            mlAjaxService.get('randname?count=1').then(function(response){
                var names = response.data;
                var nodeName = names[0];
                nodeName = prompt('Enter the node name: ', nodeName);
                var nodeModelId = null;
                addNewNode(nodeName, nodeModelId, 'Intermediate Stream');
            });
        };

        //
        // Add a new node to the chart.
        //
        var addNewNode = function (nodeName, nodeModelId, nodeModelType) {
            if(!nodeName) return;
            if(!nodeModelType) return;

            var _nodeModelType = nodeModelType;
            if(nodeModelType == 'Input Stream' || nodeModelType == 'Output Stream' || nodeModelType == 'Intermediate Stream'){
                _nodeModelType = 'Batch';
            }

            //
            // Template for a new node.
            //
            var newNodeDataModel = {
                name: nodeName,
                id: nextNodeID++,
                x: 0,
                y: 0,
                modelId: nodeModelId,
                modelType: _nodeModelType,
                inputConnectors: [ ],
                outputConnectors: [ ],
            };

            if(nodeModelType == 'Module'){
                newNodeDataModel.inputConnectors.push({name: 'Train-In'});

                newNodeDataModel.inputConnectors.push({name: 'Predict-In'})
                newNodeDataModel.outputConnectors.push({name: 'Predict-Out'});
            } else if(nodeModelType == 'Input Stream'){
                newNodeDataModel.outputConnectors.push({name: 'Out1'});
                newNodeDataModel.outputConnectors.push({name: 'Out2'});
            } else if(nodeModelType == 'Output Stream'){
                newNodeDataModel.inputConnectors.push({name: 'In1'});
            } else if(nodeModelType == 'Intermediate Stream'){
                newNodeDataModel.outputConnectors.push({name: 'Out1'});
                newNodeDataModel.outputConnectors.push({name: 'Out2'});
                newNodeDataModel.inputConnectors.push({name: 'In1'});
                newNodeDataModel.inputConnectors.push({name: 'In2'});
            }

            $scope.chartViewModel.addNode(newNodeDataModel);
        };

        //
        // Add an input connector to selected nodes.
        //
        $scope.addNewInputConnector = function () {
            var connectorName = prompt("Enter a connector name:", "New connector");
            if (!connectorName) {
                return;
            }

            var selectedNodes = $scope.chartViewModel.getSelectedNodes();
            for (var i = 0; i < selectedNodes.length; ++i) {
                var node = selectedNodes[i];
                node.addInputConnector({
                    name: connectorName,
                });
            }
        };

        //
        // Add an output connector to selected nodes.
        //
        $scope.addNewOutputConnector = function () {
            var connectorName = prompt("Enter a connector name:", "New connector");
            if (!connectorName) {
                return;
            }

            var selectedNodes = $scope.chartViewModel.getSelectedNodes();
            for (var i = 0; i < selectedNodes.length; ++i) {
                var node = selectedNodes[i];
                node.addOutputConnector({
                    name: connectorName,
                });
            }
        };

        //
        // Delete selected nodes and connections.
        //
        $scope.deleteSelected = function () {

            $scope.chartViewModel.deleteSelected();
        };




        getOverviews();
    };


    var app = angular.module('ml-app');
    app.controller('mlWorkflowCtrl', ['$scope', '$log', '$location', '$modal', 'mlAjaxService', 'mlProjectService', 'mlNvd3Service', controller]);
}());

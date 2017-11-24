(function(){
    var controller = function($scope, $log, $location, $modalInstance, mlTextService, mlProjectService, mlModuleService, project){
        $scope.project = project;
        $scope.prototypeCategories = null;
        $scope.prototypes = null;
        $scope.prototypesUnderCategory = null;

        var onCreateModule = function(response){
            
            //$location.path('/projects/'+$scope.project.id);
            $modalInstance.close(response.data);
        };

        var onListPrototypes = function(response){
            $scope.prototypes = response.data;
            $scope.prototypeCategories = [];
            for(var p in $scope.prototypes){
                $scope.prototypeCategories.push(p);
            }
            $scope.prototypeCategory = $scope.prototypeCategories[0];
            $scope.prototypesUnderCategory = $scope.prototypes[$scope.prototypeCategory];
            $scope.template = $scope.prototypesUnderCategory[0];

            updatePrototypeInfo();

        };

        var listPrototypes = function(){
            mlModuleService.listPrototypes().then(onListPrototypes);
        };

        $scope.createModule = function(){
            mlModuleService.createModule($scope.project.id, $scope.template, $scope.title, $scope.description).then(onCreateModule);
        };

        $scope.cancel = function(){
            //$location.path('/projects/'+$scope.project.id);
            $modalInstance.dismiss('cancel');
        };

        $scope.onPrototypeCategoryChanged = function(){
            $scope.prototypesUnderCategory = $scope.prototypes[$scope.prototypeCategory];
            $scope.template = $scope.prototypesUnderCategory[0];
            updatePrototypeInfo();
        };

        $scope.onPrototypeChanged = function(){
            updatePrototypeInfo();
        };

        var updatePrototypeInfo = function(){
            var lindex = $scope.template.lastIndexOf(".");
            $scope.title = mlTextService.beautifyString($scope.template.substring(lindex+1));
            $scope.description = $scope.prototypeCategory;
        };

        $scope.beautifyString = function(text){
            return mlTextService.beautifyString(text);
        };

        $scope.prototypeCategory = null;
        $scope.prototypesUnderCategory = null;
        $scope.template = null;
        $scope.createdBy = 'user@gmail.com';
        $scope.title = 'NBC';
        $scope.description = 'NBC Description';

        listPrototypes();

    };

    var app = angular.module('ml-app');
    app.controller('mlModuleCreateCtrl', ['$scope', '$log', '$location', '$modalInstance', 'mlTextService', 'mlProjectService', 'mlModuleService', 'project', controller]);
}());

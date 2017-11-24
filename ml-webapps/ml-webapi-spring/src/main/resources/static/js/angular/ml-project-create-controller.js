(function(){
    var controller = function($scope, $log, $location, mlAjaxService, mlProjectService){
        var onCreateProject = function(response){
            $location.path('/projects');
        };

        $scope.createProject = function(){
            mlProjectService.createProject($scope.title, $scope.createdBy, $scope.description).then(onCreateProject);
        };

        $scope.cancel = function(){
            $location.path('/projects');
        };

        var initialize = function(){
            mlAjaxService.get('randname?count=1').then(function(response){
                var names = response.data;
                $scope.title = names[0];
                $scope.description = 'This project '+names[0]+' is generated';
            });
        };

        $scope.createdBy = 'user@gmail.com';
        $scope.title = 'Project Title';
        $scope.description = 'Project Description';

        initialize();

    };

    var app = angular.module('ml-app');
    app.controller('mlProjectCreateCtrl', ['$scope', '$log', '$location', 'mlAjaxService', 'mlProjectService', controller]);
}());

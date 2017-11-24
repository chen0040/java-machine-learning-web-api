(function(){
    'use strict';
    var app = angular.module('ml-app', ['smart-table', 'ui.bootstrap', "ngRoute", 'nvd3', 'jsonFormatter', 'ngSanitize', 'ngCsv', 'flowChart']);

    app.config(
        [
            '$routeProvider',
            function($routeProvider) {
                $routeProvider.
                      when('/projects', {
                        templateUrl: 'partials/project.list.html',
                        controller: 'mlProjectListCtrl'
                      }).
                      when('/projects/:projectId', {
                        templateUrl: 'partials/project.detail.html',
                        controller: 'mlProjectDetailCtrl'
                      }).
                      when('/modules/:moduleId', {
                        templateUrl: 'partials/module.detail.html',
                        controller: 'mlModuleDetailCtrl'
                      }).
                      when('/batches/:batchId', {
                         templateUrl: 'partials/batch.detail.html',
                         controller: 'mlBatchDetailCtrl'
                       }).
                      when('/create-project', {
                        templateUrl: 'partials/project.create.html',
                        controller: 'mlProjectCreateCtrl'
                      }).
                      when('/experiment', {
                        templateUrl: 'partials/experiment.html',
                        controller: 'mlExperimentCtrl'
                      }).
                      when('/workflow', {
                         templateUrl: 'partials/workflow.html',
                         controller: 'mlWorkflowCtrl'
                      }).
                      otherwise({
                        redirectTo: '/projects'
                      });
            }
        ]);
    app.directive("fileread", [function () {
           return {
               scope: {
                   fileread: "="
               },
               link: function (scope, element, attributes) {
                   element.bind("change", function (changeEvent) {
                       scope.$apply(function () {
                           scope.fileread = changeEvent.target.files[0];
                           // or all selected files:
                           // scope.fileread = changeEvent.target.files;
                       });
                   });
               }
           }
       }]);
}());

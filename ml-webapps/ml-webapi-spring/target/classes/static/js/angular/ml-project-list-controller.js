(function(){
   var controller = function($scope, $log, $location, mlProjectService){
       var onListProjects = function(response){
            $scope.projects = response.data;
       };

       var listProjects = function(){
            mlProjectService.listProjects().then(onListProjects);

       };

       var onDeleteProject = function(response){
            var deletedProject = response.data;
            var projectIndex = -1;
            for(var i=0; i < $scope.projects.length; ++i){
                if($scope.projects[i].id == deletedProject.id){
                    projectIndex = i;
                    break;
                }
            }
            if(projectIndex != -1){
                if($scope.projectWithDescriptionEditing != null && $scope.projectWithDescriptionEditing.id == deletedProject.id){
                    $scope.projectWithDescriptionEditing = null;
                }
                if($scope.projectWithTitleEditing != null && $scope.projectWithTitleEditing.id == deletedProject.id){
                    $scope.projectWithTitleEditing = null;
                }
                $scope.projects.splice(projectIndex, 1);
            }
       };

       $scope.deleteProject = function(project){
            mlProjectService.deleteProject(project.id).then(onDeleteProject);
       };

       $scope.editDescription = function(project){
            $scope.projectWithDescriptionEditing = project;
       };

       $scope.isDescriptionEditing = function(project){
            return project == $scope.projectWithDescriptionEditing;
       };

       $scope.commitEditingDescription = function(project){
            $scope.projectWithDescriptionEditing = null;
            mlProjectService.updateProject(project);
       };

       $scope.cancelEditingDescription = function(project){
            $scope.projectWithDescriptionEditing = null;
       };

       $scope.editTitle = function(project){
            $scope.projectWithTitleEditing = project;
       };

       $scope.isTitleEditing = function(project){
            return project == $scope.projectWithTitleEditing;
       };

       $scope.commitEditingTitle = function(project){
            $scope.projectWithTitleEditing = null;
            mlProjectService.updateProject(project);
       };

       $scope.cancelEditingTitle = function(project){
            $scope.projectWithTitleEditing = null;
       };

       $scope.viewProjectDetail = function (project) {
            $location.path('/projects/'+project.id);
       };

       $scope.projectWithDescriptionEditing = null;
       $scope.projectWithTitleEditing = null;
       $scope.projects = [];
       listProjects();
   };

   var app = angular.module('ml-app');

   app.controller('mlProjectListCtrl', ['$scope', '$log', '$location', 'mlProjectService', controller]);

}());

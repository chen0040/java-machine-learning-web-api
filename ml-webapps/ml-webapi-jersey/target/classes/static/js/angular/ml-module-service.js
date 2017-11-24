(function(){
    var service = function($log, mlAjaxService){
        var listModules = function(projectId, myAppName){

            return mlAjaxService.get('mlmodules/project?projectId='+projectId, myAppName);
        };

        var createModule = function(projectId, prototype, title, description, myAppName){

            var params = {
                'projectId' : projectId,
                'prototype' : prototype,
                'title' : title,
                'description' : description
            };

            return mlAjaxService.postJson("mlmodules", params, myAppName);
        };

        var listPrototypes = function(myAppName){
            return mlAjaxService.get("mlmodules/prototypes", myAppName);
        };

        var getModule = function(moduleId, myAppName){
            return mlAjaxService.get("mlmodules/"+moduleId, myAppName);
        };

        var deleteModule = function(moduleId, myAppName){
            return mlAjaxService.del("mlmodules/"+moduleId, myAppName);
        };

        var updateModuleShell = function(module, myAppName){
            module.isShell = true;
            return mlAjaxService.postJson("mlmodules", module, myAppName);
        };

        var trainModule = function(moduleId, batchId, myAppName){
            return mlAjaxService.get('mlmodules/batch-update?moduleId='+moduleId+'&batchId='+batchId, myAppName);
        };

        var runModule = function(moduleId, batchId, myAppName){
            return mlAjaxService.get('mlmodules/batch-predict?moduleId='+moduleId+'&batchId='+batchId, myAppName);
        };

        return {
            listModules: listModules,
            createModule: createModule,
            getModule: getModule,
            deleteModule: deleteModule,
            updateModuleShell: updateModuleShell,
            trainModule: trainModule,
            runModule: runModule,
            listPrototypes: listPrototypes
        };
    };

    var app = angular.module('ml-app');
    app.factory('mlModuleService', ['$log', 'mlAjaxService', service]);
}());

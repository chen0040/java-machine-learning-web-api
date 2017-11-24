(function(){
    var service = function(mlAjaxService){
        var listProjects = function(myAppName){


            return mlAjaxService.get("projects", myAppName);
        };

        var createProject = function(title, createdBy, description, myAppName){

            var params = {
                'title' : title,
                'createdBy' : createdBy,
                'updatedBy' : createdBy,
                'created' : null,
                'updated' : null,
                'description' : description
            };

            return mlAjaxService.postJson("projects", params, myAppName);
        };

        var findAllOverviews = function(myAppName){
            return mlAjaxService.get("projects/overviews", myAppName);
        };

        var findOverview = function(projectId, myAppName){
            return mlAjaxService.get("projects/overviews/"+projectId, myAppName);
        };

        var getProject = function(projectId, myAppName){

            return mlAjaxService.get("projects/"+projectId, myAppName);
        };

        var deleteProject = function(projectId, myAppName){
            return mlAjaxService.del("projects/"+projectId, myAppName);
        };

        var updateProject = function(project, myAppName){
            var params = project;
            return mlAjaxService.postJson("projects", params, myAppName);
        };

        var saveWorkflow = function(workflow, projectId, myAppName){
            return mlAjaxService.postJson("projects/workflows?projectId="+projectId, workflow, myAppName);
        };

        var runWorkflow = function(workflow, projectId, myAppName){
            return mlAjaxService.postJson("projects/workflows/run?projectId="+projectId, workflow, myAppName);
        };

        var runTestCase = function(testCase, myAppName){
            return mlAjaxService.postJson("mlmodules/batch-test", testCase, myAppName);
        };

    	return {
    	    listProjects: listProjects,
    	    createProject: createProject,
    	    getProject: getProject,
    	    deleteProject: deleteProject,
    	    updateProject: updateProject,
    	    findAllOverviews: findAllOverviews,
    	    findOverview: findOverview,
    	    runTestCase: runTestCase,
    	    saveWorkflow: saveWorkflow,
    	    runWorkflow: runWorkflow
    	};


    };

    var app = angular.module("ml-app");
    app.factory("mlProjectService", ["mlAjaxService", service]);
}());

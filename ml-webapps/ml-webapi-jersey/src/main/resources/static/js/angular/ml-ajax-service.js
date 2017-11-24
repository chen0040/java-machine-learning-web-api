(function(){
	var service=function($http, encodeParamsService){

		var uploadFile = function(myApiName, data, myAppName){

            if(myAppName==undefined){
                myAppName = "/ml";
            }

            var fd = new FormData();
            for(dataKey in data){
				fd.append(dataKey, data[dataKey]);
			}

			var uploadUrl = myAppName + '/' + myApiName;

			return $http.post(uploadUrl, fd, {
				transformRequest: angular.identity,
				headers: {'Content-Type': undefined}
			});
		};

		var postJson = function(myApiName, json, myAppName){

		    if(myAppName==undefined){
                myAppName = "/ml";
            }

            return $http.post(myAppName + '/' + myApiName,
                    json);
		};

		var get = function(myApiName, myAppName){

		    if(myAppName==undefined){
		        myAppName = "/ml";
		    }


		    return $http.get(myAppName + '/' + myApiName);
		}

		var del = function(myApiName, myAppName){

            if(myAppName==undefined){
                myAppName = "/ml";
            }

            return $http.delete(myAppName + '/' + myApiName);
        }

		return {
			uploadFile: uploadFile,
			postJson: postJson,
			get: get,
			del: del
		};
	};

	var app=angular.module("ml-app");
	app.factory("mlAjaxService", ["$http", "mlEncodeParamsService", service]);
}());

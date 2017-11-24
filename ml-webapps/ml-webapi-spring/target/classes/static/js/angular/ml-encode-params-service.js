(function(){
	var service = function(){

		var encodeParams = function(data){
			var first = true;
			var result = "";
			for(var dataKey in data){
				var dataVal = data[dataKey];
				if(first) {
					first = false;
				} else {
					result += "&";
				}
				result += (dataKey+"="+dataVal);
			}
			return result;
		};

		return {
			encodeParams : encodeParams
		};
	};

	var app = angular.module("ml-app");
	app.factory("mlEncodeParamsService", [service]);


}());

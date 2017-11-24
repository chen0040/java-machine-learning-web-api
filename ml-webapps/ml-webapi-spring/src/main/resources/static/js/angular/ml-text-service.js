(function(){
    var service = function(){
        var beautifyString = function(text){
            if(text==undefined) return text;
            var comps = text.split(/(?=[A-Z])/);
            var result = '';
            for(var i=0; i < comps.length; ++i){
                if(i-1 >= 0 && comps[i-1].length==1){
                    result = result + comps[i];
                }else{
                    result = result + ' ' + comps[i];
                }
            }
            return result;
        };

        return {
            beautifyString : beautifyString
        };
    };

    var app = angular.module('ml-app');
    app.factory('mlTextService', [service]);
}());

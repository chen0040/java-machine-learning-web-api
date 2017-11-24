(function(){
    var service = function($log){
        var nextInt = function(upper){
            return parseInt(Math.floor(Math.random() * upper));
        };
        var nextDouble = function(){
            return Math.random();
        };
        return {
            nextInt : nextInt,
            nextDouble : nextDouble
        };
    };
    var app = angular.module('ml-app');
    app.factory('mlRandomService', ['$log', service]);
}());

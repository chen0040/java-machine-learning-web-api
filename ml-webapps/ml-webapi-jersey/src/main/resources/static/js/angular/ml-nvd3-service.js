(function(){
    var service = function($log){
    
       var getOptionsForLineWithFocus = function(w, h, xLabel, yLabel, chart_title, chart_sub_title){

         if(chart_title == undefined){
            chart_title = 'Sequence Chart';
         }

         if(chart_sub_title == undefined){
            chart_sub_title = 'Sequence Chart';
         }

         return {
               chart: {
                   type: 'lineChart',
                   height: h,
                   width: w,
                   margin : {
                       top: 20,
                       right: 20,
                       bottom: 40,
                       left: 55
                   },
                   x: function(d){ return d.x; },
                   y: function(d){ return d.y; },
                   useInteractiveGuideline: true,
                   dispatch: {
                       stateChange: function(e){ console.log("stateChange"); },
                       changeState: function(e){ console.log("changeState"); },
                       tooltipShow: function(e){ console.log("tooltipShow"); },
                       tooltipHide: function(e){ console.log("tooltipHide"); }
                   },
                   xAxis: {
                       axisLabel: xLabel
                   },
                   yAxis: {
                       axisLabel: yLabel,
                       tickFormat: function(d){
                           return d3.format('.02f')(d);
                       },
                       axisLabelDistance: 30
                   },
                   callback: function(chart){
                       console.log("!!! lineChart callback !!!");
                   }
               },
               title: {
                   enable: true,
                   text: chart_title
               },
               subtitle: {
                   enable: true,
                   text: chart_sub_title,
                   css: {
                       'text-align': 'center',
                       'margin': '10px 13px 0px 7px'
                   }
               },
               caption: {
                   enable: true,
                   html: '<b>Figure 1.</b> Lorem ipsum dolor sit amet, at eam blandit sadipscing, <span style="text-decoration: underline;">vim adhuc sanctus disputando ex</span>, cu usu affert alienum urbanitas. <i>Cum in purto erat, mea ne nominavi persecuti reformidans.</i> Docendi blandit abhorreant ea has, minim tantas alterum pro eu. <span style="color: darkred;">Exerci graeci ad vix, elit tacimates ea duo</span>. Id mel eruditi fuisset. Stet vidit patrioque in pro, eum ex veri verterem abhorreant, id unum oportere intellegam nec<sup>[1, <a href="https://github.com/krispo/angular-nvd3" target="_blank">2</a>, 3]</sup>.',
                   css: {
                       'text-align': 'justify',
                       'margin': '10px 13px 0px 7px'
                   }
               }
           };
       };

        var getOptionsForScatter = function(w, h, xLabels, yLabels){
            return  {
               chart: {
                   type: 'scatterChart',
                   height: 400,
                   width: 500,
                   color: d3.scale.category10().range(),
                   scatter: {
                       onlyCircles: false
                   },
                   showDistX: true,
                   showDistY: true,
                   tooltipContent: function(key) {
                       return '<h3>' + key + '</h3>';
                   },
                   transitionDuration: 350,
                   xAxis: {
                       axisLabel: xLabels,
                       tickFormat: function(d){
                           return d3.format('.02f')(d);
                       }
                   },
                   yAxis: {
                       axisLabel: yLabels,
                       tickFormat: function(d){
                           return d3.format('.02f')(d);
                       },
                       axisLabelDistance: 30
                   }
               }
           };
        };

        var getOptionsForPie = function(w, h, donut){
            return {
               chart: {
                   type: 'pieChart',
                   height: h,
                   width: w,
                   donut: donut,
                   x: function(d){return d.key;},
                   y: function(d){return d.y;},
                   showLabels: true,
                   transitionDuration: 500,
                   labelThreshold: 0.01,
                   legend: {
                       margin: {
                           top: 5,
                           right: 35,
                           bottom: 5,
                           left: 0
                       }
                   }
               }
           };
        };

        var getOptionsForDiscreteBar = function(w, h, xLabels, yLabels){
            return {
                  chart: {
                      type: 'discreteBarChart',
                      height: h,
                      width: w,
                      margin : {
                          top: 20,
                          right: 20,
                          bottom: 60,
                          left: 55
                      },
                      x: function(d){return d.label;},
                      y: function(d){return d.value;},
                      showValues: true,
                      valueFormat: function(d){
                          return d3.format(',.4f')(d);
                      },
                      transitionDuration: 500,
                      xAxis: {
                          axisLabel: xLabels
                      },
                      yAxis: {
                          axisLabel: yLabels,
                          axisLabelDistance: 30
                      }
                  }
              };
        };

        return {
            getOptionsForDiscreteBar : getOptionsForDiscreteBar,
            getOptionsForPie : getOptionsForPie,
            getOptionsForScatter : getOptionsForScatter,
            getOptionsForLineWithFocus : getOptionsForLineWithFocus
        };
    };

    var app = angular.module('ml-app');
    app.factory('mlNvd3Service', ['$log', service]);
}());

(function(){
	var service = function(){
		var play = function(myAppName, filename){
			if(sounds[filename]==undefined){
				sounds[filename] = new Howl({
					  urls: [myAppName+'assets/audio/'+filename]
					});
			}
			sounds[filename].play();
		};


		var sounds = [];

		return {
			play: play
		};
	};

	var app = angular.module("ml-app");
	app.factory("mlSoundService", [service]);
}());

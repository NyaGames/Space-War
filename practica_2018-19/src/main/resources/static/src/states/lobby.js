Spacewar.lobbyState = function(game) {
	this.numStars = 100
}

Spacewar.lobbyState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **LOBBY** state");
		}
	},

	preload : function() {
		// We create a procedural starfield background
		for (var i = 0; i < this.numStars; i++) {
			let sprite = game.add.sprite(game.world.randomX,
					game.world.randomY, 'spacewar', 'staralpha.png');
			let random = game.rnd.realInRange(0, 0.6);
			sprite.scale.setTo(random, random)
		}
	},

	create : function() {		
		this.showHTML();	
	},

	update : function() {	
		
	},
	
	hideHTML() {    
		document.getElementById("user").style.display = "none";
		document.getElementById("loginButton").style.display = "none";
	},

	showHTML() {
		document.getElementById("user").style.display = "block";
		document.getElementById("loginButton").style.display = "block";
	},

	login(){
		if (typeof game.global.myPlayer.id !== 'undefined') {
			var name = document.getElementById("user").value;
			game.global.myPlayer.username = name;

			if(name === 'undefined') name = 'noone';
			
			let message = {
				event : 'JOIN',
				username : name
			}
			game.global.socket.send(JSON.stringify(message))
			this.hideHTML();
			game.state.start('matchmakingState')
		}


	}
}
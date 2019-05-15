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
	
	},

	create : function() {
		
		let message = {
	            event : 'JOIN RANDOM ROOM',
	    }
		
	    game.global.socket.send(JSON.stringify(message))
	},

	update : function() {	
		
	}	
}
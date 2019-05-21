Spacewar.lobbyState = function(game) {

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
	            event : 'GET ROOMS',
	    }
		
		game.global.socket.send(JSON.stringify(message))			
	},

	update : function() {	
		this.rooms = game.global.myPlayer.rooms;

		if(this.rooms != undefined){
			for(let i = 0; i < this.rooms.length; i++){
				game.add.text(20, 20 + i * 50, this.rooms[i].key, { font: "20px", fill: "#fff", align: "center"});
				game.add.text(220, 20 + i * 50, this.rooms[i].gameMode, { font: "20px", fill: "#fff", align: "center"});
				game.add.text(420, 20 + i * 50, this.rooms[i].numPlayers + "/2", { font: "20px", fill: "#fff", align: "center"});
			}				
		}	
	}
}
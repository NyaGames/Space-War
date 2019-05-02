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
		this.hideHTML();
		game.state.start('matchmakingState')
	},

	update : function() {

	},
	
	hideHTML() {
        var x = document.getElementById("user");
        x.style.display = "none";
	}
}
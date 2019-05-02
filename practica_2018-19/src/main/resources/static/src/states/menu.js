Spacewar.menuState = function(game) {

}

Spacewar.menuState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MENU** state");
		}
	},

	preload : function() {
		// In case JOIN message from server failed, we force it
		if (typeof game.global.myPlayer.id == 'undefined') {
			if (game.global.DEBUG_MODE) {
				console.log("[DEBUG] Forcing joining server...");
			}
		}
	},

	create : function() {
		this.showHTML();
	},

	update : function() {		
		this.spaceKey = game.input.keyboard.addKey(Phaser.Keyboard.SPACEBAR);
		if (typeof game.global.myPlayer.id !== 'undefined' && this.spaceKey.isDown) {
			let message = {
				event : 'JOIN',
				username : document.getElementById("user").value
			}
			game.global.socket.send(JSON.stringify(message))
			game.state.start('lobbyState')
		}
	},
	showHTML() {
		var x = document.getElementById("user");
		x.style.display = "block";
	}	
}
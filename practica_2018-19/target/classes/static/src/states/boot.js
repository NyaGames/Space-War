var Spacewar = {}

Spacewar.bootState = function(game) {

}

Spacewar.bootState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **BOOT** state");
		}
	},

	preload : function() {
		this.game.renderer.renderSession.roundPixels = true
		this.time.desiredFps = game.global.FPS
	},

	create : function() {
		this.hideHTML();
	},

	update : function() {
		if (typeof game.global.socket !== 'undefined') {
			game.state.start('preloadState')
		}
	},

	hideHTML() {
		document.getElementById("user").style.display = "none";
		document.getElementById("loginButton").style.display = "none";
		document.getElementById("createRoom").style.display = "none";
		document.getElementById("joinRoom").style.display = "none";
	}
}
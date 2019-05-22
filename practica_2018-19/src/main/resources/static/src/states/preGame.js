Spacewar.preGameState = function (game) {
}

Spacewar.preGameState.prototype = {

	init: function () {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **PREGAME** state");
		}
	},

	preload: function () {

	},

	create: function () {
		let nameText = game.add.text(10, game.world.height - 50, "Esperando al resto de jugadores...", { font: "20px", fill: "#fff", align: "center" })
	},

	update: function () {
		let message = {
			event: 'MATCHMAKING'
		}
		game.global.socket.send(JSON.stringify(message))
	}
}
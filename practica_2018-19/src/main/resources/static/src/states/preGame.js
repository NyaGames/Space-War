Spacewar.preGameState = function (game) {
}

/**Estado al que va un jugador mientras la sala no tenga suficientes jugadores */
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

		//Mandamos el mensaje para preguntar si ya hay gente suficiente.
		let message = {
			event: 'MATCHMAKING'
		}
		game.global.socket.send(JSON.stringify(message))
	},
}
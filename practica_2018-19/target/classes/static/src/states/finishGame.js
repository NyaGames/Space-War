Spacewar.finishGameState = function (game) {
}

Spacewar.finishGameState.prototype = {

	init: function () {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **FINISHGAME** state");
		}
	},

	preload: function () {

	},

	create: function () {
		let nameText = game.add.text(10, game.world.height - 50, "Final", { font: "20px", fill: "#fff", align: "center" })
	},
}
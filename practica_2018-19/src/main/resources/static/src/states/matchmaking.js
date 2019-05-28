Spacewar.matchmakingState = function (game) {
	this.numStars = 100
}

/*En este estado el jugador simplemente tendrá que elgegir si quiere crear una sala o meterse en una existente*/

Spacewar.matchmakingState.prototype = {

	init: function () {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **MATCH-MAKING** state");
		}
	},

	preload: function () {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Joining room...");
		}

		// We create a procedural starfield background
		for (var i = 0; i < this.numStars; i++) {
			let sprite = game.add.sprite(game.world.randomX,
				game.world.randomY, 'spacewar', 'staralpha.png');
			let random = game.rnd.realInRange(0, 0.6);
			sprite.scale.setTo(random, random)
		}

	},

	create: function () {
		this.showHTML();
		let nameText = game.add.text(10, game.world.height - 50, "Hello " + game.global.myPlayer.username + " :)", { font: "20px", fill: "#fff", align: "center" })
	},

	createRoom: function () {
		this.hideHTML();
		game.state.start('createRoomState')
	},

	joinRoom: function () {
		this.hideHTML();

		game.state.start('lobbyState')
	},

	update: function () {
		if (typeof game.global.myPlayer.room !== 'undefined') {
			if (game.global.DEBUG_MODE) {
				console.log("[DEBUG] Joined room " + game.global.myPlayer.room);
			}	game.global.myPlayer.room = {
					name: msg.room
				}
		}
	},

	showHTML() {
		document.getElementById("createRoom").style.display = "block";
		document.getElementById("joinRoom").style.display = "block";
	},

	hideHTML() {
		document.getElementById("createRoom").style.display = "none";
		document.getElementById("joinRoom").style.display = "none";
	}
}
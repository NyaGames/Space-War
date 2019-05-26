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
		this.getPunctuations();
		
		let button = game.add.button(this.game.width - 75, this.game.height - 75, 'buttonSprite', updateRooms, this, 2, 1, 0)
		button.width = 200;
		button.height = 100;
		button.anchor.setTo(0.5);

		button.onInputOver.add(overButton, this);
		button.onInputOut.add(outButton, this);
		button.onInputUp.add(upUpdateButton, this);	
	},

	getPunctuations: function(){
		let message = {
			event: 'GET PUNCTUATION',
		}
	
		game.global.socket.send(JSON.stringify(message))
	},

	showPunctuations: function(){
		this.punctuations = game.global.myPlayer.punctuations;

		for (let i = 0; i < this.punctuations.length; i++) {
			game.add.text(420, 20 + i * 50, this.punctuations[i].player, { font: "20px", fill: "#fff", align: "center" });
			game.add.text(620, 20 + i * 50, this.punctuations[i].punctuation, { font: "20px", fill: "#fff", align: "center" });
		}
	}

}


function upUpdateButton(e) {
	if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] RESET ROOM");
		}
	
	
	let message = {
		event: 'EXIT PUNCTUATION',	
	}

	game.global.socket.send(JSON.stringify(message))
	
		
	game.state.start('matchmakingState');
}



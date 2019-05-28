Spacewar.lobbyState = function (game) {

}

/**En este estado el jugador podrá ver una lista de todas las salas disponibles. Si pulsa el botón de actualizar, se actualizará la lista de salas.*/

Spacewar.lobbyState.prototype = {

	init: function () {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **LOBBY** state");
		}
	},

	preload: function () {
		game.load.image('buttonSprite', 'assets/images/button.png');
		game.load.image('updateButton', 'assets/images/updateButton.png');
	},

	create: function () {
		//Mandamos la petición para que se nos envién las salas
		let message = {
			event: 'GET ROOMS',
		}

		game.global.socket.send(JSON.stringify(message))
		
		this.escapeKey = game.input.keyboard.addKey(Phaser.Keyboard.ESC);

		this.nameText = game.add.text(10, 50, "No rooms avaible", { font: "20px", fill: "#fff", align: "center" })

		//Botón de actualizar
		let button = game.add.button(this.game.width - 75, this.game.height - 75, 'updateButton', pressButton, this, 2, 1, 0)
		button.width = 200;
		button.height = 100;
		button.anchor.setTo(0.5);

		button.onInputOver.add(overButton, this);
		button.onInputOut.add(outButton, this);
		button.onInputUp.add(updateRooms, this);	
	},

	update: function () {		
		
		if (this.escapeKey.isDown)
			game.state.start("matchmakingState")
		
		this.rooms = game.global.myPlayer.rooms;
		
		var texts = []
		var buttons = []

		//Creamos la lista de salas.
		if (this.rooms != undefined && this.rooms.length != 0) {	
			this.nameText.setText("")	
			for (let i = 0; i < this.rooms.length; i++) {
				texts.push(game.add.text(20, 20 + i * 50, this.rooms[i].key, { font: "20px", fill: "#fff", align: "center" }));
				texts.push(game.add.text(220, 20 + i * 50, this.rooms[i].gameMode, { font: "20px", fill: "#fff", align: "center" }));
				texts.push(game.add.text(420, 20 + i * 50, this.rooms[i].numPlayers + "/2", { font: "20px", fill: "#fff", align: "center" }));

				let button = game.add.button(620, 20 + i * 50, 'buttonSprite', pressButton, this, 2, 1, 0);
				button.width = 100; 
				button.height = 30;
				button.anchor.setTo(0.5);
				button.room = this.rooms[i].key;

				button.onInputOver.add(overButton, this);
				button.onInputOut.add(outButton, this);
				button.onInputUp.add(upButton, this);		
				
				buttons.push(button);
			
			}
			let button = game.add.button(this.game.width - 50, 475, 'buttonSprite', pressButton, this, 2, 1, 0)
			button.width = 100;
			button.height = 30;
			button.anchor.setTo(0.5);

			button.onInputOver.add(overButton, this);
			button.onInputOut.add(outButton, this);
			button.onInputUp.add(randomButton, this);
			
		}else{
			this.nameText.setText("No rooms avaible")
		}
	}
}

//Métodos de los botones
function overButton(e) {
	//e.tint = 0xc0c0c0
	e.tint = 0xD4AF37
}

function outButton(e) {
	e.tint = 0xffffff
}

function pressButton(e) {
	e.tint = 0x00000;	
}

function randomButton(e) {	
	let message = {
			event: 'JOIN RANDOM ROOM',
	}

	game.global.socket.send(JSON.stringify(message))	
}

function updateRooms(e){
	/*let message = {
		event: 'GET ROOMS',
	}

	game.global.socket.send(JSON.stringify(message))*/+
	game.state.start("lobbyState");
}

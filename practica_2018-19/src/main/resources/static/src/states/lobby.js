Spacewar.lobbyState = function (game) {

}

Spacewar.lobbyState.prototype = {

	init: function () {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **LOBBY** state");
		}
	},

	preload: function () {
		game.load.image('buttonSprite', 'assets/images/button.png');
	},

	create: function () {
		let message = {
			event: 'GET ROOMS',
		}

		game.global.socket.send(JSON.stringify(message))
		
		this.escapeKey = game.input.keyboard.addKey(Phaser.Keyboard.ESC);

		this.nameText = game.add.text(10, 50, "No rooms avaible", { font: "20px", fill: "#fff", align: "center" })

		let button = game.add.button(this.game.width - 75, this.game.height - 75, 'buttonSprite', updateRooms, this, 2, 1, 0)
		button.width = 200;
		button.height = 100;
		button.anchor.setTo(0.5);

		button.onInputOver.add(overButton, this);
		button.onInputOut.add(outButton, this);
		button.onInputUp.add(randomButton, this);	
	},

	update: function () {		
		
		if (this.escapeKey.isDown)
			game.state.start("matchmakingState")
		
		this.rooms = game.global.myPlayer.rooms;
		
		var texts = []
		var buttons = []

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
			let button = game.add.button(970, 560, 'buttonSprite', pressButton, this, 2, 1, 0)
			button.width = 100;
			button.height = 30;
			button.anchor.setTo(0.5);

			button.onInputOver.add(overButton, this);
			button.onInputOut.add(outButton, this);
			button.onInputUp.add(randomButton, this);	
			
		  for(let i = 0; i < texts.length; i++){
			  texts[i].destroy();			 
		  }
		  
		  for(let i = 0; i < buttons.length; i++){
			  buttons[i].destroy();			 
		  }
		}else{
			this.nameText.setText("No rooms avaible")
		}
	}
}

function overButton(e) {
	//e.tint = 0xc0c0c0
	e.tint = 0xD4AF37
}

function upButton(e) {
	if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] JOINING **e.room** ROOM");
		}
		
	let message = {
		event: 'JOIN ROOM',
		roomName: e.room
	}

	game.global.socket.send(JSON.stringify(message))
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
	let message = {
		event: 'GET ROOMS',
	}

	game.global.socket.send(JSON.stringify(message))
}

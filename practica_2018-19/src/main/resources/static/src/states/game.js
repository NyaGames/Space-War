Spacewar.gameState = function(game) {
	this.bulletTime
	this.fireBullet
	this.numStars = 100 // Should be canvas size dependant
	this.maxProjectiles = 800 // 8 per player
}

Spacewar.gameState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **GAME** state");
		}
	},

	preload : function() {
		// We create a procedural starfield background
		for (var i = 0; i < this.numStars; i++) {
			let sprite = game.add.sprite(game.world.randomX,
					game.world.randomY, 'spacewar', 'staralpha.png');
			let random = game.rnd.realInRange(0, 0.6);
			sprite.scale.setTo(random, random)
		}

		// We preload the bullets pool
		game.global.proyectiles = new Array(this.maxProjectiles)
		for (var i = 0; i < this.maxProjectiles; i++) {
			game.global.projectiles[i] = {
				image : game.add.sprite(0, 0, 'spacewar', 'projectile.png')
			}
			game.global.projectiles[i].image.anchor.setTo(0.5, 0.5)
			game.global.projectiles[i].image.visible = false
		}

		// we load a random ship
		let random = [ 'blue', 'darkgrey', 'green', 'metalic', 'orange',
				'purple', 'red' ]
		let randomImage = random[Math.floor(Math.random() * random.length)]
				+ '_0' + (Math.floor(Math.random() * 6) + 1) + '.png'
		game.global.myPlayer.image = game.add.sprite(0, 0, 'spacewar',
				game.global.myPlayer.shipType)
		game.global.myPlayer.image.anchor.setTo(0.5, 0.5)

		game.global.myPlayer.hpRedBar = game.add.sprite(0, 0, 'hpRedBar');
		game.global.myPlayer.hpRedBar.anchor.set(0, 0.5);
		game.global.myPlayer.hpRedBar.width = 75;
		game.global.myPlayer.hpRedBar.height = 10;
		
		game.global.myPlayer.hpBar = game.add.sprite(0, 0, 'hpBar');
		game.global.myPlayer.hpBar.anchor.set(0, 0.5);
		game.global.myPlayer.hpBar.width = 75;
		game.global.myPlayer.hpBar.height = 10;
		
		game.global.myPlayer.boostBar = game.add.sprite(0, 0, 'boostBar');
		game.global.myPlayer.boostBar.anchor.set(0, 0.5);
		game.global.myPlayer.boostBar.width = 50;
		game.global.myPlayer.boostBar.height = 5;

		game.global.myPlayer.name = game.add.text(game.global.myPlayer.x, game.global.myPlayer.y - 50, "Name", { font: "20px Chakra Petch", fill: "#fff", align: "center"});
		game.global.myPlayer.name.anchor.set(0.5);
		
		game.global.myPlayer.ammo = game.add.text(game.global.myPlayer.x, game.global.myPlayer.y - 50, "0", { font: "20px Chakra Petch", fill: "#ffff00", align: "center"});
		game.global.myPlayer.ammo.anchor.set(0.5);
		
	},

	create : function() {
		this.bulletTime = 0
		this.fireBullet = function() {
			if (game.time.now > this.bulletTime) {
				this.bulletTime = game.time.now + 250;
				// this.weapon.fire()
				return true
			} else {
				return false
			}
		}

		this.wKey = game.input.keyboard.addKey(Phaser.Keyboard.W);
		this.sKey = game.input.keyboard.addKey(Phaser.Keyboard.S);
		this.aKey = game.input.keyboard.addKey(Phaser.Keyboard.A);
		this.dKey = game.input.keyboard.addKey(Phaser.Keyboard.D);
		this.spaceKey = game.input.keyboard.addKey(Phaser.Keyboard.SPACEBAR);

		game.input.keyboard.removeKeyCapture(Phaser.Keyboard.W);
		game.input.keyboard.removeKeyCapture(Phaser.Keyboard.S);
		game.input.keyboard.removeKeyCapture(Phaser.Keyboard.A);
		game.input.keyboard.removeKeyCapture(Phaser.Keyboard.D);
		game.input.keyboard.removeKeyCapture(Phaser.Keyboard.SPACEBAR);

		game.camera.follow(game.global.myPlayer.image);
	},

	update : function() {
		let msg = new Object()
		msg.event = 'UPDATE MOVEMENT'

		msg.movement = {
			thrust : false,
			brake : false,
			rotLeft : false,
			rotRight : false
		}

		msg.bullet = false

		if (this.wKey.isDown)
			msg.movement.thrust = true;
		if (this.sKey.isDown)
			msg.movement.brake = true;
		if (this.aKey.isDown)
			msg.movement.rotLeft = true;
		if (this.dKey.isDown)
			msg.movement.rotRight = true;
		if (this.spaceKey.isDown) {
			msg.bullet = this.fireBullet()
		}

		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Sending UPDATE MOVEMENT message to server")
		}
		game.global.socket.send(JSON.stringify(msg))
	}
}
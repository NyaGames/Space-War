window.onload = function() {

	game = new Phaser.Game(1024, 600, Phaser.AUTO, 'gameDiv')

	// GLOBAL VARIABLES
	game.global = {
		FPS : 30,
		DEBUG_MODE : false,
		socket : null,
		myPlayer : new Object(),
		otherPlayers : [],
		projectiles : []
	}

	// WEBSOCKET CONFIGURATOR
	game.global.socket = new WebSocket("ws://"+window.location.host+"/spacewar");
	
	game.global.socket.onopen = () => {
		if (game.global.DEBUG_MODE) {
			console.log('[DEBUG] WebSocket connection opened.')
		}
	}

	game.global.socket.onclose = () => {
		if (game.global.DEBUG_MODE) {
			console.log('[DEBUG] WebSocket connection closed.')
		}
	}
	
	game.global.socket.onmessage = (message) => {
		var msg = JSON.parse(message.data)
		
		switch (msg.event) {
		case 'JOIN':
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] JOIN message recieved')
				console.dir(msg)
			}
			game.global.myPlayer.id = msg.id
			game.global.myPlayer.shipType = msg.shipType
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] ID assigned to player: ' + game.global.myPlayer.id)
			}
			break
		case 'NEW ROOM' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] NEW ROOM message recieved')
				console.dir(msg)
			}
			game.global.myPlayer.room = {
					name : msg.room
			}
			break
		
		case 'GAME STATE UPDATE' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] GAME STATE UPDATE message recieved')
				console.dir(msg)
			}
			if (typeof game.global.myPlayer.image !== 'undefined') {
				for (var player of msg.players) {
					if (game.global.myPlayer.id == player.id) {

						game.global.myPlayer.image.x = player.posX
						game.global.myPlayer.image.y = player.posY
						game.global.myPlayer.image.angle = player.facingAngle

						game.global.myPlayer.hpRedBar.x = player.posX - 37.5
						game.global.myPlayer.hpRedBar.y = player.posY - 30

						game.global.myPlayer.hpBar.x = player.posX - 37.5
						game.global.myPlayer.hpBar.y = player.posY - 30
						game.global.myPlayer.hpBar.width = Math.max(0, 75 * player.hp * 0.01)

						game.global.myPlayer.name.x = player.posX
						game.global.myPlayer.name.y = player.posY - 50		


					} else {
						if (typeof game.global.otherPlayers[player.id] == 'undefined') {
							game.global.otherPlayers[player.id] = {
									image : game.add.sprite(player.posX, player.posY, 'spacewar', player.shipType),
									hpRedBar: game.add.sprite(player.posX, player.posY, 'hpRedBar'),	
									hpBar : game.add.sprite(player.posX, player.posY, 'hpBar'),
									name :  game.add.text(player.posX, player.posY, "Name", { font: "20px Chakra Petch", fill: "#fff", align: "center"})					
											
							}
							game.global.otherPlayers[player.id].image.anchor.setTo(0.5, 0.5)

							game.global.otherPlayers[player.id].hpRedBar.anchor.set(0, 0.5);
							game.global.otherPlayers[player.id].hpRedBar.width = 75;
							game.global.otherPlayers[player.id].hpRedBar.height = 10;

							game.global.otherPlayers[player.id].hpBar.anchor.setTo(0, 0.5)
							game.global.otherPlayers[player.id].hpBar.width = 75
							game.global.otherPlayers[player.id].hpBar.height = 10								
							
							game.global.otherPlayers[player.id].name.anchor.setTo(0.5)
		
						} else {
							game.global.otherPlayers[player.id].image.x = player.posX
							game.global.otherPlayers[player.id].image.y = player.posY
							game.global.otherPlayers[player.id].image.angle = player.facingAngle

							game.global.otherPlayers[player.id].hpRedBar.x = player.posX - 37.5
							game.global.otherPlayers[player.id].hpRedBar.y = player.posY - 30

							game.global.otherPlayers[player.id].hpBar.x = player.posX - 37.5
							game.global.otherPlayers[player.id].hpBar.y = player.posY - 30	
							game.global.otherPlayers[player.id].hpBar.width = Math.max(0, 75 * player.hp * 0.01)
							
							game.global.otherPlayers[player.id].name.x = player.posX							
							game.global.otherPlayers[player.id].name.y = player.posY - 50	
						}
					}
				}
				
				for (var projectile of msg.projectiles) {
					if (projectile.isAlive) {
						game.global.projectiles[projectile.id].image.x = projectile.posX
						game.global.projectiles[projectile.id].image.y = projectile.posY
						if (game.global.projectiles[projectile.id].image.visible === false) {
							game.global.projectiles[projectile.id].image.angle = projectile.facingAngle
							game.global.projectiles[projectile.id].image.visible = true
						}
					} else {
						if (projectile.isHit) {
							// we load explosion
							let explosion = game.add.sprite(projectile.posX, projectile.posY, 'explosion')
							explosion.animations.add('explosion')
							explosion.anchor.setTo(0.5, 0.5)
							explosion.scale.setTo(2, 2)
							explosion.animations.play('explosion', 15, false, true)
						}
						game.global.projectiles[projectile.id].image.visible = false
					}
				}
			}
			break
		case 'REMOVE PLAYER' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] REMOVE PLAYER message recieved')
				console.dir(msg.players)
			}
			game.global.otherPlayers[msg.id].image.destroy()
			delete game.global.otherPlayers[msg.id]
			break;

		case 'CHAT':			
			$('#chat').val($('#chat').val() + "\n" + msg.name + ": " + msg.message);
			break;
		default :
			console.dir(msg)
			break
		}
	}

	$("#send-btn").click(() => {
		var msg = {
			event: "CHAT",
			name: $('#name').val(),
			message: $('#message').val()
		}

		$('#message').val('');

		$('#chat').val($('#chat').val() + "\n" + msg.name + ": " + msg.message);

		game.global.socket.send(JSON.stringify(msg));
	});

	// PHASER SCENE CONFIGURATOR
	game.state.add('bootState', Spacewar.bootState)
	game.state.add('preloadState', Spacewar.preloadState)
	game.state.add('menuState', Spacewar.menuState)
	game.state.add('lobbyState', Spacewar.lobbyState)
	game.state.add('matchmakingState', Spacewar.matchmakingState)
	game.state.add('roomState', Spacewar.roomState)
	game.state.add('gameState', Spacewar.gameState)

	game.state.start('bootState')	
}
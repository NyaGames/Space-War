Spacewar.createRoomState = function(game) {
	this.numStars = 100
}

Spacewar.createRoomState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **ROOM** state");
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
        
	},

	create : function() {
        this.showHTML();
	},

	update : function() {
		
    },   
   

    createRoom(){
        var name = document.getElementById("roomName").value;
        var mode = document.getElementById("gameMode").value;   

        if(mode === "") mode = 1;

        let message = {
            event : 'NEW ROOM',
            name : name,
            mode : mode
        }

        game.global.socket.send(JSON.stringify(message))

    },

    showHTML(){
        document.getElementById("roomName").style.display = "block";
		document.getElementById("gameMode").style.display = "block";
		document.getElementById("createRoom2").style.display = "block";
    },

    hideHTML(){
        document.getElementById("roomName").style.display = "none";
		document.getElementById("gameMode").style.display = "none";
		document.getElementById("createRoom2").style.display = "none";
    },
    
    creationOk(){
        this.hideHTML();
        game.state.start('preGameState')
    }
}
class EvokerGame {
    var debugMode = true
    var turn: Int = 0
    val sceneMap = SceneMap(this)
    val messageLog = MessageLog()

    /**
     * Currently the game's entire rendering process is to display the message buffer in order. However, this
     * will get more complex over time.
     */
    private fun renderGame() {
        messageLog.outputAll().forEach { println(it.text) }
    }

    /**
     * The gameLoop has interactive and non-interactive modes.
     */
    private fun gameLoop(
        autoPlayTurnLimit: Int? = null,
        autoPlayerBehavior: ((Scene, Actor) -> Command) = { scene, _ ->
            // By default, it is a passive player.
            Command("wait", scene.actors)
        }
    ) {
        fun gameOver(): Boolean {
            val player = sceneMap.activeScene?.getPlayer() ?: error("Player not found!")
            return when (autoPlayTurnLimit) {
                null -> player.isAlive() == false
                else -> player.isAlive() == false || turn >= autoPlayTurnLimit
            }
        }

        while (!gameOver()) {
            val activeScene = sceneMap.activeScene ?: error("No active scene.")
            val player = activeScene.getPlayer() ?: error("Player not found in active scene.")
            val autoPlaying = autoPlayTurnLimit != null
            var userInput: String? = null

            fun getUserInput() {
                println("(turn $turn) Enter Command: ")
                while (userInput == null) {
                    userInput = readLine()
                }
            }

            activeScene.describeScene().forEach { messageLog.messageIn(turn, it) }

            if (!autoPlaying) {
                renderGame()
                getUserInput()
            }

            turn++
            val command = when (autoPlaying) {
                true -> autoPlayerBehavior.invoke(activeScene, player)
                else -> Command(userInput!!, activeScene.actors)
            }
            if (!autoPlaying) println(">>>\t" + command.printed())

            activeScene.handleInput(command)?.forEach { messageLog.messageIn(turn, it) }

            sceneMap.behaviorCheck().forEach { messageLog.messageIn(turn, it) }

            val deadActors = sceneMap.refreshAllActors()
            // TODO: Some Actors will drop things or change into other Actors on death, or otherwise affect the game.
        }
    }

    /**
     * Plays the game! For now that means it is just a wrapper over the game loop running in interactive mode.
     * This function will get more complex over time.
     */
    fun play() {
        gameLoop()
    }

    /**
     * Simply runs to see if the game will crash if left to its own devices for long enough.
     */
    fun passiveCrashTest(numTests: Int) {
        gameLoop(numTests)
    }
}
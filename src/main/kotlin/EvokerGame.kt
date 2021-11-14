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
        messageLog.outputAll().forEach { println(it) }
    }

    /*
        Concurrency Development Step in Progress. Notes:
            - First step is a lot of refactoring and decoupling. TODO
            - The biggest concurrency issue right now is message passing. The order in which they are handled
                matters. I would need to do something very different with them to make it parallel? TODO
     */

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

            activeScene.describeScene().forEach { msg ->
                // TODO: Refactoring. but how, exactly?
                messageLog.messageIn(msg)
            }

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

            activeScene.handleInput(command)?.forEach { msg ->
                // TODO: Refactoring.
                messageLog.messageIn(msg)
            }

            sceneMap.behaviorCheck().forEach { msg ->
                // TODO: Refactoring.
                messageLog.messageIn(msg)
            }

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
     * This function currently spawns a passive player, some golems, and some water and runs them for numTurns,
     * ensuring that nothing will crash. Eventually it will be more elaborate.
     */
    fun passiveCrashTest(numTests: Int) {
        sceneMap.scenes.values.random().addActor(Actor.WanderingGolem())
        sceneMap.scenes.values.random().addActor(Actor.WanderingGolem())
        sceneMap.scenes.values.random().addActor(Actor.FloodWater())
        sceneMap.scenes.values.random().addActor(Actor.FloodWater())
        gameLoop(numTests)
    }
}
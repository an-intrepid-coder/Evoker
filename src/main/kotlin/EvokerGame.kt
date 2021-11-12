class EvokerGame {
    var debugMode = true
    var turn: Int = 0
    val sceneMap = SceneMap(this)
    val messageLog = MessageLog()

    private fun gameOver(): Boolean {
        val player = sceneMap.activeScene?.getPlayer() ?: error("Player not found!")
        return player.isAlive() == false
    }

    /**
     * Plays the game!
     */
    fun play() {
        while (!gameOver()) {
            sceneMap.activeScene?.let { activeScene ->
                activeScene.describeScene().forEach { msg ->
                    messageLog.messageIn(msg)
                }
                messageLog.outputAll().forEach { println(it) }

                println("(turn $turn) Enter Command: ")
                readLine()?.let { userInput ->
                    turn++
                    val command = Command(userInput, activeScene.actors)
                    println(">>>\t" + command.printed())
                    activeScene.handleInput(command)?.forEach { msg ->
                        messageLog.messageIn(msg)
                    }
                    sceneMap.behaviorCheck().forEach { msg ->
                        messageLog.messageIn(msg)
                    }

                    val deadActors = activeScene.refreshActors()
                    // TODO: Some actors will cause effects or change into other kinds of Actors once destroyed.
                }
            }
        }
    }

    /**
     * The game plays itself! Doubles as a testing environment. For now the player will be passive,
     * but I'll build in some test AIs for the player down the road. I may or may not implement a real test suite
     * down the road. This will serve the purpose, for now.
     *
     * This function currently spawns a passive player, some golems, and some water and runs them for numTurns,
     * ensuring that nothing will crash. Eventually it will be more elaborate.
     */
    fun autoPlay(
        numTurns: Int,
        playerBehavior: ((Scene, Actor) -> Command) = { scene, _ ->
            // By default, it is a passive player.
            Command("wait", scene.actors)
        }
    ) {
        sceneMap.scenes.values.random().addActor(Actor.WanderingGolem())
        sceneMap.scenes.values.random().addActor(Actor.WanderingGolem())
        sceneMap.scenes.values.random().addActor(Actor.FloodWater())
        sceneMap.scenes.values.random().addActor(Actor.FloodWater())
        repeat (numTurns) {
            while (!gameOver() && turn < numTurns) {
                sceneMap.activeScene?.let { activeScene ->
                    val player = activeScene.getPlayer() ?: error("Player not found in active scene.")
                    turn++
                    activeScene.handleInput(
                        playerBehavior.invoke(activeScene, player)
                    )
                    sceneMap.behaviorCheck()
                }
            }
        }
    }
}
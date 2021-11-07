class EvokerGame {
    var turn: Int = 0
    val sceneMap = SceneMap()
    val messageLog = MessageLog()

    private fun gameOver(): Boolean {
        val player = sceneMap.activeScene?.getPlayer() ?: error("Player not found!")
        return player.isAlive() == false
    }

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
}
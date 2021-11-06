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
                turn++
                println("(turn $turn) Enter Command: ")

                readLine()?.let { userInput ->
                    val userCommand = UserCommand(userInput, activeScene.actors)
                    println(">>>\t" + userCommand.printed())
                    activeScene.handleInput(userCommand)?.forEach { msg ->
                        messageLog.messageIn(msg)
                    }

                    val deadActors = activeScene.refreshActors()
                }
            }
        }
    }
}
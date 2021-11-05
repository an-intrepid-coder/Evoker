import kotlin.system.exitProcess

class EvokerGame {
    var gameOver = false
    val scenes = mutableListOf(Scene.Opening(Actor.Player()))
    var activeScene = scenes.first()

    fun play() {
        while (!gameOver) {
            activeScene.describeScene()
            println("Enter Command: ")

            readLine()?.let { userInput ->
                val userCommand = UserCommand(userInput, activeScene.actors)
                println(">>>\t" + userCommand.printed())
                if (userCommand.command == "exit") exitProcess(0)

                activeScene.handleInput(userCommand)

                val deadActors = activeScene.bringOutYerDead()
            }
        }
    }

}
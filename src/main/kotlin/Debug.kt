/**
 * Various debugging options which are implemented as modifiers on the "debug" command. For example,
 * "debug map" or "debug log". You can have multiple modifiers; for example "debug map log" will both
 * display the SceneMap as a tree and all previously seen Messages.
 */
class Debug(command: Command) : Action(
    command = command,
    effect = { scene, _, _ ->
        val messages = mutableListOf<String>()
        scene?.parentSceneMap?.parentGame?.debugMode?.let {
            if (command.potentialModifiers.contains("map")) {
                scene.parentSceneMap.printSceneMap().forEach { messages.add(it) }
            }
            if (command.potentialModifiers.contains("log")) {
                scene.parentSceneMap.parentGame.messageLog.readMessages.forEach { messages.add(it.toString()) }
            }
            // There will be more debug modifiers eventually.
        }
        messages
    }
)

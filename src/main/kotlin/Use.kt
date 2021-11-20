/**
 * Some Actors have an interactiveEffect which is invoked when another Actor uses them. For example, Doors.
 *
 * The "my" modifier will allow the player to target items in their inventory instead of in the Scene. It does
 * this by re-interpreting the command which was initially given.
 */
class Use(command: Command) : Action(
    command = command,
    effect = { scene, self, target ->
        self ?: error("Calling Actor not found.")

        val messages = mutableListOf<String>()

        var realTarget = target
        if (command.potentialModifiers.contains("my")) {
            val inventory = self.inventory ?: error("Calling Actor has no inventory.")
            val newCommand = Command(command.raw, inventory)
            realTarget = newCommand.target
        }

        if (realTarget == null)
            messages.add("What are you trying to use?")
        val interactiveEffect = realTarget?.interactiveEffect
        if (interactiveEffect == null)
            messages.add("You can't use that.")
        else
            interactiveEffect.invoke(scene, realTarget, self).forEach { messages.add(it) }

        messages
    }
)

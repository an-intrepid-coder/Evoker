fun action(userCommand: UserCommand): Action? {
    return userCommand.command.let { command ->
        when (command) {
            null -> null
            "move" -> Action.Move(userCommand)
            "strike" -> Action.Strike(userCommand)
            "examine" -> Action.Examine(userCommand)
            "handle" -> Action.Handle(userCommand)
            "loot" -> Action.Loot(userCommand)
            // More action types to come
            else -> error("Invalid command.")
        }
    }
}

sealed class Action(
    val userCommand: UserCommand,
    val effect: ((SceneMap?, Actor?, Actor?) -> List<String>)? = null
) {
    class Move(userCommand: UserCommand) : Action(userCommand) // TODO

    class Strike(userCommand: UserCommand) : Action(userCommand) // TODO

    class Examine(userCommand: UserCommand) : Action(
        userCommand = userCommand,
        effect = { _, _, target ->
            val messages = mutableListOf<String>()
            val description = target?.description()
            if (description != null)
                messages.add(description)
            else
                messages.add("You look around but don't find what you're looking for.")
            messages
        }
    )

    class Handle(userCommand: UserCommand) : Action(userCommand) // TODO

    class Loot(userCommand: UserCommand) : Action(
        userCommand = userCommand,
        effect = { _, self, target ->
            val messages = mutableListOf<String>()
            if (target == null)
                messages.add("What are you trying to loot?")
            else if (target.inventory == null)
                messages.add("The target has no inventory.")
            else if (target.animate)
                messages.add("You can only loot inanimate objects.")
            else if (target.inventory!!.isEmpty())
                messages.add("The target is empty.")
            else if (target.inventory != null && !target.animate)
                target.transferInventory(self!!)
            messages
        }
    )
}
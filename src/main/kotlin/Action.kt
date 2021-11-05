fun action(userCommand: UserCommand): Action? {
    return userCommand.command.let { command ->
        when (command) {
            null -> null
            "move" -> Action.Move(userCommand)
            "strike" -> Action.Strike(userCommand)
            "examine" -> Action.Examine(userCommand)
            "handle" -> Action.Handle(userCommand)
            "loot" -> Action.Loot(userCommand)
            else -> error("Invalid command.")
        }
    }
}

sealed class Action(
    val userCommand: UserCommand,
    val eventTrigger: ((Scene, Actor, Actor?) -> Unit)? = null
) {
    class Move(userCommand: UserCommand) : Action(userCommand)

    class Strike(userCommand: UserCommand) : Action(userCommand)

    class Examine(userCommand: UserCommand) : Action(
        userCommand = userCommand,
        eventTrigger = { _, _, target ->
            val description = target?.description()
            if (description != null)
                println(description)
            else
                println("You look around but don't find what you're looking for.")
        }
    )

    class Handle(userCommand: UserCommand) : Action(userCommand)

    class Loot(userCommand: UserCommand) : Action(
        userCommand = userCommand,
        eventTrigger = { _, self, target ->
            if (target == null)
                println("What are you trying to open?")
            else if (target.inventory == null)
                println("The target has no inventory.")
            else if (target.animate)
                println("You can only open inanimate objects.")
            else if (target.inventory!!.isEmpty())
                println("The target is empty.")
            else if (target.inventory != null && !target.animate)
                target.transferInventory(self)
        }
    )
}
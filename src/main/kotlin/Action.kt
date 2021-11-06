fun action(userCommand: UserCommand): Action? {
    return userCommand.command.let { command ->
        when (command) {
            null -> null
            "strike" -> Action.Strike(userCommand)
            "examine" -> Action.Examine(userCommand)
            "use" -> Action.Use(userCommand)
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
    class Strike(userCommand: UserCommand) : Action(
        userCommand = userCommand,
        effect = { sceneMap, self, target ->
            // For now, striking will always do one damage. For now.
            val messages = mutableListOf<String>()
            when (target?.changeHealth(-1)) {
                null -> messages.add("What are you trying to accomplish?")
                0 -> messages.add("You destroyed a ${target.name}")
                else -> messages.add("You damaged a ${target.name}")
            }
            messages
        }
    )

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

    class Use(userCommand: UserCommand) : Action(
        userCommand = userCommand,
        effect = { sceneMap, self, target ->
            val messages = mutableListOf<String>()
            if (target == null)
                messages.add("What are you trying to use?")
            val effect = target?.interactiveEffect
            if (effect == null)
                messages.add("You can't use that.")
            else
                effect.invoke(sceneMap, target, self).forEach { messages.add(it) }
            messages
        }
    )

    class Loot(userCommand: UserCommand) : Action(
        userCommand = userCommand,
        effect = { _, self, target ->
            val messages = mutableListOf<String>()
            if (target == null)
                messages.add("What are you trying to loot?")
            else if (target.inventory == null)
                messages.add("The target has no inventory.")
            else if (target.inventory!!.isEmpty())
                messages.add("The target is empty.")
            else if (target.inventory != null && target.lootable)
                target.transferInventory(self!!)
            else if (target.inventory != null && !target.lootable)
                messages.add("That is not lootable at the moment.")
            messages
        }
    )
}
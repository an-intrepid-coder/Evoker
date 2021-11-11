fun action(command: Command): Action? {
    return command.base.let { baseCommand ->
        when (baseCommand) {
            null -> null
            "wait" -> Action.Wait(command)
            "strike" -> Action.Strike(command)
            "examine" -> Action.Examine(command)
            "use" -> Action.Use(command)
            "loot" -> Action.Loot(command)
            "debug" -> Action.Debug(command)
            // More action types to come
            else -> error("Invalid command.")
        }
    }
}

val waitingLines = listOf(
    "You twiddle your thumbs for a bit.",
    "You procrastinate like a pro.",
    "You are lost in thought for a moment.",
    "You stare at your feet for a bit.",
    "You quietly laugh at a funny joke you just thought of.",
    "You focus your concentration.",
    "You take a few deep breaths.",
    "You plan your next move.",
    "You hum a tune.",
)

sealed class Action(
    val command: Command,
    val effect: ((Scene?, Actor?, Actor?) -> List<String>)? = null
) {
    class Wait(command: Command) : Action(
        command = command,
        effect = { _, self, _ ->
            val messages = mutableListOf<String>()
            self?.isPlayer?.let {
                messages.add(waitingLines.random())
            }
            messages
        }
    )

    class Strike(command: Command) : Action(
        command = command,
        effect = { _, _, target ->
            // For now, striking will always do one damage. For now.
            val messages = mutableListOf<String>()
            when (target?.changeHealth(-1)) {
                null -> messages.add("What are you trying to accomplish?")
                0 -> {
                    messages.add("You destroyed a ${target.name}")
                    if (target.inventory != null && target.inventory!!.isNotEmpty())
                        target.lootable = true
                }
                else -> {
                    messages.add("You damaged a ${target.name}")
                    if (target.retaliating == false)
                        target.retaliating = true
                }
            }
            messages
        }
    )

    class Examine(command: Command) : Action(
        command = command,
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

    class Use(command: Command) : Action(
        command = command,
        effect = { scene, self, target ->
            val messages = mutableListOf<String>()
            if (target == null)
                messages.add("What are you trying to use?")
            val effect = target?.interactiveEffect
            if (effect == null)
                messages.add("You can't use that.")
            else
                effect.invoke(scene, target, self).forEach { messages.add(it) }
            messages
        }
    )

    class Loot(command: Command) : Action(
        command = command,
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

    class Debug(command: Command) : Action(
        command = command,
        effect = { scene, self, target ->
            val messages = mutableListOf<String>()
            scene?.parentSceneMap?.parentGame?.debugMode?.let {
                if (command.potentialModifiers.contains("map")) {
                    scene.parentSceneMap.printSceneMap().forEach { messages.add(it) }
                }
                // There will be more debug modifiers eventually.
            }
            messages
        }
    )
}
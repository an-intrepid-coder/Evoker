fun action(command: Command): Action? {
    return command.base.let { baseCommand ->
        when (baseCommand) {
            null -> null
            "water" -> Action.Water(command)
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

/**
 * Actions are anything which takes an in-game turn, from waiting to casting spells. Each Action can have a number
 * of modifiers, which will change the action. Sometimes these modifiers can be combined, and other times they
 * are mutually exclusive.
 *
 * Note: When an Actor becomes attuned to an element, including the player as a result of using a Spell, they
 * will remain attuned until they do something to change this. Since each attunement will have positive and negative
 * effects, and the player can have multiple attunements at once, the player will need to carefully manage which spells
 * they use, and when. This system is not very fleshed out yet.
 *
 * Note: No "Magic Point" system is implemented yet. Currently, I'm not even sure I want to keep the "Health Point"
 * system. The jury is still out on how to balance these things. Early days.
 */
sealed class Action(
    val command: Command,
    val isSpell: Boolean = false,
    val effect: ((Scene?, Actor?, Actor?) -> List<String>)? = null,
) {

    /**
     * Using the Water action without a modifier causes the player to be temporarily attuned with the element of
     * water, which has effects in gameplay. This effect will happen in addition to any modifiers put on the Action,
     * making it a strategic choice (once the game is more developed). Possible modifiers are:
     *
     * "lower" lowers the water level in the Scene if the water level is greater than NONE.
     *
     * "raise" raises the water level in the Scene if the water level is below UNDERWATER. "raise" and "lower" can
     * cancel each other out.
     */
    class Water(command: Command) : Action(
        command = command,
        isSpell = true,
        effect = { scene, self, _ ->
            self ?: error("No caller found.")
            scene ?: error("No Scene found.")
            val messages = mutableListOf<String>()

            if (self.spellBook.contains("water")) {
                if (!self.elementalAttunements.contains(ElementType.WATER)) {
                    self.elementalAttunements.add(ElementType.WATER)
                    messages.add("${self.name} is now attuned to the element of water.")
                }
                if (command.potentialModifiers.contains("lower")) {
                    if (scene.waterLevel.waterLevelType == WaterLevel.WaterLevelType.NONE)
                        messages.add("There is no water here!")
                    else {
                        scene.waterLevel = scene.waterLevel.decrement()
                        messages.add("The water level decreases.")
                    }
                }
                if (command.potentialModifiers.contains("raise")) {
                    if (scene.waterLevel.waterLevelType == WaterLevel.WaterLevelType.UNDERWATER)
                        messages.add("This place is already under water!")
                    else {
                        scene.waterLevel = scene.waterLevel.increment()
                        messages.add("The water level increases.")
                    }
                }
            } else messages.add("You don't know that spell.")

            messages
        }
    )

    /**
     * Waiting does nothing but advance the simulation a turn, for now. I will eventually include mechanics which
     * make waiting a tactical choice.
     */
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

    /**
     * Striking does 1 damage to anything which can take damage. This may change eventually, but striking is
     * unlikely to play a major part in the game's combat and will be reserved for specific problems which can be
     * solved that way, such as breaking certain objects.
     */
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

    /**
     * Examine displays the full description of the targeted Actor.
     */
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

    /**
     * The Loot action transfers an entire inventory from a lootable object to the player. In the future I may
     * make this process more fine-grained.
     */
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
}
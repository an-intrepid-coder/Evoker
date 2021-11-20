fun action(command: Command): Action? {
    return command.base.let { baseCommand ->
        when (baseCommand) {
            null -> null
            "water" -> Water(command)
            "shield" -> Shield(command)
            "wait" -> Wait(command)
            "strike" -> Strike(command)
            "examine" -> Examine(command)
            "use" -> Use(command)
            "loot" -> Loot(command)
            "debug" -> Debug(command)
            // More action types to come
            else -> error("Invalid command.")
        }
    }
}

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
)
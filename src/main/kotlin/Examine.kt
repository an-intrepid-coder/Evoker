/**
 * Examine displays the full description of the targeted Actor. Potential modifiers: This command is only for
 * the player.
 *
 * "inventory" displays the names of items in the player's inventory instead of the usual effect.
 */
class Examine(command: Command) : Action(
    command = command,
    effect = { _, self, target ->
        self ?: error("Caller not found.")
        if (!self.isPlayer) error("Player not found.")

        val messages = mutableListOf<String>()

        if (command.potentialModifiers.contains("inventory")) {
            handleDuplicateActors(self.inventory!!)
            self.inventory!!.forEach {
                messages.add(it.description())
            }
        } else {
            val description = target?.description()
            if (description != null)
                messages.add(description)
            else
                messages.add("You look around but don't find what you're looking for.")
        }

        messages
    }
)

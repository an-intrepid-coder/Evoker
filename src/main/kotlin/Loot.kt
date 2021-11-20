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

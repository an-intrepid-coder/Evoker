/**
 * Striking does 1 damage to anything which can take damage. This may change eventually, but striking is
 * unlikely to play a major part in the game's combat and will be reserved for specific problems which can be
 * solved that way, such as breaking certain objects.
 */
class Strike(command: Command) : Action(
    command = command,
    effect = { scene, _, target ->
        // For now, striking will always do one damage. For now.
        scene ?: error("Scene not found.")
        val messages = mutableListOf<String>()

        target ?: messages.add("You swing at nothing.")

        target?.let {
            if (target.attunements.contains(AttunementType.SHIELD)) {
                target.attunements.remove(AttunementType.SHIELD)
                scene.getPlayer()?.let {
                    messages.add("${target.name}'s force shield shimmers before dissipating.")
                }
            } else {
                when (target.changeHealth(-1)) {
                    0 -> {
                        messages.add("You destroyed a ${target.name}")
                        target.lootable = target.inventory != null && target.inventory!!.isNotEmpty()
                    }
                    else -> {
                        messages.add("You damaged a ${target.name}")
                        target.retaliating = target.retaliating == false
                    }
                }
            }
        }

        messages
    }
)
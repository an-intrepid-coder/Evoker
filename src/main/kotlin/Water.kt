

/**
 * Using the Water action/spell without a modifier causes the player to be temporarily attuned with the element of
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
            if (!self.attunements.contains(AttunementType.WATER)) {
                self.attunements.add(AttunementType.WATER)
                messages.add("${self.name} is now attuned to the element of water.")
            }
            if (command.potentialModifiers.contains("lower")) {
                if (scene.waterLevel.waterLevelType == WaterLevel.WaterLevelType.NONE)
                    messages.add("The water is already receded.")
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
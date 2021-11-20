const val areaShieldTurns = 50 // for now -- tentative
const val areaShieldLimit = 3 // for now -- tentative

/**
 * Using the Shield action/spell without a modifier places a Shield attunement on the player, which will block
 * or mitigate incoming damage for a time. Possible modifiers are:
 *
 * "area" creates a temporary shield around the Scene the caster is currently in. It will prevent some Actors
 * from being able to enter the Scene, and will also prevent water from coming in through a flood source. Wandering
 * Golems can break shields if they pass through them.
 */
class Shield(command: Command) : Action(
    command = command,
    isSpell = true,
    effect = { scene, self, _ ->
        self ?: error("No caller found.")
        scene ?: error("No Scene found.")
        val messages = mutableListOf<String>()

        fun shieldScene() {
            scene.shielded = areaShieldTurns
            self.activeSceneShields.add(scene)
            messages.add("${scene.name} begins to shimmer around its boundaries.")
        }

        if (self.spellBook.contains("shield")) {
            if (!self.attunements.contains(AttunementType.SHIELD)) {
                self.attunements.add(AttunementType.SHIELD)
                messages.add("${self.name} is now surrounded by a force shield.")
            }
            if (command.potentialModifiers.contains("area")) {
                if (scene.shielded != null)
                    messages.add("This area is already shielded.")
                else if (self.activeSceneShields.size >= areaShieldLimit) {
                    val oldestShield = self.activeSceneShields.minByOrNull { it.shielded!! }!!
                    oldestShield.shielded = null
                    self.activeSceneShields.remove(oldestShield)
                    messages.add("This shield has replaced a previously cast one.")
                    shieldScene()
                }
                else shieldScene()
            }
        } else messages.add("You don't know that spell.")

        messages
    }
)

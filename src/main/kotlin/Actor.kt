const val floodWaterTimer = 20 // for now. This arbitrary value is subject to balance changes down the road.

enum class ElementType {
    WATER,
    // More to come!
}

/**
 * Actors include not just creatures and the player, but any object which can be interacted
 * with or observed. The instances of Actor each only interact with a fraction of the base class's
 * capabilities.
 */
sealed class Actor(
    val name: String,
    var maxHealth: Int? = null,
    var additionalDescriptionLines: List<String>? = null,
    val isPlayer: Boolean = false,
    var hidden: Boolean = false,
    var locked: Boolean = false,
    val areaTransitionId: Int? = null,
    var cameFrom: Boolean = false,
    var inventory: MutableList<Actor>? = null,
    var lootable: Boolean = false,
    val interactiveEffect: ((Scene?, Actor?, Actor?) -> List<String>)? = null,
    var timer: Int? = null,
    val behavior: ((Scene, Actor) -> List<String>)? = null,
    var retaliating: Boolean? = null,
    val pathMemory: MutableList<Int>? = null,
    val pathMemorySizeLimit: Int? = null,
    val spellBook: MutableSet<String> = mutableSetOf(),
    val elementalAttunements: MutableSet<ElementType> = mutableSetOf()
    // more params to come.
) {
    var health = maxHealth

    fun isAlive(): Boolean? {
        return health?.let { health!! > 0 }
    }

    fun description(brief: Boolean = false): String {
        if (brief)
            return "You see a $name." + when (cameFrom) {
                true -> " <"
                else -> ""
            }
        else if (areaTransitionId != null)
            return when (cameFrom) {
                true -> "You just came from this direction."
                else -> "This leads somewhere..."
            }
        val descriptionLines = listOf(
            "You see a $name.",
            when (health) {
                null -> "\tIt is indestructible."
                0 -> "\tIt is dead."
                else -> "\tIt has $health/$maxHealth health."
            },
            when (hidden) {
                true -> "\tIt is hidden."
                else -> null
            },
            when (inventory != null) {
                true -> "\tIt has an inventory."
                else -> null
            },
            when (lootable) {
                true -> "\n\tIt is lootable."
                else -> null
            },
            when (locked && lootable) {
                true -> "\tIt is locked."
                else -> null
            },
            when(behavior) {
                null -> null
                else -> "\tIt seems as if it will not remain static forever."
            },
            when (additionalDescriptionLines) {
                null -> null
                else -> additionalDescriptionLines!!.joinToString("\n")
            },
        )
        return descriptionLines
            .filterNotNull()
            .joinToString("\n")
    }

    /**
     * Returns the new health after the change.
     */
    fun changeHealth(amount: Int): Int? {
        return health?.let {
            health = health!! + amount
            if (health!! > maxHealth!!) health = maxHealth
            else if (health!! < 0) health = 0
            health
        }
    }

    fun kill() {
        health = 0
        if (inventory != null && inventory!!.isNotEmpty())
            lootable = true
    }

    fun addToInventory(actor: Actor) {
        inventory?.add(actor)
    }

    fun removeFromInventory(actor: Actor) {
        inventory?.remove(actor)
    }

    fun refreshInventory(): List<Actor>? {
        val deadInventory = inventory?.filter { it.isAlive() == false }
        if (deadInventory != null) {
            inventory = inventory!!.filter { it !in deadInventory }.toMutableList()
        }
        return deadInventory
    }

    fun transferInventory(other: Actor) {
        inventory?.forEach { item ->
            other.addToInventory(item)
            if (other.isPlayer)
                println("You looted a ${item.name}!")
        }
        inventory = mutableListOf()
    }

    class Player : Actor(
        name = "Player",
        isPlayer = true,
        maxHealth = 10,
        inventory = mutableListOf(),
    )

    class WanderingGolem : Actor(
        name = "Golem",
        maxHealth = 200,
        inventory = mutableListOf(),
        retaliating = false,
        additionalDescriptionLines = listOf(
            "\tThis beast is a hulking monstrosity of stone and steel.",
            "\tIt seems to be paying you no mind."
        ),
        pathMemory = mutableListOf(),
        pathMemorySizeLimit = 8, // for now
        behavior = { scene, self ->
            /*
                The Wandering Golem has two modes: retaliating and wandering. When retaliating it will trade blows
                with the player for as long as the player attacks it, which is a losing proposition as this is meant
                to be a mini-boss that is not easily bested via combat. When not retaliating it will wander randomly,
                accruing territory in its pathMemory until it is full. Once its pathMemory is full then it will
                stay within that territory, giving the player a chance to map out its domain.

                TODO: Some sort of puzzle-oriented way of fighting the creature for the player, and a reason for
                    doing so.

                TODO: Cause rooms to spring a leak when the golem walks by, sometimes.
             */
            val messages = mutableListOf<String>()
            when (self.retaliating) {
                true -> {
                    scene.getPlayer()?.let { player ->
                        Action.Strike(Command(
                            raw = "strike player",
                            targetEnvironment = scene.actors
                        )).effect!!.invoke(scene, self, player)
                        messages.add("${self.name} strikes you in retaliation!")
                        self.retaliating = false
                    }
                }
                else -> {
                    // Remove footprints
                    scene.actors.removeAll { it.name == "Footprints" }

                    // Add path if needed
                    if (scene.id !in self.pathMemory!! && self.pathMemory.size < self.pathMemorySizeLimit!!)
                        self.pathMemory.add(scene.id)

                    // Evaluate available areaTransitions and choose one appropriately.
                    val newTransitions = scene.actors.filter {
                        it.areaTransitionId != null && it.areaTransitionId !in self.pathMemory
                    }
                    val knownTransitions = scene.actors.filter { it.areaTransitionId in self.pathMemory }
                    val areaTransition = if (self.pathMemory.size >= self.pathMemorySizeLimit!!)
                        knownTransitions.random()
                    else if (newTransitions.isEmpty())
                        knownTransitions.random()
                    else
                        newTransitions.random()

                    // Add footprints
                    scene.addActor(GolemFootprints(
                        turnsUntilExpiration = 6, // for now
                        additionalDescriptionLines = listOf(
                            "\tSomething enormous made these footprints.",
                            "\tThey lead towards ${areaTransition.name}..."
                        )
                    ))

                    // Move the Golem
                    Action.Use(Command(
                        raw = "use ${areaTransition.name}",
                        targetEnvironment = scene.actors,
                    )).effect!!.invoke(scene, self, areaTransition)

                    // Notify player if in the same Scene.
                    scene.getPlayer()?.let {
                        messages.add("The Golem's massive footsteps unsettle the ground as it leaves.")
                    }

                    // There is a chance that the Golem will cause a room to spring a leak when it moves:
                    val leakChance = 1
                    if (withChance(100, leakChance)) {
                        scene.floodSource = true
                        scene.addActor(FloodWater())
                        scene.getPlayer()?.let {
                            messages.add("Water begins to pour through a crack in the wall!")
                        }
                    }

                }
            }
            messages
        }
    )

    class GolemFootprints(
        turnsUntilExpiration: Int,
        additionalDescriptionLines: List<String>,
    ) : Actor(
        name = "Footprints",
        timer = turnsUntilExpiration,
        additionalDescriptionLines = additionalDescriptionLines,
        behavior = { _, self ->
            val messages = mutableListOf<String>()
            self.timer = self.timer!! - 1
            if (self.timer!! <= 0) self.kill()
            messages
        }
    )

    class FloodWater : Actor(
        name = "Water",
        additionalDescriptionLines = listOf("You see flood water."),
        timer = floodWaterTimer,
        behavior = { scene, self ->
            /*
                FloodWater instances cause the water level in a room to rise. They disappear when the flood source
                is plugged and the water level is NONE. They spread FloodWater to neighboring Scenes.
             */
            val messages = mutableListOf<String>()

            if (scene.waterLevel.waterLevelType == WaterLevel.WaterLevelType.NONE && !scene.floodSource) {
                self.kill()
                messages
            } else {
                if (scene.floodSource) {
                    scene.actors.removeAll { it.name == "Footprints" }
                    self.timer = self.timer!! - 1
                    if (self.timer == 0) {
                        scene.waterLevel = scene.waterLevel.increment()
                        self.timer = floodWaterTimer
                    }
                }

                self.additionalDescriptionLines = when (scene.waterLevel.waterLevelType) {
                    // TODO: When water-level effects are implemented it will give additional lines for each level.
                    //  Those lines will describe the effects to the player.
                    WaterLevel.WaterLevelType.ANKLES -> listOf("The water is up to your ankles.")
                    WaterLevel.WaterLevelType.KNEES -> listOf("The water is up to your knees.")
                    WaterLevel.WaterLevelType.WAIST -> listOf("The water is up to your waist.")
                    WaterLevel.WaterLevelType.CHEST-> listOf("The water is up to your chest.")
                    WaterLevel.WaterLevelType.UNDERWATER-> listOf("You are submerged under water.")
                    WaterLevel.WaterLevelType.NONE -> listOf("The floor is slippery and wet..")
                }

                scene.neighbors().forEach { neighbor ->
                    if (neighbor.waterLevel.waterLevelType == WaterLevel.WaterLevelType.NONE) {
                        neighbor.waterLevel = neighbor.waterLevel.increment()
                        neighbor.addActor(FloodWater())
                    }
                }

                messages
            }
        }
    ) {
        init {
            elementalAttunements.add(ElementType.WATER)
        }
    }

    /**
     * Pure Flavor is a catch-all for Actors which only serve to describe fluff and are otherwise non-interactive.
     */
    class PureFlavor(
        name: String,
        additionalDescriptionLines: List<String>
    ) : Actor(
        name = name,
        additionalDescriptionLines = additionalDescriptionLines
    )

    class DoorTo(targetScene: Scene) : Actor(
        name = "Door${targetScene.id}",
        areaTransitionId = targetScene.id,
        interactiveEffect = { scene, _, triggerer ->
            val messages = mutableListOf<String>()
            scene ?: error("Scene not found.")
            triggerer ?: error("Triggering actor not found.")
            val sceneMap = scene.parentSceneMap
            sceneMap.activeScene ?: error("No active scene found in SceneMap.")
            scene.removeActor(triggerer)
            if (triggerer.isPlayer) {
                scene.clearCameFrom()
                sceneMap.changeScene(targetScene.id).let { id ->
                    if (id == null) error("Invalid sceneId: ${targetScene.id}.")
                    messages.add("You walk through the door to " + sceneMap.activeScene!!.name + ".")
                    sceneMap.activeScene!!.markCameFrom(scene.id)
                }
            }
            sceneMap.scenes[targetScene.id]!!.addActor(triggerer)
            messages
        }
    )

    class Nectar : Actor(
        name = "Nectar",
        maxHealth = 1,
        additionalDescriptionLines = listOf(
            "\tThis softly glowing vial is full of a substance known as 'nectar'.",
            "\tIt is known for its curative properties."
        ),
        interactiveEffect = { _, self, triggerer ->
            // Heals the user and then self-destructs.
            val messages = mutableListOf<String>()
            self?.isPlayer?.let {
                messages.add("You used a Healing Potion!")
            }
            val healAmountRange = 5..10
            triggerer?.changeHealth(healAmountRange.random())?.let { newHealth ->
                self?.isPlayer?.let {
                    messages.add("\tNew health is $newHealth")
                }
            }
            self?.kill()
            messages
        }
    )

    class GenericChest(
        inventory: List<Actor>,
        locked: Boolean = false
    ) : Actor(
        name = "Chest",
        maxHealth = 5,
        inventory = inventory.toMutableList(),
        locked = locked,
        lootable = true
    )

    class Aquatome : Actor(
        name = "Aquatome",
        maxHealth = 1, // The health of this object may change as the game develops
        additionalDescriptionLines = listOf(
            "\tThis spellbook teaches the secrets of water magic.",
            "\tAt its most basic, water magic attunes the caster to its element.",
            "\tIt can also be used to raise or lower the water level in an area.",
            // more to come
        ),
        interactiveEffect = { scene, self, triggerer ->
            triggerer ?: error("Triggering actor not found.")

            val messages = mutableListOf<String>()

            if (triggerer.spellBook.contains("water")) {
                if (triggerer.isPlayer)
                    messages.add("You already know the secrets of this book.")
            } else {
                triggerer.spellBook.add("water")
                if (triggerer.isPlayer)
                    messages.add("You learn the secrets of water magic!")
            }

            messages
        },
    ) {
        init { elementalAttunements.add(ElementType.WATER) }
        /*
            Note: I'm thinking of allowing the spellbooks to be harmed or improved or otherwise altered when spells are
            used on them, in which case their elemental attunement may matter.
         */
    }
}

fun randomPotion(): Actor {
    return listOf(
        Actor.Nectar(),
        // more to come
    ).random()
}
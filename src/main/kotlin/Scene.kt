import kotlin.system.exitProcess

val hallwayNames = listOf(
    "A Winding Hallway",
    "A Long, Straight Hallway",
    "A Dark Hallway",
    "A Dirty Hallway",
    "An Uneven, Damaged Hallway",
    // more to come
)

val cellNames = listOf(
    "A Filthy Cell",
    "A Gory Cell",
    "Another Cell",
    "A Dirty Cell",
    "An Unremarkable Cell",
    "A Dilapidated Cell"
    // more to come
)

val cellFlavors = listOf(
    Actor.PureFlavor(
        name = "floor",
        additionalDescriptionLines = listOf("The floor is covered in a fine layer of dust.")
    ),
    Actor.PureFlavor(
        name = "floor",
        additionalDescriptionLines = listOf("The floor is covered in bloody hand- and foot-prints.")
    ),
    Actor.PureFlavor(
        name = "walls",
        additionalDescriptionLines = listOf("The walls have bolts for restraining people with chains.")
    ),
    Actor.PureFlavor(
        name = "walls",
        additionalDescriptionLines = listOf("The walls are windowless and bleak.")
    ),
    Actor.PureFlavor(
        name = "walls",
        additionalDescriptionLines = listOf("The walls are covered in indecipherable scribblings.")
    ),
    Actor.PureFlavor(
        name = "air",
        additionalDescriptionLines = listOf("The air in this place is dank and evil.")
    ),
    // More to come
)

sealed class WaterLevel(val waterLevelType: WaterLevelType) {
    // In progress. Not all the effects are implemented yet.
    enum class WaterLevelType {
        NONE, // No effect
        ANKLES, // Erase footprints, damage some items
        KNEES, // extra movement cost
        WAIST, // extra movement cost, unable to perform some actions
        CHEST, // extra movement cost, unable to perform most actions
        UNDERWATER, // turn-by-turn damage and swept into adjacent rooms with the flow of water, if possible.
        // * Extra movement cost can be an increasing chance of losing a turn.
    }

    open fun increment(): WaterLevel { return this }
    open fun decrement(): WaterLevel { return this }

    class None : WaterLevel(WaterLevelType.NONE) {
        override fun increment(): WaterLevel {
            return Ankles()
        }
        override fun decrement(): WaterLevel {
            return this
        }
    }

    class Ankles : WaterLevel(WaterLevelType.ANKLES) {
        override fun increment(): WaterLevel {
            return Knees()
        }
        override fun decrement(): WaterLevel {
            return None()
        }
    }

    class Knees : WaterLevel(WaterLevelType.KNEES) {
        override fun increment(): WaterLevel {
            return Waist()
        }
        override fun decrement(): WaterLevel {
            return Ankles()
        }
    }

    class Waist : WaterLevel(WaterLevelType.WAIST) {
        override fun increment(): WaterLevel {
            return Chest()
        }
        override fun decrement(): WaterLevel {
            return Knees()
        }
    }

    class Chest : WaterLevel(WaterLevelType.CHEST) {
        override fun increment(): WaterLevel {
            return Underwater()
        }
        override fun decrement(): WaterLevel {
            return Waist()
        }
    }

    class Underwater : WaterLevel(WaterLevelType.UNDERWATER) {
        override fun increment(): WaterLevel {
            return this
        }
        override fun decrement(): WaterLevel {
            return Chest()
        }
    }
}

sealed class Scene(
    val name: String,
    val parentSceneMap: SceneMap,
    var waterLevel: WaterLevel = WaterLevel.None(),
    var floodSource: Boolean = false,
) {
    val id = parentSceneMap.numScenes++
    var actors = mutableListOf<Actor>()

    fun neighbors(): List<Scene> { // TODO: Test this works
        val areaTransitions = actors.asSequence()
            .filter { it.areaTransitionId != null }
            .map { it.areaTransitionId }
            .toList()
        return parentSceneMap.scenes.values.filter { areaTransitions.contains(it.id) }
    }

    fun markCameFrom(previousSceneIndex: Int) {
        actors.asSequence()
            .filter { it.areaTransitionId == previousSceneIndex }
            .forEach { it.cameFrom = true }
    }

    fun clearCameFrom() {
        actors.asSequence()
            .filter { it.cameFrom }
            .forEach { it.cameFrom = false }
    }

    fun addActor(actor: Actor) {
        actors.add(actor)
    }

    fun removeActor(actor: Actor): Boolean {
        return actors.remove(actor)
    }

    fun describeScene(): List<String> {
        return actors
            .asSequence()
            .filter { !it.isPlayer }
            .map { it.description(brief = true) }
            .toList()
    }

    fun handleInput(command: Command): List<String>? {
        if (command.base == "exit")
            exitProcess(0)
        else if (command.base == "help") {
            return listOf("Valid Commands:").plus(validCommands)
        }
        return action(command)?.effect?.invoke(
            this,
            getPlayer(),
            command.target
        )
    }

    fun getPlayer(): Actor? {
        return actors.firstOrNull { it.isPlayer }
    }

    fun refreshActors(): List<Actor> {
        var deadActors = listOf<Actor>()
        actors.forEach { actor ->
            actor.refreshInventory()?.let { deadActors = deadActors.plus(it) }
            if (actor.isAlive() == false && actor.inventory?.isNotEmpty() != true && !actor.isPlayer)
                deadActors = deadActors.plus(actor)
        }
        actors = actors.filter { it !in deadActors }.toMutableList()
        return deadActors
    }

    /**
     * The Opening Scene serves a "seed" from which the rest of the game grows procedurally. It will get more
     * complex and varied as development continues. The entire scene graph unfolds from this root point.
     */
    class Opening(
        parentSceneMap: SceneMap,
    ) : Scene(
        "Your Filthy Cell",
        parentSceneMap
    ) {
        init {
            addActor(Actor.PureFlavor(
                name = "cell",
                additionalDescriptionLines = listOf(
                    "\tYou don't remember how long you've been here.",
                    "\tIn fact, you don't remember anything prior to this filthy cell.",
                    "\tHow long have you been here? Who are you?",
                    "\tFor some reason your cell door is wide open.",
                    "\tThere is nothing stopping you from walking out..."
                )
            ))
            Hallway(parentSceneMap, this).let { hallway ->
                addActor(Actor.DoorTo(hallway))
                parentSceneMap.numHallways++
                parentSceneMap.addScene(hallway)
            }
        }
    }

    /**
     * Hallways serve as the main vehicles of map generation. Their number is limited in the SceneMap to
     * avoid infinite growth.
     */
    class Hallway( // TODO: More Hallway types.
        parentSceneMap: SceneMap,
        cameFrom: Scene,
    ) : Scene(
        hallwayNames.random(),
        parentSceneMap
    ) {
        init {
            val additionalConnections = (1..3).random() // for now; may adjust this
            var addedHallway = false
            addActor(Actor.DoorTo(cameFrom))
            repeat (additionalConnections) {
                if (!addedHallway && parentSceneMap.canAddHallways()) {
                    parentSceneMap.numHallways++
                    Hallway(parentSceneMap, this).let { hallway ->
                        addActor(Actor.DoorTo(hallway))
                        parentSceneMap.addScene(hallway)
                    }
                    addedHallway = true
                }
                else
                    // Tentative: TODO: More types of rooms and a factory function.
                    Cell(parentSceneMap, this).let { cell ->
                        addActor(Actor.DoorTo(cell))
                        parentSceneMap.addScene(cell)
                    }
            }
            // TODO: More doors and some other features of a hallway. Perhaps enemies and loot?
        }
    }

    /**
     * Cells serve as end-nodes for the SceneMap, for now.
     */
    // TODO: More kinds of end-nodes.
    class Cell(
        parentSceneMap: SceneMap,
        cameFrom: Scene
    ) : Scene(
        cellNames.random(),
        parentSceneMap
    ) {
        init {
            addActor(Actor.DoorTo(cameFrom))
            addActor(cellFlavors.random())
            // TODO: Perhaps potential enemies and loot?
        }
    }
}
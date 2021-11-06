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
    Actor.Flavor(
        name = "floor",
        flavorText = "The floor is covered in a fine layer of dust."
    ),
    Actor.Flavor(
        name = "floor",
        flavorText = "The floor is covered in bloody hand- and foot-prints."
    ),
    Actor.Flavor(
        name = "walls",
        flavorText = "The walls have bolts for restraining people with chains."
    ),
    Actor.Flavor(
        name = "walls",
        flavorText = "The walls are windowless and bleak."
    ),
    Actor.Flavor(
        name = "walls",
        flavorText = "The walls are covered in indecipherable scribblings."
    ),
    Actor.Flavor(
        name = "air",
        flavorText = "The air in this place is dank and evil."
    ),
    // More to come
)

sealed class Scene(
    val name: String,
    private val parentSceneMap: SceneMap,
) {
    val id = parentSceneMap.numScenes++
    var actors = mutableListOf<Actor>()

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

    fun handleInput(userCommand: UserCommand): List<String>? {
        if (userCommand.command == "exit")
            exitProcess(0)
        else if (userCommand.command == "help") {
            return listOf("Valid Commands:").plus(validCommands)
        }
        return action(userCommand)?.effect?.invoke(
            parentSceneMap,
            getPlayer(),
            userCommand.target
        )
    }

    fun getPlayer(): Actor? {
        return actors.firstOrNull { it.isPlayer }
    }

    fun refreshActors(): List<Actor> {
        var deadActors = listOf<Actor>()
        actors.forEach { actor ->
            actor.refreshInventory()?.let { deadActors = deadActors.plus(it) }
            if (actor.isAlive() == false)
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
            addActor(Actor.Flavor(
                name = "note",
                flavorText = "Pinned to the wall is a dirty note. It says:" +
                        "\n'This is a test. Make it out of here and survive, or die in the attempt." +
                        "\nI have prepared the way with both obstacles and boons." +
                        "\nGood luck.'"
            ))
            addActor(Actor.Flavor(
                name = "cell",
                flavorText = "You don't remember how long you've been here." +
                        "\nIn fact, you don't remember anything prior to this filthy cell." +
                        "\nHow long have you been here? Who are you?" +
                        "\nFor some reason your cell door is wide open." +
                        "\nThere is nothing stopping you from walking out..."
            ))
            Hallway(parentSceneMap, this).let { hallway ->
                addActor(Actor.DoorTo(hallway))
                parentSceneMap.numHallways++
                parentSceneMap.addScene(hallway)
            }
        }
    }

    class Hallway(
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
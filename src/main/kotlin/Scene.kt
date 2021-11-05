import kotlin.system.exitProcess

val hallwayNames = listOf(
    "A Winding Hallway",
    "A Long, Straight Hallway",
    "A Dark Hallway",
    "A Dirty Hallway",
    "An Uneven, Damaged Hallway",
    // more to come
)

sealed class Scene(
    val name: String,
    val parentSceneMap: SceneMap,
) {
    val id = parentSceneMap.numScenes++
    var actors = mutableListOf<Actor>()

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
            if (!actor.isAlive())
                deadActors = deadActors.plus(actor)
        }
        actors = actors.filter { it !in deadActors }.toMutableList()
        return deadActors
    }

    class Opening(
        parentSceneMap: SceneMap,
    ) : Scene(
        "Your Filthy Cell",
        parentSceneMap
    ) {
        // Testing: This will get more complex -- TODO: A Door object leading out of the cell and to somewhere else
        init {
            actors.add(Actor.Flavor(
                name = "note",
                flavorText = "Pinned to the wall is a dirty note. It says:" +
                        "\n'This is a test. Make it out of here and survive, or die in the attempt." +
                        "\nWe have prepared the way with both obstacles and boons." +
                        "\nGood luck.'"
            ))
            actors.add(Actor.Flavor(
                name = "cell",
                flavorText = "You don't remember how long you've been here." +
                        "\nIn fact, you don't remember anything prior to this filthy cell." +
                        "\nHow long have you been here? Who are you?" +
                        "\nFor some reason your cell door is wide open." +
                        "\nThere is nothing stopping you from walking out..."
            ))
            Hallway(parentSceneMap, this).let { hallway ->
                actors.add(Actor.DoorTo(hallway))
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
            actors.add(Actor.DoorTo(cameFrom))
            // TODO: More doors and some other features of a hallway
        }
    }
}
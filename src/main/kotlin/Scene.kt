import kotlin.system.exitProcess

sealed class Scene(
    val name: String,
    val parentSceneMap: SceneMap,
    val leafNode: Boolean,
    var waterLevel: WaterLevel = WaterLevel.None(),
    var floodSource: Boolean = false,
    var shielded: Int? = null,
) {
    val id = parentSceneMap.numScenes++
    var actors = mutableListOf<Actor>()

    fun neighbors(): List<Scene> {
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
        val waterLine = when (waterLevel.waterLevelType) {
            WaterLevel.WaterLevelType.NONE -> listOf()
            else -> listOf("There is water here. Water level: ${waterLevel.waterLevelType}")
        }
        val shieldLine = when (shielded) {
            null -> listOf()
            else -> {
                val player = getPlayer() ?: error("Player not found.")
                if (player.activeSceneShields.contains(this))
                    listOf("This room is shielded by your magic.")
                else
                    listOf("This room is shielded by magic.")
            }
        }
        return actors
            .asSequence()
            .filter { !it.isPlayer }
            .map { it.description(brief = true) }
            .toList()
            .plus(waterLine)
            .plus(shieldLine)
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
}
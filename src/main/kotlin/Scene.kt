sealed class Scene(player: Actor.Player) {
    var actors = mutableListOf<Actor>(player)

    fun describeScene() {
        actors
            .asSequence()
            .filter { !it.isPlayer }
            .map { it.description(brief = true) }
            .forEach { println(it) }
    }

    fun handleInput(userCommand: UserCommand) {
        action(userCommand)?.eventTrigger?.invoke(
            this,
            getPlayer(),
            userCommand.target
        )
    }

    fun getPlayer(): Actor {
        return actors.first { it.isPlayer }
    }

    fun bringOutYerDead(): List<Actor> {
        var deadActors = listOf<Actor>()
        actors.forEach { actor ->
            actor.refreshInventory()?.let { deadActors = deadActors.plus(it) }
            if (!actor.isAlive())
                deadActors = deadActors.plus(actor)
        }
        actors = actors.filter { it !in deadActors }.toMutableList()
        return deadActors
    }

    class Opening(player: Actor.Player) : Scene(player) {
        // Testing: This will get more complex
        init {
            actors.add(Actor.PotionChest())
        }
    }
}
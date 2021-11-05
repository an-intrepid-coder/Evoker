sealed class Scene(player: Actor.Player) {
    var actors = mutableListOf<Actor>(player)

    fun describeScene() {
        // TODO: More complex descriptions.
        actors
            .asSequence()
            .filter { !it.isPlayer }
            .map { it.description() }
            .forEach { println(it) }
    }

    fun handleInput(userCommand: UserCommand) {
        // TODO: Create system of interaction which applies to not just the player, but all actors with AI Behavior,
        //  eventually.
    }

    fun targetContext(): List<String> {
        return actors
            .asSequence()
            .filter { !it.isPlayer && !it.hidden }
            .map { it.name }
            .toList()
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
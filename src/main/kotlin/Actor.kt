/**
 * Actors include not just creatures and the player, but any object which can be interacted
 * with or observed.
 */
sealed class Actor(
    val name: String,
    var maxHealth: Int? = null,
    val isFlavor: Boolean = false,
    val isPlayer: Boolean = false,
    var hidden: Boolean = false,
    var locked: Boolean = false,
    val areaTransitionId: Int? = null,
    var cameFrom: Boolean = false,
    var inventory: MutableList<Actor>? = null,
    var lootable: Boolean = false,
    val interactiveEffect: ((SceneMap?, Actor?, Actor?) -> List<String>)? = null,
    val behavior: ((SceneMap, Actor) -> List<String>)? = null
    // more params to come
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
        if (isFlavor)
            return interactiveEffect!!
                .invoke(null, null, null)
                .joinToString(" ")
        else if (areaTransitionId != null)
            return when (cameFrom) {
                true -> "You just came from this direction."
                else -> "This leads somewhere..."
            }
        val descriptionLines = listOf(
            "You see a $name.",
            when (health) {
                null -> "\n\tIt is indestructible."
                else -> "\n\tIt has $health/$maxHealth health."
            },
            when (hidden) {
                true -> "\n\tIt is hidden."
                else -> null
            },
            when (inventory != null) {
                true -> "\n\tIt has an inventory."
                else -> null
            },
            when (lootable) {
                true -> "\n\tIt is lootable."
                else -> null
            },
            when (locked && lootable) {
                true -> "\n\tIt is locked."
                else -> null
            },
            when(behavior) {
                null -> null
                else -> "\n\tIt seems capable of action."
            }
        )
        return descriptionLines
            .filterNotNull()
            .joinToString(" ")
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
        inventory = mutableListOf()
    )

    //class WanderingGolem TODO

    class DoorTo(scene: Scene) : Actor(
        name = "Door${scene.id}",
        areaTransitionId = scene.id,
        interactiveEffect = { sceneMap, _, triggerer ->
            val messages = mutableListOf<String>()
            sceneMap ?: error("SceneMap not found.")
            triggerer ?: error("Triggering actor not found.")
            sceneMap.activeScene ?: error("No active scene found in SceneMap.")
            sceneMap.activeScene!!.removeActor(triggerer)
            sceneMap.activeScene!!.clearCameFrom()
            val cameFromId = sceneMap.activeScene!!.id
            sceneMap.changeScene(scene.id).let { id ->
                if (id == null) error("Invalid sceneId: ${scene.id}.")
                messages.add("You walk through the door to " + sceneMap.activeScene!!.name + ".")
                sceneMap.activeScene!!.addActor(triggerer)
                sceneMap.activeScene!!.markCameFrom(cameFromId)
            }
            messages
        }
    )

    class Flavor(
        name: String,
        flavorText: String,
    ) : Actor(
        name = name,
        isFlavor = true,
        interactiveEffect = { _, _, _ ->
            listOf(flavorText + "\n")
        }
    )

    class HealingPotion : Actor(
        name = "Healing Potion",
        maxHealth = 1,
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

    class PotionChest : Actor(
        name = "Chest",
        maxHealth = 5,
        inventory = mutableListOf(randomPotion()), // for now
    )
}

fun randomPotion(): Actor {
    return listOf(
        Actor.HealingPotion(),
        // more to come
    ).random()
}
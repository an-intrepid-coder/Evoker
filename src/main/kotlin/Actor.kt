/**
 * Actors include not just creatures and the player, but any object which can be interacted
 * with or observed.
 */
sealed class Actor(
    val name: String,
    var maxHealth: Int = 10,
    val isFlavor: Boolean = false,
    val isPlayer: Boolean = false,
    var hidden: Boolean = false,
    var locked: Boolean = false,
    var animate: Boolean = false,
    val areaTransitionId: Int? = null,
    var inventory: MutableList<Actor>? = null,
    val interactiveEffect: ((SceneMap?, Actor?, Actor?) -> List<String>)? = null
    // more params to come
) {
    var health = maxHealth

    fun isAlive(): Boolean {
        return health > 0
    }

    fun description(brief: Boolean = false): String {
        if (brief)
            return "You see a $name."
        if (isFlavor)
            return interactiveEffect!!
                .invoke(null, null, null)
                .joinToString(" ")
        else if (areaTransitionId != null)
            return "This leads somewhere..."
        val descriptionLines = listOf(
            "You see a $name.",
            "\n\tIt has $health/$maxHealth health.",
            when (hidden) {
                true -> "\n\tIt is hidden."
                else -> null
            },
            when (inventory != null) {
                true -> "\n\tIt has an inventory."
                else -> null
            },
        )
        return descriptionLines
            .filterNotNull()
            .joinToString(" ")
    }

    /**
     * Returns the new health after the change.
     */
    fun changeHealth(amount: Int): Int {
        health += amount
        if (health > maxHealth) health = maxHealth
        return health
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
        val deadInventory = inventory?.filter { !it.isAlive() }
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
        animate = true,
        inventory = mutableListOf()
    )

    class DoorTo(scene: Scene) : Actor(
        name = "Door${scene.id}",
        areaTransitionId = scene.id,
        interactiveEffect = { sceneMap, _, _ ->
            val messages = mutableListOf<String>()
            sceneMap?.changeScene(scene.id).let { id ->
                if (id == null) error("Invalid sceneId: ${scene.id}.")
                messages.add("You walk through the door towards " + sceneMap!!.activeScene!!.name + ".")
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
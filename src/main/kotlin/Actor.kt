/**
 * Actors include not just creatures and the player, but any object which can be interacted
 * with or observed.
 */
sealed class Actor(
    val name: String,
    var maxHealth: Int = 10,
    val isPlayer: Boolean = false,
    var hidden: Boolean = false,
    var locked: Boolean = false,
    var animate: Boolean = false,
    var interactive: Boolean = false,
    val areaTransition: Boolean = false,
    var inventory: MutableList<Actor>? = null,
    val eventTrigger: ((Scene, Actor, Actor?) -> Unit)? = null
    // more params to come
) {
    var health = maxHealth

    fun isAlive(): Boolean {
        return health > 0
    }

    fun description(brief: Boolean = false): String {
        if (brief)
            return "You see a $name."
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
     * Returns true or false if the actor lives or dies from the change.
     */
    fun changeHealth(amount: Int): Boolean {
        health += amount
        if (health < 0) return false
        else if (health > maxHealth) health = maxHealth
        return true
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

    class HealingPotion : Actor(
        name = "Healing Potion",
        maxHealth = 1,
        interactive = true,
        eventTrigger = { _, self, triggerer ->
            // Heals the user and then self-destructs.
            val healAmountRange = 5..10
            triggerer?.changeHealth(healAmountRange.random())
            self.changeHealth(-1)
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
fun randomPotion(): Actor {
    return listOf(
        Nectar(),
        // more to come
    ).random()
}

/**
 * Checks for Actors with the same name in the scene, and appends numbers next to them if there is more than one,
 * for ease of targeting.
 */
fun handleDuplicateActors(actorList: List<Actor>) {
    actorList.forEach { it.name = it.name.filter { !it.isDigit() } }
    val chunked = mutableListOf<MutableList<Actor>>()
    actorList.forEach { actor ->
        if (chunked.none { it.any { it.name == actor.name } })
            chunked.add(mutableListOf(actor))
        else
            chunked.first { it.any { it.name == actor.name } }
                .add(actor)
    }
    chunked.filter { it.size > 1 }.forEach { actorSublist ->
        actorSublist.forEachIndexed { index, actor ->
            actor.name = actor.name + index
        }
    }
}

enum class AttunementType {
    WATER,
    SHIELD,
    // More to come!
}

/**
 * Actors include not just creatures and the player, but any object which can be interacted
 * with or observed. The instances of Actor each only interact with a fraction of the base class's
 * capabilities.
 */
sealed class Actor(
    var name: String,
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
    val attunements: MutableSet<AttunementType> = mutableSetOf(),
    val activeSceneShields: MutableList<Scene> = mutableListOf(),
    val shieldBreaker: Boolean = false
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
            when (attunements.isEmpty()) {
                true -> null
                else -> "\tAttuned to: " + attunements.joinToString(", ") { it.toString().lowercase() }
            }
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

    /**
     * Attempts to add a spell type to an Actor's spellbook.
     */
    fun addSpell(spellName: String): String? {
        if (this.spellBook.contains(spellName)) {
            if (this.isPlayer)
                return "You already know the secrets of this book."
        } else {
            this.spellBook.add(spellName)
            if (this.isPlayer)
                return "You learn the secrets of $spellName magic!"
        }
        return null
    }
}

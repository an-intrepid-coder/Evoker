class GenericChest(
    inventory: List<Actor>,
    locked: Boolean = false
) : Actor(
    name = "Chest",
    maxHealth = 5,
    inventory = inventory.toMutableList(),
    locked = locked,
    lootable = true
)
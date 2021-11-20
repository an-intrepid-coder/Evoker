class Nectar : Actor(
    name = "Nectar",
    maxHealth = 1,
    additionalDescriptionLines = listOf(
        "\tThis softly glowing vial is full of a substance known as 'nectar'.",
        "\tIt is known for its curative properties."
    ),
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
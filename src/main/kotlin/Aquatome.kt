class Aquatome : Actor(
    name = "Aquatome",
    maxHealth = 1, // The health of this object may change as the game develops
    additionalDescriptionLines = listOf(
        "\tThis spellbook teaches the secrets of water magic.",
        "\tAt its most basic, water magic attunes the caster to its element.",
        "\tIt can also be used to raise or lower the water level in an area.",
        // more to come
    ),
    interactiveEffect = { _, _, triggerer ->
        triggerer ?: error("Triggering actor not found.")
        val messages = mutableListOf<String>()
        triggerer.addSpell("water")?.let { messages.add(it) }
        messages
    },
) {
    init { attunements.add(AttunementType.WATER) }
    /*
        Note: I'm thinking of allowing the spellbooks to be harmed or improved or otherwise altered when spells are
        used on them, in which case their elemental may matter.
     */
}
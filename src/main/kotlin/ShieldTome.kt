class ShieldTome : Actor(
    name = "Shieldtome",
    maxHealth = 1,
    additionalDescriptionLines = listOf(
        "\tThis spellbook teaches the secrets of shield magic.",
        "\tAt its most basic, shield magic covers the caster in a damage-mitigating shield.",
        "\tIt can also be used to shield an entire area from enemies or the environment.",
    ),
    interactiveEffect = { _, _, triggerer ->
        triggerer ?: error("Triggering actor not found.")
        val messages = mutableListOf<String>()
        triggerer.addSpell("shield")?.let { messages.add(it) }
        messages
    }
) {
    init { attunements.add(AttunementType.SHIELD) }
}
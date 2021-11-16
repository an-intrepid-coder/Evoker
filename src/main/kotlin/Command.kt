val validCommands = listOf(
    "water",
    "wait",
    "strike",
    "examine",
    "use",
    "loot",
    "help",
    "exit",
    "debug"
)

/**
 * This is the "sentence" of the game's language. All commands consist of a base command which
 * can be optionally adjusted by a list of potential modifiers, and in some cases a target which
 * depends on given context environment.
 */
class Command(
    private val raw: String,
    private val targetEnvironment: List<Actor>,
) {
    var base: String? = null
    val potentialModifiers = mutableListOf<String>()
    var target: Actor? = null

    init {
        val targets = targetEnvironment.zip(
            targetEnvironment.map { actor ->
                if (actor.isPlayer)
                    "self"
                else
                    actor.name.lowercase()
            }
        )

        val words = raw
            .lowercase()
            .split(" ")

        base = words.firstOrNull { validCommands.contains(it) }
        target = targets.firstOrNull { words.contains(it.second) }?.first
        words.forEach { word ->
            if (word !in validCommands && word !in targets.map { it.second })
                potentialModifiers.add(word)
        }
    }

    fun printed(): String {
        return "($base -> ${target?.name ?: "No Target"}; $potentialModifiers)"
    }
}
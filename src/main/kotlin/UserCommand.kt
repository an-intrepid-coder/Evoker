val validCommands = listOf(
    "move",
    "strike",
    "examine",
    "handle",
    "loot",
    "exit"
)

/**
 * This is the "sentence" of the game's language. All commands consist of a base command which
 * can be optionally adjusted by a list of potential modifiers, and in some cases a target which
 * depends on given context environment.
 */
class UserCommand(
    val raw: String,
    val targetEnvironment: List<Actor>,
) {
    var command: String? = null
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
        command = words.firstOrNull { validCommands.contains(it) }
        target = targets.firstOrNull { words.contains(it.second) }?.first
        words.forEach { word ->
            if (word !in validCommands && word !in targets.map { it.second })
                potentialModifiers.add(word)
        }
    }

    fun printed(): String {
        return "($command -> ${target?.name}; $potentialModifiers)"
    }
}
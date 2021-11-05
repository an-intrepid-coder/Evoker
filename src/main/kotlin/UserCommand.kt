val validCommands = listOf(
    "move",
    "strike",
    "grab",
    "open",
    "exit"
)

/**
 * This is the "sentence" of the game's language. All commands consist of a base command which
 * can be optionally adjusted by a list of potential modifiers, and in some cases a target which
 * depends on given context environment.
 */
class UserCommand(
    val raw: String,
    val targetEnvironment: List<String>,
) {
    var command: String? = null
    val potentialModifiers = mutableListOf<String>()
    var target: String? = null

    init {
        val words = raw.split(" ")
        command = words.firstOrNull { validCommands.contains(it) }
        target = words.firstOrNull { targetEnvironment.contains(it) }
        words.forEach {
            if (it !in validCommands && it !in targetEnvironment)
                potentialModifiers.add(it)
        }
    }

    fun printed(): String {
        return "($command -> $target; $potentialModifiers)"
    }
}
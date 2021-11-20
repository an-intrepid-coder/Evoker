val waitingLines = listOf(
    "You twiddle your thumbs for a bit.",
    "You procrastinate like a pro.",
    "You are lost in thought for a moment.",
    "You stare at your feet for a bit.",
    "You quietly laugh at a funny joke you just thought of.",
    "You focus your concentration.",
    "You take a few deep breaths.",
    "You plan your next move.",
    "You hum a tune.",
)

/**
 * Waiting does nothing but advance the simulation a turn, for now. I will eventually include mechanics which
 * make waiting a tactical choice.
 */
class Wait(command: Command) : Action(
    command = command,
    effect = { _, self, _ ->
        self ?: error("Caller not found.")
        val messages = mutableListOf<String>()
        if (self.isPlayer)
            messages.add(waitingLines.random())
        messages
    }
)

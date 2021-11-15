data class Message(
    val id: Int,
    val turnOf: Int,
    val text: String,
)

class MessageLog {
    var unreadMessages = mutableListOf<Message>()
    val readMessages = mutableListOf<Message>()
    var numMessages = 0

    init {
        unreadMessages.add(Message(numMessages++, 0, "Welcome to Evoker!"))
    }

    fun messageIn(msg: Message) {
        unreadMessages.add(msg)
    }

    fun messageIn(turn: Int, text: String) {
        unreadMessages.add(Message(numMessages++, turn, text))
    }

    private fun messageOut(): Message? {
        return unreadMessages.removeFirstOrNull()?.let { msg ->
            readMessages.add(msg)
            msg
        }
    }

    fun outputAll(): List<Message> {
        val messages = mutableListOf<Message>()
        while (unreadMessages.isNotEmpty()) {
            messageOut()?.let { messages.add(it) }
        }
        return messages
    }
}
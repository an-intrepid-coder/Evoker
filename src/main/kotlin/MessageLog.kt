class MessageLog {
    var unreadMessages = mutableListOf("Welcome to Evoker!\n")
    val readMessages = mutableListOf<String>()

    fun messageIn(msg: String) {
        unreadMessages.add(msg)
    }

    private fun messageOut(): String? {
        return unreadMessages.removeFirstOrNull()?.let { msg ->
            readMessages.add(msg)
            msg
        }
    }

    fun outputAll(): List<String> {
        val messages = mutableListOf<String>()
        while (unreadMessages.isNotEmpty()) {
            messageOut()?.let { messages.add(it) }
        }
        return messages
    }
}
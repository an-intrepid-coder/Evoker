class GolemFootprints(
    turnsUntilExpiration: Int,
    additionalDescriptionLines: List<String>,
) : Actor(
    name = "Footprints",
    timer = turnsUntilExpiration,
    additionalDescriptionLines = additionalDescriptionLines,
    behavior = { _, self ->
        val messages = mutableListOf<String>()
        self.timer = self.timer!! - 1
        if (self.timer!! <= 0) self.kill()
        messages
    }
)
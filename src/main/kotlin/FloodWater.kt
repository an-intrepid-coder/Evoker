const val floodWaterTimer = 20 // for now

class FloodWater : Actor(
    name = "Water",
    additionalDescriptionLines = listOf("You see flood water."),
    timer = floodWaterTimer,
    behavior = { scene, self ->
        /*
            FloodWater instances cause the water level in a room to rise. They disappear when the flood source
            is plugged and the water level is NONE. They spread FloodWater to neighboring Scenes.
         */
        val messages = mutableListOf<String>()

        if (scene.waterLevel.waterLevelType == WaterLevel.WaterLevelType.NONE && !scene.floodSource) {
            self.kill()
            messages
        } else {
            if (scene.floodSource && scene.shielded == null) {
                scene.actors.removeAll { it.name == "Footprints" }
                self.timer = self.timer!! - 1
                if (self.timer == 0) {
                    scene.waterLevel = scene.waterLevel.increment()
                    self.timer = floodWaterTimer
                }
            }

            self.additionalDescriptionLines = when (scene.waterLevel.waterLevelType) {
                // TODO: When water-level effects are implemented it will give additional lines for each level.
                //  Those lines will describe the effects to the player.
                WaterLevel.WaterLevelType.ANKLES -> listOf("The water is up to your ankles.")
                WaterLevel.WaterLevelType.KNEES -> listOf("The water is up to your knees.")
                WaterLevel.WaterLevelType.WAIST -> listOf("The water is up to your waist.")
                WaterLevel.WaterLevelType.CHEST-> listOf("The water is up to your chest.")
                WaterLevel.WaterLevelType.UNDERWATER-> listOf("You are submerged under water.")
                WaterLevel.WaterLevelType.NONE -> listOf("The floor is slippery and wet..")
            }

            scene.neighbors().forEach { neighbor ->
                if (neighbor.waterLevel.waterLevelType == WaterLevel.WaterLevelType.NONE &&
                    neighbor.shielded == null) {
                    neighbor.waterLevel = neighbor.waterLevel.increment()
                    neighbor.addActor(FloodWater())
                }
            }

            messages
        }
    }
) {
    init {
        attunements.add(AttunementType.WATER)
    }
}
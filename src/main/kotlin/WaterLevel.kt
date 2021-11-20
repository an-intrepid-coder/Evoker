sealed class WaterLevel(val waterLevelType: WaterLevelType) {
    // In progress. Not all the effects are implemented yet.
    enum class WaterLevelType {
        NONE, // No effect
        ANKLES, // Erase footprints, damage some items
        KNEES, // extra movement cost
        WAIST, // extra movement cost, unable to perform some actions
        CHEST, // extra movement cost, unable to perform most actions
        UNDERWATER, // turn-by-turn damage and swept into adjacent rooms with the flow of water, if possible.
        // Unless the player is attuned to water, in which case it will be harmless.
        // * Extra movement cost can be an increasing chance of losing a turn.
    }

    open fun increment(): WaterLevel { return this }
    open fun decrement(): WaterLevel { return this }

    class None : WaterLevel(WaterLevelType.NONE) {
        override fun increment(): WaterLevel {
            return Ankles()
        }
        override fun decrement(): WaterLevel {
            return this
        }
    }

    class Ankles : WaterLevel(WaterLevelType.ANKLES) {
        override fun increment(): WaterLevel {
            return Knees()
        }
        override fun decrement(): WaterLevel {
            return None()
        }
    }

    class Knees : WaterLevel(WaterLevelType.KNEES) {
        override fun increment(): WaterLevel {
            return Waist()
        }
        override fun decrement(): WaterLevel {
            return Ankles()
        }
    }

    class Waist : WaterLevel(WaterLevelType.WAIST) {
        override fun increment(): WaterLevel {
            return Chest()
        }
        override fun decrement(): WaterLevel {
            return Knees()
        }
    }

    class Chest : WaterLevel(WaterLevelType.CHEST) {
        override fun increment(): WaterLevel {
            return Underwater()
        }
        override fun decrement(): WaterLevel {
            return Waist()
        }
    }

    class Underwater : WaterLevel(WaterLevelType.UNDERWATER) {
        override fun increment(): WaterLevel {
            return this
        }
        override fun decrement(): WaterLevel {
            return Chest()
        }
    }
}
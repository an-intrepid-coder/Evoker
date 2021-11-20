class WanderingGolem : Actor(
    name = "Golem",
    maxHealth = 200,
    inventory = mutableListOf(),
    retaliating = false,
    additionalDescriptionLines = listOf(
        "\tThis beast is a hulking monstrosity of stone and steel.",
        "\tIt seems to be paying you no mind."
    ),
    pathMemory = mutableListOf(),
    pathMemorySizeLimit = 8, // for now
    behavior = { scene, self ->
        /*
            The Wandering Golem has two modes: retaliating and wandering. When retaliating it will trade blows
            with the player for as long as the player attacks it, which is a losing proposition as this is meant
            to be a mini-boss that is not easily bested via combat. When not retaliating it will wander randomly,
            accruing territory in its pathMemory until it is full. Once its pathMemory is full then it will
            stay within that territory, giving the player a chance to map out its domain.

            TODO: Some sort of puzzle-oriented way of fighting the creature for the player, and a reason for
                doing so.
         */
        val messages = mutableListOf<String>()
        when (self.retaliating) {
            true -> {
                scene.getPlayer()?.let { player ->
                    messages.add("${self.name} strikes you in retaliation!")
                    Strike(Command(
                        raw = "strike player",
                        targetEnvironment = scene.actors
                    )).effect!!.invoke(scene, self, player).forEach { messages.add(it) }
                    self.retaliating = false
                }
            }
            else -> {
                // Remove footprints
                scene.actors.removeAll { it.name == "Footprints" }

                // Add path if needed
                if (scene.id !in self.pathMemory!! && self.pathMemory.size < self.pathMemorySizeLimit!!)
                    self.pathMemory.add(scene.id)

                // Evaluate available areaTransitions and choose one appropriately.
                val newTransitions = scene.actors.filter {
                    it.areaTransitionId != null && it.areaTransitionId !in self.pathMemory
                }
                val knownTransitions = scene.actors.filter { it.areaTransitionId in self.pathMemory }
                val areaTransition = if (self.pathMemory.size >= self.pathMemorySizeLimit!!)
                    knownTransitions.random()
                else if (newTransitions.isEmpty())
                    knownTransitions.random()
                else
                    newTransitions.random()

                // Add footprints
                scene.addActor(GolemFootprints(
                    turnsUntilExpiration = 6, // for now
                    additionalDescriptionLines = listOf(
                        "\tSomething enormous made these footprints.",
                        "\tThey lead towards ${areaTransition.name}..."
                    )
                ))

                // Move the Golem
                Use(Command(
                    raw = "use ${areaTransition.name}",
                    targetEnvironment = scene.actors,
                )).effect!!.invoke(scene, self, areaTransition)

                // Notify player if in the same Scene.
                scene.getPlayer()?.let {
                    messages.add("The Golem's massive footsteps unsettle the ground as it leaves.")
                }

                // There is a chance that the Golem will cause a room to spring a leak when it moves:
                val leakChance = 1
                if (withChance(100, leakChance) && !scene.floodSource) {
                    scene.floodSource = true
                    scene.addActor(FloodWater())
                    scene.getPlayer()?.let {
                        messages.add("Water begins to pour through a crack in the wall!")
                    }
                }

            }
        }
        messages
    },
    shieldBreaker = true,
)
class SceneMap(val parentGame: EvokerGame) {
    var numScenes = 0
    val scenes = mutableMapOf<Int, Scene>()
    var activeScene: Scene? = null

    val maxHallways = 3 // for now, but the game will be bigger when it's further along.
    var numHallways = 0

    // TODO: A variety of switches and triggers to guide map generation.

    init {
        addScene(Scene.Opening(this)).let {
            activeScene = scenes[it]
            activeScene!!.addActor(Actor.Player())
        }
        scenes.values.random().addActor(Actor.WanderingGolem())
    }

    /**
     * This is a debugging function which prints all available information on the SceneMap's graph and the
     * status of each Scene. I may adapt it later into a number of in-game functions, but for now it is a debugging
     * function only.
     */
    fun printSceneMap(): List<String> {
        val messages = mutableListOf<String>()

        fun printScenesAsTree(
            depth: Int,
            node: Scene,
            seen: List<Scene>,
        ) {
            val playerHere = node.getPlayer() != null
            val golemHere = node.actors.any { it.name == "Golem" }
            var line = ""
            repeat (depth) { line += " " }
            if (depth > 0) line += "|_"
            line += "Scene #${node.id} | ${node.name} | @=$playerHere " +
                    "| Gol.=$golemHere | Wtr.=${node.waterLevel.waterLevelType} | flooding=${node.floodSource} " +
                    "| Shield=${node.shielded}"
            messages.add(line)
            node.neighbors().let { neighbors ->
                neighbors.asSequence()
                    .filter { it !in seen }
                    .forEach { neighbor ->
                        printScenesAsTree(
                            depth = depth + 1,
                            node = neighbor,
                            seen = seen.plus(node),
                        )
                    }
            }
        }

        val node = activeScene ?: error("No Active Scene!")
        printScenesAsTree(0, node, listOf())

        return messages
    }

    fun canAddHallways(): Boolean {
        return numHallways < maxHallways
    }

    fun addScene(scene: Scene): Int {
        scenes[scene.id] = scene
        return scene.id
    }

    /**
     * Returns the ID of the new active scene or null in the case of an invalid ID.
     */
    fun changeScene(sceneId: Int): Int? {
        return scenes[sceneId]?.let { scene ->
            activeScene = scene
            sceneId
        }
    }

    /**
     * Checks every scene for the existence of Actors with behavior functions and runs them,
     * returning the list of messages which results from their actions, if any.
     */
    fun behaviorCheck(): List<String> {
        val messages = mutableListOf<String>()
        val actorsWithBehavior = mutableMapOf<Actor, Scene>()
        scenes.values.forEach { scene ->
            scene.actors.asSequence()
                .filter { it.behavior != null }
                .forEach { actor ->
                    actorsWithBehavior[actor] = scene
                }
        }
        actorsWithBehavior.forEach { entry ->
            entry.key.behavior!!.invoke(entry.value, entry.key).forEach { msg ->
                messages.add(msg)
            }
        }
        return messages
    }

    fun shieldCheck(): List<String> {
        val messages = mutableListOf<String>()
        scenes.values.asSequence()
            .filter { it.shielded != null }
            .forEach { scene ->
                scene.shielded = scene.shielded!! - 1
                if (scene.shielded == 0) {
                    scene.shielded = null
                    scene.getPlayer()?.let { messages.add("The shield in this area shimmers before disappearing.") }
                }
            }
        return messages
    }

    /**
     * Refreshes the Actor list in all Scenes and returns the complete list of expired Actors.
     */
    fun refreshAllActors(): List<Actor> {
        val deadActors = mutableListOf<Actor>()
        scenes.values.forEach { scene ->
            scene.refreshActors().forEach { deadActors.add(it) }
        }
        return deadActors
    }
}
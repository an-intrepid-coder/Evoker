class SceneMap {
    var numScenes = 0
    val scenes = mutableMapOf<Int, Scene>()
    var activeScene: Scene? = null

    val maxHallways = 3 // for now
    var numHallways = 0

    // TODO: A variety of switches and triggers to guide map generation.

    init {
        addScene(Scene.Opening(this)).let {
            activeScene = scenes[it]
            activeScene!!.addActor(Actor.Player())
        }
        scenes.values.random().addActor(Actor.WanderingGolem())
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
        /*
            Debug Note: Will need to find another way to do this, as it currently causes a concurrency
                exception. The idea is sound, but the syntax is off, as no concurrency error would occur.
                Best to find the idiom for it.
         */
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
}
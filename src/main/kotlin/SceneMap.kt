class SceneMap {
    var numScenes = 0
    val scenes = mutableMapOf<Int, Scene>()
    var activeScene: Scene? = null

    val maxHallways = 3 // for now
    var numHallways = 0

    fun canAddHallways(): Boolean {
        return numHallways < maxHallways
    }

    // TODO: A variety of switches and triggers to guide map generation.

    init {
        addScene(Scene.Opening(this)).let {
            activeScene = scenes[it]
            activeScene!!.addActor(Actor.Player())
        }
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
}
class SceneMap {
    var numScenes = 0
    val scenes = mutableMapOf<Int, Scene>()
    var activeScene: Scene? = null

    init { // TODO: Tie the whole map together and determine procedural order of operations:
        addScene(Scene.Opening(this)).let {
            activeScene = scenes[it]
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
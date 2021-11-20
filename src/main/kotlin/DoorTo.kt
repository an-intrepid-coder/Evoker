class DoorTo(targetScene: Scene) : Actor(
    name = "Door",
    areaTransitionId = targetScene.id,
    interactiveEffect = { scene, _, triggerer ->
        val messages = mutableListOf<String>()
        scene ?: error("Scene not found.")
        triggerer ?: error("Triggering actor not found.")
        val sceneMap = scene.parentSceneMap
        sceneMap.activeScene ?: error("No active scene found in SceneMap.")
        val playerInScene = scene.getPlayer() != null

        fun playerBlockedByShield(): Boolean {
            return targetScene.shielded != null
                    && triggerer.isPlayer
                    && !triggerer.activeSceneShields.contains(scene)
        }

        fun blockedByShield(): Boolean {
            return targetScene.shielded != null
                    && !triggerer.activeSceneShields.contains(targetScene)
                    && !triggerer.shieldBreaker
        }

        if (playerBlockedByShield()) {
            messages.add("A shimmering shield blocks your path.")
        } else if (blockedByShield() && playerInScene) {
            messages.add("${triggerer.name} blocked by shield magic.")
        } else if (!blockedByShield()) {
            scene.removeActor(triggerer)
            if (triggerer.isPlayer) {
                scene.clearCameFrom()
                sceneMap.changeScene(targetScene.id).let { id ->
                    if (id == null) error("Invalid sceneId: ${targetScene.id}.")
                    messages.add("You walk through the door to " + sceneMap.activeScene!!.name + ".")
                    sceneMap.activeScene!!.markCameFrom(scene.id)
                    handleDuplicateActors(sceneMap.activeScene!!.actors)
                }
            }
            sceneMap.scenes[targetScene.id]!!.addActor(triggerer)
        }

        messages
    }
)
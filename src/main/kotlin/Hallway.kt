val hallwayNames = listOf(
    "A Winding Hallway",
    "A Long, Straight Hallway",
    "A Dark Hallway",
    "A Dirty Hallway",
    "An Uneven, Damaged Hallway",
    // more to come
)

/**
 * Hallways serve as the main vehicles of map generation. Their number is limited in the SceneMap to
 * avoid infinite growth.
 */
class Hallway( // TODO: More Hallway types.
    parentSceneMap: SceneMap,
    cameFrom: Scene,
) : Scene(
    name = hallwayNames.random(),
    parentSceneMap = parentSceneMap,
    leafNode = false,
) {
    init {
        val additionalConnections = (1..3).random() // for now; may adjust this
        var addedHallway = false
        addActor(DoorTo(cameFrom))
        repeat (additionalConnections) {
            if (!addedHallway && parentSceneMap.canAddHallways()) {
                // Note: For now, this avoids having more than one hallway extend from any given hallway. This
                //  is an arbitrary limitation, and I will eventually change it to create more interesting maps.
                parentSceneMap.numHallways++
                Hallway(parentSceneMap, this).let { hallway ->
                    addActor(DoorTo(hallway))
                    parentSceneMap.addScene(hallway)
                }
                addedHallway = true
            }
            else
            // Tentative: TODO: More types of rooms and a factory function.
                Cell(parentSceneMap, this).let { cell ->
                    addActor(DoorTo(cell))
                    parentSceneMap.addScene(cell)
                }
        }
        // TODO: More doors and some other features of a hallway. Perhaps enemies and loot?
    }
}
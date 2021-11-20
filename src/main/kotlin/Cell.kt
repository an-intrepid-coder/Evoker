val cellNames = listOf(
    "A Filthy Cell",
    "A Gory Cell",
    "Another Cell",
    "A Dirty Cell",
    "An Unremarkable Cell",
    "A Dilapidated Cell"
    // more to come
)

val cellFlavors = listOf(
    PureFlavor(
        name = "floor",
        additionalDescriptionLines = listOf("\tThe floor is covered in a fine layer of dust.")
    ),
    PureFlavor(
        name = "floor",
        additionalDescriptionLines = listOf("\tThe floor is covered in bloody hand- and foot-prints.")
    ),
    PureFlavor(
        name = "walls",
        additionalDescriptionLines = listOf("\tThe walls have bolts for restraining people with chains.")
    ),
    PureFlavor(
        name = "walls",
        additionalDescriptionLines = listOf("\tThe walls are windowless and bleak.")
    ),
    PureFlavor(
        name = "walls",
        additionalDescriptionLines = listOf("\tThe walls are covered in indecipherable scribblings.")
    ),
    PureFlavor(
        name = "air",
        additionalDescriptionLines = listOf("\tThe air in this place is dank and evil.")
    ),
    // More to come
)

/**
 * Cells serve as leaf-nodes for the SceneMap, for now.
 */
// TODO: More kinds of leaf-nodes.
class Cell(
    parentSceneMap: SceneMap,
    cameFrom: Scene
) : Scene(
    name = cellNames.random(),
    parentSceneMap = parentSceneMap,
    leafNode = true
) {
    init {
        addActor(DoorTo(cameFrom))
        addActor(cellFlavors.random())
        // TODO: Perhaps potential enemies and loot?
    }
}
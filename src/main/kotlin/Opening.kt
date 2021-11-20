/**
 * The Opening Scene serves a "seed" from which the rest of the game grows procedurally. It will get more
 * complex and varied as development continues. The entire scene graph unfolds from this root point.
 */
class Opening(
    parentSceneMap: SceneMap,
) : Scene(
    name = "Your Filthy Cell",
    parentSceneMap = parentSceneMap,
    leafNode = true,
) {
    init {
        addActor(PureFlavor(
            name = "cell",
            additionalDescriptionLines = listOf(
                "\tYou don't remember how long you've been here.",
                "\tIn fact, you don't remember anything prior to this filthy cell.",
                "\tHow long have you been here? Who are you?",
                "\tFor some reason your cell door is wide open.",
                "\tThere is nothing stopping you from walking out..."
            )
        ))

        Hallway(parentSceneMap, this).let { hallway ->
            addActor(DoorTo(hallway))
            parentSceneMap.numHallways++
            parentSceneMap.addScene(hallway)
        }
    }
}
fun main(args: Array<String>) {
    //EvokerGame().play()
    crashTest(1000, 100000)
}

/**
 * A very simple test which makes sure that none of the emergent and procedural systems will cause a crash over time,
 * if left to their own devices.
 */
fun crashTest(
    numTests: Int,
    numTurnsPerTest: Int
) {
    println("Testing...")
    repeat (numTests) { EvokerGame().autoPlay(numTurnsPerTest) }
    println("Test passed! No crashes.")
}
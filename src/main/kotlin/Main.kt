import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun main(args: Array<String>) {
    //EvokerGame().play()
    crashTest(1000, 10000)
}

/**
 * A very simple test which makes sure that none of the emergent and procedural systems will cause a crash over time,
 * if left to their own devices.
 */
suspend fun crashTest(
    numTests: Int,
    numTurnsPerTest: Int
) {
    coroutineScope {
        println("Testing...")

        repeat (numTests) { test ->
            launch {
                EvokerGame().autoPlay(numTurnsPerTest)
                println("Finished test #$test")
                if (test == numTests - 1)
                    println("Test passed! No crashes.")
            }
        }

    }
}
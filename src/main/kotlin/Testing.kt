import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

data class TestResult(val passed: Boolean, val infoText: String)

/**
 * A very simple test which makes sure that none of the emergent and procedural systems will cause a crash over time,
 * if left to their own devices. Currently, measures the following metrics:
 *
 * 1. Average time required to simulate the given number of turns per test.
 * 2. Average number of FloodWater Actors at the end of each test. This is to ensure that no exponential growth is
 *    happening.
 */
@OptIn(ExperimentalTime::class)
suspend fun parallelStressTest(
    numTests: Int,
    numTurnsPerTest: Int,
): TestResult {
    var sampleSize = 0
    var averageTime = 0.0
    var floodWaterAverage = 0
    coroutineScope {

        val jobs = mutableListOf<Job>()

        repeat (numTests) {
            jobs.add(launch {
                val game = EvokerGame()
                val time = measureTime { game.passiveCrashTest(numTurnsPerTest) }
                averageTime += time.toDouble(DurationUnit.SECONDS)
                floodWaterAverage += game.sceneMap.scenes.values.asSequence()
                    .map { it.actors }
                    .map { it.filter { it.name == "Water" } }
                    .flatten()
                    .toList()
                    .size
                sampleSize++
            })
        }

        jobs.forEach { it.join() }
    }
    averageTime /= sampleSize
    floodWaterAverage /= sampleSize
    return TestResult(
        passed = true,
        infoText = "Average time over $numTests tests w/ $numTurnsPerTest turns/test: $averageTime seconds." +
                "\nAverage # of FloodWater Actors per test: $floodWaterAverage"
    )
}

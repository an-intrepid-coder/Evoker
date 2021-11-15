import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

data class TestResult(val passed: Boolean, val infoText: String)

/**
 * A very simple test which makes sure that none of the emergent and procedural systems will cause a crash over time,
 * if left to their own devices.
 */
@OptIn(ExperimentalTime::class)
suspend fun parallelStressTest(
    numTests: Int,
    numTurnsPerTest: Int,
): TestResult {
    var sampleSize = 0
    var averageTime = 0.0
    coroutineScope {
        repeat (numTests) {
            launch {
                val time = measureTime { EvokerGame().passiveCrashTest(numTurnsPerTest) }
                averageTime += time.toDouble(DurationUnit.SECONDS)
                sampleSize++
            }
        }
    }
    averageTime /= sampleSize
    return TestResult(
        passed = true,
        infoText = "Average time over $numTests tests w/ $numTurnsPerTest turns/test: $averageTime seconds."
    )
}

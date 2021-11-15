suspend fun main(args: Array<String>) {
    //EvokerGame().play()
    println("Testing...")
    val testResult = parallelStressTest(500, 100000)
    println(testResult)
}
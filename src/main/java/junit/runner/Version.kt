package junit.runner

/**
 * This class defines the current version of JUnit
 */
object Version {

    fun id(): String {
        return "4.12"
    }

    @JvmStatic fun main(args: Array<String>) {
        println(id())
    }
}// don't instantiate

package junit.framework

import org.junit.Ignore
import org.junit.runner.Describable
import org.junit.runner.Description
import org.junit.runner.Request
import org.junit.runner.Runner
import org.junit.runner.manipulation.Filter
import org.junit.runner.manipulation.Filterable
import org.junit.runner.manipulation.NoTestsRemainException
import org.junit.runner.manipulation.Sortable
import org.junit.runner.manipulation.Sorter

class JUnit4TestAdapter @JvmOverloads constructor(// reflective interface for Eclipse
        val testClass: Class<*>, private val fCache: JUnit4TestAdapterCache = JUnit4TestAdapterCache.default) : Test, Filterable, Sortable, Describable {

    private val fRunner: Runner

    init {
        fRunner = Request.classWithoutSuiteMethod(testClass).runner
    }

    override fun countTestCases(): Int {
        return fRunner.testCount()
    }

    override fun run(result: TestResult) {
        fRunner.run(fCache.getNotifier(result, this))
    }

    // reflective interface for Eclipse
    val tests: List<Test>
        get() = fCache.asTestList(description)

    override fun getDescription(): Description {
        val description = fRunner.description
        return removeIgnored(description)
    }

    private fun removeIgnored(description: Description): Description {
        if (isIgnored(description)) {
            return Description.EMPTY
        }
        val result = description.childlessCopy()
        for (each in description.children) {
            val child = removeIgnored(each)
            if (!child.isEmpty) {
                result.addChild(child)
            }
        }
        return result
    }

    private fun isIgnored(description: Description): Boolean {
        return description.getAnnotation(Ignore::class.java) != null
    }

    override fun toString(): String {
        return testClass.name
    }

    @Throws(NoTestsRemainException::class)
    override fun filter(filter: Filter) {
        filter.apply(fRunner)
    }

    override fun sort(sorter: Sorter) {
        sorter.apply(fRunner)
    }
}
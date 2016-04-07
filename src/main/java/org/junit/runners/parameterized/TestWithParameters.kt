package org.junit.runners.parameterized

import java.util.Collections.unmodifiableList

import java.util.ArrayList

import org.junit.runners.model.TestClass

/**
 * A `TestWithParameters` keeps the data together that are needed for
 * creating a runner for a single data set of a parameterized test. It has a
 * name, the test class and a list of parameters.

 * @since 4.12
 */
class TestWithParameters(val name: String, val testClass: TestClass,
                         parameters: List<Any>) {

    val parameters: List<Any>

    init {
        notNull(name, "The name is missing.")
        notNull(testClass, "The test class is missing.")
        notNull(parameters, "The parameters are missing.")
        this.parameters = unmodifiableList(ArrayList(parameters))
    }

    override fun hashCode(): Int {
        val prime = 14747
        var result = prime + name.hashCode()
        result = prime * result + testClass.hashCode()
        return prime * result + parameters.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as TestWithParameters?
        return name == other.name
                && parameters == other.parameters
                && testClass == other.testClass
    }

    override fun toString(): String {
        return testClass.name + " '" + name + "' with parameters "
        +parameters
    }

    private fun notNull(value: Any?, message: String) {
        if (value == null) {
            throw NullPointerException(message)
        }
    }
}

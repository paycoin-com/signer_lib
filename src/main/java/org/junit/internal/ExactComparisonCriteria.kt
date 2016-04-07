package org.junit.internal

import org.junit.Assert

class ExactComparisonCriteria : ComparisonCriteria() {
    override fun assertElementsEqual(expected: Any, actual: Any) {
        Assert.assertEquals(expected, actual)
    }
}

package org.junit.internal

import org.junit.Assert

class InexactComparisonCriteria : ComparisonCriteria {
    var fDelta: Any

    constructor(delta: Double) {
        fDelta = delta
    }

    constructor(delta: Float) {
        fDelta = delta
    }

    override fun assertElementsEqual(expected: Any, actual: Any) {
        if (expected is Double) {
            Assert.assertEquals(expected, actual as Double, fDelta as Double)
        } else {
            Assert.assertEquals(expected as Float, actual as Float, fDelta as Float)
        }
    }
}
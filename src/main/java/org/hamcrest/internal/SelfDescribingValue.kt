package org.hamcrest.internal

import org.hamcrest.Description
import org.hamcrest.SelfDescribing

class SelfDescribingValue<T>(private val value: T) : SelfDescribing {

    override fun describeTo(description: Description) {
        description.appendValue(value)
    }
}

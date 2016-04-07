package org.hamcrest.internal

import org.hamcrest.SelfDescribing

class SelfDescribingValueIterator<T>(private val values: MutableIterator<T>) : Iterator<SelfDescribing> {

    override fun hasNext(): Boolean {
        return values.hasNext()
    }

    override fun next(): SelfDescribing {
        return SelfDescribingValue(values.next())
    }

    override fun remove() {
        values.remove()
    }
}

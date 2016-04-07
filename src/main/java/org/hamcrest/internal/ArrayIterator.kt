package org.hamcrest.internal

import java.lang.reflect.Array

class ArrayIterator(private val array: Any) : Iterator<Any> {
    private var currentIndex = 0

    init {
        if (!array.javaClass.isArray) {
            throw IllegalArgumentException("not an array")
        }
    }

    override fun hasNext(): Boolean {
        return currentIndex < Array.getLength(array)
    }

    override fun next(): Any {
        return Array.get(array, currentIndex++)
    }

    override fun remove() {
        throw UnsupportedOperationException("cannot remove items from an array")
    }
}

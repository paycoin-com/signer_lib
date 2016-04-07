package org.junit.internal.requests

import java.util.Comparator

import org.junit.runner.Description
import org.junit.runner.Request
import org.junit.runner.Runner
import org.junit.runner.manipulation.Sorter

class SortingRequest(private val request: Request, private val comparator: Comparator<Description>) : Request() {

    override val runner: Runner
        get() {
            val runner = request.runner
            Sorter(comparator).apply(runner)
            return runner
        }
}

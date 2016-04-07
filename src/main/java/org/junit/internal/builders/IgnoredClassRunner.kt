package org.junit.internal.builders

import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier

class IgnoredClassRunner(private val clazz: Class<*>) : Runner() {

    override fun run(notifier: RunNotifier) {
        notifier.fireTestIgnored(description)
    }

    override val description: Description
        get() = Description.createSuiteDescription(clazz)
}
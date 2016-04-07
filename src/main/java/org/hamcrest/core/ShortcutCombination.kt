package org.hamcrest.core

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

internal abstract class ShortcutCombination<T>(private val matchers: Iterable<Matcher<in T>>) : BaseMatcher<T>() {

    abstract override fun matches(o: Any): Boolean

    abstract override fun describeTo(description: Description)

    protected fun matches(o: Any, shortcut: Boolean): Boolean {
        for (matcher in matchers) {
            if (matcher.matches(o) == shortcut) {
                return shortcut
            }
        }
        return !shortcut
    }

    fun describeTo(description: Description, operator: String) {
        description.appendList("(", " $operator ", ")", matchers)
    }
}

/*  Copyright (c) 2000-2006 hamcrest.org
 */
package org.hamcrest.core

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Factory
import org.hamcrest.Matcher

import java.util.regex.Pattern

import java.lang.Integer.parseInt

/**
 * Provides a custom description to another matcher.
 */
class DescribedAs<T>(private val descriptionTemplate: String, private val matcher: Matcher<T>, values: Array<Any>) : BaseMatcher<T>() {
    private val values: Array<Any>

    init {
        this.values = values.clone()
    }

    override fun matches(o: Any): Boolean {
        return matcher.matches(o)
    }

    override fun describeTo(description: Description) {
        val arg = ARG_PATTERN.matcher(descriptionTemplate)

        var textStart = 0
        while (arg.find()) {
            description.appendText(descriptionTemplate.substring(textStart, arg.start()))
            description.appendValue(values[parseInt(arg.group(1))])
            textStart = arg.end()
        }

        if (textStart < descriptionTemplate.length) {
            description.appendText(descriptionTemplate.substring(textStart))
        }
    }

    override fun describeMismatch(item: Any, description: Description) {
        matcher.describeMismatch(item, description)
    }

    companion object {

        private val ARG_PATTERN = Pattern.compile("%([0-9]+)")

        /**
         * Wraps an existing matcher, overriding its description with that specified.  All other functions are
         * delegated to the decorated matcher, including its mismatch description.
         *
         *
         * For example:
         * describedAs("a big decimal equal to %0", equalTo(myBigDecimal), myBigDecimal.toPlainString())

         * @param description
         * *     the new description for the wrapped matcher
         * *
         * @param matcher
         * *     the matcher to wrap
         * *
         * @param values
         * *     optional values to insert into the tokenised description
         */
        @Factory
        fun <T> describedAs(description: String, matcher: Matcher<T>, vararg values: Any): Matcher<T> {
            return DescribedAs(description, matcher, values)
        }
    }
}

package org.junit.internal.runners.rules

import org.junit.runners.model.FrameworkMember

internal class ValidationError(member: FrameworkMember<*>, annotation: Class<out Annotation>, suffix: String) : Exception(String.format("The @%s '%s' %s", annotation.simpleName, member.name, suffix))

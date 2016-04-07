package org.junit.runners.model

import java.lang.reflect.Modifier

/**
 * Parent class for [FrameworkField] and [FrameworkMethod]

 * @since 4.7
 */
abstract class FrameworkMember<T : FrameworkMember<T>> : Annotatable {
    internal abstract fun isShadowedBy(otherMember: T): Boolean

    internal fun isShadowedBy(members: List<T>): Boolean {
        for (each in members) {
            if (isShadowedBy(each)) {
                return true
            }
        }
        return false
    }

    protected abstract val modifiers: Int

    /**
     * Returns true if this member is static, false if not.
     */
    val isStatic: Boolean
        get() = Modifier.isStatic(modifiers)

    /**
     * Returns true if this member is public, false if not.
     */
    val isPublic: Boolean
        get() = Modifier.isPublic(modifiers)

    abstract val name: String

    abstract val type: Class<*>

    abstract val declaringClass: Class<*>
}

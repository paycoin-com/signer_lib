package org.junit.runners.model

import java.lang.reflect.Field

import org.junit.runners.BlockJUnit4ClassRunner

/**
 * Represents a field on a test class (currently used only for Rules in
 * [BlockJUnit4ClassRunner], but custom runners can make other uses)

 * @since 4.7
 */
class FrameworkField internal constructor(
        /**
         * @return the underlying java Field
         */
        val field: Field?) : FrameworkMember<FrameworkField>() {

    init {
        if (field == null) {
            throw NullPointerException(
                    "FrameworkField cannot be created without an underlying field.")
        }
    }

    override val name: String
        get() = field.getName()

    override val annotations: Array<Annotation>
        get() = field.getAnnotations()

    override fun <T : Annotation> getAnnotation(annotationType: Class<T>): T {
        return field.getAnnotation(annotationType)
    }

    public override fun isShadowedBy(otherMember: FrameworkField): Boolean {
        return otherMember.name == name
    }

    protected override val modifiers: Int
        get() = field.getModifiers()

    /**
     * @return the underlying Java Field type
     * *
     * @see java.lang.reflect.Field.getType
     */
    override val type: Class<*>
        get() = field.getType()

    override val declaringClass: Class<*>
        get() = field.declaringClass

    /**
     * Attempts to retrieve the value of this field on `target`
     */
    @Throws(IllegalArgumentException::class, IllegalAccessException::class)
    operator fun get(target: Any): Any {
        return field.get(target)
    }

    override fun toString(): String {
        return field.toString()
    }
}

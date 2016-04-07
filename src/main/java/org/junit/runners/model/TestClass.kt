package org.junit.runners.model

import java.lang.reflect.Modifier.isStatic
import org.junit.internal.MethodSorter.NAME_ASCENDING
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Comparator
import java.util.LinkedHashMap
import java.util.LinkedHashSet

import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.internal.MethodSorter

/**
 * Wraps a class to be run, providing method validation and annotation searching

 * @since 4.5
 */
class TestClass
/**
 * Creates a `TestClass` wrapping `clazz`. Each time this
 * constructor executes, the class is scanned for annotations, which can be
 * an expensive process (we hope in future JDK's it will not be.) Therefore,
 * try to share instances of `TestClass` where possible.
 */
(
        /**
         * Returns the underlying Java class.
         */
        val javaClass: Class<*>?) : Annotatable {
    private val methodsForAnnotations: MutableMap<Class<out Annotation>, List<FrameworkMethod>>
    private val fieldsForAnnotations: MutableMap<Class<out Annotation>, List<FrameworkField>>

    init {
        if (javaClass != null && javaClass.constructors.size > 1) {
            throw IllegalArgumentException(
                    "Test class can only have one constructor")
        }

        val methodsForAnnotations = LinkedHashMap<Class<out Annotation>, List<FrameworkMethod>>()
        val fieldsForAnnotations = LinkedHashMap<Class<out Annotation>, List<FrameworkField>>()

        scanAnnotatedMembers(methodsForAnnotations, fieldsForAnnotations)

        this.methodsForAnnotations = makeDeeplyUnmodifiable(methodsForAnnotations)
        this.fieldsForAnnotations = makeDeeplyUnmodifiable(fieldsForAnnotations)
    }

    protected fun scanAnnotatedMembers(methodsForAnnotations: MutableMap<Class<out Annotation>, List<FrameworkMethod>>, fieldsForAnnotations: MutableMap<Class<out Annotation>, List<FrameworkField>>) {
        for (eachClass in getSuperClasses(javaClass)) {
            for (eachMethod in MethodSorter.getDeclaredMethods(eachClass)) {
                addToAnnotationLists(FrameworkMethod(eachMethod), methodsForAnnotations)
            }
            // ensuring fields are sorted to make sure that entries are inserted
            // and read from fieldForAnnotations in a deterministic order
            for (eachField in getSortedDeclaredFields(eachClass)) {
                addToAnnotationLists(FrameworkField(eachField), fieldsForAnnotations)
            }
        }
    }

    /**
     * Returns, efficiently, all the non-overridden methods in this class and
     * its superclasses that are annotated}.

     * @since 4.12
     */
    val annotatedMethods: List<FrameworkMethod>
        get() {
            val methods = collectValues(methodsForAnnotations)
            Collections.sort(methods, METHOD_COMPARATOR)
            return methods
        }

    /**
     * Returns, efficiently, all the non-overridden methods in this class and
     * its superclasses that are annotated with `annotationClass`.
     */
    fun getAnnotatedMethods(
            annotationClass: Class<out Annotation>): List<FrameworkMethod> {
        return Collections.unmodifiableList(getAnnotatedMembers(methodsForAnnotations, annotationClass, false))
    }

    /**
     * Returns, efficiently, all the non-overridden fields in this class and its
     * superclasses that are annotated.

     * @since 4.12
     */
    val annotatedFields: List<FrameworkField>
        get() = collectValues(fieldsForAnnotations)

    /**
     * Returns, efficiently, all the non-overridden fields in this class and its
     * superclasses that are annotated with `annotationClass`.
     */
    fun getAnnotatedFields(
            annotationClass: Class<out Annotation>): List<FrameworkField> {
        return Collections.unmodifiableList(getAnnotatedMembers(fieldsForAnnotations, annotationClass, false))
    }

    private fun <T> collectValues(map: Map<*, List<T>>): List<T> {
        val values = LinkedHashSet<T>()
        for (additionalValues in map.values) {
            values.addAll(additionalValues)
        }
        return ArrayList(values)
    }

    /**
     * Returns the class's name.
     */
    val name: String
        get() {
            if (javaClass == null) {
                return "null"
            }
            return javaClass.name
        }

    /**
     * Returns the only public constructor in the class, or throws an `AssertionError` if there are more or less than one.
     */

    val onlyConstructor: Constructor<*>
        get() {
            val constructors = javaClass!!.constructors
            Assert.assertEquals(1, constructors.size.toLong())
            return constructors[0]
        }

    /**
     * Returns the annotations on this class
     */
    override val annotations: Array<Annotation>
        get() {
            if (javaClass == null) {
                return arrayOfNulls(0)
            }
            return javaClass.annotations
        }

    override fun <T : Annotation> getAnnotation(annotationType: Class<T>): T? {
        if (javaClass == null) {
            return null
        }
        return javaClass.getAnnotation(annotationType)
    }

    fun <T> getAnnotatedFieldValues(test: Any,
                                    annotationClass: Class<out Annotation>, valueClass: Class<T>): List<T> {
        val results = ArrayList<T>()
        for (each in getAnnotatedFields(annotationClass)) {
            try {
                val fieldValue = each.get(test)
                if (valueClass.isInstance(fieldValue)) {
                    results.add(valueClass.cast(fieldValue))
                }
            } catch (e: IllegalAccessException) {
                throw RuntimeException(
                        "How did getFields return a field we couldn't access?", e)
            }

        }
        return results
    }

    fun <T> getAnnotatedMethodValues(test: Any,
                                     annotationClass: Class<out Annotation>, valueClass: Class<T>): List<T> {
        val results = ArrayList<T>()
        for (each in getAnnotatedMethods(annotationClass)) {
            try {
                /*
                 * A method annotated with @Rule may return a @TestRule or a @MethodRule,
                 * we cannot call the method to check whether the return type matches our
                 * expectation i.e. subclass of valueClass. If we do that then the method 
                 * will be invoked twice and we do not want to do that. So we first check
                 * whether return type matches our expectation and only then call the method
                 * to fetch the MethodRule
                 */
                if (valueClass.isAssignableFrom(each.returnType)) {
                    val fieldValue = each.invokeExplosively(test)
                    results.add(valueClass.cast(fieldValue))
                }
            } catch (e: Throwable) {
                throw RuntimeException(
                        "Exception in " + each.name, e)
            }

        }
        return results
    }

    val isPublic: Boolean
        get() = Modifier.isPublic(javaClass!!.modifiers)

    val isANonStaticInnerClass: Boolean
        get() = javaClass!!.isMemberClass && !isStatic(javaClass.modifiers)

    override fun hashCode(): Int {
        return if (javaClass == null) 0 else javaClass.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as TestClass?
        return javaClass == other.javaClass
    }

    /**
     * Compares two fields by its name.
     */
    private class FieldComparator : Comparator<Field> {
        override fun compare(left: Field, right: Field): Int {
            return left.name.compareTo(right.name)
        }
    }

    /**
     * Compares two methods by its name.
     */
    private class MethodComparator : Comparator<FrameworkMethod> {
        override fun compare(left: FrameworkMethod, right: FrameworkMethod): Int {
            return NAME_ASCENDING.compare(left.method, right.method)
        }
    }

    companion object {
        private val FIELD_COMPARATOR = FieldComparator()
        private val METHOD_COMPARATOR = MethodComparator()

        private fun getSortedDeclaredFields(clazz: Class<*>): Array<Field> {
            val declaredFields = clazz.declaredFields
            Arrays.sort(declaredFields, FIELD_COMPARATOR)
            return declaredFields
        }

        protected fun <T : FrameworkMember<T>> addToAnnotationLists(member: T,
                                                                    map: MutableMap<Class<out Annotation>, List<T>>) {
            for (each in member.annotations) {
                val type = each.annotationType()
                val members = getAnnotatedMembers(map, type, true)
                if (member.isShadowedBy(members)) {
                    return
                }
                if (runsTopToBottom(type)) {
                    members.add(0, member)
                } else {
                    members.add(member)
                }
            }
        }

        private fun <T : FrameworkMember<T>> makeDeeplyUnmodifiable(source: Map<Class<out Annotation>, List<T>>): MutableMap<Class<out Annotation>, List<T>> {
            val copy = LinkedHashMap<Class<out Annotation>, List<T>>()
            for (entry in source.entries) {
                copy.put(entry.key, Collections.unmodifiableList(entry.value))
            }
            return Collections.unmodifiableMap(copy)
        }

        private fun <T> getAnnotatedMembers(map: MutableMap<Class<out Annotation>, List<T>>,
                                            type: Class<out Annotation>, fillIfAbsent: Boolean): MutableList<T> {
            if (!map.containsKey(type) && fillIfAbsent) {
                map.put(type, ArrayList<T>())
            }
            val members = map[type]
            return members ?: emptyList<T>()
        }

        private fun runsTopToBottom(annotation: Class<out Annotation>): Boolean {
            return annotation == Before::class.java || annotation == BeforeClass::class.java
        }

        private fun getSuperClasses(testClass: Class<*>): List<Class<*>> {
            val results = ArrayList<Class<*>>()
            var current: Class<*>? = testClass
            while (current != null) {
                results.add(current)
                current = current.superclass
            }
            return results
        }
    }
}

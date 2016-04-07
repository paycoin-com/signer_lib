package org.junit.experimental.theories

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.HashMap

class ParameterSignature private constructor(val type: Class<*>, private val annotations: Array<Annotation>) {

    fun canAcceptValue(candidate: Any?): Boolean {
        return if (candidate == null) !type.isPrimitive else canAcceptType(candidate.javaClass)
    }

    fun canAcceptType(candidate: Class<*>): Boolean {
        return type.isAssignableFrom(candidate) || isAssignableViaTypeConversion(type, candidate)
    }

    fun canPotentiallyAcceptType(candidate: Class<*>): Boolean {
        return candidate.isAssignableFrom(type) ||
                isAssignableViaTypeConversion(candidate, type) ||
                canAcceptType(candidate)
    }

    private fun isAssignableViaTypeConversion(targetType: Class<*>, candidate: Class<*>): Boolean {
        if (CONVERTABLE_TYPES_MAP.containsKey(candidate)) {
            val wrapperClass = CONVERTABLE_TYPES_MAP[candidate]
            return targetType.isAssignableFrom(wrapperClass)
        } else {
            return false
        }
    }

    fun getAnnotations(): List<Annotation> {
        return Arrays.asList(*annotations)
    }

    fun hasAnnotation(type: Class<out Annotation>): Boolean {
        return getAnnotation<out Annotation>(type) != null
    }

    fun <T : Annotation> findDeepAnnotation(annotationType: Class<T>): T {
        val annotations2 = annotations
        return findDeepAnnotation(annotations2, annotationType, 3)
    }

    private fun <T : Annotation> findDeepAnnotation(
            annotations: Array<Annotation>, annotationType: Class<T>, depth: Int): T? {
        if (depth == 0) {
            return null
        }
        for (each in annotations) {
            if (annotationType.isInstance(each)) {
                return annotationType.cast(each)
            }
            val candidate = findDeepAnnotation(each.annotationType().getAnnotations(), annotationType, depth - 1)
            if (candidate != null) {
                return annotationType.cast(candidate)
            }
        }

        return null
    }

    fun <T : Annotation> getAnnotation(annotationType: Class<T>): T? {
        for (each in getAnnotations()) {
            if (annotationType.isInstance(each)) {
                return annotationType.cast(each)
            }
        }
        return null
    }

    companion object {

        private val CONVERTABLE_TYPES_MAP = buildConvertableTypesMap()

        private fun buildConvertableTypesMap(): Map<Class<*>, Class<*>> {
            val map = HashMap<Class<*>, Class<*>>()

            putSymmetrically(map, Boolean.TYPE, Boolean::class.java)
            putSymmetrically(map, java.lang.Byte.TYPE, Byte::class.java)
            putSymmetrically(map, java.lang.Short.TYPE, Short::class.java)
            putSymmetrically(map, Character.TYPE, Char::class.java)
            putSymmetrically(map, Integer.TYPE, Int::class.java)
            putSymmetrically(map, java.lang.Long.TYPE, Long::class.java)
            putSymmetrically(map, java.lang.Float.TYPE, Float::class.java)
            putSymmetrically(map, java.lang.Double.TYPE, Double::class.java)

            return Collections.unmodifiableMap(map)
        }

        private fun <T> putSymmetrically(map: MutableMap<T, T>, a: T, b: T) {
            map.put(a, b)
            map.put(b, a)
        }

        fun signatures(method: Method): ArrayList<ParameterSignature> {
            return signatures(method.parameterTypes, method.parameterAnnotations)
        }

        fun signatures(constructor: Constructor<*>): List<ParameterSignature> {
            return signatures(constructor.parameterTypes, constructor.parameterAnnotations)
        }

        private fun signatures(
                parameterTypes: Array<Class<*>>, parameterAnnotations: Array<Array<Annotation>>): ArrayList<ParameterSignature> {
            val sigs = ArrayList<ParameterSignature>()
            for (i in parameterTypes.indices) {
                sigs.add(ParameterSignature(parameterTypes[i],
                        parameterAnnotations[i]))
            }
            return sigs
        }
    }
}
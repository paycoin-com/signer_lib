package org.junit.internal

import java.lang.reflect.Method
import java.util.Arrays
import java.util.Comparator

import org.junit.FixMethodOrder

object MethodSorter {
    /**
     * DEFAULT sort order
     */
    val DEFAULT: Comparator<Method> = Comparator { m1, m2 ->
        val i1 = m1.name.hashCode()
        val i2 = m2.name.hashCode()
        if (i1 != i2) {
            return@Comparator if (i1 < i2) -1 else 1
        }
        NAME_ASCENDING.compare(m1, m2)
    }

    /**
     * Method name ascending lexicographic sort order, with [Method.toString] as a tiebreaker
     */
    val NAME_ASCENDING: Comparator<Method> = Comparator { m1, m2 ->
        val comparison = m1.name.compareTo(m2.name)
        if (comparison != 0) {
            return@Comparator comparison
        }
        m1.toString().compareTo(m2.toString())
    }

    /**
     * Gets declared methods of a class in a predictable order, unless @FixMethodOrder(MethodSorters.JVM) is specified.

     * Using the JVM order is unwise since the Java platform does not
     * specify any particular order, and in fact JDK 7 returns a more or less
     * random order; well-written test code would not assume any order, but some
     * does, and a predictable failure is better than a random failure on
     * certain platforms. By default, uses an unspecified but deterministic order.

     * @param clazz a class
     * *
     * @return same as [Class.getDeclaredMethods] but sorted
     * *
     * @see [JDK

    ](http://bugs.sun.com/view_bug.do?bug_id=7023180) */
    fun getDeclaredMethods(clazz: Class<*>): Array<Method> {
        val comparator = getSorter(clazz.getAnnotation(FixMethodOrder::class.java))

        val methods = clazz.declaredMethods
        if (comparator != null) {
            Arrays.sort(methods, comparator)
        }

        return methods
    }

    private fun getSorter(fixMethodOrder: FixMethodOrder?): Comparator<Method>? {
        if (fixMethodOrder == null) {
            return DEFAULT
        }

        return fixMethodOrder.value().comparator
    }
}

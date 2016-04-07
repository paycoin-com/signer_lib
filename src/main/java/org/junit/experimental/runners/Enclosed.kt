package org.junit.experimental.runners

import java.lang.reflect.Modifier
import java.util.ArrayList

import org.junit.runners.Suite
import org.junit.runners.model.RunnerBuilder

/**
 * If you put tests in inner classes, Ant, for example, won't find them. By running the outer class
 * with Enclosed, the tests in the inner classes will be run. You might put tests in inner classes
 * to group them for convenience or to share constants. Abstract inner classes are ignored.
 *
 *
 * So, for example:
 *
 * &#064;RunWith(Enclosed.class)
 * public class ListTests {
 * ...useful shared stuff...
 * public static class OneKindOfListTest {...}
 * public static class AnotherKind {...}
 * abstract public static class Ignored {...}
 * }
 *
 */
class Enclosed
/**
 * Only called reflectively. Do not use programmatically.
 */
@Throws(Throwable::class)
constructor(klass: Class<*>, builder: RunnerBuilder) : Suite(builder, klass, Enclosed.filterAbstractClasses(klass.classes)) {

    private fun filterAbstractClasses(classes: Array<Class<*>>): Array<Class<*>> {
        val filteredList = ArrayList<Class<*>>(classes.size)

        for (clazz in classes) {
            if (!Modifier.isAbstract(clazz.modifiers)) {
                filteredList.add(clazz)
            }
        }

        return filteredList.toArray<Class<*>>(arrayOfNulls<Class<*>>(filteredList.size))
    }
}

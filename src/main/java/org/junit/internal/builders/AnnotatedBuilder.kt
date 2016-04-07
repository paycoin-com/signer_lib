package org.junit.internal.builders

import org.junit.runner.RunWith
import org.junit.runner.Runner
import org.junit.runners.model.InitializationError
import org.junit.runners.model.RunnerBuilder

import java.lang.reflect.Modifier


/**
 * The `AnnotatedBuilder` is a strategy for constructing runners for test class that have been annotated with the
 * `@RunWith` annotation. All tests within this class will be executed using the runner that was specified within
 * the annotation.
 *
 *
 * If a runner supports inner member classes, the member classes will inherit the runner from the enclosing class, e.g.:
 *
 * &#064;RunWith(MyRunner.class)
 * public class MyTest {
 * // some tests might go here

 * public class MyMemberClass {
 * &#064;Test
 * public void thisTestRunsWith_MyRunner() {
 * // some test logic
 * }

 * // some more tests might go here
 * }

 * &#064;RunWith(AnotherRunner.class)
 * public class AnotherMemberClass {
 * // some tests might go here

 * public class DeepInnerClass {
 * &#064;Test
 * public void thisTestRunsWith_AnotherRunner() {
 * // some test logic
 * }
 * }

 * public class DeepInheritedClass extends SuperTest {
 * &#064;Test
 * public void thisTestRunsWith_SuperRunner() {
 * // some test logic
 * }
 * }
 * }
 * }

 * &#064;RunWith(SuperRunner.class)
 * public class SuperTest {
 * // some tests might go here
 * }
 *
 * The key points to note here are:
 *
 *  * If there is no RunWith annotation, no runner will be created.
 *  * The resolve step is inside-out, e.g. the closest RunWith annotation wins
 *  * RunWith annotations are inherited and work as if the class was annotated itself.
 *  * The default JUnit runner does not support inner member classes,
 * so this is only valid for custom runners that support inner member classes.
 *  * Custom runners with support for inner classes may or may not support RunWith annotations for member
 * classes. Please refer to the custom runner documentation.
 *

 * @see org.junit.runners.model.RunnerBuilder

 * @see org.junit.runner.RunWith

 * @since 4.0
 */
class AnnotatedBuilder(private val suiteBuilder: RunnerBuilder) : RunnerBuilder() {

    @Throws(Exception::class)
    override fun runnerForClass(testClass: Class<*>): Runner? {
        var currentTestClass: Class<*>? = testClass
        while (currentTestClass != null) {
            val annotation = currentTestClass.getAnnotation(RunWith::class.java)
            if (annotation != null) {
                return buildRunner(annotation.value(), testClass)
            }
            currentTestClass = getEnclosingClassForNonStaticMemberClass(currentTestClass)
        }

        return null
    }

    private fun getEnclosingClassForNonStaticMemberClass(currentTestClass: Class<*>): Class<*>? {
        if (currentTestClass.isMemberClass && !Modifier.isStatic(currentTestClass.modifiers)) {
            return currentTestClass.enclosingClass
        } else {
            return null
        }
    }

    @Throws(Exception::class)
    fun buildRunner(runnerClass: Class<out Runner>,
                    testClass: Class<*>): Runner {
        try {
            return runnerClass.getConstructor(Class<Any>::class.java).newInstance(testClass)
        } catch (e: NoSuchMethodException) {
            try {
                return runnerClass.getConstructor(Class<Any>::class.java,
                        RunnerBuilder::class.java).newInstance(testClass, suiteBuilder)
            } catch (e2: NoSuchMethodException) {
                val simpleName = runnerClass.simpleName
                throw InitializationError(String.format(
                        CONSTRUCTOR_ERROR_FORMAT, simpleName, simpleName))
            }

        }

    }

    companion object {
        private val CONSTRUCTOR_ERROR_FORMAT = "Custom runner class %s should have a public constructor with signature %s(Class testClass)"
    }
}
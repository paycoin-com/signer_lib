package org.junit.runners

import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.Collections

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.InitializationError
import org.junit.runners.model.RunnerBuilder
import kotlin.reflect.KClass

/**
 * Using `Suite` as a runner allows you to manually
 * build a suite containing tests from many classes. It is the JUnit 4 equivalent of the JUnit 3.8.x
 * static [junit.framework.Test] `suite()` method. To use it, annotate a class
 * with `@RunWith(Suite.class)` and `@SuiteClasses({TestClass1.class, ...})`.
 * When you run this class, it will run all the tests in all the suite classes.

 * @since 4.0
 */
open class Suite
/**
 * Called by this class and subclasses once the runners making up the suite have been determined

 * @param klass root of the suite
 * *
 * @param runners for each class in the suite, a [Runner]
 */
@Throws(InitializationError::class)
protected constructor(klass: Class<*>?, runners: List<Runner>) : ParentRunner<Runner>(klass) {

    /**
     * The `SuiteClasses` annotation specifies the classes to be run when a class
     * annotated with `@RunWith(Suite.class)` is run.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
    @Inherited
    annotation class SuiteClasses(
            /**
             * @return the classes to be run
             */
            vararg val value: KClass<*>)

    protected override val children: List<Runner>

    /**
     * Called reflectively on classes annotated with `@RunWith(Suite.class)`

     * @param klass the root class
     * *
     * @param builder builds runners for classes in the suite
     */
    @Throws(InitializationError::class)
    constructor(klass: Class<*>, builder: RunnerBuilder) : this(builder, klass, getAnnotatedClasses(klass)) {
    }

    /**
     * Call this when there is no single root class (for example, multiple class names
     * passed on the command line to [org.junit.runner.JUnitCore]

     * @param builder builds runners for classes in the suite
     * *
     * @param classes the classes in the suite
     */
    @Throws(InitializationError::class)
    constructor(builder: RunnerBuilder, classes: Array<Class<*>>) : this(null, builder.runners(null, classes)) {
    }

    /**
     * Call this when the default builder is good enough. Left in for compatibility with JUnit 4.4.

     * @param klass the root of the suite
     * *
     * @param suiteClasses the classes in the suite
     */
    @Throws(InitializationError::class)
    protected constructor(klass: Class<*>, suiteClasses: Array<Class<*>>) : this(AllDefaultPossibilitiesBuilder(true), klass, suiteClasses) {
    }

    /**
     * Called by this class and subclasses once the classes making up the suite have been determined

     * @param builder builds runners for classes in the suite
     * *
     * @param klass the root of the suite
     * *
     * @param suiteClasses the classes in the suite
     */
    @Throws(InitializationError::class)
    protected constructor(builder: RunnerBuilder, klass: Class<*>, suiteClasses: Array<Class<*>>) : this(klass, builder.runners(klass, suiteClasses)) {
    }

    init {
        this.children = Collections.unmodifiableList(runners)
    }

    override fun describeChild(child: Runner): Description {
        return child.description
    }

    override fun runChild(runner: Runner, notifier: RunNotifier) {
        runner.run(notifier)
    }

    companion object {
        /**
         * Returns an empty suite.
         */
        fun emptySuite(): Runner {
            try {
                return Suite(null as Class<*>, arrayOfNulls<Class<*>>(0))
            } catch (e: InitializationError) {
                throw RuntimeException("This shouldn't be possible")
            }

        }

        @Throws(InitializationError::class)
        private fun getAnnotatedClasses(klass: Class<*>): Array<Class<*>> {
            val annotation = klass.getAnnotation(SuiteClasses::class.java) ?: throw InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.name))
            return annotation.value()
        }
    }
}

package org.junit.runners

import org.junit.internal.runners.rules.RuleMemberValidator.RULE_METHOD_VALIDATOR
import org.junit.internal.runners.rules.RuleMemberValidator.RULE_VALIDATOR
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.Test.None
import org.junit.internal.runners.model.ReflectiveCallable
import org.junit.internal.runners.statements.ExpectException
import org.junit.internal.runners.statements.Fail
import org.junit.internal.runners.statements.FailOnTimeout
import org.junit.internal.runners.statements.InvokeMethod
import org.junit.internal.runners.statements.RunAfters
import org.junit.internal.runners.statements.RunBefores
import org.junit.rules.MethodRule
import org.junit.rules.RunRules
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.junit.runners.model.MultipleFailureException
import org.junit.runners.model.Statement

/**
 * Implements the JUnit 4 standard test case class model, as defined by the
 * annotations in the org.junit package. Many users will never notice this
 * class: it is now the default test class runner, but it should have exactly
 * the same behavior as the old test class runner (`JUnit4ClassRunner`).
 *
 *
 * BlockJUnit4ClassRunner has advantages for writers of custom JUnit runners
 * that are slight changes to the default behavior, however:

 *
 *  * It has a much simpler implementation based on [Statement]s,
 * allowing new operations to be inserted into the appropriate point in the
 * execution flow.

 *  * It is published, and extension and reuse are encouraged, whereas `JUnit4ClassRunner` was in an internal package, and is now deprecated.
 *
 *
 *
 * In turn, in 2009 we introduced [Rule]s.  In many cases where extending
 * BlockJUnit4ClassRunner was necessary to add new behavior, [Rule]s can
 * be used, which makes the extension more reusable and composable.

 * @since 4.5
 */
open class BlockJUnit4ClassRunner
/**
 * Creates a BlockJUnit4ClassRunner to run `klass`

 * @throws InitializationError if the test class is malformed.
 */
@Throws(InitializationError::class)
constructor(klass: Class<*>) : ParentRunner<FrameworkMethod>(klass) {
    private val methodDescriptions = ConcurrentHashMap<FrameworkMethod, Description>()

    //
    // Implementation of ParentRunner
    //

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        val description = describeChild(method)
        if (isIgnored(method)) {
            notifier.fireTestIgnored(description)
        } else {
            runLeaf(methodBlock(method), description, notifier)
        }
    }

    /**
     * Evaluates whether [FrameworkMethod]s are ignored based on the
     * [Ignore] annotation.
     */
    override fun isIgnored(child: FrameworkMethod): Boolean {
        return child.getAnnotation(Ignore::class.java) != null
    }

    override fun describeChild(method: FrameworkMethod): Description {
        var description: Description? = methodDescriptions[method]

        if (description == null) {
            description = Description.createTestDescription(testClass.javaClass,
                    testName(method), *method.annotations)
            methodDescriptions.putIfAbsent(method, description)
        }

        return description
    }

    protected override val children: List<FrameworkMethod>
        get() = computeTestMethods()

    //
    // Override in subclasses
    //

    /**
     * Returns the methods that run tests. Default implementation returns all
     * methods annotated with `@Test` on this class and superclasses that
     * are not overridden.
     */
    protected open fun computeTestMethods(): List<FrameworkMethod> {
        return testClass.getAnnotatedMethods(Test::class.java)
    }

    override fun collectInitializationErrors(errors: MutableList<Throwable>) {
        super.collectInitializationErrors(errors)

        validateNoNonStaticInnerClass(errors)
        validateConstructor(errors)
        validateInstanceMethods(errors)
        validateFields(errors)
        validateMethods(errors)
    }

    protected fun validateNoNonStaticInnerClass(errors: MutableList<Throwable>) {
        if (testClass.isANonStaticInnerClass) {
            val gripe = "The inner class " + testClass.name
            +" is not static."
            errors.add(Exception(gripe))
        }
    }

    /**
     * Adds to `errors` if the test class has more than one constructor,
     * or if the constructor takes parameters. Override if a subclass requires
     * different validation rules.
     */
    protected open fun validateConstructor(errors: MutableList<Throwable>) {
        validateOnlyOneConstructor(errors)
        validateZeroArgConstructor(errors)
    }

    /**
     * Adds to `errors` if the test class has more than one constructor
     * (do not override)
     */
    protected fun validateOnlyOneConstructor(errors: MutableList<Throwable>) {
        if (!hasOneConstructor()) {
            val gripe = "Test class should have exactly one public constructor"
            errors.add(Exception(gripe))
        }
    }

    /**
     * Adds to `errors` if the test class's single constructor takes
     * parameters (do not override)
     */
    protected fun validateZeroArgConstructor(errors: MutableList<Throwable>) {
        if (!testClass.isANonStaticInnerClass
                && hasOneConstructor()
                && testClass.onlyConstructor.parameterTypes.size != 0) {
            val gripe = "Test class should have exactly one public zero-argument constructor"
            errors.add(Exception(gripe))
        }
    }

    private fun hasOneConstructor(): Boolean {
        return testClass.javaClass.constructors.size == 1
    }

    /**
     * Adds to `errors` for each method annotated with `@Test`,
     * `@Before`, or `@After` that is not a public, void instance
     * method with no arguments.
     */
    @Deprecated("")
    protected fun validateInstanceMethods(errors: MutableList<Throwable>) {
        validatePublicVoidNoArgMethods(After::class.java, false, errors)
        validatePublicVoidNoArgMethods(Before::class.java, false, errors)
        validateTestMethods(errors)

        if (computeTestMethods().size == 0) {
            errors.add(Exception("No runnable methods"))
        }
    }

    protected open fun validateFields(errors: List<Throwable>) {
        RULE_VALIDATOR.validate(testClass, errors)
    }

    private fun validateMethods(errors: List<Throwable>) {
        RULE_METHOD_VALIDATOR.validate(testClass, errors)
    }

    /**
     * Adds to `errors` for each method annotated with `@Test`that
     * is not a public, void instance method with no arguments.
     */
    protected open fun validateTestMethods(errors: MutableList<Throwable>) {
        validatePublicVoidNoArgMethods(Test::class.java, false, errors)
    }

    /**
     * Returns a new fixture for running a test. Default implementation executes
     * the test class's no-argument constructor (validation should have ensured
     * one exists).
     */
    @Throws(Exception::class)
    protected open fun createTest(): Any {
        return testClass.onlyConstructor.newInstance()
    }

    /**
     * Returns the name that describes `method` for [Description]s.
     * Default implementation is the method's name
     */
    protected open fun testName(method: FrameworkMethod): String {
        return method.name
    }

    /**
     * Returns a Statement that, when executed, either returns normally if
     * `method` passes, or throws an exception if `method` fails.

     * Here is an outline of the default implementation:

     *
     *  * Invoke `method` on the result of `createTest()`, and
     * throw any exceptions thrown by either operation.
     *  * HOWEVER, if `method`'s `@Test` annotation has the `expecting` attribute, return normally only if the previous step threw an
     * exception of the correct type, and throw an exception otherwise.
     *  * HOWEVER, if `method`'s `@Test` annotation has the `timeout` attribute, throw an exception if the previous step takes more
     * than the specified number of milliseconds.
     *  * ALWAYS run all non-overridden `@Before` methods on this class
     * and superclasses before any of the previous steps; if any throws an
     * Exception, stop execution and pass the exception on.
     *  * ALWAYS run all non-overridden `@After` methods on this class
     * and superclasses after any of the previous steps; all After methods are
     * always executed: exceptions thrown by previous steps are combined, if
     * necessary, with exceptions from After methods into a
     * [MultipleFailureException].
     *  * ALWAYS allow `@Rule` fields to modify the execution of the
     * above steps. A `Rule` may prevent all execution of the above steps,
     * or add additional behavior before and after, or modify thrown exceptions.
     * For more information, see [TestRule]
     *

     * This can be overridden in subclasses, either by overriding this method,
     * or the implementations creating each sub-statement.
     */
    protected open fun methodBlock(method: FrameworkMethod): Statement {
        val test: Any
        try {
            test = object : ReflectiveCallable() {
                @Throws(Throwable::class)
                override fun runReflectiveCall(): Any {
                    return createTest()
                }
            }.run()
        } catch (e: Throwable) {
            return Fail(e)
        }

        var statement = methodInvoker(method, test)
        statement = possiblyExpectingExceptions(method, test, statement)
        statement = withPotentialTimeout(method, test, statement)
        statement = withBefores(method, test, statement)
        statement = withAfters(method, test, statement)
        statement = withRules(method, test, statement)
        return statement
    }

    //
    // Statement builders
    //

    /**
     * Returns a [Statement] that invokes `method` on `test`
     */
    protected open fun methodInvoker(method: FrameworkMethod, test: Any): Statement {
        return InvokeMethod(method, test)
    }

    /**
     * Returns a [Statement]: if `method`'s `@Test` annotation
     * has the `expecting` attribute, return normally only if `next`
     * throws an exception of the correct type, and throw an exception
     * otherwise.
     */
    protected fun possiblyExpectingExceptions(method: FrameworkMethod,
                                              test: Any, next: Statement): Statement {
        val annotation = method.getAnnotation(Test::class.java)
        return if (expectsException(annotation))
            ExpectException(next,
                    getExpectedException(annotation))
        else
            next
    }

    /**
     * Returns a [Statement]: if `method`'s `@Test` annotation
     * has the `timeout` attribute, throw an exception if `next`
     * takes more than the specified number of milliseconds.
     */
    @Deprecated("")
    protected fun withPotentialTimeout(method: FrameworkMethod,
                                       test: Any, next: Statement): Statement {
        val timeout = getTimeout(method.getAnnotation(Test::class.java))
        if (timeout <= 0) {
            return next
        }
        return FailOnTimeout.builder().withTimeout(timeout, TimeUnit.MILLISECONDS).build(next)
    }

    /**
     * Returns a [Statement]: run all non-overridden `@Before`
     * methods on this class and superclasses before running `next`; if
     * any throws an Exception, stop execution and pass the exception on.
     */
    protected fun withBefores(method: FrameworkMethod, target: Any,
                              statement: Statement): Statement {
        val befores = testClass.getAnnotatedMethods(
                Before::class.java)
        return if (befores.isEmpty())
            statement
        else
            RunBefores(statement,
                    befores, target)
    }

    /**
     * Returns a [Statement]: run all non-overridden `@After`
     * methods on this class and superclasses before running `next`; all
     * After methods are always executed: exceptions thrown by previous steps
     * are combined, if necessary, with exceptions from After methods into a
     * [MultipleFailureException].
     */
    protected fun withAfters(method: FrameworkMethod, target: Any,
                             statement: Statement): Statement {
        val afters = testClass.getAnnotatedMethods(
                After::class.java)
        return if (afters.isEmpty())
            statement
        else
            RunAfters(statement, afters,
                    target)
    }

    private fun withRules(method: FrameworkMethod, target: Any,
                          statement: Statement): Statement {
        val testRules = getTestRules(target)
        var result = statement
        result = withMethodRules(method, testRules, target, result)
        result = withTestRules(method, testRules, result)

        return result
    }

    private fun withMethodRules(method: FrameworkMethod, testRules: List<TestRule>,
                                target: Any, result: Statement): Statement {
        var result = result
        for (each in getMethodRules(target)) {
            if (!testRules.contains(each)) {
                result = each.apply(result, method, target)
            }
        }
        return result
    }

    private fun getMethodRules(target: Any): List<org.junit.rules.MethodRule> {
        return rules(target)
    }

    /**
     * @param target the test case instance
     * *
     * @return a list of MethodRules that should be applied when executing this
     * *         test
     */
    protected fun rules(target: Any): List<MethodRule> {
        val rules = testClass.getAnnotatedMethodValues(target,
                Rule::class.java, MethodRule::class.java)

        rules.addAll(testClass.getAnnotatedFieldValues(target,
                Rule::class.java, MethodRule::class.java))

        return rules
    }

    /**
     * Returns a [Statement]: apply all non-static fields
     * annotated with [Rule].

     * @param statement The base statement
     * *
     * @return a RunRules statement if any class-level [Rule]s are
     * *         found, or the base statement
     */
    private fun withTestRules(method: FrameworkMethod, testRules: List<TestRule>,
                              statement: Statement): Statement {
        return if (testRules.isEmpty())
            statement
        else
            RunRules(statement, testRules, describeChild(method))
    }

    /**
     * @param target the test case instance
     * *
     * @return a list of TestRules that should be applied when executing this
     * *         test
     */
    protected fun getTestRules(target: Any): List<TestRule> {
        val result = testClass.getAnnotatedMethodValues(target,
                Rule::class.java, TestRule::class.java)

        result.addAll(testClass.getAnnotatedFieldValues(target,
                Rule::class.java, TestRule::class.java))

        return result
    }

    private fun getExpectedException(annotation: Test?): Class<out Throwable>? {
        if (annotation == null || annotation.expected() == None::class.java) {
            return null
        } else {
            return annotation.expected()
        }
    }

    private fun expectsException(annotation: Test): Boolean {
        return getExpectedException(annotation) != null
    }

    private fun getTimeout(annotation: Test?): Long {
        if (annotation == null) {
            return 0
        }
        return annotation.timeout()
    }
}

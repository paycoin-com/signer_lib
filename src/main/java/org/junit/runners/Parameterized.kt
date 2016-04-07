package org.junit.runners

import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.text.MessageFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections

import org.junit.runner.Runner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.junit.runners.model.TestClass
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParametersFactory
import org.junit.runners.parameterized.ParametersRunnerFactory
import org.junit.runners.parameterized.TestWithParameters
import kotlin.reflect.KClass

/**
 * The custom runner `Parameterized` implements parameterized tests.
 * When running a parameterized test class, instances are created for the
 * cross-product of the test methods and the test data elements.
 *
 *
 * For example, to test a Fibonacci function, write:
 *
 * &#064;RunWith(Parameterized.class)
 * public class FibonacciTest {
 * &#064;Parameters(name= &quot;{index}: fib[{0}]={1}&quot;)
 * public static Iterable&lt;Object[]&gt; data() {
 * return Arrays.asList(new Object[][] { { 0, 0 }, { 1, 1 }, { 2, 1 },
 * { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 } });
 * }

 * private int fInput;

 * private int fExpected;

 * public FibonacciTest(int input, int expected) {
 * fInput= input;
 * fExpected= expected;
 * }

 * &#064;Test
 * public void test() {
 * assertEquals(fExpected, Fibonacci.compute(fInput));
 * }
 * }
 *
 *
 *
 * Each instance of `FibonacciTest` will be constructed using the
 * two-argument constructor and the data values in the
 * `&#064;Parameters` method.
 *
 *
 * In order that you can easily identify the individual tests, you may provide a
 * name for the `&#064;Parameters` annotation. This name is allowed
 * to contain placeholders, which are replaced at runtime. The placeholders are
 *
 * {index}
 * the current parameter index
 * {0}
 * the first parameter value
 * {1}
 * the second parameter value
 * ...
 * ...
 *
 *
 *
 * In the example given above, the `Parameterized` runner creates
 * names like `[1: fib(3)=2]`. If you don't use the name parameter,
 * then the current parameter index is used as name.
 *
 *
 * You can also write:
 *
 * &#064;RunWith(Parameterized.class)
 * public class FibonacciTest {
 * &#064;Parameters
 * public static Iterable&lt;Object[]&gt; data() {
 * return Arrays.asList(new Object[][] { { 0, 0 }, { 1, 1 }, { 2, 1 },
 * { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 } });
 * }

 * &#064;Parameter(0)
 * public int fInput;

 * &#064;Parameter(1)
 * public int fExpected;

 * &#064;Test
 * public void test() {
 * assertEquals(fExpected, Fibonacci.compute(fInput));
 * }
 * }
 *
 *
 *
 * Each instance of `FibonacciTest` will be constructed with the default constructor
 * and fields annotated by `&#064;Parameter`  will be initialized
 * with the data values in the `&#064;Parameters` method.

 *
 *
 * The parameters can be provided as an array, too:

 *
 * &#064;Parameters
 * public static Object[][] data() {
 * return new Object[][] { { 0, 0 }, { 1, 1 }, { 2, 1 }, { 3, 2 }, { 4, 3 },
 * { 5, 5 }, { 6, 8 } };
 * }
 *

 * Tests with single parameter
 *
 *
 * If your test needs a single parameter only, you don't have to wrap it with an
 * array. Instead you can provide an `Iterable` or an array of
 * objects.
 *
 * &#064;Parameters
 * public static Iterable&lt;? extends Object&gt; data() {
 * return Arrays.asList(&quot;first test&quot;, &quot;second test&quot;);
 * }
 *
 *
 *
 * or
 *
 * &#064;Parameters
 * public static Object[] data() {
 * return new Object[] { &quot;first test&quot;, &quot;second test&quot; };
 * }
 *

 * Create different runners
 *
 *
 * By default the `Parameterized` runner creates a slightly modified
 * [BlockJUnit4ClassRunner] for each set of parameters. You can build an
 * own `Parameterized` runner that creates another runner for each set of
 * parameters. Therefore you have to build a [ParametersRunnerFactory]
 * that creates a runner for each [TestWithParameters]. (
 * `TestWithParameters` are bundling the parameters and the test name.)
 * The factory must have a public zero-arg constructor.

 *
 * public class YourRunnerFactory implements ParameterizedRunnerFactory {
 * public Runner createRunnerForTestWithParameters(TestWithParameters test)
 * throws InitializationError {
 * return YourRunner(test);
 * }
 * }
 *
 *
 *
 * Use the [UseParametersRunnerFactory] to tell the `Parameterized`
 * runner that it should use your factory.

 *
 * &#064;RunWith(Parameterized.class)
 * &#064;UseParametersRunnerFactory(YourRunnerFactory.class)
 * public class YourTest {
 * ...
 * }
 *

 * @since 4.0
 */
class Parameterized
/**
 * Only called reflectively. Do not use programmatically.
 */
@Throws(Throwable::class)
constructor(klass: Class<*>) : Suite(klass, Parameterized.NO_RUNNERS) {
    /**
     * Annotation for a method which provides parameters to be injected into the
     * test class constructor by `Parameterized`. The method has to
     * be public and static.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    annotation class Parameters(
            /**
             * Optional pattern to derive the test's name from the parameters. Use
             * numbers in braces to refer to the parameters or the additional data
             * as follows:
             *
             * {index} - the current parameter index
             * {0} - the first parameter value
             * {1} - the second parameter value
             * etc...
             *
             *
             *
             * Default value is "{index}" for compatibility with previous JUnit
             * versions.

             * @return [MessageFormat] pattern string, except the index
             * *         placeholder.
             * *
             * @see MessageFormat
             */
            val name: String = "{index}")

    /**
     * Annotation for fields of the test class which will be initialized by the
     * method annotated by `Parameters`.
     * By using directly this annotation, the test class constructor isn't needed.
     * Index range must start at 0.
     * Default value is 0.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(AnnotationTarget.FIELD)
    annotation class Parameter(
            /**
             * Method that returns the index of the parameter in the array
             * returned by the method annotated by `Parameters`.
             * Index range must start at 0.
             * Default value is 0.

             * @return the index of the parameter.
             */
            val value: Int = 0)

    /**
     * Add this annotation to your test class if you want to generate a special
     * runner. You have to specify a [ParametersRunnerFactory] class that
     * creates such runners. The factory must have a public zero-arg
     * constructor.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
    annotation class UseParametersRunnerFactory(
            /**
             * @return a [ParametersRunnerFactory] class (must have a default
             * *         constructor)
             */
            val value: KClass<out ParametersRunnerFactory> = BlockJUnit4ClassRunnerWithParametersFactory::class)

    protected override val children: List<Runner>

    init {
        val runnerFactory = getParametersRunnerFactory(
                klass)
        val parameters = parametersMethod.getAnnotation(Parameters::class.java)
        children = Collections.unmodifiableList(createRunnersForParameters(
                allParameters(), parameters.name(), runnerFactory))
    }

    @Throws(InstantiationException::class, IllegalAccessException::class)
    private fun getParametersRunnerFactory(klass: Class<*>): ParametersRunnerFactory {
        val annotation = klass.getAnnotation(UseParametersRunnerFactory::class.java)
        if (annotation == null) {
            return DEFAULT_FACTORY
        } else {
            val factoryClass = annotation.value()
            return factoryClass.newInstance()
        }
    }

    private fun createTestWithNotNormalizedParameters(
            pattern: String, index: Int, parametersOrSingleParameter: Any): TestWithParameters {
        val parameters = if (parametersOrSingleParameter is Array<Any>)
            parametersOrSingleParameter
        else
            arrayOf(parametersOrSingleParameter)
        return createTestWithParameters(testClass, pattern, index,
                parameters)
    }

    @SuppressWarnings("unchecked")
    @Throws(Throwable::class)
    private fun allParameters(): Iterable<Any> {
        val parameters = parametersMethod.invokeExplosively(null)
        if (parameters is Iterable<Any>) {
            return parameters
        } else if (parameters is Array<Any>) {
            return Arrays.asList(*parameters)
        } else {
            throw parametersMethodReturnedWrongType()
        }
    }

    private val parametersMethod: FrameworkMethod
        @Throws(Exception::class)
        get() {
            val methods = testClass.getAnnotatedMethods(
                    Parameters::class.java)
            for (each in methods) {
                if (each.isStatic && each.isPublic) {
                    return each
                }
            }

            throw Exception("No public static parameters method on class " + testClass.name)
        }

    @Throws(InitializationError::class, Exception::class)
    private fun createRunnersForParameters(
            allParameters: Iterable<Any>, namePattern: String,
            runnerFactory: ParametersRunnerFactory): List<Runner> {
        try {
            val tests = createTestsForParameters(
                    allParameters, namePattern)
            val runners = ArrayList<Runner>()
            for (test in tests) {
                runners.add(runnerFactory.createRunnerForTestWithParameters(test))
            }
            return runners
        } catch (e: ClassCastException) {
            throw parametersMethodReturnedWrongType()
        }

    }

    @Throws(Exception::class)
    private fun createTestsForParameters(
            allParameters: Iterable<Any>, namePattern: String): List<TestWithParameters> {
        var i = 0
        val children = ArrayList<TestWithParameters>()
        for (parametersOfSingleTest in allParameters) {
            children.add(createTestWithNotNormalizedParameters(namePattern,
                    i++, parametersOfSingleTest))
        }
        return children
    }

    @Throws(Exception::class)
    private fun parametersMethodReturnedWrongType(): Exception {
        val className = testClass.name
        val methodName = parametersMethod.name
        val message = MessageFormat.format(
                "{0}.{1}() must return an Iterable of arrays.",
                className, methodName)
        return Exception(message)
    }

    companion object {

        private val DEFAULT_FACTORY = BlockJUnit4ClassRunnerWithParametersFactory()

        private val NO_RUNNERS = emptyList<Runner>()

        private fun createTestWithParameters(
                testClass: TestClass, pattern: String, index: Int, parameters: Array<Any>): TestWithParameters {
            val finalPattern = pattern.replace("\\{index\\}".toRegex(), Integer.toString(index))
            val name = MessageFormat.format(finalPattern, *parameters)
            return TestWithParameters("[$name]", testClass,
                    Arrays.asList(*parameters))
        }
    }
}

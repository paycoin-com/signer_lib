package org.junit.experimental.theories

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.ArrayList

import org.junit.Assert
import org.junit.Assume
import org.junit.experimental.theories.internal.Assignments
import org.junit.experimental.theories.internal.ParameterizedAssertionError
import org.junit.internal.AssumptionViolatedException
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.junit.runners.model.Statement
import org.junit.runners.model.TestClass

/**
 * The Theories runner allows to test a certain functionality against a subset of an infinite set of data points.
 *
 *
 * A Theory is a piece of functionality (a method) that is executed against several data inputs called data points.
 * To make a test method a theory you mark it with **&#064;Theory**. To create a data point you create a public
 * field in your test class and mark it with **&#064;DataPoint**. The Theories runner then executes your test
 * method as many times as the number of data points declared, providing a different data point as
 * the input argument on each invocation.
 *
 *
 *
 * A Theory differs from standard test method in that it captures some aspect of the intended behavior in possibly
 * infinite numbers of scenarios which corresponds to the number of data points declared. Using assumptions and
 * assertions properly together with covering multiple scenarios with different data points can make your tests more
 * flexible and bring them closer to scientific theories (hence the name).
 *
 *
 *
 * For example:
 *

 * &#064;RunWith(**Theories.class**)
 * public class UserTest {
 * **&#064;DataPoint**
 * public static String GOOD_USERNAME = "optimus";
 * **&#064;DataPoint**
 * public static String USERNAME_WITH_SLASH = "optimus/prime";

 * **&#064;Theory**
 * public void filenameIncludesUsername(String username) {
 * assumeThat(username, not(containsString("/")));
 * assertThat(new User(username).configFileName(), containsString(username));
 * }
 * }
 *
 * This makes it clear that the user's filename should be included in the config file name,
 * only if it doesn't contain a slash. Another test or theory might define what happens when a username does contain
 * a slash. `UserTest` will attempt to run `filenameIncludesUsername` on every compatible data
 * point defined in the class. If any of the assumptions fail, the data point is silently ignored. If all of the
 * assumptions pass, but an assertion fails, the test fails.
 *
 *
 * Defining general statements as theories allows data point reuse across a bunch of functionality tests and also
 * allows automated tools to search for new, unexpected data points that expose bugs.
 *
 *
 *
 * The support for Theories has been absorbed from the Popper project, and more complete documentation can be found
 * from that projects archived documentation.
 *

 * @see [Archived Popper project documentation](http://web.archive.org/web/20071012143326/popper.tigris.org/tutorial.html)

 * @see [Paper on Theories](http://web.archive.org/web/20110608210825/http://shareandenjoy.saff.net/tdd-specifications.pdf)
 */
class Theories @Throws(InitializationError::class)
constructor(klass: Class<*>) : BlockJUnit4ClassRunner(klass) {

    override fun collectInitializationErrors(errors: MutableList<Throwable>) {
        super.collectInitializationErrors(errors)
        validateDataPointFields(errors)
        validateDataPointMethods(errors)
    }

    private fun validateDataPointFields(errors: MutableList<Throwable>) {
        val fields = testClass.javaClass.declaredFields

        for (field in fields) {
            if (field.getAnnotation(DataPoint::class.java) == null && field.getAnnotation(DataPoints::class.java) == null) {
                continue
            }
            if (!Modifier.isStatic(field.modifiers)) {
                errors.add(Error("DataPoint field " + field.name + " must be static"))
            }
            if (!Modifier.isPublic(field.modifiers)) {
                errors.add(Error("DataPoint field " + field.name + " must be public"))
            }
        }
    }

    private fun validateDataPointMethods(errors: MutableList<Throwable>) {
        val methods = testClass.javaClass.declaredMethods

        for (method in methods) {
            if (method.getAnnotation(DataPoint::class.java) == null && method.getAnnotation(DataPoints::class.java) == null) {
                continue
            }
            if (!Modifier.isStatic(method.modifiers)) {
                errors.add(Error("DataPoint method " + method.name + " must be static"))
            }
            if (!Modifier.isPublic(method.modifiers)) {
                errors.add(Error("DataPoint method " + method.name + " must be public"))
            }
        }
    }

    override fun validateConstructor(errors: MutableList<Throwable>) {
        validateOnlyOneConstructor(errors)
    }

    override fun validateTestMethods(errors: MutableList<Throwable>) {
        for (each in computeTestMethods()) {
            if (each.getAnnotation(Theory::class.java) != null) {
                each.validatePublicVoid(false, errors)
                each.validateNoTypeParametersOnArgs(errors)
            } else {
                each.validatePublicVoidNoArg(false, errors)
            }

            for (signature in ParameterSignature.signatures(each.method)) {
                val annotation = signature.findDeepAnnotation(ParametersSuppliedBy::class.java)
                if (annotation != null) {
                    validateParameterSupplier(annotation.value(), errors)
                }
            }
        }
    }

    private fun validateParameterSupplier(supplierClass: Class<out ParameterSupplier>, errors: MutableList<Throwable>) {
        val constructors = supplierClass.constructors

        if (constructors.size != 1) {
            errors.add(Error("ParameterSupplier " + supplierClass.name +
                    " must have only one constructor (either empty or taking only a TestClass)"))
        } else {
            val paramTypes = constructors[0].parameterTypes
            if (paramTypes.size != 0 && paramTypes[0] != TestClass::class.java) {
                errors.add(Error("ParameterSupplier " + supplierClass.name +
                        " constructor must take either nothing or a single TestClass instance"))
            }
        }
    }

    override fun computeTestMethods(): List<FrameworkMethod> {
        val testMethods = ArrayList(super.computeTestMethods())
        val theoryMethods = testClass.getAnnotatedMethods(Theory::class.java)
        testMethods.removeAll(theoryMethods)
        testMethods.addAll(theoryMethods)
        return testMethods
    }

    public override fun methodBlock(method: FrameworkMethod): Statement {
        return TheoryAnchor(method, testClass)
    }

    class TheoryAnchor(private val testMethod: FrameworkMethod, private val testClass: TestClass) : Statement() {
        private var successes = 0

        private val fInvalidParameters = ArrayList<AssumptionViolatedException>()

        @Throws(Throwable::class)
        override fun evaluate() {
            runWithAssignment(Assignments.allUnassigned(
                    testMethod.method, testClass))

            //if this test method is not annotated with Theory, then no successes is a valid case
            val hasTheoryAnnotation = testMethod.getAnnotation(Theory::class.java) != null
            if (successes == 0 && hasTheoryAnnotation) {
                Assert.fail("Never found parameters that satisfied method assumptions.  Violated assumptions: " + fInvalidParameters)
            }
        }

        @Throws(Throwable::class)
        protected fun runWithAssignment(parameterAssignment: Assignments) {
            if (!parameterAssignment.isComplete) {
                runWithIncompleteAssignment(parameterAssignment)
            } else {
                runWithCompleteAssignment(parameterAssignment)
            }
        }

        @Throws(Throwable::class)
        protected fun runWithIncompleteAssignment(incomplete: Assignments) {
            for (source in incomplete.potentialsForNextUnassigned()) {
                runWithAssignment(incomplete.assignNext(source))
            }
        }

        @Throws(Throwable::class)
        protected fun runWithCompleteAssignment(complete: Assignments) {
            object : BlockJUnit4ClassRunner(testClass.javaClass) {
                override fun collectInitializationErrors(
                        errors: MutableList<Throwable>) {
                    // do nothing
                }

                public override fun methodBlock(method: FrameworkMethod): Statement {
                    val statement = super.methodBlock(method)
                    return object : Statement() {
                        @Throws(Throwable::class)
                        override fun evaluate() {
                            try {
                                statement.evaluate()
                                handleDataPointSuccess()
                            } catch (e: AssumptionViolatedException) {
                                handleAssumptionViolation(e)
                            } catch (e: Throwable) {
                                reportParameterizedError(e, *complete.getArgumentStrings(nullsOk()))
                            }

                        }

                    }
                }

                override fun methodInvoker(method: FrameworkMethod, test: Any): Statement {
                    return methodCompletesWithParameters(method, complete, test)
                }

                @Throws(Exception::class)
                public override fun createTest(): Any {
                    val params = complete.constructorArguments

                    if (!nullsOk()) {
                        Assume.assumeNotNull(*params)
                    }

                    return testClass.onlyConstructor.newInstance(*params)
                }
            }.methodBlock(testMethod).evaluate()
        }

        private fun methodCompletesWithParameters(
                method: FrameworkMethod, complete: Assignments, freshInstance: Any): Statement {
            return object : Statement() {
                @Throws(Throwable::class)
                override fun evaluate() {
                    val values = complete.methodArguments

                    if (!nullsOk()) {
                        Assume.assumeNotNull(*values)
                    }

                    method.invokeExplosively(freshInstance, *values)
                }
            }
        }

        protected fun handleAssumptionViolation(e: AssumptionViolatedException) {
            fInvalidParameters.add(e)
        }

        @Throws(Throwable::class)
        protected fun reportParameterizedError(e: Throwable, vararg params: Any) {
            if (params.size == 0) {
                throw e
            }
            throw ParameterizedAssertionError(e, testMethod.name,
                    *params)
        }

        private fun nullsOk(): Boolean {
            val annotation = testMethod.method.getAnnotation(Theory::class.java) ?: return false
            return annotation.nullsAccepted()
        }

        protected fun handleDataPointSuccess() {
            successes++
        }
    }
}

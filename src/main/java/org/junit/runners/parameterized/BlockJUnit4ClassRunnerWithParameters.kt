package org.junit.runners.parameterized

import java.lang.reflect.Field

import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.model.FrameworkField
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.junit.runners.model.Statement

/**
 * A [BlockJUnit4ClassRunner] with parameters support. Parameters can be
 * injected via constructor or into annotated fields.
 */
class BlockJUnit4ClassRunnerWithParameters @Throws(InitializationError::class)
constructor(test: TestWithParameters) : BlockJUnit4ClassRunner(test.testClass.javaClass) {
    private val parameters: Array<Any>

    protected override val name: String

    init {
        parameters = test.parameters.toArray<Any>(
                arrayOfNulls<Any>(test.parameters.size))
        name = test.name
    }

    @Throws(Exception::class)
    public override fun createTest(): Any {
        if (fieldsAreAnnotated()) {
            return createTestUsingFieldInjection()
        } else {
            return createTestUsingConstructorInjection()
        }
    }

    @Throws(Exception::class)
    private fun createTestUsingConstructorInjection(): Any {
        return testClass.onlyConstructor.newInstance(*parameters)
    }

    @Throws(Exception::class)
    private fun createTestUsingFieldInjection(): Any {
        val annotatedFieldsByParameter = annotatedFieldsByParameter
        if (annotatedFieldsByParameter.size != parameters.size) {
            throw Exception(
                    "Wrong number of parameters and @Parameter fields."
                            + " @Parameter fields counted: "
                            + annotatedFieldsByParameter.size
                            + ", available parameters: " + parameters.size
                            + ".")
        }
        val testClassInstance = testClass.javaClass.newInstance()
        for (each in annotatedFieldsByParameter) {
            val field = each.field
            val annotation = field.getAnnotation(Parameter::class.java)
            val index = annotation.value()
            try {
                field.set(testClassInstance, parameters[index])
            } catch (iare: IllegalArgumentException) {
                throw Exception(testClass.name
                        + ": Trying to set " + field.name
                        + " with the value " + parameters[index]
                        + " that is not the right type ("
                        + parameters[index].javaClass.simpleName
                        + " instead of " + field.type.simpleName
                        + ").", iare)
            }

        }
        return testClassInstance
    }

    override fun testName(method: FrameworkMethod): String {
        return method.name + name
    }

    override fun validateConstructor(errors: MutableList<Throwable>) {
        validateOnlyOneConstructor(errors)
        if (fieldsAreAnnotated()) {
            validateZeroArgConstructor(errors)
        }
    }

    override fun validateFields(errors: MutableList<Throwable>) {
        super.validateFields(errors)
        if (fieldsAreAnnotated()) {
            val annotatedFieldsByParameter = annotatedFieldsByParameter
            val usedIndices = IntArray(annotatedFieldsByParameter.size)
            for (each in annotatedFieldsByParameter) {
                val index = each.field.getAnnotation(Parameter::class.java).value()
                if (index < 0 || index > annotatedFieldsByParameter.size - 1) {
                    errors.add(Exception("Invalid @Parameter value: "
                            + index + ". @Parameter fields counted: "
                            + annotatedFieldsByParameter.size
                            + ". Please use an index between 0 and "
                            + (annotatedFieldsByParameter.size - 1) + "."))
                } else {
                    usedIndices[index]++
                }
            }
            for (index in usedIndices.indices) {
                val numberOfUse = usedIndices[index]
                if (numberOfUse == 0) {
                    errors.add(Exception("@Parameter($index) is never used."))
                } else if (numberOfUse > 1) {
                    errors.add(Exception("@Parameter($index) is used more than once ($numberOfUse)."))
                }
            }
        }
    }

    override fun classBlock(notifier: RunNotifier): Statement {
        return childrenInvoker(notifier)
    }

    protected override val runnerAnnotations: Array<Annotation>
        get() = arrayOfNulls(0)

    private val annotatedFieldsByParameter: List<FrameworkField>
        get() = testClass.getAnnotatedFields(Parameter::class.java)

    private fun fieldsAreAnnotated(): Boolean {
        return !annotatedFieldsByParameter.isEmpty()
    }
}

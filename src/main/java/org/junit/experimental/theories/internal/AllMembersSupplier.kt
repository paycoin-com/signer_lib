package org.junit.experimental.theories.internal

import java.lang.reflect.Array
import java.lang.reflect.Field
import java.util.ArrayList

import org.junit.Assume
import org.junit.experimental.theories.DataPoint
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.ParameterSignature
import org.junit.experimental.theories.ParameterSupplier
import org.junit.experimental.theories.PotentialAssignment
import org.junit.runners.model.FrameworkField
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.TestClass

/**
 * Supplies Theory parameters based on all public members of the target class.
 */
open class AllMembersSupplier
/**
 * Constructs a new supplier for `type`
 */
(private val clazz: TestClass) : ParameterSupplier() {
    internal class MethodParameterValue private constructor(private val method: FrameworkMethod) : PotentialAssignment() {

        override val value: Any
            @Throws(PotentialAssignment.CouldNotGenerateValueException::class)
            get() {
                try {
                    return method.invokeExplosively(null)
                } catch (e: IllegalArgumentException) {
                    throw RuntimeException(
                            "unexpected: argument length is checked")
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(
                            "unexpected: getMethods returned an inaccessible method")
                } catch (throwable: Throwable) {
                    val annotation = method.getAnnotation(DataPoint::class.java)
                    Assume.assumeTrue(annotation == null || !isAssignableToAnyOf(annotation.ignoredExceptions(), throwable))

                    throw PotentialAssignment.CouldNotGenerateValueException(throwable)
                }

            }

        override val description: String
            @Throws(PotentialAssignment.CouldNotGenerateValueException::class)
            get() = method.name
    }

    @Throws(Throwable::class)
    override fun getValueSources(sig: ParameterSignature): List<PotentialAssignment> {
        val list = ArrayList<PotentialAssignment>()

        addSinglePointFields(sig, list)
        addMultiPointFields(sig, list)
        addSinglePointMethods(sig, list)
        addMultiPointMethods(sig, list)

        return list
    }

    @Throws(Throwable::class)
    private fun addMultiPointMethods(sig: ParameterSignature, list: MutableList<PotentialAssignment>) {
        for (dataPointsMethod in getDataPointsMethods(sig)) {
            val returnType = dataPointsMethod.returnType

            if (returnType.isArray && sig.canPotentiallyAcceptType(returnType.componentType) || Iterable<Any>::class.java!!.isAssignableFrom(returnType)) {
                try {
                    addDataPointsValues(returnType, sig, dataPointsMethod.name, list,
                            dataPointsMethod.invokeExplosively(null))
                } catch (throwable: Throwable) {
                    val annotation = dataPointsMethod.getAnnotation(DataPoints::class.java)
                    if (annotation != null && isAssignableToAnyOf(annotation.ignoredExceptions(), throwable)) {
                        return
                    } else {
                        throw throwable
                    }
                }

            }
        }
    }

    private fun addSinglePointMethods(sig: ParameterSignature, list: MutableList<PotentialAssignment>) {
        for (dataPointMethod in getSingleDataPointMethods(sig)) {
            if (sig.canAcceptType(dataPointMethod.type)) {
                list.add(MethodParameterValue(dataPointMethod))
            }
        }
    }

    private fun addMultiPointFields(sig: ParameterSignature, list: MutableList<PotentialAssignment>) {
        for (field in getDataPointsFields(sig)) {
            val type = field.type
            addDataPointsValues(type, sig, field.name, list, getStaticFieldValue(field))
        }
    }

    private fun addSinglePointFields(sig: ParameterSignature, list: MutableList<PotentialAssignment>) {
        for (field in getSingleDataPointFields(sig)) {
            val value = getStaticFieldValue(field)

            if (sig.canAcceptValue(value)) {
                list.add(PotentialAssignment.forValue(field.name, value))
            }
        }
    }

    private fun addDataPointsValues(type: Class<*>, sig: ParameterSignature, name: String,
                                    list: MutableList<PotentialAssignment>, value: Any) {
        if (type.isArray) {
            addArrayValues(sig, name, list, value)
        } else if (Iterable<Any>::class.java!!.isAssignableFrom(type)) {
            addIterableValues(sig, name, list, value as Iterable<*>)
        }
    }

    private fun addArrayValues(sig: ParameterSignature, name: String, list: MutableList<PotentialAssignment>, array: Any) {
        for (i in 0..Array.getLength(array) - 1) {
            val value = Array.get(array, i)
            if (sig.canAcceptValue(value)) {
                list.add(PotentialAssignment.forValue("$name[$i]", value))
            }
        }
    }

    private fun addIterableValues(sig: ParameterSignature, name: String, list: MutableList<PotentialAssignment>, iterable: Iterable<*>) {
        val iterator = iterable.iterator()
        var i = 0
        while (iterator.hasNext()) {
            val value = iterator.next()
            if (sig.canAcceptValue(value)) {
                list.add(PotentialAssignment.forValue("$name[$i]", value))
            }
            i += 1
        }
    }

    private fun getStaticFieldValue(field: Field): Any {
        try {
            return field.get(null)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException(
                    "unexpected: field from getClass doesn't exist on object")
        } catch (e: IllegalAccessException) {
            throw RuntimeException(
                    "unexpected: getFields returned an inaccessible field")
        }

    }

    protected open fun getDataPointsMethods(sig: ParameterSignature): Collection<FrameworkMethod> {
        return clazz.getAnnotatedMethods(DataPoints::class.java)
    }

    protected open fun getSingleDataPointFields(sig: ParameterSignature): Collection<Field> {
        val fields = clazz.getAnnotatedFields(DataPoint::class.java)
        val validFields = ArrayList<Field>()

        for (frameworkField in fields) {
            validFields.add(frameworkField.field)
        }

        return validFields
    }

    protected open fun getDataPointsFields(sig: ParameterSignature): Collection<Field> {
        val fields = clazz.getAnnotatedFields(DataPoints::class.java)
        val validFields = ArrayList<Field>()

        for (frameworkField in fields) {
            validFields.add(frameworkField.field)
        }

        return validFields
    }

    protected open fun getSingleDataPointMethods(sig: ParameterSignature): Collection<FrameworkMethod> {
        return clazz.getAnnotatedMethods(DataPoint::class.java)
    }

    companion object {

        private fun isAssignableToAnyOf(typeArray: Array<Class<*>>, target: Any): Boolean {
            for (type in typeArray) {
                if (type.isAssignableFrom(target.javaClass)) {
                    return true
                }
            }
            return false
        }
    }

}
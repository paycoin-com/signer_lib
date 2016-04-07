package org.junit.experimental.theories.internal

import java.util.Collections.emptyList

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.ArrayList

import org.junit.experimental.theories.ParameterSignature
import org.junit.experimental.theories.ParameterSupplier
import org.junit.experimental.theories.ParametersSuppliedBy
import org.junit.experimental.theories.PotentialAssignment
import org.junit.experimental.theories.PotentialAssignment.CouldNotGenerateValueException
import org.junit.runners.model.TestClass

/**
 * A potentially incomplete list of value assignments for a method's formal
 * parameters
 */
class Assignments private constructor(private val assigned: List<PotentialAssignment>,
                                      private val unassigned: List<ParameterSignature>, private val clazz: TestClass) {

    val isComplete: Boolean
        get() = unassigned.size == 0

    fun nextUnassigned(): ParameterSignature {
        return unassigned[0]
    }

    fun assignNext(source: PotentialAssignment): Assignments {
        val assigned = ArrayList(
                this.assigned)
        assigned.add(source)

        return Assignments(assigned, unassigned.subList(1,
                unassigned.size), clazz)
    }

    @Throws(CouldNotGenerateValueException::class)
    fun getActualValues(start: Int, stop: Int): Array<Any> {
        val values = arrayOfNulls<Any>(stop - start)
        for (i in start..stop - 1) {
            values[i - start] = assigned[i].value
        }
        return values
    }

    @Throws(Throwable::class)
    fun potentialsForNextUnassigned(): List<PotentialAssignment> {
        val unassigned = nextUnassigned()
        var assignments = getSupplier(unassigned).getValueSources(unassigned)

        if (assignments.size == 0) {
            assignments = generateAssignmentsFromTypeAlone(unassigned)
        }

        return assignments
    }

    private fun generateAssignmentsFromTypeAlone(unassigned: ParameterSignature): List<PotentialAssignment> {
        val paramType = unassigned.type

        if (paramType.isEnum) {
            return EnumSupplier(paramType).getValueSources(unassigned)
        } else if (paramType == Boolean::class.java || paramType == Boolean.TYPE) {
            return BooleanSupplier().getValueSources(unassigned)
        } else {
            return emptyList()
        }
    }

    @Throws(Exception::class)
    private fun getSupplier(unassigned: ParameterSignature): ParameterSupplier {
        val annotation = unassigned.findDeepAnnotation(ParametersSuppliedBy::class.java)

        if (annotation != null) {
            return buildParameterSupplierFromClass(annotation.value())
        } else {
            return AllMembersSupplier(clazz)
        }
    }

    @Throws(Exception::class)
    private fun buildParameterSupplierFromClass(
            cls: Class<out ParameterSupplier>): ParameterSupplier {
        val supplierConstructors = cls.constructors

        for (constructor in supplierConstructors) {
            val parameterTypes = constructor.parameterTypes
            if (parameterTypes.size == 1 && parameterTypes[0] == TestClass::class.java) {
                return constructor.newInstance(clazz) as ParameterSupplier
            }
        }

        return cls.newInstance()
    }

    val constructorArguments: Array<Any>
        @Throws(CouldNotGenerateValueException::class)
        get() = getActualValues(0, constructorParameterCount)

    val methodArguments: Array<Any>
        @Throws(CouldNotGenerateValueException::class)
        get() = getActualValues(constructorParameterCount, assigned.size)

    val allArguments: Array<Any>
        @Throws(CouldNotGenerateValueException::class)
        get() = getActualValues(0, assigned.size)

    private val constructorParameterCount: Int
        get() {
            val signatures = ParameterSignature.signatures(clazz.onlyConstructor)
            val constructorParameterCount = signatures.size
            return constructorParameterCount
        }

    @Throws(CouldNotGenerateValueException::class)
    fun getArgumentStrings(nullsOk: Boolean): Array<Any> {
        val values = arrayOfNulls<Any>(assigned.size)
        for (i in values.indices) {
            values[i] = assigned[i].description
        }
        return values
    }

    companion object {

        /**
         * Returns a new assignment list for `testMethod`, with no params
         * assigned.
         */
        fun allUnassigned(testMethod: Method,
                          testClass: TestClass): Assignments {
            val signatures: MutableList<ParameterSignature>
            signatures = ParameterSignature.signatures(testClass.onlyConstructor)
            signatures.addAll(ParameterSignature.signatures(testMethod))
            return Assignments(ArrayList<PotentialAssignment>(),
                    signatures, testClass)
        }
    }
}
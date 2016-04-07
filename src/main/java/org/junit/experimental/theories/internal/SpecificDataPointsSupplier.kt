package org.junit.experimental.theories.internal

import java.lang.reflect.Field
import java.util.ArrayList
import java.util.Arrays

import org.junit.experimental.theories.DataPoint
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.FromDataPoints
import org.junit.experimental.theories.ParameterSignature
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.TestClass

class SpecificDataPointsSupplier(testClass: TestClass) : AllMembersSupplier(testClass) {

    override fun getSingleDataPointFields(sig: ParameterSignature): Collection<Field> {
        val fields = super.getSingleDataPointFields(sig)
        val requestedName = sig.getAnnotation(FromDataPoints::class.java)!!.value()

        val fieldsWithMatchingNames = ArrayList<Field>()

        for (field in fields) {
            val fieldNames = field.getAnnotation(DataPoint::class.java).value()
            if (Arrays.asList<String>(*fieldNames).contains(requestedName)) {
                fieldsWithMatchingNames.add(field)
            }
        }

        return fieldsWithMatchingNames
    }

    override fun getDataPointsFields(sig: ParameterSignature): Collection<Field> {
        val fields = super.getDataPointsFields(sig)
        val requestedName = sig.getAnnotation(FromDataPoints::class.java)!!.value()

        val fieldsWithMatchingNames = ArrayList<Field>()

        for (field in fields) {
            val fieldNames = field.getAnnotation(DataPoints::class.java).value()
            if (Arrays.asList<String>(*fieldNames).contains(requestedName)) {
                fieldsWithMatchingNames.add(field)
            }
        }

        return fieldsWithMatchingNames
    }

    override fun getSingleDataPointMethods(sig: ParameterSignature): Collection<FrameworkMethod> {
        val methods = super.getSingleDataPointMethods(sig)
        val requestedName = sig.getAnnotation(FromDataPoints::class.java)!!.value()

        val methodsWithMatchingNames = ArrayList<FrameworkMethod>()

        for (method in methods) {
            val methodNames = method.getAnnotation(DataPoint::class.java).value()
            if (Arrays.asList<String>(*methodNames).contains(requestedName)) {
                methodsWithMatchingNames.add(method)
            }
        }

        return methodsWithMatchingNames
    }

    override fun getDataPointsMethods(sig: ParameterSignature): Collection<FrameworkMethod> {
        val methods = super.getDataPointsMethods(sig)
        val requestedName = sig.getAnnotation(FromDataPoints::class.java)!!.value()

        val methodsWithMatchingNames = ArrayList<FrameworkMethod>()

        for (method in methods) {
            val methodNames = method.getAnnotation(DataPoints::class.java).value()
            if (Arrays.asList<String>(*methodNames).contains(requestedName)) {
                methodsWithMatchingNames.add(method)
            }
        }

        return methodsWithMatchingNames
    }

}

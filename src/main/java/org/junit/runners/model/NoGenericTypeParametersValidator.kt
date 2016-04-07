package org.junit.runners.model

import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

internal class NoGenericTypeParametersValidator(private val method: Method) {

    fun validate(errors: MutableList<Throwable>) {
        for (each in method.genericParameterTypes) {
            validateNoTypeParameterOnType(each, errors)
        }
    }

    private fun validateNoTypeParameterOnType(type: Type, errors: MutableList<Throwable>) {
        if (type is TypeVariable<*>) {
            errors.add(Exception("Method " + method.name
                    + "() contains unresolved type variable " + type))
        } else if (type is ParameterizedType) {
            validateNoTypeParameterOnParameterizedType(type, errors)
        } else if (type is WildcardType) {
            validateNoTypeParameterOnWildcardType(type, errors)
        } else if (type is GenericArrayType) {
            validateNoTypeParameterOnGenericArrayType(type, errors)
        }
    }

    private fun validateNoTypeParameterOnParameterizedType(parameterized: ParameterizedType,
                                                           errors: List<Throwable>) {
        for (each in parameterized.actualTypeArguments) {
            validateNoTypeParameterOnType(each, errors)
        }
    }

    private fun validateNoTypeParameterOnWildcardType(wildcard: WildcardType,
                                                      errors: List<Throwable>) {
        for (each in wildcard.upperBounds) {
            validateNoTypeParameterOnType(each, errors)
        }
        for (each in wildcard.lowerBounds) {
            validateNoTypeParameterOnType(each, errors)
        }
    }

    private fun validateNoTypeParameterOnGenericArrayType(
            arrayType: GenericArrayType, errors: List<Throwable>) {
        validateNoTypeParameterOnType(arrayType.genericComponentType, errors)
    }
}
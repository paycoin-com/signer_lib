package org.junit.experimental.theories.internal

import java.util.ArrayList

import org.junit.experimental.theories.ParameterSignature
import org.junit.experimental.theories.ParameterSupplier
import org.junit.experimental.theories.PotentialAssignment

class EnumSupplier(private val enumType: Class<*>) : ParameterSupplier() {

    override fun getValueSources(sig: ParameterSignature): List<PotentialAssignment> {
        val enumValues = enumType.enumConstants

        val assignments = ArrayList<PotentialAssignment>()
        for (value in enumValues) {
            assignments.add(PotentialAssignment.forValue(value.toString(), value))
        }

        return assignments
    }

}

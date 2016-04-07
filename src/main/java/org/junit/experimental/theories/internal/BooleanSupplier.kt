package org.junit.experimental.theories.internal

import java.util.Arrays

import org.junit.experimental.theories.ParameterSignature
import org.junit.experimental.theories.ParameterSupplier
import org.junit.experimental.theories.PotentialAssignment

class BooleanSupplier : ParameterSupplier() {

    override fun getValueSources(sig: ParameterSignature): List<PotentialAssignment> {
        return Arrays.asList(PotentialAssignment.forValue("true", true),
                PotentialAssignment.forValue("false", false))
    }

}

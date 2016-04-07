package org.junit.experimental.theories.suppliers

import java.util.ArrayList

import org.junit.experimental.theories.ParameterSignature
import org.junit.experimental.theories.ParameterSupplier
import org.junit.experimental.theories.PotentialAssignment

/**
 * @see org.junit.experimental.theories.suppliers.TestedOn

 * @see org.junit.experimental.theories.ParameterSupplier
 */
class TestedOnSupplier : ParameterSupplier() {
    override fun getValueSources(sig: ParameterSignature): List<PotentialAssignment> {
        val list = ArrayList<PotentialAssignment>()
        val testedOn = sig.getAnnotation(TestedOn::class.java)
        val ints = testedOn.ints()
        for (i in ints) {
            list.add(PotentialAssignment.forValue("ints", i))
        }
        return list
    }
}

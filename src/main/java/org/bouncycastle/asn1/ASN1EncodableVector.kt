package org.bouncycastle.asn1

import java.util.Enumeration
import java.util.Vector

/**
 * Mutable class for building ASN.1 constructed objects.
 */
open class ASN1EncodableVector {
    private val v = Vector()

    /**
     * Add an encodable to the vector.

     * @param obj the encodable to add.
     */
    fun add(obj: ASN1Encodable) {
        v.addElement(obj)
    }

    /**
     * Add the contents of another vector.

     * @param other the vector to add.
     */
    fun addAll(other: ASN1EncodableVector) {
        val en = other.v.elements()
        while (en.hasMoreElements()) {
            v.addElement(en.nextElement())
        }
    }

    /**
     * Return the object at position i in this vector.

     * @param i the index of the object of interest.
     * *
     * @return the object at position i.
     */
    operator fun get(i: Int): ASN1Encodable {
        return v.elementAt(i)
    }

    /**
     * Return the size of the vector.

     * @return the object count in the vector.
     */
    fun size(): Int {
        return v.size
    }
}
/**
 * Base constructor.
 */

package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Enumeration
import java.util.Vector


@Deprecated("use BEROctetString")
class BERConstructedOctetString : BEROctetString {

    private val octs: Vector<Any>?

    /**
     * @param string the octets making up the octet string.
     */
    constructor(
            string: ByteArray) : super(string) {
    }

    constructor(
            octs: Vector<Any>) : super(toBytes(octs)) {

        this.octs = octs
    }

    constructor(
            obj: ASN1Primitive) : super(toByteArray(obj)) {
    }

    constructor(
            obj: ASN1Encodable) : this(obj.toASN1Primitive()) {
    }

    override var octets: ByteArray
        get() = octets
        set(value: ByteArray) {
            super.octets = value
        }

    /**
     * return the DER octets that make up this string.
     */
    override val objects: Enumeration<Any>
        get() {
            if (octs == null) {
                return generateOcts().elements()
            }

            return octs.elements()
        }

    private fun generateOcts(): Vector<Any> {
        val vec = Vector()
        var i = 0
        while (i < octets.size) {
            val end: Int

            if (i + MAX_LENGTH > octets.size) {
                end = octets.size
            } else {
                end = i + MAX_LENGTH
            }

            val nStr = ByteArray(end - i)

            System.arraycopy(octets, i, nStr, 0, nStr.size)

            vec.addElement(DEROctetString(nStr))
            i += MAX_LENGTH
        }

        return vec
    }

    companion object {
        private val MAX_LENGTH = 1000

        /**
         * convert a vector of octet strings into a single byte string
         */
        private fun toBytes(
                octs: Vector<Any>): ByteArray {
            val bOut = ByteArrayOutputStream()

            for (i in octs.indices) {
                try {
                    val o = octs.elementAt(i) as DEROctetString

                    bOut.write(o.octets)
                } catch (e: ClassCastException) {
                    throw IllegalArgumentException(octs.elementAt(i).javaClass.name + " found in input should only contain DEROctetString")
                } catch (e: IOException) {
                    throw IllegalArgumentException("exception converting octets " + e.toString())
                }

            }

            return bOut.toByteArray()
        }

        private fun toByteArray(obj: ASN1Primitive): ByteArray {
            try {
                return obj.encoded
            } catch (e: IOException) {
                throw IllegalArgumentException("Unable to encode object")
            }

        }

        override fun fromSequence(seq: ASN1Sequence): BEROctetString {
            val v = Vector()
            val e = seq.objects

            while (e.hasMoreElements()) {
                v.addElement(e.nextElement())
            }

            return BERConstructedOctetString(v)
        }
    }
}

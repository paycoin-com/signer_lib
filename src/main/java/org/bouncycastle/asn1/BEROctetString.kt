package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Enumeration
import java.util.Vector

open class BEROctetString : ASN1OctetString {

    private val octs: Array<ASN1OctetString>?

    /**
     * @param string the octets making up the octet string.
     */
    constructor(
            string: ByteArray) : super(string) {
    }

    constructor(
            octs: Array<ASN1OctetString>) : super(toBytes(octs)) {

        this.octs = octs
    }

    override var octets: ByteArray
        get() = octets
        set(value: ByteArray) {
            super.octets = value
        }

    /**
     * return the DER octets that make up this string.
     */
    open val objects: Enumeration<Any>
        get() {
            if (octs == null) {
                return generateOcts().elements()
            }

            return object : Enumeration {
                internal var counter = 0

                override fun hasMoreElements(): Boolean {
                    return counter < octs.size
                }

                override fun nextElement(): Any {
                    return octs[counter++]
                }
            }
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

    internal override val isConstructed: Boolean
        get() = true

    @Throws(IOException::class)
    internal override fun encodedLength(): Int {
        var length = 0
        val e = objects
        while (e.hasMoreElements()) {
            length += (e.nextElement() as ASN1Encodable).toASN1Primitive().encodedLength()
        }

        return 2 + length + 2
    }

    @Throws(IOException::class)
    public override fun encode(
            out: ASN1OutputStream) {
        out.write(BERTags.CONSTRUCTED or BERTags.OCTET_STRING)

        out.write(0x80)

        //
        // write out the octet array
        //
        val e = objects
        while (e.hasMoreElements()) {
            out.writeObject(e.nextElement() as ASN1Encodable)
        }

        out.write(0x00)
        out.write(0x00)
    }

    companion object {
        private val MAX_LENGTH = 1000

        /**
         * convert a vector of octet strings into a single byte string
         */
        private fun toBytes(
                octs: Array<ASN1OctetString>): ByteArray {
            val bOut = ByteArrayOutputStream()

            for (i in octs.indices) {
                try {
                    val o = octs[i] as DEROctetString

                    bOut.write(o.octets)
                } catch (e: ClassCastException) {
                    throw IllegalArgumentException(octs[i].javaClass.name + " found in input should only contain DEROctetString")
                } catch (e: IOException) {
                    throw IllegalArgumentException("exception converting octets " + e.toString())
                }

            }

            return bOut.toByteArray()
        }

        internal fun fromSequence(seq: ASN1Sequence): BEROctetString {
            val v = arrayOfNulls<ASN1OctetString>(seq.size())
            val e = seq.objects
            var index = 0

            while (e.hasMoreElements()) {
                v[index++] = e.nextElement() as ASN1OctetString
            }

            return BEROctetString(v)
        }
    }
}

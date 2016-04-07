package org.bouncycastle.asn1

import java.io.IOException
import java.util.Enumeration

/**
 * Note: this class is for processing DER/DL encoded sequences only.
 */
internal class LazyEncodedSequence @Throws(IOException::class)
constructor(
        private var encoded: ByteArray?) : ASN1Sequence() {

    private fun parse() {
        val en = LazyConstructionEnumeration(encoded)

        while (en.hasMoreElements()) {
            seq.addElement(en.nextElement())
        }

        encoded = null
    }

    @Synchronized override fun getObjectAt(index: Int): ASN1Encodable {
        if (encoded != null) {
            parse()
        }

        return super.getObjectAt(index)
    }

    override val objects: Enumeration<Any>
        @Synchronized get() {
            if (encoded == null) {
                return super.objects
            }

            return LazyConstructionEnumeration(encoded)
        }

    @Synchronized override fun size(): Int {
        if (encoded != null) {
            parse()
        }

        return super.size()
    }

    internal override fun toDERObject(): ASN1Primitive {
        if (encoded != null) {
            parse()
        }

        return super.toDERObject()
    }

    internal override fun toDLObject(): ASN1Primitive {
        if (encoded != null) {
            parse()
        }

        return super.toDLObject()
    }

    @Throws(IOException::class)
    internal override fun encodedLength(): Int {
        if (encoded != null) {
            return 1 + StreamUtil.calculateBodyLength(encoded!!.size) + encoded!!.size
        } else {
            return super.toDLObject().encodedLength()
        }
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        if (encoded != null) {
            out.writeEncoded(BERTags.SEQUENCE or BERTags.CONSTRUCTED, encoded)
        } else {
            super.toDLObject().encode(out)
        }
    }
}

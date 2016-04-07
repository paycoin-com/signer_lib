package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.util.Strings

class GeneralNames : ASN1Object {
    private val names: Array<GeneralName>

    /**
     * Construct a GeneralNames object containing one GeneralName.

     * @param name the name to be contained.
     */
    constructor(
            name: GeneralName) {
        this.names = arrayOf(name)
    }


    constructor(
            names: Array<GeneralName>) {
        this.names = names
    }

    private constructor(
            seq: ASN1Sequence) {
        this.names = arrayOfNulls<GeneralName>(seq.size())

        for (i in 0..seq.size() - 1) {
            names[i] = GeneralName.getInstance(seq.getObjectAt(i))
        }
    }

    fun getNames(): Array<GeneralName> {
        val tmp = arrayOfNulls<GeneralName>(names.size)

        System.arraycopy(names, 0, tmp, 0, names.size)

        return tmp
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * GeneralNames ::= SEQUENCE SIZE {1..MAX} OF GeneralName
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return DERSequence(names)
    }

    override fun toString(): String {
        val buf = StringBuffer()
        val sep = Strings.lineSeparator()

        buf.append("GeneralNames:")
        buf.append(sep)

        for (i in names.indices) {
            buf.append("    ")
            buf.append(names[i])
            buf.append(sep)
        }
        return buf.toString()
    }

    companion object {

        fun getInstance(
                obj: Any?): GeneralNames? {
            if (obj is GeneralNames) {
                return obj
            }

            if (obj != null) {
                return GeneralNames(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): GeneralNames {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun fromExtensions(extensions: Extensions, extOID: ASN1ObjectIdentifier): GeneralNames {
            return GeneralNames.getInstance(extensions.getExtensionParsedValue(extOID))
        }
    }
}

package org.bouncycastle.asn1.x509

import java.util.Enumeration
import java.util.Vector

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTF8String

/**
 * Implementation of `IetfAttrSyntax` as specified by RFC3281.
 */
class IetfAttrSyntax
/**

 */
private constructor(seq: ASN1Sequence) : ASN1Object() {
    var policyAuthority: GeneralNames? = null
        internal set
    internal var values = Vector()
    var valueType = -1
        internal set

    init {
        var seq = seq
        var i = 0

        if (seq.getObjectAt(0) is ASN1TaggedObject) {
            policyAuthority = GeneralNames.getInstance(seq.getObjectAt(0) as ASN1TaggedObject, false)
            i++
        } else if (seq.size() == 2) {
            // VOMS fix
            policyAuthority = GeneralNames.getInstance(seq.getObjectAt(0))
            i++
        }

        if (seq.getObjectAt(i) !is ASN1Sequence) {
            throw IllegalArgumentException("Non-IetfAttrSyntax encoding")
        }

        seq = seq.getObjectAt(i) as ASN1Sequence

        val e = seq.objects
        while (e.hasMoreElements()) {
            val obj = e.nextElement() as ASN1Primitive
            val type: Int

            if (obj is ASN1ObjectIdentifier) {
                type = VALUE_OID
            } else if (obj is DERUTF8String) {
                type = VALUE_UTF8
            } else if (obj is DEROctetString) {
                type = VALUE_OCTETS
            } else {
                throw IllegalArgumentException("Bad value type encoding IetfAttrSyntax")
            }

            if (valueType < 0) {
                valueType = type
            }

            if (type != valueType) {
                throw IllegalArgumentException("Mix of value types in IetfAttrSyntax")
            }

            values.addElement(obj)
        }
    }

    fun getValues(): Array<Any> {
        if (this.valueType == VALUE_OCTETS) {
            val tmp = arrayOfNulls<ASN1OctetString>(values.size)

            for (i in tmp.indices) {
                tmp[i] = values.elementAt(i) as ASN1OctetString
            }

            return tmp
        } else if (this.valueType == VALUE_OID) {
            val tmp = arrayOfNulls<ASN1ObjectIdentifier>(values.size)

            for (i in tmp.indices) {
                tmp[i] = values.elementAt(i) as ASN1ObjectIdentifier
            }

            return tmp
        } else {
            val tmp = arrayOfNulls<DERUTF8String>(values.size)

            for (i in tmp.indices) {
                tmp[i] = values.elementAt(i) as DERUTF8String
            }

            return tmp
        }
    }

    /**

     *

     * IetfAttrSyntax ::= SEQUENCE {
     * policyAuthority [0] GeneralNames OPTIONAL,
     * values SEQUENCE OF CHOICE {
     * octets OCTET STRING,
     * oid OBJECT IDENTIFIER,
     * string UTF8String
     * }
     * }

     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (policyAuthority != null) {
            v.add(DERTaggedObject(0, policyAuthority))
        }

        val v2 = ASN1EncodableVector()

        val i = values.elements()
        while (i.hasMoreElements()) {
            v2.add(i.nextElement() as ASN1Encodable)
        }

        v.add(DERSequence(v2))

        return DERSequence(v)
    }

    companion object {
        val VALUE_OCTETS = 1
        val VALUE_OID = 2
        val VALUE_UTF8 = 3

        fun getInstance(obj: Any?): IetfAttrSyntax? {
            if (obj is IetfAttrSyntax) {
                return obj
            }
            if (obj != null) {
                return IetfAttrSyntax(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

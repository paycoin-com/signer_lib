package org.bouncycastle.asn1.x500

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet

/**
 * Holding class for a single Relative Distinguished Name (RDN).
 */
class RDN : ASN1Object {
    private var values: ASN1Set? = null

    private constructor(values: ASN1Set) {
        this.values = values
    }

    /**
     * Create a single valued RDN.

     * @param oid RDN type.
     * *
     * @param value RDN value.
     */
    constructor(oid: ASN1ObjectIdentifier, value: ASN1Encodable) {
        val v = ASN1EncodableVector()

        v.add(oid)
        v.add(value)

        this.values = DERSet(DERSequence(v))
    }

    constructor(attrTAndV: AttributeTypeAndValue) {
        this.values = DERSet(attrTAndV)
    }

    /**
     * Create a multi-valued RDN.

     * @param aAndVs attribute type/value pairs making up the RDN
     */
    constructor(aAndVs: Array<AttributeTypeAndValue>) {
        this.values = DERSet(aAndVs)
    }

    val isMultiValued: Boolean
        get() = this.values!!.size() > 1

    /**
     * Return the number of AttributeTypeAndValue objects in this RDN,

     * @return size of RDN, greater than 1 if multi-valued.
     */
    fun size(): Int {
        return this.values!!.size()
    }

    val first: AttributeTypeAndValue?
        get() {
            if (this.values!!.size() == 0) {
                return null
            }

            return AttributeTypeAndValue.getInstance(this.values!!.getObjectAt(0))
        }

    val typesAndValues: Array<AttributeTypeAndValue>
        get() {
            val tmp = arrayOfNulls<AttributeTypeAndValue>(values!!.size())

            for (i in tmp.indices) {
                tmp[i] = AttributeTypeAndValue.getInstance(values!!.getObjectAt(i))
            }

            return tmp
        }

    /**
     *
     * RelativeDistinguishedName ::=
     * SET OF AttributeTypeAndValue

     * AttributeTypeAndValue ::= SEQUENCE {
     * type     AttributeType,
     * value    AttributeValue }
     *
     * @return this object as its ASN1Primitive type
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return values
    }

    companion object {

        fun getInstance(obj: Any?): RDN? {
            if (obj is RDN) {
                return obj
            } else if (obj != null) {
                return RDN(ASN1Set.getInstance(obj))
            }

            return null
        }
    }
}

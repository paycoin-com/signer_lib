package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.util.Strings

/**
 * The DistributionPointName object.
 *
 * DistributionPointName ::= CHOICE {
 * fullName                 [0] GeneralNames,
 * nameRelativeToCRLIssuer  [1] RDN
 * }
 *
 */
class DistributionPointName : ASN1Object, ASN1Choice {
    internal var name: ASN1Encodable
    /**
     * Return the tag number applying to the underlying choice.

     * @return the tag number for this point name.
     */
    var type: Int = 0
        internal set

    constructor(
            type: Int,
            name: ASN1Encodable) {
        this.type = type
        this.name = name
    }

    constructor(
            name: GeneralNames) : this(FULL_NAME, name) {
    }

    /**
     * Return the tagged object inside the distribution point name.

     * @return the underlying choice item.
     */
    fun getName(): ASN1Encodable {
        return name
    }

    constructor(
            obj: ASN1TaggedObject) {
        this.type = obj.tagNo

        if (type == 0) {
            this.name = GeneralNames.getInstance(obj, false)
        } else {
            this.name = ASN1Set.getInstance(obj, false)
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return DERTaggedObject(false, type, name)
    }

    override fun toString(): String {
        val sep = Strings.lineSeparator()
        val buf = StringBuffer()
        buf.append("DistributionPointName: [")
        buf.append(sep)
        if (type == FULL_NAME) {
            appendObject(buf, sep, "fullName", name.toString())
        } else {
            appendObject(buf, sep, "nameRelativeToCRLIssuer", name.toString())
        }
        buf.append("]")
        buf.append(sep)
        return buf.toString()
    }

    private fun appendObject(buf: StringBuffer, sep: String, name: String, value: String) {
        val indent = "    "

        buf.append(indent)
        buf.append(name)
        buf.append(":")
        buf.append(sep)
        buf.append(indent)
        buf.append(indent)
        buf.append(value)
        buf.append(sep)
    }

    companion object {

        val FULL_NAME = 0
        val NAME_RELATIVE_TO_CRL_ISSUER = 1

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DistributionPointName {
            return getInstance(ASN1TaggedObject.getInstance(obj, true))
        }

        fun getInstance(
                obj: Any?): DistributionPointName {
            if (obj == null || obj is DistributionPointName) {
                return obj as DistributionPointName?
            } else if (obj is ASN1TaggedObject) {
                return DistributionPointName(obj as ASN1TaggedObject?)
            }

            throw IllegalArgumentException("unknown object in factory: " + obj.javaClass.name)
        }
    }
}

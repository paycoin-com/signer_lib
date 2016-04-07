package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject

class AttCertIssuer : ASN1Object, ASN1Choice {
    var issuer: ASN1Encodable
        internal set
    internal var choiceObj: ASN1Primitive

    /**
     * Don't use this one if you are trying to be RFC 3281 compliant.
     * Use it for v1 attribute certificates only.

     * @param names our GeneralNames structure
     */
    constructor(
            names: GeneralNames) {
        issuer = names
        choiceObj = issuer.toASN1Primitive()
    }

    constructor(
            v2Form: V2Form) {
        issuer = v2Form
        choiceObj = DERTaggedObject(false, 0, issuer)
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * AttCertIssuer ::= CHOICE {
     * v1Form   GeneralNames,  -- MUST NOT be used in this
     * -- profile
     * v2Form   [0] V2Form     -- v2 only
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return choiceObj
    }

    companion object {

        fun getInstance(
                obj: Any?): AttCertIssuer {
            if (obj == null || obj is AttCertIssuer) {
                return obj as AttCertIssuer?
            } else if (obj is V2Form) {
                return AttCertIssuer(V2Form.getInstance(obj))
            } else if (obj is GeneralNames) {
                return AttCertIssuer(obj as GeneralNames?)
            } else if (obj is ASN1TaggedObject) {
                return AttCertIssuer(V2Form.getInstance(obj as ASN1TaggedObject?, false))
            } else if (obj is ASN1Sequence) {
                return AttCertIssuer(GeneralNames.getInstance(obj))
            }

            throw IllegalArgumentException("unknown object in factory: " + obj.javaClass.name)
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): AttCertIssuer {
            return getInstance(obj.`object`) // must be explicitly tagged
        }
    }
}

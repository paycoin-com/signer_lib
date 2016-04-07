package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.asn1.x509.X509Name

/**
 * PKCS10 CertificationRequestInfo object.
 *
 * CertificationRequestInfo ::= SEQUENCE {
 * version             INTEGER { v1(0) } (v1,...),
 * subject             Name,
 * subjectPKInfo   SubjectPublicKeyInfo{{ PKInfoAlgorithms }},
 * attributes          [0] Attributes{{ CRIAttributes }}
 * }

 * Attributes { ATTRIBUTE:IOSet } ::= SET OF Attribute{{ IOSet }}

 * Attribute { ATTRIBUTE:IOSet } ::= SEQUENCE {
 * type    ATTRIBUTE.&amp;id({IOSet}),
 * values  SET SIZE(1..MAX) OF ATTRIBUTE.&amp;Type({IOSet}{\@type})
 * }
 *
 */
class CertificationRequestInfo : ASN1Object {
    var version: ASN1Integer? = ASN1Integer(0)
        internal set
    var subject: X500Name? = null
        internal set
    var subjectPublicKeyInfo: SubjectPublicKeyInfo? = null
        internal set
    var attributes: ASN1Set? = null
        internal set

    /**
     * Basic constructor.
     *
     *
     * Note: Early on a lot of CAs would only accept messages with attributes missing. As the ASN.1 def shows
     * the attributes field is not optional so should always at least contain an empty set. If a fully compliant
     * request is required, pass in an empty set, the class will otherwise interpret a null as it should
     * encode the request with the field missing.
     *

     * @param subject subject to be associated with the public key
     * *
     * @param pkInfo public key to be associated with subject
     * *
     * @param attributes any attributes to be associated with the request.
     */
    constructor(
            subject: X500Name?,
            pkInfo: SubjectPublicKeyInfo?,
            attributes: ASN1Set) {
        if (subject == null || pkInfo == null) {
            throw IllegalArgumentException("Not all mandatory fields set in CertificationRequestInfo generator.")
        }

        this.subject = subject
        this.subjectPublicKeyInfo = pkInfo
        this.attributes = attributes
    }


    @Deprecated("use X500Name method.")
    constructor(
            subject: X509Name?,
            pkInfo: SubjectPublicKeyInfo?,
            attributes: ASN1Set) {
        if (subject == null || pkInfo == null) {
            throw IllegalArgumentException("Not all mandatory fields set in CertificationRequestInfo generator.")
        }

        this.subject = X500Name.getInstance(subject.toASN1Primitive())
        this.subjectPublicKeyInfo = pkInfo
        this.attributes = attributes
    }


    @Deprecated("use getInstance().")
    constructor(
            seq: ASN1Sequence) {
        version = seq.getObjectAt(0) as ASN1Integer

        subject = X500Name.getInstance(seq.getObjectAt(1))
        subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(seq.getObjectAt(2))

        //
        // some CertificationRequestInfo objects seem to treat this field
        // as optional.
        //
        if (seq.size() > 3) {
            val tagobj = seq.getObjectAt(3) as DERTaggedObject
            attributes = ASN1Set.getInstance(tagobj, false)
        }

        if (subject == null || version == null || subjectPublicKeyInfo == null) {
            throw IllegalArgumentException("Not all mandatory fields set in CertificationRequestInfo generator.")
        }
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(subject)
        v.add(subjectPublicKeyInfo)

        if (attributes != null) {
            v.add(DERTaggedObject(false, 0, attributes))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: Any?): CertificationRequestInfo? {
            if (obj is CertificationRequestInfo) {
                return obj
            } else if (obj != null) {
                return CertificationRequestInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

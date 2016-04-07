package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.crmf.EncryptedValue
import org.bouncycastle.asn1.crmf.PKIPublicationInfo

class CertifiedKeyPair : ASN1Object {
    var certOrEncCert: CertOrEncCert? = null
        private set
    var privateKey: EncryptedValue? = null
        private set
    var publicationInfo: PKIPublicationInfo? = null
        private set

    private constructor(seq: ASN1Sequence) {
        certOrEncCert = CertOrEncCert.getInstance(seq.getObjectAt(0))

        if (seq.size() >= 2) {
            if (seq.size() == 2) {
                val tagged = ASN1TaggedObject.getInstance(seq.getObjectAt(1))
                if (tagged.tagNo == 0) {
                    privateKey = EncryptedValue.getInstance(tagged.`object`)
                } else {
                    publicationInfo = PKIPublicationInfo.getInstance(tagged.`object`)
                }
            } else {
                privateKey = EncryptedValue.getInstance(ASN1TaggedObject.getInstance(seq.getObjectAt(1)))
                publicationInfo = PKIPublicationInfo.getInstance(ASN1TaggedObject.getInstance(seq.getObjectAt(2)))
            }
        }
    }

    @JvmOverloads constructor(
            certOrEncCert: CertOrEncCert?,
            privateKey: EncryptedValue? = null,
            publicationInfo: PKIPublicationInfo? = null) {
        if (certOrEncCert == null) {
            throw IllegalArgumentException("'certOrEncCert' cannot be null")
        }

        this.certOrEncCert = certOrEncCert
        this.privateKey = privateKey
        this.publicationInfo = publicationInfo
    }

    /**
     *
     * CertifiedKeyPair ::= SEQUENCE {
     * certOrEncCert       CertOrEncCert,
     * privateKey      [0] EncryptedValue      OPTIONAL,
     * -- see [CRMF] for comment on encoding
     * publicationInfo [1] PKIPublicationInfo  OPTIONAL
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certOrEncCert)

        if (privateKey != null) {
            v.add(DERTaggedObject(true, 0, privateKey))
        }

        if (publicationInfo != null) {
            v.add(DERTaggedObject(true, 1, publicationInfo))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): CertifiedKeyPair? {
            if (o is CertifiedKeyPair) {
                return o
            }

            if (o != null) {
                return CertifiedKeyPair(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

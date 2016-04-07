package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.crmf.EncryptedValue

class CertOrEncCert : ASN1Object, ASN1Choice {
    var certificate: CMPCertificate? = null
        private set
    var encryptedCert: EncryptedValue? = null
        private set

    private constructor(tagged: ASN1TaggedObject) {
        if (tagged.tagNo == 0) {
            certificate = CMPCertificate.getInstance(tagged.`object`)
        } else if (tagged.tagNo == 1) {
            encryptedCert = EncryptedValue.getInstance(tagged.`object`)
        } else {
            throw IllegalArgumentException("unknown tag: " + tagged.tagNo)
        }
    }

    constructor(certificate: CMPCertificate?) {
        if (certificate == null) {
            throw IllegalArgumentException("'certificate' cannot be null")
        }

        this.certificate = certificate
    }

    constructor(encryptedCert: EncryptedValue?) {
        if (encryptedCert == null) {
            throw IllegalArgumentException("'encryptedCert' cannot be null")
        }

        this.encryptedCert = encryptedCert
    }

    /**
     *
     * CertOrEncCert ::= CHOICE {
     * certificate     [0] CMPCertificate,
     * encryptedCert   [1] EncryptedValue
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        if (certificate != null) {
            return DERTaggedObject(true, 0, certificate)
        }

        return DERTaggedObject(true, 1, encryptedCert)
    }

    companion object {

        fun getInstance(o: Any): CertOrEncCert? {
            if (o is CertOrEncCert) {
                return o
            }

            if (o is ASN1TaggedObject) {
                return CertOrEncCert(o)
            }

            return null
        }
    }
}

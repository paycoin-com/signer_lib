package org.bouncycastle.asn1.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500Name

/**
 * PKIX RFC-2459

 * The X.509 v2 CRL syntax is as follows.  For signature calculation,
 * the data that is to be signed is ASN.1 DER encoded.

 *
 * CertificateList  ::=  SEQUENCE  {
 * tbsCertList          TBSCertList,
 * signatureAlgorithm   AlgorithmIdentifier,
 * signatureValue       BIT STRING  }
 *
 */
class CertificateList
/**
 * @param seq
 */
@Deprecated("use getInstance() method.\n      ")
constructor(
        seq: ASN1Sequence) : ASN1Object() {
    var tbsCertList: TBSCertList
        internal set
    var signatureAlgorithm: AlgorithmIdentifier
        internal set
    var signature: DERBitString
        internal set
    internal var isHashCodeSet = false
    internal var hashCodeValue: Int = 0

    init {
        if (seq.size() == 3) {
            tbsCertList = TBSCertList.getInstance(seq.getObjectAt(0))
            signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
            signature = DERBitString.getInstance(seq.getObjectAt(2))
        } else {
            throw IllegalArgumentException("sequence wrong size for CertificateList")
        }
    }

    val revokedCertificates: Array<TBSCertList.CRLEntry>
        get() = tbsCertList.getRevokedCertificates()

    val revokedCertificateEnumeration: Enumeration<Any>
        get() = tbsCertList.revokedCertificateEnumeration

    val versionNumber: Int
        get() = tbsCertList.versionNumber

    val issuer: X500Name
        get() = tbsCertList.issuer

    val thisUpdate: Time
        get() = tbsCertList.thisUpdate

    val nextUpdate: Time
        get() = tbsCertList.nextUpdate

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(tbsCertList)
        v.add(signatureAlgorithm)
        v.add(signature)

        return DERSequence(v)
    }

    override fun hashCode(): Int {
        if (!isHashCodeSet) {
            hashCodeValue = super.hashCode()
            isHashCodeSet = true
        }

        return hashCodeValue
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): CertificateList {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): CertificateList? {
            if (obj is CertificateList) {
                return obj
            } else if (obj != null) {
                return CertificateList(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

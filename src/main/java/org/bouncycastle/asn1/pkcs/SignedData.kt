package org.bouncycastle.asn1.pkcs

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * a PKCS#7 signed data object.
 */
class SignedData : ASN1Object, PKCSObjectIdentifiers {
    var version: ASN1Integer? = null
        private set
    var digestAlgorithms: ASN1Set? = null
        private set
    var contentInfo: ContentInfo? = null
        private set
    var certificates: ASN1Set? = null
        private set
    var crLs: ASN1Set? = null
        private set
    var signerInfos: ASN1Set? = null
        private set

    constructor(
            _version: ASN1Integer,
            _digestAlgorithms: ASN1Set,
            _contentInfo: ContentInfo,
            _certificates: ASN1Set,
            _crls: ASN1Set,
            _signerInfos: ASN1Set) {
        version = _version
        digestAlgorithms = _digestAlgorithms
        contentInfo = _contentInfo
        certificates = _certificates
        crLs = _crls
        signerInfos = _signerInfos
    }

    constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        version = e.nextElement() as ASN1Integer
        digestAlgorithms = e.nextElement() as ASN1Set
        contentInfo = ContentInfo.getInstance(e.nextElement())

        while (e.hasMoreElements()) {
            val o = e.nextElement() as ASN1Primitive

            //
            // an interesting feature of SignedData is that there appear to be varying implementations...
            // for the moment we ignore anything which doesn't fit.
            //
            if (o is ASN1TaggedObject) {

                when (o.tagNo) {
                    0 -> certificates = ASN1Set.getInstance(o, false)
                    1 -> crLs = ASN1Set.getInstance(o, false)
                    else -> throw IllegalArgumentException("unknown tag value " + o.tagNo)
                }
            } else {
                signerInfos = o as ASN1Set
            }
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * SignedData ::= SEQUENCE {
     * version Version,
     * digestAlgorithms DigestAlgorithmIdentifiers,
     * contentInfo ContentInfo,
     * certificates
     * [0] IMPLICIT ExtendedCertificatesAndCertificates
     * OPTIONAL,
     * crls
     * [1] IMPLICIT CertificateRevocationLists OPTIONAL,
     * signerInfos SignerInfos }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(digestAlgorithms)
        v.add(contentInfo)

        if (certificates != null) {
            v.add(DERTaggedObject(false, 0, certificates))
        }

        if (crLs != null) {
            v.add(DERTaggedObject(false, 1, crLs))
        }

        v.add(signerInfos)

        return BERSequence(v)
    }

    companion object {

        fun getInstance(
                o: Any?): SignedData? {
            if (o is SignedData) {
                return o
            } else if (o != null) {
                return SignedData(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

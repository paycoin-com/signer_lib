package org.bouncycastle.asn1.cms

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.BERSet
import org.bouncycastle.asn1.BERTaggedObject
import org.bouncycastle.asn1.DERTaggedObject

/**
 * [RFC 5652](http://tools.ietf.org/html/rfc5652#section-5.1):
 *
 *
 * A signed data object containing multitude of [SignerInfo]s.
 *
 * SignedData ::= SEQUENCE {
 * version CMSVersion,
 * digestAlgorithms DigestAlgorithmIdentifiers,
 * encapContentInfo EncapsulatedContentInfo,
 * certificates [0] IMPLICIT CertificateSet OPTIONAL,
 * crls [1] IMPLICIT CertificateRevocationLists OPTIONAL,
 * signerInfos SignerInfos
 * }

 * DigestAlgorithmIdentifiers ::= SET OF DigestAlgorithmIdentifier

 * SignerInfos ::= SET OF SignerInfo
 *
 *
 *
 * The version calculation uses following ruleset from RFC 5652 section 5.1:
 *
 * IF ((certificates is present) AND
 * (any certificates with a type of other are present)) OR
 * ((crls is present) AND
 * (any crls with a type of other are present))
 * THEN version MUST be 5
 * ELSE
 * IF (certificates is present) AND
 * (any version 2 attribute certificates are present)
 * THEN version MUST be 4
 * ELSE
 * IF ((certificates is present) AND
 * (any version 1 attribute certificates are present)) OR
 * (any SignerInfo structures are version 3) OR
 * (encapContentInfo eContentType is other than id-data)
 * THEN version MUST be 3
 * ELSE version MUST be 1
 *
 *
 *
 */
class SignedData : ASN1Object {

    var version: ASN1Integer? = null
        private set
    var digestAlgorithms: ASN1Set? = null
        private set
    var encapContentInfo: ContentInfo? = null
        private set
    var certificates: ASN1Set? = null
        private set
    var crLs: ASN1Set? = null
        private set
    var signerInfos: ASN1Set? = null
        private set
    private var certsBer: Boolean = false
    private var crlsBer: Boolean = false

    constructor(
            digestAlgorithms: ASN1Set,
            contentInfo: ContentInfo,
            certificates: ASN1Set,
            crls: ASN1Set,
            signerInfos: ASN1Set) {
        this.version = calculateVersion(contentInfo.contentType, certificates, crls, signerInfos)
        this.digestAlgorithms = digestAlgorithms
        this.encapContentInfo = contentInfo
        this.certificates = certificates
        this.crLs = crls
        this.signerInfos = signerInfos
        this.crlsBer = crls is BERSet
        this.certsBer = certificates is BERSet
    }


    private fun calculateVersion(
            contentOid: ASN1ObjectIdentifier,
            certs: ASN1Set?,
            crls: ASN1Set?,
            signerInfs: ASN1Set): ASN1Integer {
        var otherCert = false
        var otherCrl = false
        var attrCertV1Found = false
        var attrCertV2Found = false

        if (certs != null) {
            val en = certs.objects
            while (en.hasMoreElements()) {
                val obj = en.nextElement()
                if (obj is ASN1TaggedObject) {
                    val tagged = ASN1TaggedObject.getInstance(obj)

                    if (tagged.tagNo == 1) {
                        attrCertV1Found = true
                    } else if (tagged.tagNo == 2) {
                        attrCertV2Found = true
                    } else if (tagged.tagNo == 3) {
                        otherCert = true
                    }
                }
            }
        }

        if (otherCert) {
            return ASN1Integer(5)
        }

        if (crls != null)
        // no need to check if otherCert is true
        {
            val en = crls.objects
            while (en.hasMoreElements()) {
                val obj = en.nextElement()
                if (obj is ASN1TaggedObject) {
                    otherCrl = true
                }
            }
        }

        if (otherCrl) {
            return VERSION_5
        }

        if (attrCertV2Found) {
            return VERSION_4
        }

        if (attrCertV1Found) {
            return VERSION_3
        }

        if (checkForVersion3(signerInfs)) {
            return VERSION_3
        }

        if (CMSObjectIdentifiers.data != contentOid) {
            return VERSION_3
        }

        return VERSION_1
    }

    private fun checkForVersion3(signerInfs: ASN1Set): Boolean {
        val e = signerInfs.objects
        while (e.hasMoreElements()) {
            val s = SignerInfo.getInstance(e.nextElement())

            if (s.version.value.toInt() == 3) {
                return true
            }
        }

        return false
    }

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        version = ASN1Integer.getInstance(e.nextElement())
        digestAlgorithms = e.nextElement() as ASN1Set
        encapContentInfo = ContentInfo.getInstance(e.nextElement())

        while (e.hasMoreElements()) {
            val o = e.nextElement() as ASN1Primitive

            //
            // an interesting feature of SignedData is that there appear
            // to be varying implementations...
            // for the moment we ignore anything which doesn't fit.
            //
            if (o is ASN1TaggedObject) {

                when (o.tagNo) {
                    0 -> {
                        certsBer = o is BERTaggedObject
                        certificates = ASN1Set.getInstance(o, false)
                    }
                    1 -> {
                        crlsBer = o is BERTaggedObject
                        crLs = ASN1Set.getInstance(o, false)
                    }
                    else -> throw IllegalArgumentException("unknown tag value " + o.tagNo)
                }
            } else {
                signerInfos = o as ASN1Set
            }
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(digestAlgorithms)
        v.add(encapContentInfo)

        if (certificates != null) {
            if (certsBer) {
                v.add(BERTaggedObject(false, 0, certificates))
            } else {
                v.add(DERTaggedObject(false, 0, certificates))
            }
        }

        if (crLs != null) {
            if (crlsBer) {
                v.add(BERTaggedObject(false, 1, crLs))
            } else {
                v.add(DERTaggedObject(false, 1, crLs))
            }
        }

        v.add(signerInfos)

        return BERSequence(v)
    }

    companion object {
        private val VERSION_1 = ASN1Integer(1)
        private val VERSION_3 = ASN1Integer(3)
        private val VERSION_4 = ASN1Integer(4)
        private val VERSION_5 = ASN1Integer(5)

        /**
         * Return a SignedData object from the given object.
         *
         *
         * Accepted inputs:
         *
         *  *  null  null
         *  *  [SignedData] object
         *  *  [ASN1Sequence][org.bouncycastle.asn1.ASN1Sequence.getInstance] input formats with SignedData structure inside
         *

         * @param o the object we want converted.
         * *
         * @return a reference that can be assigned to SignedData (may be null)
         * *
         * @throws IllegalArgumentException if the object cannot be converted.
         */
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

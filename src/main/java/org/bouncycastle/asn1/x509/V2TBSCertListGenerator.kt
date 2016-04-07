package org.bouncycastle.asn1.x509

import java.io.IOException

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1UTCTime
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.X500Name

/**
 * Generator for Version 2 TBSCertList structures.
 *
 * TBSCertList  ::=  SEQUENCE  {
 * version                 Version OPTIONAL,
 * -- if present, shall be v2
 * signature               AlgorithmIdentifier,
 * issuer                  Name,
 * thisUpdate              Time,
 * nextUpdate              Time OPTIONAL,
 * revokedCertificates     SEQUENCE OF SEQUENCE  {
 * userCertificate         CertificateSerialNumber,
 * revocationDate          Time,
 * crlEntryExtensions      Extensions OPTIONAL
 * -- if present, shall be v2
 * }  OPTIONAL,
 * crlExtensions           [0]  EXPLICIT Extensions OPTIONAL
 * -- if present, shall be v2
 * }
 *

 * **Note: This class may be subject to change**
 */
class V2TBSCertListGenerator {
    private val version = ASN1Integer(1)
    private var signature: AlgorithmIdentifier? = null
    private var issuer: X500Name? = null
    private var thisUpdate: Time? = null
    private var nextUpdate: Time? = null
    private var extensions: Extensions? = null
    private val crlentries = ASN1EncodableVector()


    fun setSignature(
            signature: AlgorithmIdentifier) {
        this.signature = signature
    }


    @Deprecated("use X500Name method")
    fun setIssuer(
            issuer: X509Name) {
        this.issuer = X500Name.getInstance(issuer.toASN1Primitive())
    }

    fun setIssuer(issuer: X500Name) {
        this.issuer = issuer
    }

    fun setThisUpdate(
            thisUpdate: ASN1UTCTime) {
        this.thisUpdate = Time(thisUpdate)
    }

    fun setNextUpdate(
            nextUpdate: ASN1UTCTime) {
        this.nextUpdate = Time(nextUpdate)
    }

    fun setThisUpdate(
            thisUpdate: Time) {
        this.thisUpdate = thisUpdate
    }

    fun setNextUpdate(
            nextUpdate: Time) {
        this.nextUpdate = nextUpdate
    }

    fun addCRLEntry(
            crlEntry: ASN1Sequence) {
        crlentries.add(crlEntry)
    }

    fun addCRLEntry(userCertificate: ASN1Integer, revocationDate: ASN1UTCTime, reason: Int) {
        addCRLEntry(userCertificate, Time(revocationDate), reason)
    }

    @JvmOverloads fun addCRLEntry(userCertificate: ASN1Integer, revocationDate: Time, reason: Int, invalidityDate: ASN1GeneralizedTime? = null) {
        if (reason != 0) {
            val v = ASN1EncodableVector()

            if (reason < reasons.size) {
                if (reason < 0) {
                    throw IllegalArgumentException("invalid reason value: " + reason)
                }
                v.add(reasons[reason])
            } else {
                v.add(createReasonExtension(reason))
            }

            if (invalidityDate != null) {
                v.add(createInvalidityDateExtension(invalidityDate))
            }

            internalAddCRLEntry(userCertificate, revocationDate, DERSequence(v))
        } else if (invalidityDate != null) {
            val v = ASN1EncodableVector()

            v.add(createInvalidityDateExtension(invalidityDate))

            internalAddCRLEntry(userCertificate, revocationDate, DERSequence(v))
        } else {
            addCRLEntry(userCertificate, revocationDate, null)
        }
    }

    private fun internalAddCRLEntry(userCertificate: ASN1Integer, revocationDate: Time, extensions: ASN1Sequence?) {
        val v = ASN1EncodableVector()

        v.add(userCertificate)
        v.add(revocationDate)

        if (extensions != null) {
            v.add(extensions)
        }

        addCRLEntry(DERSequence(v))
    }

    fun addCRLEntry(userCertificate: ASN1Integer, revocationDate: Time, extensions: Extensions?) {
        val v = ASN1EncodableVector()

        v.add(userCertificate)
        v.add(revocationDate)

        if (extensions != null) {
            v.add(extensions)
        }

        addCRLEntry(DERSequence(v))
    }

    fun setExtensions(
            extensions: X509Extensions) {
        setExtensions(Extensions.getInstance(extensions))
    }

    fun setExtensions(
            extensions: Extensions) {
        this.extensions = extensions
    }

    fun generateTBSCertList(): TBSCertList {
        if (signature == null || issuer == null || thisUpdate == null) {
            throw IllegalStateException("Not all mandatory fields set in V2 TBSCertList generator.")
        }

        val v = ASN1EncodableVector()

        v.add(version)
        v.add(signature)
        v.add(issuer)

        v.add(thisUpdate)
        if (nextUpdate != null) {
            v.add(nextUpdate)
        }

        // Add CRLEntries if they exist
        if (crlentries.size() != 0) {
            v.add(DERSequence(crlentries))
        }

        if (extensions != null) {
            v.add(DERTaggedObject(0, extensions))
        }

        return TBSCertList(DERSequence(v))
    }

    companion object {

        private val reasons: Array<ASN1Sequence>

        init {
            reasons = arrayOfNulls<ASN1Sequence>(11)

            reasons[0] = createReasonExtension(CRLReason.unspecified)
            reasons[1] = createReasonExtension(CRLReason.keyCompromise)
            reasons[2] = createReasonExtension(CRLReason.cACompromise)
            reasons[3] = createReasonExtension(CRLReason.affiliationChanged)
            reasons[4] = createReasonExtension(CRLReason.superseded)
            reasons[5] = createReasonExtension(CRLReason.cessationOfOperation)
            reasons[6] = createReasonExtension(CRLReason.certificateHold)
            reasons[7] = createReasonExtension(7) // 7 -> unknown
            reasons[8] = createReasonExtension(CRLReason.removeFromCRL)
            reasons[9] = createReasonExtension(CRLReason.privilegeWithdrawn)
            reasons[10] = createReasonExtension(CRLReason.aACompromise)
        }

        private fun createReasonExtension(reasonCode: Int): ASN1Sequence {
            val v = ASN1EncodableVector()

            val crlReason = CRLReason.lookup(reasonCode)

            try {
                v.add(Extension.reasonCode)
                v.add(DEROctetString(crlReason.encoded))
            } catch (e: IOException) {
                throw IllegalArgumentException("error encoding reason: " + e)
            }

            return DERSequence(v)
        }

        private fun createInvalidityDateExtension(invalidityDate: ASN1GeneralizedTime): ASN1Sequence {
            val v = ASN1EncodableVector()

            try {
                v.add(Extension.invalidityDate)
                v.add(DEROctetString(invalidityDate.encoded))
            } catch (e: IOException) {
                throw IllegalArgumentException("error encoding reason: " + e)
            }

            return DERSequence(v)
        }
    }
}

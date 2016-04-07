package org.bouncycastle.asn1.x509

import java.util.Enumeration
import java.util.NoSuchElementException

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1UTCTime
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.X500Name

/**
 * PKIX RFC-2459 - TBSCertList object.
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
 */
class TBSCertList(
        seq: ASN1Sequence) : ASN1Object() {
    class CRLEntry private constructor(
            internal var seq: ASN1Sequence) : ASN1Object() {

        internal var crlEntryExtensions: Extensions? = null

        init {
            if (seq.size() < 2 || seq.size() > 3) {
                throw IllegalArgumentException("Bad sequence size: " + seq.size())
            }
        }

        val userCertificate: ASN1Integer
            get() = ASN1Integer.getInstance(seq.getObjectAt(0))

        val revocationDate: Time
            get() = Time.getInstance(seq.getObjectAt(1))

        val extensions: Extensions
            get() {
                if (crlEntryExtensions == null && seq.size() == 3) {
                    crlEntryExtensions = Extensions.getInstance(seq.getObjectAt(2))
                }

                return crlEntryExtensions
            }

        override fun toASN1Primitive(): ASN1Primitive {
            return seq
        }

        fun hasExtensions(): Boolean {
            return seq.size() == 3
        }

        companion object {

            fun getInstance(o: Any?): CRLEntry? {
                if (o is CRLEntry) {
                    return o
                } else if (o != null) {
                    return CRLEntry(ASN1Sequence.getInstance(o))
                }

                return null
            }
        }
    }

    private inner class RevokedCertificatesEnumeration internal constructor(private val en: Enumeration<Any>) : Enumeration<Any> {

        override fun hasMoreElements(): Boolean {
            return en.hasMoreElements()
        }

        override fun nextElement(): Any {
            return CRLEntry.getInstance(en.nextElement())
        }
    }

    private inner class EmptyEnumeration : Enumeration<Any> {
        override fun hasMoreElements(): Boolean {
            return false
        }

        override fun nextElement(): Any {
            throw NoSuchElementException("Empty Enumeration")
        }
    }

    var version: ASN1Integer? = null
        internal set
    var signature: AlgorithmIdentifier
        internal set
    var issuer: X500Name
        internal set
    var thisUpdate: Time
        internal set
    var nextUpdate: Time? = null
        internal set
    internal var revokedCertificates: ASN1Sequence? = null
    var extensions: Extensions? = null
        internal set

    init {
        if (seq.size() < 3 || seq.size() > 7) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        var seqPos = 0

        if (seq.getObjectAt(seqPos) is ASN1Integer) {
            version = ASN1Integer.getInstance(seq.getObjectAt(seqPos++))
        } else {
            version = null  // version is optional
        }

        signature = AlgorithmIdentifier.getInstance(seq.getObjectAt(seqPos++))
        issuer = X500Name.getInstance(seq.getObjectAt(seqPos++))
        thisUpdate = Time.getInstance(seq.getObjectAt(seqPos++))

        if (seqPos < seq.size() && (seq.getObjectAt(seqPos) is ASN1UTCTime
                || seq.getObjectAt(seqPos) is ASN1GeneralizedTime
                || seq.getObjectAt(seqPos) is Time)) {
            nextUpdate = Time.getInstance(seq.getObjectAt(seqPos++))
        }

        if (seqPos < seq.size() && seq.getObjectAt(seqPos) !is DERTaggedObject) {
            revokedCertificates = ASN1Sequence.getInstance(seq.getObjectAt(seqPos++))
        }

        if (seqPos < seq.size() && seq.getObjectAt(seqPos) is DERTaggedObject) {
            extensions = Extensions.getInstance(ASN1Sequence.getInstance(seq.getObjectAt(seqPos) as ASN1TaggedObject, true))
        }
    }

    val versionNumber: Int
        get() {
            if (version == null) {
                return 1
            }
            return version!!.value.toInt() + 1
        }

    fun getRevokedCertificates(): Array<CRLEntry> {
        if (revokedCertificates == null) {
            return arrayOfNulls(0)
        }

        val entries = arrayOfNulls<CRLEntry>(revokedCertificates!!.size())

        for (i in entries.indices) {
            entries[i] = CRLEntry.getInstance(revokedCertificates!!.getObjectAt(i))
        }

        return entries
    }

    val revokedCertificateEnumeration: Enumeration<Any>
        get() {
            if (revokedCertificates == null) {
                return EmptyEnumeration()
            }

            return RevokedCertificatesEnumeration(revokedCertificates!!.objects)
        }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (version != null) {
            v.add(version)
        }
        v.add(signature)
        v.add(issuer)

        v.add(thisUpdate)
        if (nextUpdate != null) {
            v.add(nextUpdate)
        }

        // Add CRLEntries if they exist
        if (revokedCertificates != null) {
            v.add(revokedCertificates)
        }

        if (extensions != null) {
            v.add(DERTaggedObject(0, extensions))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): TBSCertList {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): TBSCertList? {
            if (obj is TBSCertList) {
                return obj
            } else if (obj != null) {
                return TBSCertList(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

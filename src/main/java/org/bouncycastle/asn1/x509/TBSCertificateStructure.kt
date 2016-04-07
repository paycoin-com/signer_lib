package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name

/**
 * The TBSCertificate object.
 *
 * TBSCertificate ::= SEQUENCE {
 * version          [ 0 ]  Version DEFAULT v1(0),
 * serialNumber            CertificateSerialNumber,
 * signature               AlgorithmIdentifier,
 * issuer                  Name,
 * validity                Validity,
 * subject                 Name,
 * subjectPublicKeyInfo    SubjectPublicKeyInfo,
 * issuerUniqueID    [ 1 ] IMPLICIT UniqueIdentifier OPTIONAL,
 * subjectUniqueID   [ 2 ] IMPLICIT UniqueIdentifier OPTIONAL,
 * extensions        [ 3 ] Extensions OPTIONAL
 * }
 *
 *
 *
 * Note: issuerUniqueID and subjectUniqueID are both deprecated by the IETF. This class
 * will parse them, but you really shouldn't be creating new ones.
 */
class TBSCertificateStructure(
        internal var seq: ASN1Sequence) : ASN1Object(), X509ObjectIdentifiers, PKCSObjectIdentifiers {

    var versionNumber: ASN1Integer
        internal set
    var serialNumber: ASN1Integer
        internal set
    var signature: AlgorithmIdentifier
        internal set
    var issuer: X500Name
        internal set
    var startDate: Time
        internal set
    var endDate: Time
        internal set
    var subject: X500Name
        internal set
    var subjectPublicKeyInfo: SubjectPublicKeyInfo
        internal set
    var issuerUniqueId: DERBitString
        internal set
    var subjectUniqueId: DERBitString
        internal set
    var extensions: X509Extensions
        internal set

    init {
        var seqStart = 0

        //
        // some certficates don't include a version number - we assume v1
        //
        if (seq.getObjectAt(0) is DERTaggedObject) {
            versionNumber = ASN1Integer.getInstance(seq.getObjectAt(0) as ASN1TaggedObject, true)
        } else {
            seqStart = -1          // field 0 is missing!
            versionNumber = ASN1Integer(0)
        }

        serialNumber = ASN1Integer.getInstance(seq.getObjectAt(seqStart + 1))

        signature = AlgorithmIdentifier.getInstance(seq.getObjectAt(seqStart + 2))
        issuer = X500Name.getInstance(seq.getObjectAt(seqStart + 3))

        //
        // before and after dates
        //
        val dates = seq.getObjectAt(seqStart + 4) as ASN1Sequence

        startDate = Time.getInstance(dates.getObjectAt(0))
        endDate = Time.getInstance(dates.getObjectAt(1))

        subject = X500Name.getInstance(seq.getObjectAt(seqStart + 5))

        //
        // public key info.
        //
        subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(seq.getObjectAt(seqStart + 6))

        for (extras in seq.size() - (seqStart + 6) - 1 downTo 1) {
            val extra = seq.getObjectAt(seqStart + 6 + extras) as DERTaggedObject

            when (extra.tagNo) {
                1 -> issuerUniqueId = DERBitString.getInstance(extra, false)
                2 -> subjectUniqueId = DERBitString.getInstance(extra, false)
                3 -> extensions = X509Extensions.getInstance(extra)
            }
        }
    }

    val version: Int
        get() = versionNumber.value.toInt() + 1

    override fun toASN1Primitive(): ASN1Primitive {
        return seq
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): TBSCertificateStructure {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): TBSCertificateStructure? {
            if (obj is TBSCertificateStructure) {
                return obj
            } else if (obj != null) {
                return TBSCertificateStructure(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

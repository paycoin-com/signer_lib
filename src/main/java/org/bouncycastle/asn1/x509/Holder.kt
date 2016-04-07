package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * The Holder object.
 *
 *
 * For an v2 attribute certificate this is:

 *
 * Holder ::= SEQUENCE {
 * baseCertificateID   [0] IssuerSerial OPTIONAL,
 * -- the issuer and serial number of
 * -- the holder's Public Key Certificate
 * entityName          [1] GeneralNames OPTIONAL,
 * -- the name of the claimant or role
 * objectDigestInfo    [2] ObjectDigestInfo OPTIONAL
 * -- used to directly authenticate the holder,
 * -- for example, an executable
 * }
 *

 *
 *
 * For an v1 attribute certificate this is:

 *
 * subject CHOICE {
 * baseCertificateID [0] EXPLICIT IssuerSerial,
 * -- associated with a Public Key Certificate
 * subjectName [1] EXPLICIT GeneralNames },
 * -- associated with a name
 *
 */
class Holder : ASN1Object {

    var baseCertificateID: IssuerSerial? = null
        internal set

    /**
     * Returns the entityName for an V2 attribute certificate or the subjectName
     * for an V1 attribute certificate.

     * @return The entityname or subjectname.
     */
    var entityName: GeneralNames? = null
        internal set

    var objectDigestInfo: ObjectDigestInfo? = null
        internal set

    /**
     * Returns 1 for V2 attribute certificates or 0 for V1 attribute
     * certificates.
     * @return The version of the attribute certificate.
     */
    var version = V2_CERTIFICATE_HOLDER
        private set

    /**
     * Constructor for a holder for an V1 attribute certificate.

     * @param tagObj The ASN.1 tagged holder object.
     */
    private constructor(tagObj: ASN1TaggedObject) {
        when (tagObj.tagNo) {
            0 -> baseCertificateID = IssuerSerial.getInstance(tagObj, true)
            1 -> entityName = GeneralNames.getInstance(tagObj, true)
            else -> throw IllegalArgumentException("unknown tag in Holder")
        }
        version = 0
    }

    /**
     * Constructor for a holder for an V2 attribute certificate.

     * @param seq The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() > 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        for (i in 0..seq.size() - 1) {
            val tObj = ASN1TaggedObject.getInstance(seq.getObjectAt(i))

            when (tObj.tagNo) {
                0 -> baseCertificateID = IssuerSerial.getInstance(tObj, false)
                1 -> entityName = GeneralNames.getInstance(tObj, false)
                2 -> objectDigestInfo = ObjectDigestInfo.getInstance(tObj, false)
                else -> throw IllegalArgumentException("unknown tag in Holder")
            }
        }
        version = 1
    }

    /**
     * Constructs a holder from a IssuerSerial for a V1 or V2 certificate.
     * .
     * @param baseCertificateID The IssuerSerial.
     * *
     * @param version The version of the attribute certificate.
     */
    @JvmOverloads constructor(baseCertificateID: IssuerSerial, version: Int = V2_CERTIFICATE_HOLDER) {
        this.baseCertificateID = baseCertificateID
        this.version = version
    }

    /**
     * Constructs a holder with an entityName for V2 attribute certificates or
     * with a subjectName for V1 attribute certificates.

     * @param entityName The entity or subject name.
     * *
     * @param version The version of the attribute certificate.
     */
    @JvmOverloads constructor(entityName: GeneralNames, version: Int = V2_CERTIFICATE_HOLDER) {
        this.entityName = entityName
        this.version = version
    }

    /**
     * Constructs a holder from an object digest info.

     * @param objectDigestInfo The object digest info object.
     */
    constructor(objectDigestInfo: ObjectDigestInfo) {
        this.objectDigestInfo = objectDigestInfo
    }

    override fun toASN1Primitive(): ASN1Primitive {
        if (version == 1) {
            val v = ASN1EncodableVector()

            if (baseCertificateID != null) {
                v.add(DERTaggedObject(false, 0, baseCertificateID))
            }

            if (entityName != null) {
                v.add(DERTaggedObject(false, 1, entityName))
            }

            if (objectDigestInfo != null) {
                v.add(DERTaggedObject(false, 2, objectDigestInfo))
            }

            return DERSequence(v)
        } else {
            if (entityName != null) {
                return DERTaggedObject(true, 1, entityName)
            } else {
                return DERTaggedObject(true, 0, baseCertificateID)
            }
        }
    }

    companion object {
        val V1_CERTIFICATE_HOLDER = 0
        val V2_CERTIFICATE_HOLDER = 1

        fun getInstance(obj: Any?): Holder? {
            if (obj is Holder) {
                return obj
            } else if (obj is ASN1TaggedObject) {
                return Holder(ASN1TaggedObject.getInstance(obj))
            } else if (obj != null) {
                return Holder(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
/**
 * Constructs a holder with an entityName for V2 attribute certificates.

 * @param entityName The entity or subject name.
 */

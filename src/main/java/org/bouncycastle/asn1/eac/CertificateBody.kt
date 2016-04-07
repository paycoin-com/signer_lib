package org.bouncycastle.asn1.eac

import java.io.IOException

import org.bouncycastle.asn1.ASN1ApplicationSpecific
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DERApplicationSpecific
import org.bouncycastle.asn1.DEROctetString


/**
 * an Iso7816CertificateBody structure.
 *
 * CertificateBody ::= SEQUENCE {
 * // version of the certificate format. Must be 0 (version 1)
 * CertificateProfileIdentifer         DERApplicationSpecific,
 * //uniquely identifies the issuinng CA's signature key pair
 * // contains the iso3166-1 alpha2 encoded country code, the
 * // name of issuer and the sequence number of the key pair.
 * CertificationAuthorityReference        DERApplicationSpecific,
 * // stores the encoded public key
 * PublicKey                            Iso7816PublicKey,
 * //associates the public key contained in the certificate with a unique name
 * // contains the iso3166-1 alpha2 encoded country code, the
 * // name of the holder and the sequence number of the key pair.
 * certificateHolderReference            DERApplicationSpecific,
 * // Encodes the role of the holder (i.e. CVCA, DV, IS) and assigns read/write
 * // access rights to data groups storing sensitive data
 * certificateHolderAuthorization        Iso7816CertificateHolderAuthorization,
 * // the date of the certificate generation
 * CertificateEffectiveDate            DERApplicationSpecific,
 * // the date after wich the certificate expires
 * certificateExpirationDate            DERApplicationSpecific
 * }
 *
 */
class CertificateBody : ASN1Object {
    internal var seq: ASN1InputStream
    /**
     * CertificateProfileIdentifier : version of the certificate format. Must be 0 (version 1)

     * @return the CertificateProfileIdentifier
     */
    var certificateProfileIdentifier: DERApplicationSpecific? = null
        @Throws(IllegalArgumentException::class)
        private set(certificateProfileIdentifier) = if (certificateProfileIdentifier.getApplicationTag() == EACTags.INTERCHANGE_PROFILE) {
            this.certificateProfileIdentifier = certificateProfileIdentifier
            certificateType = certificateType or CPI
        } else {
            throw IllegalArgumentException("Not an Iso7816Tags.INTERCHANGE_PROFILE tag :" + EACTags.encodeTag(certificateProfileIdentifier))
        }// version of the certificate format. Must be 0 (version 1)
    private var certificationAuthorityReference: DERApplicationSpecific? = null//uniquely identifies the issuinng CA's signature key pair
    /**
     * @return the PublicKey
     */
    /**
     * set the public Key

     * @param publicKey : the DERApplicationSpecific containing the public key
     * *
     * @throws java.io.IOException
     */
    var publicKey: PublicKeyDataObject? = null
        private set(publicKey) {
            this.publicKey = PublicKeyDataObject.getInstance(publicKey)
            this.certificateType = this.certificateType or PK
        }// stores the encoded public key
    private var certificateHolderReference: DERApplicationSpecific? = null//associates the public key contained in the certificate with a unique name
    private var certificateHolderAuthorization: CertificateHolderAuthorization? = null// Encodes the role of the holder (i.e. CVCA, DV, IS) and assigns read/write access rights to data groups storing sensitive data
    private var certificateEffectiveDate: DERApplicationSpecific? = null// the date of the certificate generation
    private var certificateExpirationDate: DERApplicationSpecific? = null// the date after wich the certificate expires
    /**
     * gives the type of the certificate (value should be profileType or requestType if all data are set).

     * @return the int representing the data already set.
     */
    var certificateType = 0
        private set// bit field of initialized data. This will tell us if the data are valid.

    @Throws(IOException::class)
    private fun setIso7816CertificateBody(appSpe: ASN1ApplicationSpecific) {
        val content: ByteArray
        if (appSpe.applicationTag == EACTags.CERTIFICATE_CONTENT_TEMPLATE) {
            content = appSpe.contents
        } else {
            throw IOException("Bad tag : not an iso7816 CERTIFICATE_CONTENT_TEMPLATE")
        }
        val aIS = ASN1InputStream(content)
        var obj: ASN1Primitive
        while ((obj = aIS.readObject()) != null) {
            val aSpe: DERApplicationSpecific

            if (obj is DERApplicationSpecific) {
                aSpe = obj
            } else {
                throw IOException("Not a valid iso7816 content : not a DERApplicationSpecific Object :" + EACTags.encodeTag(appSpe) + obj.javaClass)
            }
            when (aSpe.applicationTag) {
                EACTags.INTERCHANGE_PROFILE -> certificateProfileIdentifier = aSpe
                EACTags.ISSUER_IDENTIFICATION_NUMBER -> setCertificationAuthorityReference(aSpe)
                EACTags.CARDHOLDER_PUBLIC_KEY_TEMPLATE -> publicKey = PublicKeyDataObject.getInstance(aSpe.getObject(BERTags.SEQUENCE))
                EACTags.CARDHOLDER_NAME -> setCertificateHolderReference(aSpe)
                EACTags.CERTIFICATE_HOLDER_AUTHORIZATION_TEMPLATE -> setCertificateHolderAuthorization(CertificateHolderAuthorization(aSpe))
                EACTags.APPLICATION_EFFECTIVE_DATE -> setCertificateEffectiveDate(aSpe)
                EACTags.APPLICATION_EXPIRATION_DATE -> setCertificateExpirationDate(aSpe)
                else -> {
                    certificateType = 0
                    throw IOException("Not a valid iso7816 DERApplicationSpecific tag " + aSpe.applicationTag)
                }
            }
        }
    }

    /**
     * builds an Iso7816CertificateBody by settings each parameters.

     * @param certificateProfileIdentifier
     * *
     * @param certificationAuthorityReference
     * *
     * *
     * @param publicKey
     * *
     * @param certificateHolderReference
     * *
     * @param certificateHolderAuthorization
     * *
     * @param certificateEffectiveDate
     * *
     * @param certificateExpirationDate
     */
    constructor(
            certificateProfileIdentifier: DERApplicationSpecific,
            certificationAuthorityReference: CertificationAuthorityReference,
            publicKey: PublicKeyDataObject,
            certificateHolderReference: CertificateHolderReference,
            certificateHolderAuthorization: CertificateHolderAuthorization,
            certificateEffectiveDate: PackedDate,
            certificateExpirationDate: PackedDate) {
        certificateProfileIdentifier = certificateProfileIdentifier
        setCertificationAuthorityReference(DERApplicationSpecific(
                EACTags.ISSUER_IDENTIFICATION_NUMBER, certificationAuthorityReference.encoded))
        publicKey = publicKey
        setCertificateHolderReference(DERApplicationSpecific(
                EACTags.CARDHOLDER_NAME, certificateHolderReference.encoded))
        setCertificateHolderAuthorization(certificateHolderAuthorization)
        try {
            setCertificateEffectiveDate(DERApplicationSpecific(
                    false, EACTags.APPLICATION_EFFECTIVE_DATE, DEROctetString(certificateEffectiveDate.encoding)))
            setCertificateExpirationDate(DERApplicationSpecific(
                    false, EACTags.APPLICATION_EXPIRATION_DATE, DEROctetString(certificateExpirationDate.encoding)))
        } catch (e: IOException) {
            throw IllegalArgumentException("unable to encode dates: " + e.message)
        }

    }

    /**
     * builds an Iso7816CertificateBody with an ASN1InputStream.

     * @param obj DERApplicationSpecific containing the whole body.
     * *
     * @throws IOException if the body is not valid.
     */
    @Throws(IOException::class)
    private constructor(obj: ASN1ApplicationSpecific) {
        setIso7816CertificateBody(obj)
    }

    /**
     * create a profile type Iso7816CertificateBody.

     * @return return the "profile" type certificate body.
     * *
     * @throws IOException if the DERApplicationSpecific cannot be created.
     */
    @Throws(IOException::class)
    private fun profileToASN1Object(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certificateProfileIdentifier)
        v.add(certificationAuthorityReference)
        v.add(DERApplicationSpecific(false, EACTags.CARDHOLDER_PUBLIC_KEY_TEMPLATE, publicKey))
        v.add(certificateHolderReference)
        v.add(certificateHolderAuthorization)
        v.add(certificateEffectiveDate)
        v.add(certificateExpirationDate)
        return DERApplicationSpecific(EACTags.CERTIFICATE_CONTENT_TEMPLATE, v)
    }

    @Throws(IllegalArgumentException::class)
    private fun setCertificateHolderReference(certificateHolderReference: DERApplicationSpecific) {
        if (certificateHolderReference.applicationTag == EACTags.CARDHOLDER_NAME) {
            this.certificateHolderReference = certificateHolderReference
            certificateType = certificateType or CHR
        } else {
            throw IllegalArgumentException("Not an Iso7816Tags.CARDHOLDER_NAME tag")
        }
    }

    /**
     * set the CertificationAuthorityReference.

     * @param certificationAuthorityReference
     * *         the DERApplicationSpecific containing the CertificationAuthorityReference.
     * *
     * @throws IllegalArgumentException if the DERApplicationSpecific is not valid.
     */
    @Throws(IllegalArgumentException::class)
    private fun setCertificationAuthorityReference(
            certificationAuthorityReference: DERApplicationSpecific) {
        if (certificationAuthorityReference.applicationTag == EACTags.ISSUER_IDENTIFICATION_NUMBER) {
            this.certificationAuthorityReference = certificationAuthorityReference
            certificateType = certificateType or CAR
        } else {
            throw IllegalArgumentException("Not an Iso7816Tags.ISSUER_IDENTIFICATION_NUMBER tag")
        }
    }

    /**
     * create a request type Iso7816CertificateBody.

     * @return return the "request" type certificate body.
     * *
     * @throws IOException if the DERApplicationSpecific cannot be created.
     */
    @Throws(IOException::class)
    private fun requestToASN1Object(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certificateProfileIdentifier)
        v.add(DERApplicationSpecific(false, EACTags.CARDHOLDER_PUBLIC_KEY_TEMPLATE, publicKey))
        v.add(certificateHolderReference)
        return DERApplicationSpecific(EACTags.CERTIFICATE_CONTENT_TEMPLATE, v)
    }

    /**
     * create a "request" or "profile" type Iso7816CertificateBody according to the variables sets.

     * @return return the ASN1Primitive representing the "request" or "profile" type certificate body.
     */
    override fun toASN1Primitive(): ASN1Primitive? {
        try {
            if (certificateType == profileType) {
                return profileToASN1Object()
            }
            if (certificateType == requestType) {
                return requestToASN1Object()
            }
        } catch (e: IOException) {
            return null
        }

        return null
    }

    /**
     * @return the date of the certificate generation
     */
    fun getCertificateEffectiveDate(): PackedDate? {
        if (this.certificateType and CertificateBody.CEfD == CertificateBody.CEfD) {
            return PackedDate(certificateEffectiveDate!!.contents)
        }
        return null
    }

    /**
     * set the date of the certificate generation

     * @param ced DERApplicationSpecific containing the date of the certificate generation
     * *
     * @throws IllegalArgumentException if the tag is not Iso7816Tags.APPLICATION_EFFECTIVE_DATE
     */
    @Throws(IllegalArgumentException::class)
    private fun setCertificateEffectiveDate(ced: DERApplicationSpecific) {
        if (ced.applicationTag == EACTags.APPLICATION_EFFECTIVE_DATE) {
            this.certificateEffectiveDate = ced
            certificateType = certificateType or CEfD
        } else {
            throw IllegalArgumentException("Not an Iso7816Tags.APPLICATION_EFFECTIVE_DATE tag :" + EACTags.encodeTag(ced))
        }
    }

    /**
     * @return the date after wich the certificate expires
     */
    @Throws(IOException::class)
    fun getCertificateExpirationDate(): PackedDate {
        if (this.certificateType and CertificateBody.CExD == CertificateBody.CExD) {
            return PackedDate(certificateExpirationDate!!.contents)
        }
        throw IOException("certificate Expiration Date not set")
    }

    /**
     * set the date after wich the certificate expires

     * @param ced DERApplicationSpecific containing the date after wich the certificate expires
     * *
     * @throws IllegalArgumentException if the tag is not Iso7816Tags.APPLICATION_EXPIRATION_DATE
     */
    @Throws(IllegalArgumentException::class)
    private fun setCertificateExpirationDate(ced: DERApplicationSpecific) {
        if (ced.applicationTag == EACTags.APPLICATION_EXPIRATION_DATE) {
            this.certificateExpirationDate = ced
            certificateType = certificateType or CExD
        } else {
            throw IllegalArgumentException("Not an Iso7816Tags.APPLICATION_EXPIRATION_DATE tag")
        }
    }

    /**
     * the Iso7816CertificateHolderAuthorization encodes the role of the holder
     * (i.e. CVCA, DV, IS) and assigns read/write access rights to data groups
     * storing sensitive data. This functions returns the Certificate Holder
     * Authorization

     * @return the Iso7816CertificateHolderAuthorization
     */
    @Throws(IOException::class)
    fun getCertificateHolderAuthorization(): CertificateHolderAuthorization {
        if (this.certificateType and CertificateBody.CHA == CertificateBody.CHA) {
            return certificateHolderAuthorization
        }
        throw IOException("Certificate Holder Authorisation not set")
    }

    /**
     * set the CertificateHolderAuthorization

     * @param cha the Certificate Holder Authorization
     */
    private fun setCertificateHolderAuthorization(
            cha: CertificateHolderAuthorization) {
        this.certificateHolderAuthorization = cha
        certificateType = certificateType or CHA
    }

    /**
     * certificateHolderReference : associates the public key contained in the certificate with a unique name

     * @return the certificateHolderReference.
     */
    fun getCertificateHolderReference(): CertificateHolderReference {
        return CertificateHolderReference(certificateHolderReference!!.contents)
    }

    /**
     * get the certificationAuthorityReference
     * certificationAuthorityReference : uniquely identifies the issuinng CA's signature key pair

     * @return the certificationAuthorityReference
     */
    @Throws(IOException::class)
    fun getCertificationAuthorityReference(): CertificationAuthorityReference {
        if (this.certificateType and CertificateBody.CAR == CertificateBody.CAR) {
            return CertificationAuthorityReference(certificationAuthorityReference!!.contents)
        }
        throw IOException("Certification authority reference not set")
    }

    companion object {
        private val CPI = 0x01//certificate Profile Identifier
        private val CAR = 0x02//certification Authority Reference
        private val PK = 0x04//public Key
        private val CHR = 0x08//certificate Holder Reference
        private val CHA = 0x10//certificate Holder Authorization
        private val CEfD = 0x20//certificate Effective Date
        private val CExD = 0x40//certificate Expiration Date

        val profileType = 0x7f//Profile type Certificate
        val requestType = 0x0D// Request type Certificate

        /**
         * Gives an instance of Iso7816CertificateBody taken from Object obj

         * @param obj is the Object to extract the certificate body from.
         * *
         * @return the Iso7816CertificateBody taken from Object obj.
         * *
         * @throws IOException if object is not valid.
         */
        @Throws(IOException::class)
        fun getInstance(obj: Any?): CertificateBody? {
            if (obj is CertificateBody) {
                return obj
            } else if (obj != null) {
                return CertificateBody(ASN1ApplicationSpecific.getInstance(obj))
            }

            return null
        }
    }
}

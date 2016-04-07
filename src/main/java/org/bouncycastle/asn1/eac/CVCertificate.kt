package org.bouncycastle.asn1.eac


import java.io.IOException

import org.bouncycastle.asn1.ASN1ApplicationSpecific
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1ParsingException
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERApplicationSpecific
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.util.Arrays


/**
 * an iso7816Certificate structure.
 *
 * Certificate ::= SEQUENCE {
 * CertificateBody         Iso7816CertificateBody,
 * signature               DER Application specific
 * }
 *
 */
class CVCertificate : ASN1Object {
    /**
     * Gives the body of the certificate.

     * @return the body.
     */
    var body: CertificateBody? = null
        private set
    private var signature: ByteArray? = null
    private var valid: Int = 0

    /**
     * Sets the values of the certificate (body and signature).

     * @param appSpe is a ASN1ApplicationSpecific object containing body and signature.
     * *
     * @throws IOException if tags or value are incorrect.
     */
    @Throws(IOException::class)
    private fun setPrivateData(appSpe: ASN1ApplicationSpecific) {
        valid = 0
        if (appSpe.applicationTag == EACTags.CARDHOLDER_CERTIFICATE) {
            val content = ASN1InputStream(appSpe.contents)
            var tmpObj: ASN1Primitive
            while ((tmpObj = content.readObject()) != null) {
                val aSpe: DERApplicationSpecific
                if (tmpObj is DERApplicationSpecific) {
                    aSpe = tmpObj
                    when (aSpe.applicationTag) {
                        EACTags.CERTIFICATE_CONTENT_TEMPLATE -> {
                            body = CertificateBody.getInstance(aSpe)
                            valid = valid or bodyValid
                        }
                        EACTags.STATIC_INTERNAL_AUTHENTIFICATION_ONE_STEP -> {
                            signature = aSpe.contents
                            valid = valid or signValid
                        }
                        else -> throw IOException("Invalid tag, not an Iso7816CertificateStructure :" + aSpe.applicationTag)
                    }
                } else {
                    throw IOException("Invalid Object, not an Iso7816CertificateStructure")
                }
            }
        } else {
            throw IOException("not a CARDHOLDER_CERTIFICATE :" + appSpe.applicationTag)
        }

        if (valid != signValid or bodyValid) {
            throw IOException("invalid CARDHOLDER_CERTIFICATE :" + appSpe.applicationTag)
        }
    }

    /**
     * Create an iso7816Certificate structure from an ASN1InputStream.

     * @param aIS the byte stream to parse.
     * *
     * @throws IOException if there is a problem parsing the data.
     */
    @Throws(IOException::class)
    constructor(aIS: ASN1InputStream) {
        initFrom(aIS)
    }

    @Throws(IOException::class)
    private fun initFrom(aIS: ASN1InputStream) {
        var obj: ASN1Primitive
        while ((obj = aIS.readObject()) != null) {
            if (obj is DERApplicationSpecific) {
                setPrivateData(obj)
            } else {
                throw IOException("Invalid Input Stream for creating an Iso7816CertificateStructure")
            }
        }
    }

    /**
     * Create an iso7816Certificate structure from a DERApplicationSpecific.

     * @param appSpe the DERApplicationSpecific object.
     * *
     * @return the Iso7816CertificateStructure represented by the DERApplicationSpecific object.
     * *
     * @throws IOException if there is a problem parsing the data.
     */
    @Throws(IOException::class)
    private constructor(appSpe: ASN1ApplicationSpecific) {
        setPrivateData(appSpe)
    }

    /**
     * Create an iso7816Certificate structure from a body and its signature.

     * @param body the Iso7816CertificateBody object containing the body.
     * *
     * @param signature   the byte array containing the signature
     * *
     * @throws IOException if there is a problem parsing the data.
     */
    @Throws(IOException::class)
    constructor(body: CertificateBody, signature: ByteArray) {
        this.body = body
        this.signature = signature
        // patch remi
        valid = valid or bodyValid
        valid = valid or signValid
    }

    /**
     * Gives the signature of the whole body. Type of signature is given in
     * the Iso7816CertificateBody.Iso7816PublicKey.ASN1ObjectIdentifier

     * @return the signature of the body.
     */
    fun getSignature(): ByteArray {
        return Arrays.clone(signature)
    }

    /**
     * @see org.bouncycastle.asn1.ASN1Object.toASN1Primitive
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(body)

        try {
            v.add(DERApplicationSpecific(false, EACTags.STATIC_INTERNAL_AUTHENTIFICATION_ONE_STEP, DEROctetString(signature)))
        } catch (e: IOException) {
            throw IllegalStateException("unable to convert signature!")
        }

        return DERApplicationSpecific(EACTags.CARDHOLDER_CERTIFICATE, v)
    }

    /**
     * @return the Holder authorization and role (CVCA, DV, IS).
     */
    val holderAuthorization: ASN1ObjectIdentifier
        @Throws(IOException::class)
        get() {
            val cha = body!!.certificateHolderAuthorization
            return cha.oid
        }

    /**
     * @return the date of the certificate generation
     */
    val effectiveDate: PackedDate
        @Throws(IOException::class)
        get() = body!!.certificateEffectiveDate


    /**
     * @return the type of certificate (request or profile)
     * *         value is either Iso7816CertificateBody.profileType
     * *         or Iso7816CertificateBody.requestType. Any other value
     * *         is not valid.
     */
    val certificateType: Int
        get() = this.body!!.certificateType

    /**
     * @return the date of the certificate generation
     */
    val expirationDate: PackedDate
        @Throws(IOException::class)
        get() = body!!.certificateExpirationDate


    /**
     * return a bits field coded on one byte. For signification of the
     * several bit see Iso7816CertificateHolderAuthorization

     * @return role and access rigth
     * *
     * @throws IOException
     * *
     * @see CertificateHolderAuthorization
     */
    val role: Int
        @Throws(IOException::class)
        get() {
            val cha = body!!.certificateHolderAuthorization
            return cha.getAccessRights()
        }

    /**
     * @return the Authority Reference field of the certificate
     * *
     * @throws IOException
     */
    val authorityReference: CertificationAuthorityReference
        @Throws(IOException::class)
        get() = body!!.certificationAuthorityReference

    /**
     * @return the Holder Reference Field of the certificate
     * *
     * @throws IOException
     */
    val holderReference: CertificateHolderReference
        @Throws(IOException::class)
        get() = body!!.certificateHolderReference

    /**
     * @return the bits corresponding to the role intented for the certificate
     * *         See Iso7816CertificateHolderAuthorization static int for values
     * *
     * @throws IOException
     */
    val holderAuthorizationRole: Int
        @Throws(IOException::class)
        get() {
            val rights = body!!.certificateHolderAuthorization.getAccessRights()
            return rights and 0xC0
        }

    /**
     * @return the bits corresponding the authorizations contained in the certificate
     * *         See Iso7816CertificateHolderAuthorization static int for values
     * *
     * @throws IOException
     */
    val holderAuthorizationRights: Flags
        @Throws(IOException::class)
        get() = Flags(body!!.certificateHolderAuthorization.getAccessRights() and 0x1F)

    companion object {
        private val bodyValid = 0x01
        private val signValid = 0x02

        /**
         * Create an iso7816Certificate structure from an object.

         * @param obj the Object to extract the certificate from.
         * *
         * @return the Iso7816CertificateStructure represented by the byte stream.
         */
        fun getInstance(obj: Any?): CVCertificate? {
            if (obj is CVCertificate) {
                return obj
            } else if (obj != null) {
                try {
                    return CVCertificate(DERApplicationSpecific.getInstance(obj))
                } catch (e: IOException) {
                    throw ASN1ParsingException("unable to parse data: " + e.message, e)
                }

            }

            return null
        }
    }
}

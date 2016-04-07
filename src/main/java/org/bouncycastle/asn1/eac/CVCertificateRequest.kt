package org.bouncycastle.asn1.eac

import java.io.IOException
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1ApplicationSpecific
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1ParsingException
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DERApplicationSpecific
import org.bouncycastle.asn1.DEROctetString

//import java.math.BigInteger;


class CVCertificateRequest @Throws(IOException::class)
private constructor(request: ASN1ApplicationSpecific) : ASN1Object() {
    /**
     * Returns the body of the certificate template

     * @return the body.
     */
    var certificateBody: CertificateBody? = null
        private set

    var innerSignature: ByteArray? = null
        private set
    var outerSignature: ByteArray? = null
        private set

    private var valid: Int = 0

    init {
        if (request.applicationTag == EACTags.AUTHENTIFICATION_DATA) {
            val seq = ASN1Sequence.getInstance(request.getObject(BERTags.SEQUENCE))

            initCertBody(ASN1ApplicationSpecific.getInstance(seq.getObjectAt(0)))

            outerSignature = ASN1ApplicationSpecific.getInstance(seq.getObjectAt(seq.size() - 1)).contents
        } else {
            initCertBody(request)
        }
    }

    @Throws(IOException::class)
    private fun initCertBody(request: ASN1ApplicationSpecific) {
        if (request.applicationTag == EACTags.CARDHOLDER_CERTIFICATE) {
            val seq = ASN1Sequence.getInstance(request.getObject(BERTags.SEQUENCE))
            val en = seq.objects
            while (en.hasMoreElements()) {
                val obj = ASN1ApplicationSpecific.getInstance(en.nextElement())
                when (obj.applicationTag) {
                    EACTags.CERTIFICATE_CONTENT_TEMPLATE -> {
                        certificateBody = CertificateBody.getInstance(obj)
                        valid = valid or bodyValid
                    }
                    EACTags.STATIC_INTERNAL_AUTHENTIFICATION_ONE_STEP -> {
                        innerSignature = obj.contents
                        valid = valid or signValid
                    }
                    else -> throw IOException("Invalid tag, not an CV Certificate Request element:" + obj.applicationTag)
                }
            }
        } else {
            throw IOException("not a CARDHOLDER_CERTIFICATE in request:" + request.applicationTag)
        }
    }

    internal var signOid: ASN1ObjectIdentifier? = null
    internal var keyOid: ASN1ObjectIdentifier? = null


    internal var strCertificateHolderReference: String

    internal var encodedAuthorityReference: ByteArray

    internal var ProfileId: Int = 0

    /**
     * Return the public key data object carried in the request
     * @return  the public key
     */
    val publicKey: PublicKeyDataObject
        get() = certificateBody!!.publicKey

    internal var certificate: ByteArray? = null
    protected var overSignerReference: String? = null

    fun hasOuterSignature(): Boolean {
        return outerSignature != null
    }

    internal var encoded: ByteArray

    internal var iso7816PubKey: PublicKeyDataObject? = null

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certificateBody)

        try {
            v.add(DERApplicationSpecific(false, EACTags.STATIC_INTERNAL_AUTHENTIFICATION_ONE_STEP, DEROctetString(innerSignature)))
        } catch (e: IOException) {
            throw IllegalStateException("unable to convert signature!")
        }

        return DERApplicationSpecific(EACTags.CARDHOLDER_CERTIFICATE, v)
    }

    companion object {

        private val bodyValid = 0x01
        private val signValid = 0x02

        fun getInstance(obj: Any?): CVCertificateRequest? {
            if (obj is CVCertificateRequest) {
                return obj
            } else if (obj != null) {
                try {
                    return CVCertificateRequest(ASN1ApplicationSpecific.getInstance(obj))
                } catch (e: IOException) {
                    throw ASN1ParsingException("unable to parse data: " + e.message, e)
                }

            }

            return null
        }

        var ZeroArray = byteArrayOf(0)
    }
}

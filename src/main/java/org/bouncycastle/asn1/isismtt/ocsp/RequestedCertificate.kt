package org.bouncycastle.asn1.isismtt.ocsp

import java.io.IOException

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Certificate

/**
 * ISIS-MTT-Optional: The certificate requested by the client by inserting the
 * RetrieveIfAllowed extension in the request, will be returned in this
 * extension.
 *
 *
 * ISIS-MTT-SigG: The signature act allows publishing certificates only then,
 * when the certificate owner gives his explicit permission. Accordingly, there
 * may be �nondownloadable� certificates, about which the responder must provide
 * status information, but MUST NOT include them in the response. Clients may
 * get therefore the following three kind of answers on a single request
 * including the RetrieveIfAllowed extension:
 *
 *  *  a) the responder supports the extension and is allowed to publish the
 * certificate: RequestedCertificate returned including the requested
 * certificate
 *  * b) the responder supports the extension but is NOT allowed to publish
 * the certificate: RequestedCertificate returned including an empty OCTET
 * STRING
 *  * c) the responder does not support the extension: RequestedCertificate is
 * not included in the response
 *
 * Clients requesting RetrieveIfAllowed MUST be able to handle these cases. If
 * any of the OCTET STRING options is used, it MUST contain the DER encoding of
 * the requested certificate.
 *
 * RequestedCertificate ::= CHOICE {
 * Certificate Certificate,
 * publicKeyCertificate [0] EXPLICIT OCTET STRING,
 * attributeCertificate [1] EXPLICIT OCTET STRING
 * }
 *
 */
class RequestedCertificate : ASN1Object, ASN1Choice {

    private val cert: Certificate?
    private var publicKeyCert: ByteArray? = null
    private var attributeCert: ByteArray? = null

    private constructor(tagged: ASN1TaggedObject) {
        if (tagged.tagNo == publicKeyCertificate) {
            publicKeyCert = ASN1OctetString.getInstance(tagged, true).octets
        } else if (tagged.tagNo == attributeCertificate) {
            attributeCert = ASN1OctetString.getInstance(tagged, true).octets
        } else {
            throw IllegalArgumentException("unknown tag number: " + tagged.tagNo)
        }
    }

    /**
     * Constructor from a given details.
     *
     *
     * Only one parameter can be given. All other must be `null`.

     * @param certificate          Given as Certificate
     */
    constructor(certificate: Certificate) {
        this.cert = certificate
    }

    constructor(type: Int, certificateOctets: ByteArray) : this(DERTaggedObject(type, DEROctetString(certificateOctets))) {
    }

    val type: Int
        get() {
            if (cert != null) {
                return certificate
            }
            if (publicKeyCert != null) {
                return publicKeyCertificate
            }
            return attributeCertificate
        }

    val certificateBytes: ByteArray
        get() {
            if (cert != null) {
                try {
                    return cert.encoded
                } catch (e: IOException) {
                    throw IllegalStateException("can't decode certificate: " + e)
                }

            }
            if (publicKeyCert != null) {
                return publicKeyCert
            }
            return attributeCert
        }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * RequestedCertificate ::= CHOICE {
     * Certificate Certificate,
     * publicKeyCertificate [0] EXPLICIT OCTET STRING,
     * attributeCertificate [1] EXPLICIT OCTET STRING
     * }
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        if (publicKeyCert != null) {
            return DERTaggedObject(0, DEROctetString(publicKeyCert))
        }
        if (attributeCert != null) {
            return DERTaggedObject(1, DEROctetString(attributeCert))
        }
        return cert!!.toASN1Primitive()
    }

    companion object {
        val certificate = -1
        val publicKeyCertificate = 0
        val attributeCertificate = 1

        fun getInstance(obj: Any?): RequestedCertificate {
            if (obj == null || obj is RequestedCertificate) {
                return obj as RequestedCertificate?
            }

            if (obj is ASN1Sequence) {
                return RequestedCertificate(Certificate.getInstance(obj))
            }
            if (obj is ASN1TaggedObject) {
                return RequestedCertificate(obj as ASN1TaggedObject?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        fun getInstance(obj: ASN1TaggedObject, explicit: Boolean): RequestedCertificate {
            if (!explicit) {
                throw IllegalArgumentException("choice item must be explicitly tagged")
            }

            return getInstance(obj.`object`)
        }
    }
}

package org.bouncycastle.asn1.cmp

import java.io.IOException

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.AttributeCertificate
import org.bouncycastle.asn1.x509.Certificate

class CMPCertificate : ASN1Object, ASN1Choice {
    val x509v3PKCert: Certificate?

    val otherCertTag: Int
    val otherCert: ASN1Object?

    /**
     * Note: the addition of attribute certificates is a BC extension. If you use this constructor they
     * will be added with a tag value of 1.
     */
    @Deprecated("use (type. otherCert) constructor")
    constructor(x509v2AttrCert: AttributeCertificate) : this(1, x509v2AttrCert) {
    }

    /**
     * Note: the addition of other certificates is a BC extension. If you use this constructor they
     * will be added with an explicit tag value of type.

     * @param type the type of the certificate (used as a tag value).
     * *
     * @param otherCert the object representing the certificate
     */
    constructor(type: Int, otherCert: ASN1Object) {
        this.otherCertTag = type
        this.otherCert = otherCert
    }

    constructor(x509v3PKCert: Certificate) {
        if (x509v3PKCert.versionNumber != 3) {
            throw IllegalArgumentException("only version 3 certificates allowed")
        }

        this.x509v3PKCert = x509v3PKCert
    }

    val isX509v3PKCert: Boolean
        get() = x509v3PKCert != null

    /**
     * Return an AttributeCertificate interpretation of otherCert.
     * @return  an AttributeCertificate
     */
    val x509v2AttrCert: AttributeCertificate
        @Deprecated("use getOtherCert and getOtherTag to make sure message is really what it should be.\n     \n      ")
        get() = AttributeCertificate.getInstance(otherCert)

    /**
     *
     * CMPCertificate ::= CHOICE {
     * x509v3PKCert    Certificate
     * otherCert      [tag] EXPLICIT ANY DEFINED BY tag
     * }
     *
     * Note: the addition of the explicit tagging is a BC extension. We apologise for the warped syntax, but hopefully you get the idea.

     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        if (otherCert != null) {
            // explicit following CMP conventions
            return DERTaggedObject(true, otherCertTag, otherCert)
        }

        return x509v3PKCert!!.toASN1Primitive()
    }

    companion object {

        fun getInstance(o: Any?): CMPCertificate {
            var o = o
            if (o == null || o is CMPCertificate) {
                return o as CMPCertificate?
            }

            if (o is ByteArray) {
                try {
                    o = ASN1Primitive.fromByteArray(o as ByteArray?)
                } catch (e: IOException) {
                    throw IllegalArgumentException("Invalid encoding in CMPCertificate")
                }

            }

            if (o is ASN1Sequence) {
                return CMPCertificate(Certificate.getInstance(o))
            }

            if (o is ASN1TaggedObject) {

                return CMPCertificate(o.tagNo, o.`object`)
            }

            throw IllegalArgumentException("Invalid object: " + o!!.javaClass.name)
        }
    }
}

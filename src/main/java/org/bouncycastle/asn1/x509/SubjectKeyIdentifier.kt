package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString

/**
 * The SubjectKeyIdentifier object.
 *
 * SubjectKeyIdentifier::= OCTET STRING
 *
 */
class SubjectKeyIdentifier : ASN1Object {
    var keyIdentifier: ByteArray? = null
        private set

    constructor(
            keyid: ByteArray) {
        this.keyIdentifier = keyid
    }

    protected constructor(
            keyid: ASN1OctetString) {
        this.keyIdentifier = keyid.octets
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return DEROctetString(keyIdentifier)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): SubjectKeyIdentifier {
            return getInstance(ASN1OctetString.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): SubjectKeyIdentifier? {
            if (obj is SubjectKeyIdentifier) {
                return obj
            } else if (obj != null) {
                return SubjectKeyIdentifier(ASN1OctetString.getInstance(obj))
            }

            return null
        }

        fun fromExtensions(extensions: Extensions): SubjectKeyIdentifier {
            return SubjectKeyIdentifier.getInstance(extensions.getExtensionParsedValue(Extension.subjectKeyIdentifier))
        }
    }
}

package org.bouncycastle.asn1.x509

import java.io.IOException
import java.util.Hashtable
import java.util.Vector

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DEROctetString

/**
 * Generator for X.509 extensions
 */
@Deprecated("use org.bouncycastle.asn1.x509.ExtensionsGenerator")
class X509ExtensionsGenerator {
    private var extensions = Hashtable()
    private var extOrdering = Vector()

    /**
     * Reset the generator
     */
    fun reset() {
        extensions = Hashtable()
        extOrdering = Vector()
    }

    /**
     * Add an extension with the given oid and the passed in value to be included
     * in the OCTET STRING associated with the extension.

     * @param oid  OID for the extension.
     * *
     * @param critical  true if critical, false otherwise.
     * *
     * @param value the ASN.1 object to be included in the extension.
     */
    fun addExtension(
            oid: ASN1ObjectIdentifier,
            critical: Boolean,
            value: ASN1Encodable) {
        try {
            this.addExtension(oid, critical, value.toASN1Primitive().getEncoded(ASN1Encoding.DER))
        } catch (e: IOException) {
            throw IllegalArgumentException("error encoding value: " + e)
        }

    }

    /**
     * Add an extension with the given oid and the passed in byte array to be wrapped in the
     * OCTET STRING associated with the extension.

     * @param oid OID for the extension.
     * *
     * @param critical true if critical, false otherwise.
     * *
     * @param value the byte array to be wrapped.
     */
    fun addExtension(
            oid: ASN1ObjectIdentifier,
            critical: Boolean,
            value: ByteArray) {
        if (extensions.containsKey(oid)) {
            throw IllegalArgumentException("extension $oid already added")
        }

        extOrdering.addElement(oid)
        extensions.put(oid, X509Extension(critical, DEROctetString(value)))
    }

    /**
     * Return true if there are no extension present in this generator.

     * @return true if empty, false otherwise
     */
    val isEmpty: Boolean
        get() = extOrdering.isEmpty()

    /**
     * Generate an X509Extensions object based on the current state of the generator.

     * @return  an X09Extensions object.
     */
    fun generate(): X509Extensions {
        return X509Extensions(extOrdering, extensions)
    }
}

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
class ExtensionsGenerator {
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
    @Throws(IOException::class)
    fun addExtension(
            oid: ASN1ObjectIdentifier,
            critical: Boolean,
            value: ASN1Encodable) {
        this.addExtension(oid, critical, value.toASN1Primitive().getEncoded(ASN1Encoding.DER))
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
        extensions.put(oid, Extension(oid, critical, DEROctetString(value)))
    }

    /**
     * Add a given extension.

     * @param extension the full extension value.
     */
    fun addExtension(
            extension: Extension) {
        if (extensions.containsKey(extension.extnId)) {
            throw IllegalArgumentException("extension " + extension.extnId + " already added")
        }

        extOrdering.addElement(extension.extnId)
        extensions.put(extension.extnId, extension)
    }

    /**
     * Return true if there are no extension present in this generator.

     * @return true if empty, false otherwise
     */
    val isEmpty: Boolean
        get() = extOrdering.isEmpty()

    /**
     * Generate an Extensions object based on the current state of the generator.

     * @return  an X09Extensions object.
     */
    fun generate(): Extensions {
        val exts = arrayOfNulls<Extension>(extOrdering.size)

        for (i in extOrdering.indices) {
            exts[i] = extensions.get(extOrdering.elementAt(i)) as Extension
        }

        return Extensions(exts)
    }
}

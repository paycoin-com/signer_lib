package org.bouncycastle.asn1

/**
 * General interface implemented by ASN.1 STRING objects.
 */
interface ASN1String {
    /**
     * Return a Java String representation of this STRING type's content.
     * @return a Java String representation of this STRING.
     */
    val string: String
}
package org.bouncycastle.asn1.x500

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier

/**
 * This interface provides a profile to conform to when
 * DNs are being converted into strings and back. The idea being that we'll be able to deal with
 * the number of standard ways the fields in a DN should be
 * encoded into their ASN.1 counterparts - a number that is rapidly approaching the
 * number of machines on the internet.
 */
interface X500NameStyle {
    /**
     * Convert the passed in String value into the appropriate ASN.1
     * encoded object.

     * @param oid the OID associated with the value in the DN.
     * *
     * @param value the value of the particular DN component.
     * *
     * @return the ASN.1 equivalent for the value.
     */
    fun stringToValue(oid: ASN1ObjectIdentifier, value: String): ASN1Encodable

    /**
     * Return the OID associated with the passed in name.

     * @param attrName the string to match.
     * *
     * @return an OID
     */
    fun attrNameToOID(attrName: String): ASN1ObjectIdentifier

    /**
     * Return an array of RDN generated from the passed in String.
     * @param dirName  the String representation.
     * *
     * @return  an array of corresponding RDNs.
     */
    fun fromString(dirName: String): Array<RDN>

    /**
     * Return true if the two names are equal.

     * @param name1 first name for comparison.
     * *
     * @param name2 second name for comparison.
     * *
     * @return true if name1 = name 2, false otherwise.
     */
    fun areEqual(name1: X500Name, name2: X500Name): Boolean

    /**
     * Calculate a hashCode for the passed in name.

     * @param name the name the hashCode is required for.
     * *
     * @return the calculated hashCode.
     */
    fun calculateHashCode(name: X500Name): Int

    /**
     * Convert the passed in X500Name to a String.
     * @param name the name to convert.
     * *
     * @return a String representation.
     */
    fun toString(name: X500Name): String

    /**
     * Return the display name for toString() associated with the OID.

     * @param oid  the OID of interest.
     * *
     * @return the name displayed in toString(), null if no mapping provided.
     */
    fun oidToDisplayName(oid: ASN1ObjectIdentifier): String

    /**
     * Return the acceptable names in a String DN that map to OID.

     * @param oid  the OID of interest.
     * *
     * @return an array of String aliases for the OID, zero length if there are none.
     */
    fun oidToAttrNames(oid: ASN1ObjectIdentifier): Array<String>
}
package org.bouncycastle.asn1.x509

import java.io.IOException

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERGeneralizedTime
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERUTF8String

/**
 * The default converter for X509 DN entries when going from their
 * string value to ASN.1 strings.
 */
class X509DefaultEntryConverter : X509NameEntryConverter() {
    /**
     * Apply default coversion for the given value depending on the oid
     * and the character range of the value.

     * @param oid the object identifier for the DN entry
     * *
     * @param value the value associated with it
     * *
     * @return the ASN.1 equivalent for the string value.
     */
    override fun getConvertedValue(
            oid: ASN1ObjectIdentifier,
            value: String): ASN1Primitive {
        var value = value
        if (value.length != 0 && value[0] == '#') {
            try {
                return convertHexEncoded(value, 1)
            } catch (e: IOException) {
                throw RuntimeException("can't recode value for oid " + oid.id)
            }

        } else {
            if (value.length != 0 && value[0] == '\\') {
                value = value.substring(1)
            }
            if (oid == X509Name.EmailAddress || oid == X509Name.DC) {
                return DERIA5String(value)
            } else if (oid == X509Name.DATE_OF_BIRTH)
            // accept time string as well as # (for compatibility)
            {
                return DERGeneralizedTime(value)
            } else if (oid == X509Name.C || oid == X509Name.SN || oid == X509Name.DN_QUALIFIER
                    || oid == X509Name.TELEPHONE_NUMBER) {
                return DERPrintableString(value)
            }
        }

        return DERUTF8String(value)
    }
}

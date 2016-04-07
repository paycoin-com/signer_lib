package org.bouncycastle.asn1.x509

import java.io.IOException

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.util.Strings

/**
 * It turns out that the number of standard ways the fields in a DN should be
 * encoded into their ASN.1 counterparts is rapidly approaching the
 * number of machines on the internet. By default the X509Name class
 * will produce UTF8Strings in line with the current recommendations (RFC 3280).
 *
 *
 * An example of an encoder look like below:
 *
 * public class X509DirEntryConverter
 * extends X509NameEntryConverter
 * {
 * public ASN1Primitive getConvertedValue(
 * ASN1ObjectIdentifier  oid,
 * String               value)
 * {
 * if (str.length() != 0 &amp;&amp; str.charAt(0) == '#')
 * {
 * return convertHexEncoded(str, 1);
 * }
 * if (oid.equals(EmailAddress))
 * {
 * return new DERIA5String(str);
 * }
 * else if (canBePrintable(str))
 * {
 * return new DERPrintableString(str);
 * }
 * else if (canBeUTF8(str))
 * {
 * return new DERUTF8String(str);
 * }
 * else
 * {
 * return new DERBMPString(str);
 * }
 * }
 * }
 *
 */
abstract class X509NameEntryConverter {
    /**
     * Convert an inline encoded hex string rendition of an ASN.1
     * object back into its corresponding ASN.1 object.

     * @param str the hex encoded object
     * *
     * @param off the index at which the encoding starts
     * *
     * @return the decoded object
     */
    @Throws(IOException::class)
    protected fun convertHexEncoded(
            str: String,
            off: Int): ASN1Primitive {
        var str = str
        str = Strings.toLowerCase(str)
        val data = ByteArray((str.length - off) / 2)
        for (index in data.indices) {
            val left = str[index * 2 + off]
            val right = str[index * 2 + off + 1]

            if (left < 'a') {
                data[index] = (left - '0' shl 4).toByte()
            } else {
                data[index] = (left - 'a' + 10 shl 4).toByte()
            }
            if (right < 'a') {
                data[index] = data[index] or (right - '0').toByte()
            } else {
                data[index] = data[index] or (right - 'a' + 10).toByte()
            }
        }

        val aIn = ASN1InputStream(data)

        return aIn.readObject()
    }

    /**
     * return true if the passed in String can be represented without
     * loss as a PrintableString, false otherwise.
     */
    protected fun canBePrintable(
            str: String): Boolean {
        return DERPrintableString.isPrintableString(str)
    }

    /**
     * Convert the passed in String value into the appropriate ASN.1
     * encoded object.

     * @param oid the oid associated with the value in the DN.
     * *
     * @param value the value of the particular DN component.
     * *
     * @return the ASN.1 equivalent for the value.
     */
    abstract fun getConvertedValue(oid: ASN1ObjectIdentifier, value: String): ASN1Primitive
}

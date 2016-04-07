package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBMPString
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERVisibleString

/**
 * `DisplayText` class, used in
 * `CertificatePolicies` X509 V3 extensions (in policy qualifiers).

 *
 * It stores a string in a chosen encoding.
 *
 * DisplayText ::= CHOICE {
 * ia5String        IA5String      (SIZE (1..200)),
 * visibleString    VisibleString  (SIZE (1..200)),
 * bmpString        BMPString      (SIZE (1..200)),
 * utf8String       UTF8String     (SIZE (1..200)) }
 *
 * @see PolicyQualifierInfo

 * @see PolicyInformation
 */
class DisplayText : ASN1Object, ASN1Choice {

    internal var contentType: Int = 0
    internal var contents: ASN1String

    /**
     * Creates a new `DisplayText` instance.

     * @param type the desired encoding type for the text.
     * *
     * @param text the text to store. Strings longer than 200
     * * characters are truncated.
     */
    constructor(type: Int, text: String) {
        var text = text
        if (text.length > DISPLAY_TEXT_MAXIMUM_SIZE) {
            // RFC3280 limits these strings to 200 chars
            // truncate the string
            text = text.substring(0, DISPLAY_TEXT_MAXIMUM_SIZE)
        }

        contentType = type
        when (type) {
            CONTENT_TYPE_IA5STRING -> contents = DERIA5String(text)
            CONTENT_TYPE_UTF8STRING -> contents = DERUTF8String(text)
            CONTENT_TYPE_VISIBLESTRING -> contents = DERVisibleString(text)
            CONTENT_TYPE_BMPSTRING -> contents = DERBMPString(text)
            else -> contents = DERUTF8String(text)
        }
    }

    /**
     * Creates a new `DisplayText` instance.

     * @param text the text to encapsulate. Strings longer than 200
     * * characters are truncated.
     */
    constructor(text: String) {
        var text = text
        // by default use UTF8String
        if (text.length > DISPLAY_TEXT_MAXIMUM_SIZE) {
            text = text.substring(0, DISPLAY_TEXT_MAXIMUM_SIZE)
        }

        contentType = CONTENT_TYPE_UTF8STRING
        contents = DERUTF8String(text)
    }

    /**
     * Creates a new `DisplayText` instance.
     *
     * Useful when reading back a `DisplayText` class
     * from it's ASN1Encodable/DEREncodable form.

     * @param de a `DEREncodable` instance.
     */
    private constructor(de: ASN1String) {
        contents = de
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return contents as ASN1Primitive
    }

    /**
     * Returns the stored `String` object.

     * @return the stored text as a `String`.
     */
    val string: String
        get() = contents.string

    companion object {
        /**
         * Constant corresponding to ia5String encoding.

         */
        val CONTENT_TYPE_IA5STRING = 0
        /**
         * Constant corresponding to bmpString encoding.

         */
        val CONTENT_TYPE_BMPSTRING = 1
        /**
         * Constant corresponding to utf8String encoding.

         */
        val CONTENT_TYPE_UTF8STRING = 2
        /**
         * Constant corresponding to visibleString encoding.

         */
        val CONTENT_TYPE_VISIBLESTRING = 3

        /**
         * Describe constant `DISPLAY_TEXT_MAXIMUM_SIZE` here.

         */
        val DISPLAY_TEXT_MAXIMUM_SIZE = 200

        fun getInstance(obj: Any?): DisplayText {
            if (obj is ASN1String) {
                return DisplayText(obj as ASN1String?)
            } else if (obj == null || obj is DisplayText) {
                return obj as DisplayText?
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DisplayText {
            return getInstance(obj.`object`) // must be explicitly tagged
        }
    }
}

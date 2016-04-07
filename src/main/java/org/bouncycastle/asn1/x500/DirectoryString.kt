package org.bouncycastle.asn1.x500

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBMPString
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERT61String
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERUniversalString

/**
 * The DirectoryString CHOICE object.
 */
class DirectoryString : ASN1Object, ASN1Choice, ASN1String {
    private var string: ASN1String? = null

    private constructor(
            string: DERT61String) {
        this.string = string
    }

    private constructor(
            string: DERPrintableString) {
        this.string = string
    }

    private constructor(
            string: DERUniversalString) {
        this.string = string
    }

    private constructor(
            string: DERUTF8String) {
        this.string = string
    }

    private constructor(
            string: DERBMPString) {
        this.string = string
    }

    constructor(string: String) {
        this.string = DERUTF8String(string)
    }

    override fun getString(): String {
        return string!!.string
    }

    override fun toString(): String {
        return string!!.string
    }

    /**
     *
     * DirectoryString ::= CHOICE {
     * teletexString               TeletexString (SIZE (1..MAX)),
     * printableString             PrintableString (SIZE (1..MAX)),
     * universalString             UniversalString (SIZE (1..MAX)),
     * utf8String                  UTF8String (SIZE (1..MAX)),
     * bmpString                   BMPString (SIZE (1..MAX))  }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return (string as ASN1Encodable).toASN1Primitive()
    }

    companion object {

        fun getInstance(o: Any?): DirectoryString {
            if (o == null || o is DirectoryString) {
                return o as DirectoryString?
            }

            if (o is DERT61String) {
                return DirectoryString(o as DERT61String?)
            }

            if (o is DERPrintableString) {
                return DirectoryString(o as DERPrintableString?)
            }

            if (o is DERUniversalString) {
                return DirectoryString(o as DERUniversalString?)
            }

            if (o is DERUTF8String) {
                return DirectoryString(o as DERUTF8String?)
            }

            if (o is DERBMPString) {
                return DirectoryString(o as DERBMPString?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + o.javaClass.name)
        }

        fun getInstance(o: ASN1TaggedObject, explicit: Boolean): DirectoryString {
            if (!explicit) {
                throw IllegalArgumentException("choice item must be explicitly tagged")
            }

            return getInstance(o.`object`)
        }
    }
}

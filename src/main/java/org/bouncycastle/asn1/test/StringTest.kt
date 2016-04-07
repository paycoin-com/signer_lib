package org.bouncycastle.asn1.test

import java.io.IOException

import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.DERBMPString
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERGeneralString
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERNumericString
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERT61String
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERUniversalString
import org.bouncycastle.asn1.DERVisibleString
import org.bouncycastle.util.Strings
import org.bouncycastle.util.test.SimpleTest

/**
 * X.690 test example
 */
class StringTest : SimpleTest() {
    override fun getName(): String {
        return "String"
    }

    @Throws(IOException::class)
    override fun performTest() {
        var bs = DERBitString(
                byteArrayOf(0x01.toByte(), 0x23.toByte(), 0x45.toByte(), 0x67.toByte(), 0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte()))

        if (bs.string != "#0309000123456789ABCDEF") {
            fail("DERBitString.getString() result incorrect")
        }

        if (bs.toString() != "#0309000123456789ABCDEF") {
            fail("DERBitString.toString() result incorrect")
        }

        bs = DERBitString(
                byteArrayOf(0xfe.toByte(), 0xdc.toByte(), 0xba.toByte(), 0x98.toByte(), 0x76.toByte(), 0x54.toByte(), 0x32.toByte(), 0x10.toByte()))

        if (bs.string != "#030900FEDCBA9876543210") {
            fail("DERBitString.getString() result incorrect")
        }

        if (bs.toString() != "#030900FEDCBA9876543210") {
            fail("DERBitString.toString() result incorrect")
        }

        var us = DERUniversalString(
                byteArrayOf(0x01.toByte(), 0x23.toByte(), 0x45.toByte(), 0x67.toByte(), 0x89.toByte(), 0xab.toByte(), 0xcd.toByte(), 0xef.toByte()))

        if (us.string != "#1C080123456789ABCDEF") {
            fail("DERUniversalString.getString() result incorrect")
        }

        if (us.toString() != "#1C080123456789ABCDEF") {
            fail("DERUniversalString.toString() result incorrect")
        }

        us = DERUniversalString(
                byteArrayOf(0xfe.toByte(), 0xdc.toByte(), 0xba.toByte(), 0x98.toByte(), 0x76.toByte(), 0x54.toByte(), 0x32.toByte(), 0x10.toByte()))

        if (us.string != "#1C08FEDCBA9876543210") {
            fail("DERUniversalString.getString() result incorrect")
        }

        if (us.toString() != "#1C08FEDCBA9876543210") {
            fail("DERUniversalString.toString() result incorrect")
        }

        val t61Bytes = byteArrayOf(-1, -2, -3, -4, -5, -6, -7, -8)
        val t61String = String(t61Bytes, "iso-8859-1")
        val t61 = DERT61String(Strings.fromByteArray(t61Bytes))

        if (t61.string != t61String) {
            fail("DERT61String.getString() result incorrect")
        }

        if (t61.toString() != t61String) {
            fail("DERT61String.toString() result incorrect")
        }

        var shortChars = charArrayOf('a', 'b', 'c', 'd', 'e')
        var longChars = CharArray(1000)

        for (i in longChars.indices) {
            longChars[i] = 'X'
        }

        checkString(DERBMPString(String(shortChars)), DERBMPString(String(longChars)))
        checkString(DERUTF8String(String(shortChars)), DERUTF8String(String(longChars)))
        checkString(DERIA5String(String(shortChars)), DERIA5String(String(longChars)))
        checkString(DERPrintableString(String(shortChars)), DERPrintableString(String(longChars)))
        checkString(DERVisibleString(String(shortChars)), DERVisibleString(String(longChars)))
        checkString(DERGeneralString(String(shortChars)), DERGeneralString(String(longChars)))
        checkString(DERT61String(String(shortChars)), DERT61String(String(longChars)))

        shortChars = charArrayOf('1', '2', '3', '4', '5')
        longChars = CharArray(1000)

        for (i in longChars.indices) {
            longChars[i] = '1'
        }

        checkString(DERNumericString(String(shortChars)), DERNumericString(String(longChars)))

        val shortBytes = byteArrayOf('a'.toByte(), 'b'.toByte(), 'c'.toByte(), 'd'.toByte(), 'e'.toByte())
        val longBytes = ByteArray(1000)

        for (i in longChars.indices) {
            longBytes[i] = 'X'.toByte()
        }

        checkString(DERUniversalString(shortBytes), DERUniversalString(longBytes))

    }

    @Throws(IOException::class)
    private fun checkString(shortString: ASN1String, longString: ASN1String) {
        val short2 = ASN1Primitive.fromByteArray((shortString as ASN1Primitive).encoded) as ASN1String

        if (shortString.toString() != short2.toString()) {
            fail(short2.javaClass.name + " shortBytes result incorrect")
        }

        val long2 = ASN1Primitive.fromByteArray((longString as ASN1Primitive).encoded) as ASN1String

        if (longString.toString() != long2.toString()) {
            fail(long2.javaClass.name + " longBytes result incorrect")
        }
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(StringTest())
        }
    }
}

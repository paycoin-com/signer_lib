package org.bouncycastle.asn1.test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Date

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OutputStream
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.BERConstructedOctetString
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.BERSet
import org.bouncycastle.asn1.BERTaggedObject
import org.bouncycastle.asn1.DERApplicationSpecific
import org.bouncycastle.asn1.DERBMPString
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERGeneralString
import org.bouncycastle.asn1.DERGeneralizedTime
import org.bouncycastle.asn1.DERGraphicString
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DERNumericString
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.DERT61String
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.DERUTCTime
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERUniversalString
import org.bouncycastle.asn1.DERVideotexString
import org.bouncycastle.asn1.DERVisibleString
import org.bouncycastle.util.Strings
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.test.SimpleTestResult
import org.bouncycastle.util.test.Test
import org.bouncycastle.util.test.TestResult

class EqualsAndHashCodeTest : Test {
    override fun perform(): TestResult {
        val data = byteArrayOf(0, 1, 0, 1, 0, 0, 1)

        val values = arrayOf(BERConstructedOctetString(data), BERSequence(DERPrintableString("hello world")), BERSet(DERPrintableString("hello world")), BERTaggedObject(0, DERPrintableString("hello world")), DERApplicationSpecific(0, data), DERBitString(data), DERBMPString("hello world"), ASN1Boolean(true), ASN1Boolean(false), ASN1Enumerated(100), DERGeneralizedTime("20070315173729Z"), DERGeneralString("hello world"), DERIA5String("hello"), ASN1Integer(1000), DERNull(), DERNumericString("123456"), ASN1ObjectIdentifier("1.1.1.10000.1"), DEROctetString(data), DERPrintableString("hello world"), DERSequence(DERPrintableString("hello world")), DERSet(DERPrintableString("hello world")), DERT61String("hello world"), DERTaggedObject(0, DERPrintableString("hello world")), DERUniversalString(data), DERUTCTime(Date()), DERUTF8String("hello world"), DERVisibleString("hello world"), DERGraphicString(Hex.decode("deadbeef")), DERVideotexString(Strings.toByteArray("Hello World")))

        try {
            val bOut = ByteArrayOutputStream()
            val aOut = ASN1OutputStream(bOut)

            for (i in values.indices) {
                aOut.writeObject(values[i])
            }

            val bIn = ByteArrayInputStream(bOut.toByteArray())
            val aIn = ASN1InputStream(bIn)

            for (i in values.indices) {
                val o = aIn.readObject()
                if (o != values[i]) {
                    return SimpleTestResult(false, name + ": Failed equality test for " + o.javaClass)
                }

                if (o.hashCode() != values[i].hashCode()) {
                    return SimpleTestResult(false, name + ": Failed hashCode test for " + o.javaClass)
                }
            }
        } catch (e: Exception) {
            return SimpleTestResult(false, name + ": Failed - exception " + e.toString(), e)
        }

        return SimpleTestResult(true, name + ": Okay")
    }

    override fun getName(): String {
        return "EqualsAndHashCode"
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            val test = EqualsAndHashCodeTest()
            val result = test.perform()

            println(result)
        }
    }
}

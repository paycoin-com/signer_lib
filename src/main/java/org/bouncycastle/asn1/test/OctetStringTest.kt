package org.bouncycastle.asn1.test

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetStringParser
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.ASN1StreamParser
import org.bouncycastle.asn1.BEROctetStringGenerator
import org.bouncycastle.asn1.BERSequenceGenerator
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DERSequenceGenerator
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers
import org.bouncycastle.asn1.cms.CompressedDataParser
import org.bouncycastle.asn1.cms.ContentInfoParser

class OctetStringTest : TestCase() {
    @Throws(Exception::class)
    fun testReadingWriting() {
        val bOut = ByteArrayOutputStream()
        val octGen = BEROctetStringGenerator(bOut)

        val out = octGen.octetOutputStream

        out.write(byteArrayOf(1, 2, 3, 4))
        out.write(ByteArray(4))

        out.close()

        val aIn = ASN1StreamParser(bOut.toByteArray())

        val s = aIn.readObject() as ASN1OctetStringParser

        val `in` = s.octetStream
        var count = 0

        while (`in`.read() >= 0) {
            count++
        }

        TestCase.Companion.assertEquals(8, count)
    }

    @Throws(Exception::class)
    fun testReadingWritingZeroInLength() {
        val bOut = ByteArrayOutputStream()
        val octGen = BEROctetStringGenerator(bOut)

        val out = octGen.octetOutputStream

        out.write(byteArrayOf(1, 2, 3, 4))
        out.write(ByteArray(512))  // forces a zero to appear in length

        out.close()

        val aIn = ASN1StreamParser(bOut.toByteArray())

        val s = aIn.readObject() as ASN1OctetStringParser

        val `in` = s.octetStream
        var count = 0

        while (`in`.read() >= 0) {
            count++
        }

        TestCase.Companion.assertEquals(516, count)
    }

    @Throws(Exception::class)
    fun testReadingWritingNested() {
        val bOut = ByteArrayOutputStream()
        val sGen = BERSequenceGenerator(bOut)
        val octGen = BEROctetStringGenerator(sGen.rawOutputStream)

        val out = octGen.octetOutputStream

        val inSGen = BERSequenceGenerator(out)

        val inOctGen = BEROctetStringGenerator(inSGen.rawOutputStream)

        val inOut = inOctGen.octetOutputStream

        inOut.write(byteArrayOf(1, 2, 3, 4))
        inOut.write(ByteArray(10))

        inOut.close()

        inSGen.close()

        out.close()

        sGen.close()

        val aIn = ASN1StreamParser(bOut.toByteArray())

        val sq = aIn.readObject() as ASN1SequenceParser

        val s = sq.readObject() as ASN1OctetStringParser

        val aIn2 = ASN1StreamParser(s.octetStream)

        val sq2 = aIn2.readObject() as ASN1SequenceParser

        val inS = sq2.readObject() as ASN1OctetStringParser

        val `in` = inS.octetStream
        var count = 0

        while (`in`.read() >= 0) {
            count++
        }

        TestCase.Companion.assertEquals(14, count)
    }

    @Throws(Exception::class)
    fun testNestedStructure() {
        val bOut = ByteArrayOutputStream()

        val sGen = BERSequenceGenerator(bOut)

        sGen.addObject(ASN1ObjectIdentifier(CMSObjectIdentifiers.compressedData.id))

        val cGen = BERSequenceGenerator(sGen.rawOutputStream, 0, true)

        cGen.addObject(ASN1Integer(0))

        //
        // AlgorithmIdentifier
        //
        val algGen = DERSequenceGenerator(cGen.rawOutputStream)

        algGen.addObject(ASN1ObjectIdentifier("1.2"))

        algGen.close()

        //
        // Encapsulated ContentInfo
        //
        val eiGen = BERSequenceGenerator(cGen.rawOutputStream)

        eiGen.addObject(ASN1ObjectIdentifier("1.1"))

        val octGen = BEROctetStringGenerator(eiGen.rawOutputStream, 0, true)

        //
        // output containing zeroes
        //
        val out = octGen.octetOutputStream

        out.write(byteArrayOf(1, 2, 3, 4))
        out.write(ByteArray(4))
        out.write(ByteArray(20))

        out.close()
        eiGen.close()
        cGen.close()
        sGen.close()

        //
        // reading back
        //
        val aIn = ASN1StreamParser(bOut.toByteArray())

        val cp = ContentInfoParser(aIn.readObject() as ASN1SequenceParser)

        val comData = CompressedDataParser(cp.getContent(BERTags.SEQUENCE) as ASN1SequenceParser?)
        val content = comData.encapContentInfo

        val bytes = content.getContent(BERTags.OCTET_STRING) as ASN1OctetStringParser?

        val `in` = bytes.getOctetStream()
        var count = 0

        while (`in`.read() >= 0) {
            count++
        }

        TestCase.Companion.assertEquals(28, count)
    }

    companion object {

        fun suite(): Test {
            return TestSuite(OctetStringTest::class.java)
        }
    }
}

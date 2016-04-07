package org.bouncycastle.asn1.test

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.util.Arrays

import junit.framework.Test
import junit.framework.TestCase
import junit.framework.TestSuite
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Null
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1SequenceParser
import org.bouncycastle.asn1.ASN1StreamParser
import org.bouncycastle.asn1.BERSequenceGenerator
import org.bouncycastle.asn1.DERSequenceGenerator
import org.bouncycastle.util.encoders.Hex

class ASN1SequenceParserTest : TestCase() {

    @Throws(Exception::class)
    fun testDERWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen = DERSequenceGenerator(bOut)

        seqGen.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen.addObject(ASN1ObjectIdentifier("1.1"))

        seqGen.close()

        TestCase.Companion.assertTrue("basic DER writing test failed.", Arrays.equals(seqData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testNestedDERWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen1 = DERSequenceGenerator(bOut)

        seqGen1.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen1.addObject(ASN1ObjectIdentifier("1.1"))

        val seqGen2 = DERSequenceGenerator(seqGen1.rawOutputStream)

        seqGen2.addObject(ASN1Integer(BigInteger.valueOf(1)))

        seqGen2.close()

        seqGen1.close()

        TestCase.Companion.assertTrue("nested DER writing test failed.", Arrays.equals(nestedSeqData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testDERExplicitTaggedSequenceWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen = DERSequenceGenerator(bOut, 1, true)

        seqGen.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen.addObject(ASN1ObjectIdentifier("1.1"))

        seqGen.close()

        TestCase.Companion.assertTrue("explicit tag writing test failed.", Arrays.equals(expTagSeqData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testDERImplicitTaggedSequenceWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen = DERSequenceGenerator(bOut, 1, false)

        seqGen.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen.addObject(ASN1ObjectIdentifier("1.1"))

        seqGen.close()

        TestCase.Companion.assertTrue("implicit tag writing test failed.", Arrays.equals(implTagSeqData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testNestedExplicitTagDERWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen1 = DERSequenceGenerator(bOut)

        seqGen1.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen1.addObject(ASN1ObjectIdentifier("1.1"))

        val seqGen2 = DERSequenceGenerator(seqGen1.rawOutputStream, 1, true)

        seqGen2.addObject(ASN1Integer(BigInteger.valueOf(1)))

        seqGen2.close()

        seqGen1.close()

        TestCase.Companion.assertTrue("nested explicit tagged DER writing test failed.", Arrays.equals(nestedSeqExpTagData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testNestedImplicitTagDERWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen1 = DERSequenceGenerator(bOut)

        seqGen1.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen1.addObject(ASN1ObjectIdentifier("1.1"))

        val seqGen2 = DERSequenceGenerator(seqGen1.rawOutputStream, 1, false)

        seqGen2.addObject(ASN1Integer(BigInteger.valueOf(1)))

        seqGen2.close()

        seqGen1.close()

        TestCase.Companion.assertTrue("nested implicit tagged DER writing test failed.", Arrays.equals(nestedSeqImpTagData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testBERWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen = BERSequenceGenerator(bOut)

        seqGen.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen.addObject(ASN1ObjectIdentifier("1.1"))

        seqGen.close()

        TestCase.Companion.assertTrue("basic BER writing test failed.", Arrays.equals(berSeqData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testNestedBERDERWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen1 = BERSequenceGenerator(bOut)

        seqGen1.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen1.addObject(ASN1ObjectIdentifier("1.1"))

        val seqGen2 = DERSequenceGenerator(seqGen1.rawOutputStream)

        seqGen2.addObject(ASN1Integer(BigInteger.valueOf(1)))

        seqGen2.close()

        seqGen1.close()

        TestCase.Companion.assertTrue("nested BER/DER writing test failed.", Arrays.equals(berDERNestedSeqData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testNestedBERWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen1 = BERSequenceGenerator(bOut)

        seqGen1.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen1.addObject(ASN1ObjectIdentifier("1.1"))

        val seqGen2 = BERSequenceGenerator(seqGen1.rawOutputStream)

        seqGen2.addObject(ASN1Integer(BigInteger.valueOf(1)))

        seqGen2.close()

        seqGen1.close()

        TestCase.Companion.assertTrue("nested BER writing test failed.", Arrays.equals(berNestedSeqData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testDERReading() {
        val aIn = ASN1StreamParser(seqData)

        val seq = aIn.readObject() as ASN1SequenceParser
        var o: Any
        var count = 0

        TestCase.Companion.assertNotNull("null sequence returned", seq)

        while ((o = seq.readObject()) != null) {
            when (count) {
                0 -> TestCase.Companion.assertTrue(o is ASN1Integer)
                1 -> TestCase.Companion.assertTrue(o is ASN1ObjectIdentifier)
            }
            count++
        }

        TestCase.Companion.assertEquals("wrong number of objects in sequence", 2, count)
    }

    @Throws(Exception::class)
    private fun testNestedReading(
            data: ByteArray) {
        val aIn = ASN1StreamParser(data)

        val seq = aIn.readObject() as ASN1SequenceParser
        var o: Any
        var count = 0

        TestCase.Companion.assertNotNull("null sequence returned", seq)

        while ((o = seq.readObject()) != null) {
            when (count) {
                0 -> TestCase.Companion.assertTrue(o is ASN1Integer)
                1 -> TestCase.Companion.assertTrue(o is ASN1ObjectIdentifier)
                2 -> {
                    TestCase.Companion.assertTrue(o is ASN1SequenceParser)

                    val s = o as ASN1SequenceParser

                    // NB: Must exhaust the nested parser
                    while (s.readObject() != null) {
                        // Nothing
                    }
                }
            }
            count++
        }

        TestCase.Companion.assertEquals("wrong number of objects in sequence", 3, count)
    }

    @Throws(Exception::class)
    fun testNestedDERReading() {
        testNestedReading(nestedSeqData)
    }

    @Throws(Exception::class)
    fun testBERReading() {
        val aIn = ASN1StreamParser(berSeqData)

        val seq = aIn.readObject() as ASN1SequenceParser
        var o: Any
        var count = 0

        TestCase.Companion.assertNotNull("null sequence returned", seq)

        while ((o = seq.readObject()) != null) {
            when (count) {
                0 -> TestCase.Companion.assertTrue(o is ASN1Integer)
                1 -> TestCase.Companion.assertTrue(o is ASN1ObjectIdentifier)
            }
            count++
        }

        TestCase.Companion.assertEquals("wrong number of objects in sequence", 2, count)
    }

    @Throws(Exception::class)
    fun testNestedBERDERReading() {
        testNestedReading(berDERNestedSeqData)
    }

    @Throws(Exception::class)
    fun testNestedBERReading() {
        testNestedReading(berNestedSeqData)
    }

    @Throws(Exception::class)
    fun testBERExplicitTaggedSequenceWriting() {
        val bOut = ByteArrayOutputStream()
        val seqGen = BERSequenceGenerator(bOut, 1, true)

        seqGen.addObject(ASN1Integer(BigInteger.valueOf(0)))

        seqGen.addObject(ASN1ObjectIdentifier("1.1"))

        seqGen.close()

        TestCase.Companion.assertTrue("explicit BER tag writing test failed.", Arrays.equals(berExpTagSeqData, bOut.toByteArray()))
    }

    @Throws(Exception::class)
    fun testSequenceWithDERNullReading() {
        testParseWithNull(berSeqWithDERNullData)
    }

    @Throws(IOException::class)
    private fun testParseWithNull(data: ByteArray) {
        val aIn = ASN1StreamParser(data)
        val seq = aIn.readObject() as ASN1SequenceParser
        var o: Any
        var count = 0

        TestCase.Companion.assertNotNull("null sequence returned", seq)

        while ((o = seq.readObject()) != null) {
            when (count) {
                0 -> TestCase.Companion.assertTrue(o is ASN1Null)
                1 -> TestCase.Companion.assertTrue(o is ASN1Integer)
                2 -> TestCase.Companion.assertTrue(o is ASN1ObjectIdentifier)
            }
            count++
        }

        TestCase.Companion.assertEquals("wrong number of objects in sequence", 3, count)
    }

    companion object {
        private val seqData = Hex.decode("3006020100060129")
        private val nestedSeqData = Hex.decode("300b0201000601293003020101")
        private val expTagSeqData = Hex.decode("a1083006020100060129")
        private val implTagSeqData = Hex.decode("a106020100060129")
        private val nestedSeqExpTagData = Hex.decode("300d020100060129a1053003020101")
        private val nestedSeqImpTagData = Hex.decode("300b020100060129a103020101")

        private val berSeqData = Hex.decode("30800201000601290000")
        private val berDERNestedSeqData = Hex.decode("308002010006012930030201010000")
        private val berNestedSeqData = Hex.decode("3080020100060129308002010100000000")
        private val berExpTagSeqData = Hex.decode("a180308002010006012900000000")

        private val berSeqWithDERNullData = Hex.decode("308005000201000601290000")

        fun suite(): Test {
            return TestSuite(ASN1SequenceParserTest::class.java)
        }
    }
}

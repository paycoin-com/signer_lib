package org.bouncycastle.asn1.test

import java.io.IOException

import junit.framework.TestCase

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.util.encoders.Hex

/**
 * Tests used to verify correct decoding of the ENUMERATED type.
 */
class EnumeratedTest : TestCase() {

    /**
     * Makes sure multiple identically sized values are parsed correctly.
     */
    @Throws(IOException::class)
    fun testReadingMultipleSingleByteItems() {
        val obj = ASN1Primitive.fromByteArray(MultipleSingleByteItems)

        TestCase.Companion.assertTrue("Null ASN.1 SEQUENCE", obj is ASN1Sequence)

        val sequence = obj as ASN1Sequence

        TestCase.Companion.assertEquals("2 items expected", 2, sequence.size())

        val enumerated = ASN1Enumerated.getInstance(sequence.getObjectAt(0))

        TestCase.Companion.assertNotNull("ENUMERATED expected", enumerated)

        TestCase.Companion.assertEquals("Unexpected ENUMERATED value", 1, enumerated.value.toInt())

        val b = ASN1Boolean.getInstance(sequence.getObjectAt(1))

        TestCase.Companion.assertNotNull("BOOLEAN expected", b)

        TestCase.Companion.assertTrue("Unexpected BOOLEAN value", b.isTrue)
    }

    /**
     * Makes sure multiple identically sized values are parsed correctly.
     */
    @Throws(IOException::class)
    fun testReadingMultipleDoubleByteItems() {
        val obj = ASN1Primitive.fromByteArray(MultipleDoubleByteItems)

        TestCase.Companion.assertTrue("Null ASN.1 SEQUENCE", obj is ASN1Sequence)

        val sequence = obj as ASN1Sequence

        TestCase.Companion.assertEquals("2 items expected", 2, sequence.size())

        val enumerated1 = ASN1Enumerated.getInstance(sequence.getObjectAt(0))

        TestCase.Companion.assertNotNull("ENUMERATED expected", enumerated1)

        TestCase.Companion.assertEquals("Unexpected ENUMERATED value", 257, enumerated1.value.toInt())

        val enumerated2 = ASN1Enumerated.getInstance(sequence.getObjectAt(1))

        TestCase.Companion.assertNotNull("ENUMERATED expected", enumerated2)

        TestCase.Companion.assertEquals("Unexpected ENUMERATED value", 514, enumerated2.value.toInt())
    }

    /**
     * Makes sure multiple identically sized values are parsed correctly.
     */
    @Throws(IOException::class)
    fun testReadingMultipleTripleByteItems() {
        val obj = ASN1Primitive.fromByteArray(MultipleTripleByteItems)

        TestCase.Companion.assertTrue("Null ASN.1 SEQUENCE", obj is ASN1Sequence)

        val sequence = obj as ASN1Sequence

        TestCase.Companion.assertEquals("2 items expected", 2, sequence.size())

        val enumerated = ASN1Enumerated.getInstance(sequence.getObjectAt(0))

        TestCase.Companion.assertNotNull("ENUMERATED expected", enumerated)

        TestCase.Companion.assertEquals("Unexpected ENUMERATED value", 65793, enumerated.value.toInt())

        val objectId = ASN1ObjectIdentifier.getInstance(sequence.getObjectAt(1))

        TestCase.Companion.assertNotNull("OBJECT IDENTIFIER expected", objectId)

        TestCase.Companion.assertEquals("Unexpected OBJECT IDENTIFIER value", "1.3.6.1", objectId.id)
    }

    companion object {
        /**
         * Test vector used to test decoding of multiple items. This sample uses an ENUMERATED and a BOOLEAN.
         */
        private val MultipleSingleByteItems = Hex.decode("30060a01010101ff")

        /**
         * Test vector used to test decoding of multiple items. This sample uses two ENUMERATEDs.
         */
        private val MultipleDoubleByteItems = Hex.decode("30080a0201010a020202")

        /**
         * Test vector used to test decoding of multiple items. This sample uses an ENUMERATED and an OBJECT IDENTIFIER.
         */
        private val MultipleTripleByteItems = Hex.decode("300a0a0301010106032b0601")
    }
}

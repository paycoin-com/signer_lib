package org.bouncycastle.asn1.test

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.BERSet
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.util.test.SimpleTest

/**
 * Set sorting test example
 */
class SetTest : SimpleTest() {

    override fun getName(): String {
        return "Set"
    }

    private fun checkedSortedSet(attempt: Int, s: ASN1Set) {
        if (s.getObjectAt(0) is ASN1Boolean
                && s.getObjectAt(1) is ASN1Integer
                && s.getObjectAt(2) is DERBitString
                && s.getObjectAt(3) is DEROctetString) {
            return
        }

        fail("sorting failed on attempt: " + attempt)
    }

    override fun performTest() {
        var v = ASN1EncodableVector()
        val data = ByteArray(10)

        v.add(DEROctetString(data))
        v.add(DERBitString(data))
        v.add(ASN1Integer(100))
        v.add(ASN1Boolean.getInstance(true))

        checkedSortedSet(0, DERSet(v))

        v = ASN1EncodableVector()
        v.add(ASN1Integer(100))
        v.add(ASN1Boolean.getInstance(true))
        v.add(DEROctetString(data))
        v.add(DERBitString(data))

        checkedSortedSet(1, DERSet(v))

        v = ASN1EncodableVector()
        v.add(ASN1Boolean.getInstance(true))
        v.add(DEROctetString(data))
        v.add(DERBitString(data))
        v.add(ASN1Integer(100))


        checkedSortedSet(2, DERSet(v))

        v = ASN1EncodableVector()
        v.add(DERBitString(data))
        v.add(DEROctetString(data))
        v.add(ASN1Integer(100))
        v.add(ASN1Boolean.getInstance(true))

        checkedSortedSet(3, DERSet(v))

        v = ASN1EncodableVector()
        v.add(DEROctetString(data))
        v.add(DERBitString(data))
        v.add(ASN1Integer(100))
        v.add(ASN1Boolean.getInstance(true))

        var s: ASN1Set = BERSet(v)

        if (s.getObjectAt(0) !is DEROctetString) {
            fail("BER set sort order changed.")
        }

        // create an implicitly tagged "set" without sorting
        val tag = DERTaggedObject(false, 1, DERSequence(v))
        s = ASN1Set.getInstance(tag, false)

        if (s.getObjectAt(0) is ASN1Boolean) {
            fail("sorted when shouldn't be.")
        }

        // equality test
        v = ASN1EncodableVector()

        v.add(ASN1Boolean.getInstance(true))
        v.add(ASN1Boolean.getInstance(true))
        v.add(ASN1Boolean.getInstance(true))

        s = DERSet(v)
    }

    companion object {

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(SetTest())
        }
    }
}

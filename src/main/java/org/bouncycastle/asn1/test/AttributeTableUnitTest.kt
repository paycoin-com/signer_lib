package org.bouncycastle.asn1.test

import java.util.Hashtable

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.cms.Attribute
import org.bouncycastle.asn1.cms.AttributeTable
import org.bouncycastle.util.test.SimpleTest

class AttributeTableUnitTest : SimpleTest() {

    override fun getName(): String {
        return "AttributeTable"
    }

    @Throws(Exception::class)
    override fun performTest() {
        var v = ASN1EncodableVector()

        v.add(Attribute(type1, DERSet(type1)))
        v.add(Attribute(type2, DERSet(type2)))

        var table = AttributeTable(v)

        var a: Attribute? = table[type1]
        if (a == null) {
            fail("type1 attribute not found.")
        }
        if (!a!!.attrValues!!.equals(DERSet(type1))) {
            fail("wrong value retrieved for type1!")
        }

        a = table[type2]
        if (a == null) {
            fail("type2 attribute not found.")
        }
        if (!a.attrValues!!.equals(DERSet(type2))) {
            fail("wrong value retrieved for type2!")
        }

        a = table[type3]
        if (a != null) {
            fail("type3 attribute found when none expected.")
        }

        var vec: ASN1EncodableVector = table.getAll(type1)
        if (vec.size() != 1) {
            fail("wrong vector size for type1.")
        }

        vec = table.getAll(type3)
        if (vec.size() != 0) {
            fail("wrong vector size for type3.")
        }

        vec = table.toASN1EncodableVector()
        if (vec.size() != 2) {
            fail("wrong vector size for single.")
        }

        val t = table.toHashtable()

        if (t.size != 2) {
            fail("hashtable wrong size.")
        }

        // multiple

        v = ASN1EncodableVector()

        v.add(Attribute(type1, DERSet(type1)))
        v.add(Attribute(type1, DERSet(type2)))
        v.add(Attribute(type1, DERSet(type3)))
        v.add(Attribute(type2, DERSet(type2)))

        table = AttributeTable(v)

        a = table[type1]
        if (!a.attrValues!!.equals(DERSet(type1))) {
            fail("wrong value retrieved for type1 multi get!")
        }

        vec = table.getAll(type1)
        if (vec.size() != 3) {
            fail("wrong vector size for multiple type1.")
        }

        a = vec.get(0) as Attribute
        if (!a.attrValues!!.equals(DERSet(type1))) {
            fail("wrong value retrieved for type1(0)!")
        }

        a = vec.get(1) as Attribute
        if (!a.attrValues!!.equals(DERSet(type2))) {
            fail("wrong value retrieved for type1(1)!")
        }

        a = vec.get(2) as Attribute
        if (!a.attrValues!!.equals(DERSet(type3))) {
            fail("wrong value retrieved for type1(2)!")
        }

        vec = table.getAll(type2)
        if (vec.size() != 1) {
            fail("wrong vector size for multiple type2.")
        }

        vec = table.toASN1EncodableVector()
        if (vec.size() != 4) {
            fail("wrong vector size for multiple.")
        }
    }

    companion object {
        private val type1 = ASN1ObjectIdentifier("1.1.1")
        private val type2 = ASN1ObjectIdentifier("1.1.2")
        private val type3 = ASN1ObjectIdentifier("1.1.3")

        @JvmStatic fun main(
                args: Array<String>) {
            SimpleTest.runTest(AttributeTableUnitTest())
        }
    }
}

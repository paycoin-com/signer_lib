package org.bouncycastle.asn1.cms

import java.util.Enumeration
import java.util.Hashtable
import java.util.Vector

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.DERSet

/**
 * This is helper tool to construct [Attributes] sets.
 */
class AttributeTable {
    private var attributes = Hashtable()

    constructor(
            attrs: Hashtable<Any, Any>) {
        attributes = copyTable(attrs)
    }

    constructor(
            v: ASN1EncodableVector) {
        for (i in 0..v.size() - 1) {
            val a = Attribute.getInstance(v.get(i))

            addAttribute(a.attrType, a)
        }
    }

    constructor(
            s: ASN1Set) {
        for (i in 0..s.size() - 1) {
            val a = Attribute.getInstance(s.getObjectAt(i))

            addAttribute(a.attrType, a)
        }
    }

    constructor(
            attr: Attribute) {
        addAttribute(attr.attrType, attr)
    }

    constructor(
            attrs: Attributes) : this(ASN1Set.getInstance(attrs.toASN1Primitive())) {
    }

    private fun addAttribute(
            oid: ASN1ObjectIdentifier,
            a: Attribute) {
        val value = attributes.get(oid)

        if (value == null) {
            attributes.put(oid, a)
        } else {
            val v: Vector<Any>

            if (value is Attribute) {
                v = Vector()

                v.addElement(value)
                v.addElement(a)
            } else {
                v = value as Vector<Any>?

                v.addElement(a)
            }

            attributes.put(oid, v)
        }
    }

    /**
     * Return the first attribute matching the OBJECT IDENTIFIER oid.

     * @param oid type of attribute required.
     * *
     * @return first attribute found of type oid.
     */
    operator fun get(
            oid: ASN1ObjectIdentifier): Attribute {
        val value = attributes.get(oid)

        if (value is Vector<Any>) {
            return (value as Vector<Any>).elementAt(0) as Attribute
        }

        return value
    }

    /**
     * Return all the attributes matching the OBJECT IDENTIFIER oid. The vector will be
     * empty if there are no attributes of the required type present.

     * @param oid type of attribute required.
     * *
     * @return a vector of all the attributes found of type oid.
     */
    fun getAll(
            oid: ASN1ObjectIdentifier): ASN1EncodableVector {
        val v = ASN1EncodableVector()

        val value = attributes.get(oid)

        if (value is Vector<Any>) {
            val e = (value as Vector<Any>).elements()

            while (e.hasMoreElements()) {
                v.add(e.nextElement() as Attribute)
            }
        } else if (value != null) {
            v.add(value as Attribute?)
        }

        return v
    }

    fun size(): Int {
        var size = 0

        val en = attributes.elements()
        while (en.hasMoreElements()) {
            val o = en.nextElement()

            if (o is Vector<Any>) {
                size += (o as Vector<Any>).size
            } else {
                size++
            }
        }

        return size
    }

    fun toHashtable(): Hashtable<Any, Any> {
        return copyTable(attributes)
    }

    fun toASN1EncodableVector(): ASN1EncodableVector {
        val v = ASN1EncodableVector()
        val e = attributes.elements()

        while (e.hasMoreElements()) {
            val value = e.nextElement()

            if (value is Vector<Any>) {
                val en = (value as Vector<Any>).elements()

                while (en.hasMoreElements()) {
                    v.add(Attribute.getInstance(en.nextElement()))
                }
            } else {
                v.add(Attribute.getInstance(value))
            }
        }

        return v
    }

    fun toASN1Structure(): Attributes {
        return Attributes(this.toASN1EncodableVector())
    }

    private fun copyTable(
            `in`: Hashtable<Any, Any>): Hashtable<Any, Any> {
        val out = Hashtable()
        val e = `in`.keys()

        while (e.hasMoreElements()) {
            val key = e.nextElement()

            out.put(key, `in`[key])
        }

        return out
    }

    /**
     * Return a new table with the passed in attribute added.

     * @param attrType the type of the attribute to add.
     * *
     * @param attrValue the value corresponding to the attribute (will be wrapped in a SET).
     * *
     * @return a new table with the extra attribute in it.
     */
    fun add(attrType: ASN1ObjectIdentifier, attrValue: ASN1Encodable): AttributeTable {
        val newTable = AttributeTable(attributes)

        newTable.addAttribute(attrType, Attribute(attrType, DERSet(attrValue)))

        return newTable
    }

    fun remove(attrType: ASN1ObjectIdentifier): AttributeTable {
        val newTable = AttributeTable(attributes)

        newTable.attributes.remove(attrType)

        return newTable
    }
}

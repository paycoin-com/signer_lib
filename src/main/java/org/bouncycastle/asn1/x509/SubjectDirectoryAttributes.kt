package org.bouncycastle.asn1.x509

import java.util.Enumeration
import java.util.Vector

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * This extension may contain further X.500 attributes of the subject. See also
 * RFC 3039.

 *
 * SubjectDirectoryAttributes ::= Attributes
 * Attributes ::= SEQUENCE SIZE (1..MAX) OF Attribute
 * Attribute ::= SEQUENCE
 * {
 * type AttributeType
 * values SET OF AttributeValue
 * }

 * AttributeType ::= OBJECT IDENTIFIER
 * AttributeValue ::= ANY DEFINED BY AttributeType
 *

 * @see org.bouncycastle.asn1.x500.style.BCStyle for AttributeType ObjectIdentifiers.
 */
class SubjectDirectoryAttributes : ASN1Object {
    /**
     * @return Returns the attributes.
     */
    val attributes = Vector()

    /**
     * Constructor from ASN1Sequence.

     * The sequence is of type SubjectDirectoryAttributes:

     *
     * SubjectDirectoryAttributes ::= Attributes
     * Attributes ::= SEQUENCE SIZE (1..MAX) OF Attribute
     * Attribute ::= SEQUENCE
     * {
     * type AttributeType
     * values SET OF AttributeValue
     * }

     * AttributeType ::= OBJECT IDENTIFIER
     * AttributeValue ::= ANY DEFINED BY AttributeType
     *

     * @param seq
     * *            The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {
        val e = seq.objects

        while (e.hasMoreElements()) {
            val s = ASN1Sequence.getInstance(e.nextElement())
            attributes.addElement(Attribute.getInstance(s))
        }
    }

    /**
     * Constructor from a vector of attributes.

     * The vector consists of attributes of type [Attribute]

     * @param attributes
     * *            The attributes.
     */
    constructor(attributes: Vector<Any>) {
        val e = attributes.elements()

        while (e.hasMoreElements()) {
            this.attributes.addElement(e.nextElement())
        }
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.

     * Returns:

     *
     * SubjectDirectoryAttributes ::= Attributes
     * Attributes ::= SEQUENCE SIZE (1..MAX) OF Attribute
     * Attribute ::= SEQUENCE
     * {
     * type AttributeType
     * values SET OF AttributeValue
     * }

     * AttributeType ::= OBJECT IDENTIFIER
     * AttributeValue ::= ANY DEFINED BY AttributeType
     *

     * @return a ASN1Primitive
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()
        val e = attributes.elements()

        while (e.hasMoreElements()) {

            vec.add(e.nextElement() as Attribute)
        }

        return DERSequence(vec)
    }

    companion object {

        fun getInstance(
                obj: Any?): SubjectDirectoryAttributes? {
            if (obj is SubjectDirectoryAttributes) {
                return obj
            }

            if (obj != null) {
                return SubjectDirectoryAttributes(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

package org.bouncycastle.asn1.x500

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.style.BCStyle

/**
 * The X.500 Name object.
 *
 * Name ::= CHOICE {
 * RDNSequence }

 * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName

 * RelativeDistinguishedName ::= SET SIZE (1..MAX) OF AttributeTypeAndValue

 * AttributeTypeAndValue ::= SEQUENCE {
 * type  OBJECT IDENTIFIER,
 * value ANY }
 *
 */
class X500Name : ASN1Object, ASN1Choice {

    private var isHashCodeCalculated: Boolean = false
    private var hashCodeValue: Int = 0

    private var style: X500NameStyle? = null
    private var rdns: Array<RDN>? = null


    @Deprecated("use the getInstance() method that takes a style.")
    constructor(style: X500NameStyle, name: X500Name) {
        this.rdns = name.rdns
        this.style = style
    }

    /**
     * Constructor from ASN1Sequence

     * the principal will be a list of constructed sets, each containing an (OID, String) pair.
     */
    private constructor(
            seq: ASN1Sequence) : this(defaultStyle, seq) {
    }

    private constructor(
            style: X500NameStyle,
            seq: ASN1Sequence) {
        this.style = style
        this.rdns = arrayOfNulls<RDN>(seq.size())

        var index = 0

        val e = seq.objects
        while (e.hasMoreElements()) {
            rdns[index++] = RDN.getInstance(e.nextElement())
        }
    }

    constructor(
            rDNs: Array<RDN>) : this(defaultStyle, rDNs) {
    }

    constructor(
            style: X500NameStyle,
            rDNs: Array<RDN>) {
        this.rdns = rDNs
        this.style = style
    }

    constructor(
            dirName: String) : this(defaultStyle, dirName) {
    }

    constructor(
            style: X500NameStyle,
            dirName: String) : this(style.fromString(dirName)) {

        this.style = style
    }

    /**
     * return an array of RDNs in structure order.

     * @return an array of RDN objects.
     */
    val rdNs: Array<RDN>
        get() {
            val tmp = arrayOfNulls<RDN>(this.rdns!!.size)

            System.arraycopy(rdns, 0, tmp, 0, tmp.size)

            return tmp
        }

    /**
     * return an array of OIDs contained in the attribute type of each RDN in structure order.

     * @return an array, possibly zero length, of ASN1ObjectIdentifiers objects.
     */
    val attributeTypes: Array<ASN1ObjectIdentifier>
        get() {
            var count = 0

            for (i in rdns!!.indices) {
                val rdn = rdns!![i]

                count += rdn.size()
            }

            val res = arrayOfNulls<ASN1ObjectIdentifier>(count)

            count = 0

            for (i in rdns!!.indices) {
                val rdn = rdns!![i]

                if (rdn.isMultiValued) {
                    val attr = rdn.typesAndValues
                    for (j in attr.indices) {
                        res[count++] = attr[j].type
                    }
                } else if (rdn.size() != 0) {
                    res[count++] = rdn.first!!.type
                }
            }

            return res
        }

    /**
     * return an array of RDNs containing the attribute type given by OID in structure order.

     * @param attributeType the type OID we are looking for.
     * *
     * @return an array, possibly zero length, of RDN objects.
     */
    fun getRDNs(attributeType: ASN1ObjectIdentifier): Array<RDN> {
        val res = arrayOfNulls<RDN>(rdns!!.size)
        var count = 0

        for (i in rdns!!.indices) {
            val rdn = rdns!![i]

            if (rdn.isMultiValued) {
                val attr = rdn.typesAndValues
                for (j in attr.indices) {
                    if (attr[j].type == attributeType) {
                        res[count++] = rdn
                        break
                    }
                }
            } else {
                if (rdn.first!!.type == attributeType) {
                    res[count++] = rdn
                }
            }
        }

        val tmp = arrayOfNulls<RDN>(count)

        System.arraycopy(res, 0, tmp, 0, tmp.size)

        return tmp
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return DERSequence(rdns)
    }

    override fun hashCode(): Int {
        if (isHashCodeCalculated) {
            return hashCodeValue
        }

        isHashCodeCalculated = true

        hashCodeValue = style!!.calculateHashCode(this)

        return hashCodeValue
    }

    /**
     * test for equality - note: case is ignored.
     */
    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }

        if (!(obj is X500Name || obj is ASN1Sequence)) {
            return false
        }

        val derO = (obj as ASN1Encodable).toASN1Primitive()

        if (this.toASN1Primitive() == derO) {
            return true
        }

        try {
            return style!!.areEqual(this, X500Name(ASN1Sequence.getInstance(obj.toASN1Primitive())))
        } catch (e: Exception) {
            return false
        }

    }

    override fun toString(): String {
        return style!!.toString(this)
    }

    companion object {
        /**
         * Return the current default style.

         * @return default style for X500Name construction.
         */
        /**
         * Set the default style for X500Name construction.

         * @param style  an X500NameStyle
         */
        var defaultStyle: X500NameStyle? = BCStyle.INSTANCE
            set(style) {
                if (style == null) {
                    throw NullPointerException("cannot set style to null")
                }

                defaultStyle = style
            }

        /**
         * Return a X500Name based on the passed in tagged object.

         * @param obj tag object holding name.
         * *
         * @param explicit true if explicitly tagged false otherwise.
         * *
         * @return the X500Name
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): X500Name {
            // must be true as choice item
            return getInstance(ASN1Sequence.getInstance(obj, true))
        }

        fun getInstance(
                obj: Any?): X500Name? {
            if (obj is X500Name) {
                return obj
            } else if (obj != null) {
                return X500Name(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                style: X500NameStyle,
                obj: Any?): X500Name? {
            if (obj is X500Name) {
                return X500Name(style, obj as X500Name?)
            } else if (obj != null) {
                return X500Name(style, ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

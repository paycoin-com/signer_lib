package org.bouncycastle.asn1.x500

import java.util.Vector

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.style.BCStyle

/**
 * A builder class for making X.500 Name objects.
 */
class X500NameBuilder
/**
 * Constructor using a specified style.

 * @param template the style template for string to DN conversion.
 */
@JvmOverloads constructor(private val template: X500NameStyle = BCStyle.INSTANCE) {
    private val rdns = Vector()

    /**
     * Add an RDN based on a single OID and a string representation of its value.

     * @param oid the OID for this RDN.
     * *
     * @param value the string representation of the value the OID refers to.
     * *
     * @return the current builder instance.
     */
    fun addRDN(oid: ASN1ObjectIdentifier, value: String): X500NameBuilder {
        this.addRDN(oid, template.stringToValue(oid, value))

        return this
    }

    /**
     * Add an RDN based on a single OID and an ASN.1 value.

     * @param oid the OID for this RDN.
     * *
     * @param value the ASN.1 value the OID refers to.
     * *
     * @return the current builder instance.
     */
    fun addRDN(oid: ASN1ObjectIdentifier, value: ASN1Encodable): X500NameBuilder {
        rdns.addElement(RDN(oid, value))

        return this
    }

    /**
     * Add an RDN based on the passed in AttributeTypeAndValue.

     * @param attrTAndV the AttributeTypeAndValue to build the RDN from.
     * *
     * @return the current builder instance.
     */
    fun addRDN(attrTAndV: AttributeTypeAndValue): X500NameBuilder {
        rdns.addElement(RDN(attrTAndV))

        return this
    }

    /**
     * Add a multi-valued RDN made up of the passed in OIDs and associated string values.

     * @param oids the OIDs making up the RDN.
     * *
     * @param values the string representation of the values the OIDs refer to.
     * *
     * @return the current builder instance.
     */
    fun addMultiValuedRDN(oids: Array<ASN1ObjectIdentifier>, values: Array<String>): X500NameBuilder {
        val vals = arrayOfNulls<ASN1Encodable>(values.size)

        for (i in vals.indices) {
            vals[i] = template.stringToValue(oids[i], values[i])
        }

        return addMultiValuedRDN(oids, vals)
    }

    /**
     * Add a multi-valued RDN made up of the passed in OIDs and associated ASN.1 values.

     * @param oids the OIDs making up the RDN.
     * *
     * @param values the ASN.1 values the OIDs refer to.
     * *
     * @return the current builder instance.
     */
    fun addMultiValuedRDN(oids: Array<ASN1ObjectIdentifier>, values: Array<ASN1Encodable>): X500NameBuilder {
        val avs = arrayOfNulls<AttributeTypeAndValue>(oids.size)

        for (i in oids.indices) {
            avs[i] = AttributeTypeAndValue(oids[i], values[i])
        }

        return addMultiValuedRDN(avs)
    }

    /**
     * Add an RDN based on the passed in AttributeTypeAndValues.

     * @param attrTAndVs the AttributeTypeAndValues to build the RDN from.
     * *
     * @return the current builder instance.
     */
    fun addMultiValuedRDN(attrTAndVs: Array<AttributeTypeAndValue>): X500NameBuilder {
        rdns.addElement(RDN(attrTAndVs))

        return this
    }

    /**
     * Build an X.500 name for the current builder state.

     * @return a new X.500 name.
     */
    fun build(): X500Name {
        val vals = arrayOfNulls<RDN>(rdns.size)

        for (i in vals.indices) {
            vals[i] = rdns.elementAt(i) as RDN
        }

        return X500Name(template, vals)
    }
}
/**
 * Constructor using the default style (BCStyle).
 */
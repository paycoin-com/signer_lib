package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Class representing the DER-type External
 */
class DERExternal : ASN1Primitive {
    /**
     * Returns the direct reference of the external element
     * @return The reference
     */
    /**
     * Sets the direct reference of the external element
     * @param directReferemce The reference
     */
    var directReference: ASN1ObjectIdentifier? = null
        private set(directReferemce) {
            this.directReference = directReferemce
        }
    /**
     * Returns the indirect reference of this element
     * @return The reference
     */
    /**
     * Sets the indirect reference of this element
     * @param indirectReference The reference
     */
    var indirectReference: ASN1Integer? = null
        private set(indirectReference) {
            this.indirectReference = indirectReference
        }
    /**
     * Returns the data value descriptor
     * @return The descriptor
     */
    /**
     * Sets the data value descriptor
     * @param dataValueDescriptor The descriptor
     */
    var dataValueDescriptor: ASN1Primitive? = null
        private set(dataValueDescriptor) {
            this.dataValueDescriptor = dataValueDescriptor
        }
    /**
     * Returns the encoding of the content. Valid values are
     *
     *  * `0` single-ASN1-type
     *  * `1` OCTET STRING
     *  * `2` BIT STRING
     *
     * @return The encoding
     */
    /**
     * Sets the encoding of the content. Valid values are
     *
     *  * `0` single-ASN1-type
     *  * `1` OCTET STRING
     *  * `2` BIT STRING
     *
     * @param encoding The encoding
     */
    var encoding: Int = 0
        private set(encoding) {
            if (encoding < 0 || encoding > 2) {
                throw IllegalArgumentException("invalid encoding value: " + encoding)
            }
            this.encoding = encoding
        }
    /**
     * Returns the content of this element
     * @return The content
     */
    /**
     * Sets the content of this element
     * @param externalContent The content
     */
    var externalContent: ASN1Primitive? = null
        private set(externalContent) {
            this.externalContent = externalContent
        }

    constructor(vector: ASN1EncodableVector) {
        var offset = 0

        var enc = getObjFromVector(vector, offset)
        if (enc is ASN1ObjectIdentifier) {
            directReference = enc
            offset++
            enc = getObjFromVector(vector, offset)
        }
        if (enc is ASN1Integer) {
            indirectReference = enc
            offset++
            enc = getObjFromVector(vector, offset)
        }
        if (enc !is ASN1TaggedObject) {
            dataValueDescriptor = enc
            offset++
            enc = getObjFromVector(vector, offset)
        }

        if (vector.size() != offset + 1) {
            throw IllegalArgumentException("input vector too large")
        }

        if (enc !is ASN1TaggedObject) {
            throw IllegalArgumentException("No tagged object found in vector. Structure doesn't seem to be of type External")
        }
        encoding = enc.tagNo
        externalContent = enc.`object`
    }

    private fun getObjFromVector(v: ASN1EncodableVector, index: Int): ASN1Primitive {
        if (v.size() <= index) {
            throw IllegalArgumentException("too few objects in input vector")
        }

        return v.get(index).toASN1Primitive()
    }

    /**
     * Creates a new instance of DERExternal
     * See X.690 for more informations about the meaning of these parameters
     * @param directReference The direct reference or `null` if not set.
     * *
     * @param indirectReference The indirect reference or `null` if not set.
     * *
     * @param dataValueDescriptor The data value descriptor or `null` if not set.
     * *
     * @param externalData The external data in its encoded form.
     */
    constructor(directReference: ASN1ObjectIdentifier, indirectReference: ASN1Integer, dataValueDescriptor: ASN1Primitive, externalData: DERTaggedObject) : this(directReference, indirectReference, dataValueDescriptor, externalData.tagNo, externalData.toASN1Primitive()) {
    }

    /**
     * Creates a new instance of DERExternal.
     * See X.690 for more informations about the meaning of these parameters
     * @param directReference The direct reference or `null` if not set.
     * *
     * @param indirectReference The indirect reference or `null` if not set.
     * *
     * @param dataValueDescriptor The data value descriptor or `null` if not set.
     * *
     * @param encoding The encoding to be used for the external data
     * *
     * @param externalData The external data
     */
    constructor(directReference: ASN1ObjectIdentifier, indirectReference: ASN1Integer, dataValueDescriptor: ASN1Primitive, encoding: Int, externalData: ASN1Primitive) {
        directReference = directReference
        indirectReference = indirectReference
        dataValueDescriptor = dataValueDescriptor
        encoding = encoding
        externalContent = externalData.toASN1Primitive()
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    override fun hashCode(): Int {
        var ret = 0
        if (directReference != null) {
            ret = directReference!!.hashCode()
        }
        if (indirectReference != null) {
            ret = ret xor indirectReference!!.hashCode()
        }
        if (dataValueDescriptor != null) {
            ret = ret xor dataValueDescriptor!!.hashCode()
        }
        ret = ret xor externalContent!!.hashCode()
        return ret
    }

    internal override val isConstructed: Boolean
        get() = true

    @Throws(IOException::class)
    internal override fun encodedLength(): Int {
        return this.encoded.size
    }

    /* (non-Javadoc)
     * @see org.bouncycastle.asn1.ASN1Primitive#encode(org.bouncycastle.asn1.DEROutputStream)
     */
    @Throws(IOException::class)
    internal override fun encode(out: ASN1OutputStream) {
        val baos = ByteArrayOutputStream()
        if (directReference != null) {
            baos.write(directReference!!.getEncoded(ASN1Encoding.DER))
        }
        if (indirectReference != null) {
            baos.write(indirectReference!!.getEncoded(ASN1Encoding.DER))
        }
        if (dataValueDescriptor != null) {
            baos.write(dataValueDescriptor!!.getEncoded(ASN1Encoding.DER))
        }
        val obj = DERTaggedObject(true, encoding, externalContent)
        baos.write(obj.getEncoded(ASN1Encoding.DER))
        out.writeEncoded(BERTags.CONSTRUCTED, BERTags.EXTERNAL, baos.toByteArray())
    }

    /* (non-Javadoc)
     * @see org.bouncycastle.asn1.ASN1Primitive#asn1Equals(org.bouncycastle.asn1.ASN1Primitive)
     */
    internal override fun asn1Equals(o: ASN1Primitive): Boolean {
        if (o !is DERExternal) {
            return false
        }
        if (this === o) {
            return true
        }
        if (directReference != null) {
            if (o.directReference == null || o.directReference != directReference) {
                return false
            }
        }
        if (indirectReference != null) {
            if (o.indirectReference == null || o.indirectReference != indirectReference) {
                return false
            }
        }
        if (dataValueDescriptor != null) {
            if (o.dataValueDescriptor == null || o.dataValueDescriptor != dataValueDescriptor) {
                return false
            }
        }
        return externalContent == o.externalContent
    }
}

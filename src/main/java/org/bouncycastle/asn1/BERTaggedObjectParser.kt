package org.bouncycastle.asn1

import java.io.IOException

/**
 * Parser for indefinite-length tagged objects.
 */
class BERTaggedObjectParser internal constructor(
        /**
         * Return true if this tagged object is marked as constructed.

         * @return true if constructed, false otherwise.
         */
        val isConstructed: Boolean,
        /**
         * Return the tag number associated with this object.

         * @return the tag number.
         */
        override val tagNo: Int,
        private val _parser: ASN1StreamParser) : ASN1TaggedObjectParser {

    /**
     * Return an object parser for the contents of this tagged object.

     * @param tag the actual tag number of the object (needed if implicit).
     * *
     * @param isExplicit true if the contained object was explicitly tagged, false if implicit.
     * *
     * @return an ASN.1 encodable object parser.
     * *
     * @throws IOException if there is an issue building the object parser from the stream.
     */
    @Throws(IOException::class)
    override fun getObjectParser(
            tag: Int,
            isExplicit: Boolean): ASN1Encodable {
        if (isExplicit) {
            if (!isConstructed) {
                throw IOException("Explicit tags must be constructed (see X.690 8.14.2)")
            }
            return _parser.readObject()
        }

        return _parser.readImplicit(isConstructed, tag)
    }

    /**
     * Return an in-memory, encodable, representation of the tagged object.

     * @return an ASN1TaggedObject.
     * *
     * @throws IOException if there is an issue loading the data.
     */
    override val loadedObject: ASN1Primitive
        @Throws(IOException::class)
        get() = _parser.readTaggedObject(isConstructed, tagNo)

    /**
     * Return an ASN1TaggedObject representing this parser and its contents.

     * @return an ASN1TaggedObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        try {
            return this.loadedObject
        } catch (e: IOException) {
            throw ASN1ParsingException(e.message)
        }

    }
}
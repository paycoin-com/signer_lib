package org.bouncycastle.asn1

import java.io.IOException

interface ASN1TaggedObjectParser : ASN1Encodable, InMemoryRepresentable {
    val tagNo: Int

    @Throws(IOException::class)
    fun getObjectParser(tag: Int, isExplicit: Boolean): ASN1Encodable
}

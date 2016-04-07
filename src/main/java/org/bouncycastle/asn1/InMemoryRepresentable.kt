package org.bouncycastle.asn1

import java.io.IOException

/**
 * Interface implemented by objects that can be converted from streaming to in-memory objects.
 */
interface InMemoryRepresentable {
    /**
     * Get the in-memory representation of the ASN.1 object.
     * @return an ASN1Primitive representing the loaded object.
     * *
     * @throws IOException for bad input data.
     */
    val loadedObject: ASN1Primitive
}

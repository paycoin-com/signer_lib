package org.bouncycastle.asn1.esf

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 *
 * OtherHash ::= CHOICE {
 * sha1Hash  OtherHashValue, -- This contains a SHA-1 hash
 * otherHash  OtherHashAlgAndValue
 * }
 *
 */
class OtherHash : ASN1Object, ASN1Choice {

    private var sha1Hash: ASN1OctetString? = null
    private val otherHash: OtherHashAlgAndValue?

    private constructor(sha1Hash: ASN1OctetString) {
        this.sha1Hash = sha1Hash
    }

    constructor(otherHash: OtherHashAlgAndValue) {
        this.otherHash = otherHash
    }

    constructor(sha1Hash: ByteArray) {
        this.sha1Hash = DEROctetString(sha1Hash)
    }

    val hashAlgorithm: AlgorithmIdentifier
        get() {
            if (null == this.otherHash) {
                return AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1)
            }
            return this.otherHash.hashAlgorithm
        }

    val hashValue: ByteArray
        get() {
            if (null == this.otherHash) {
                return this.sha1Hash!!.octets
            }
            return this.otherHash.hashValue.octets
        }

    override fun toASN1Primitive(): ASN1Primitive {
        if (null == this.otherHash) {
            return this.sha1Hash
        }
        return this.otherHash.toASN1Primitive()
    }

    companion object {

        fun getInstance(obj: Any): OtherHash {
            if (obj is OtherHash) {
                return obj
            }
            if (obj is ASN1OctetString) {
                return OtherHash(obj)
            }
            return OtherHash(OtherHashAlgAndValue.getInstance(obj))
        }
    }
}

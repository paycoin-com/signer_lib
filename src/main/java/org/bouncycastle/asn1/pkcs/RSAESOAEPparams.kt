package org.bouncycastle.asn1.pkcs

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class RSAESOAEPparams : ASN1Object {
    var hashAlgorithm: AlgorithmIdentifier? = null
        private set
    var maskGenAlgorithm: AlgorithmIdentifier? = null
        private set
    var pSourceAlgorithm: AlgorithmIdentifier? = null
        private set

    /**
     * The default version
     */
    constructor() {
        hashAlgorithm = DEFAULT_HASH_ALGORITHM
        maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION
        pSourceAlgorithm = DEFAULT_P_SOURCE_ALGORITHM
    }

    constructor(
            hashAlgorithm: AlgorithmIdentifier,
            maskGenAlgorithm: AlgorithmIdentifier,
            pSourceAlgorithm: AlgorithmIdentifier) {
        this.hashAlgorithm = hashAlgorithm
        this.maskGenAlgorithm = maskGenAlgorithm
        this.pSourceAlgorithm = pSourceAlgorithm
    }

    /**
     * @param seq
     */
    @Deprecated("use getInstance()\n      ")
    constructor(
            seq: ASN1Sequence) {
        hashAlgorithm = DEFAULT_HASH_ALGORITHM
        maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION
        pSourceAlgorithm = DEFAULT_P_SOURCE_ALGORITHM

        for (i in 0..seq.size() - 1) {
            val o = seq.getObjectAt(i) as ASN1TaggedObject

            when (o.tagNo) {
                0 -> hashAlgorithm = AlgorithmIdentifier.getInstance(o, true)
                1 -> maskGenAlgorithm = AlgorithmIdentifier.getInstance(o, true)
                2 -> pSourceAlgorithm = AlgorithmIdentifier.getInstance(o, true)
                else -> throw IllegalArgumentException("unknown tag")
            }
        }
    }

    /**
     *
     * RSAES-OAEP-params ::= SEQUENCE {
     * hashAlgorithm      [0] OAEP-PSSDigestAlgorithms     DEFAULT sha1,
     * maskGenAlgorithm   [1] PKCS1MGFAlgorithms  DEFAULT mgf1SHA1,
     * pSourceAlgorithm   [2] PKCS1PSourceAlgorithms  DEFAULT pSpecifiedEmpty
     * }

     * OAEP-PSSDigestAlgorithms    ALGORITHM-IDENTIFIER ::= {
     * { OID id-sha1 PARAMETERS NULL   }|
     * { OID id-sha256 PARAMETERS NULL }|
     * { OID id-sha384 PARAMETERS NULL }|
     * { OID id-sha512 PARAMETERS NULL },
     * ...  -- Allows for future expansion --
     * }
     * PKCS1MGFAlgorithms    ALGORITHM-IDENTIFIER ::= {
     * { OID id-mgf1 PARAMETERS OAEP-PSSDigestAlgorithms },
     * ...  -- Allows for future expansion --
     * }
     * PKCS1PSourceAlgorithms    ALGORITHM-IDENTIFIER ::= {
     * { OID id-pSpecified PARAMETERS OCTET STRING },
     * ...  -- Allows for future expansion --
     * }
     *
     * @return the asn1 primitive representing the parameters.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (hashAlgorithm != DEFAULT_HASH_ALGORITHM) {
            v.add(DERTaggedObject(true, 0, hashAlgorithm))
        }

        if (maskGenAlgorithm != DEFAULT_MASK_GEN_FUNCTION) {
            v.add(DERTaggedObject(true, 1, maskGenAlgorithm))
        }

        if (pSourceAlgorithm != DEFAULT_P_SOURCE_ALGORITHM) {
            v.add(DERTaggedObject(true, 2, pSourceAlgorithm))
        }

        return DERSequence(v)
    }

    companion object {

        val DEFAULT_HASH_ALGORITHM = AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE)
        val DEFAULT_MASK_GEN_FUNCTION = AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, DEFAULT_HASH_ALGORITHM)
        val DEFAULT_P_SOURCE_ALGORITHM = AlgorithmIdentifier(PKCSObjectIdentifiers.id_pSpecified, DEROctetString(ByteArray(0)))

        fun getInstance(
                obj: Any?): RSAESOAEPparams? {
            if (obj is RSAESOAEPparams) {
                return obj
            } else if (obj != null) {
                return RSAESOAEPparams(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

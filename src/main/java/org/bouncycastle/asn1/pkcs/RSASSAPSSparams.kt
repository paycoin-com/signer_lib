package org.bouncycastle.asn1.pkcs

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class RSASSAPSSparams : ASN1Object {
    var hashAlgorithm: AlgorithmIdentifier? = null
        private set
    var maskGenAlgorithm: AlgorithmIdentifier? = null
        private set
    private var saltLength: ASN1Integer? = null
    private var trailerField: ASN1Integer? = null

    /**
     * The default version
     */
    constructor() {
        hashAlgorithm = DEFAULT_HASH_ALGORITHM
        maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION
        saltLength = DEFAULT_SALT_LENGTH
        trailerField = DEFAULT_TRAILER_FIELD
    }

    constructor(
            hashAlgorithm: AlgorithmIdentifier,
            maskGenAlgorithm: AlgorithmIdentifier,
            saltLength: ASN1Integer,
            trailerField: ASN1Integer) {
        this.hashAlgorithm = hashAlgorithm
        this.maskGenAlgorithm = maskGenAlgorithm
        this.saltLength = saltLength
        this.trailerField = trailerField
    }

    private constructor(
            seq: ASN1Sequence) {
        hashAlgorithm = DEFAULT_HASH_ALGORITHM
        maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION
        saltLength = DEFAULT_SALT_LENGTH
        trailerField = DEFAULT_TRAILER_FIELD

        for (i in 0..seq.size() - 1) {
            val o = seq.getObjectAt(i) as ASN1TaggedObject

            when (o.tagNo) {
                0 -> hashAlgorithm = AlgorithmIdentifier.getInstance(o, true)
                1 -> maskGenAlgorithm = AlgorithmIdentifier.getInstance(o, true)
                2 -> saltLength = ASN1Integer.getInstance(o, true)
                3 -> trailerField = ASN1Integer.getInstance(o, true)
                else -> throw IllegalArgumentException("unknown tag")
            }
        }
    }

    fun getSaltLength(): BigInteger {
        return saltLength!!.value
    }

    fun getTrailerField(): BigInteger {
        return trailerField!!.value
    }

    /**
     *
     * RSASSA-PSS-params ::= SEQUENCE {
     * hashAlgorithm      [0] OAEP-PSSDigestAlgorithms  DEFAULT sha1,
     * maskGenAlgorithm   [1] PKCS1MGFAlgorithms  DEFAULT mgf1SHA1,
     * saltLength         [2] INTEGER  DEFAULT 20,
     * trailerField       [3] TrailerField  DEFAULT trailerFieldBC
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

     * TrailerField ::= INTEGER { trailerFieldBC(1) }
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

        if (saltLength != DEFAULT_SALT_LENGTH) {
            v.add(DERTaggedObject(true, 2, saltLength))
        }

        if (trailerField != DEFAULT_TRAILER_FIELD) {
            v.add(DERTaggedObject(true, 3, trailerField))
        }

        return DERSequence(v)
    }

    companion object {

        val DEFAULT_HASH_ALGORITHM = AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE)
        val DEFAULT_MASK_GEN_FUNCTION = AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, DEFAULT_HASH_ALGORITHM)
        val DEFAULT_SALT_LENGTH = ASN1Integer(20)
        val DEFAULT_TRAILER_FIELD = ASN1Integer(1)

        fun getInstance(
                obj: Any?): RSASSAPSSparams? {
            if (obj is RSASSAPSSparams) {
                return obj
            } else if (obj != null) {
                return RSASSAPSSparams(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

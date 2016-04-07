package org.bouncycastle.asn1.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence

/**
 * The DigestInfo object.
 *
 * DigestInfo::=SEQUENCE{
 * digestAlgorithm  AlgorithmIdentifier,
 * digest OCTET STRING }
 *
 */
class DigestInfo : ASN1Object {
    var digest: ByteArray? = null
        private set
    var algorithmId: AlgorithmIdentifier? = null
        private set

    constructor(
            algId: AlgorithmIdentifier,
            digest: ByteArray) {
        this.digest = digest
        this.algorithmId = algId
    }

    constructor(
            obj: ASN1Sequence) {
        val e = obj.objects

        algorithmId = AlgorithmIdentifier.getInstance(e.nextElement())
        digest = ASN1OctetString.getInstance(e.nextElement()).octets
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(algorithmId)
        v.add(DEROctetString(digest))

        return DERSequence(v)
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): DigestInfo {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): DigestInfo? {
            if (obj is DigestInfo) {
                return obj
            } else if (obj != null) {
                return DigestInfo(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

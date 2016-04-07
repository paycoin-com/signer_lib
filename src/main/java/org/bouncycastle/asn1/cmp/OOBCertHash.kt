package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.crmf.CertId
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class OOBCertHash : ASN1Object {
    var hashAlg: AlgorithmIdentifier? = null
        private set
    var certId: CertId? = null
        private set
    var hashVal: DERBitString? = null
        private set

    private constructor(seq: ASN1Sequence) {
        var index = seq.size() - 1

        hashVal = DERBitString.getInstance(seq.getObjectAt(index--))

        for (i in index downTo 0) {
            val tObj = seq.getObjectAt(i) as ASN1TaggedObject

            if (tObj.tagNo == 0) {
                hashAlg = AlgorithmIdentifier.getInstance(tObj, true)
            } else {
                certId = CertId.getInstance(tObj, true)
            }
        }

    }

    constructor(hashAlg: AlgorithmIdentifier, certId: CertId, hashVal: ByteArray) : this(hashAlg, certId, DERBitString(hashVal)) {
    }

    constructor(hashAlg: AlgorithmIdentifier, certId: CertId, hashVal: DERBitString) {
        this.hashAlg = hashAlg
        this.certId = certId
        this.hashVal = hashVal
    }

    /**
     *
     * OOBCertHash ::= SEQUENCE {
     * hashAlg     [0] AlgorithmIdentifier     OPTIONAL,
     * certId      [1] CertId                  OPTIONAL,
     * hashVal         BIT STRING
     * -- hashVal is calculated over the DER encoding of the
     * -- self-signed certificate with the identifier certID.
     * }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        addOptional(v, 0, hashAlg)
        addOptional(v, 1, certId)

        v.add(hashVal)

        return DERSequence(v)
    }

    private fun addOptional(v: ASN1EncodableVector, tagNo: Int, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(DERTaggedObject(true, tagNo, obj))
        }
    }

    companion object {

        fun getInstance(o: Any?): OOBCertHash? {
            if (o is OOBCertHash) {
                return o
            }

            if (o != null) {
                return OOBCertHash(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

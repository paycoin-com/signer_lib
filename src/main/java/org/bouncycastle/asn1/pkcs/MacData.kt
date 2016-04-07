package org.bouncycastle.asn1.pkcs

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.DigestInfo

class MacData : ASN1Object {

    var mac: DigestInfo
        internal set
    var salt: ByteArray
        internal set
    var iterationCount: BigInteger
        internal set

    private constructor(
            seq: ASN1Sequence) {
        this.mac = DigestInfo.getInstance(seq.getObjectAt(0))

        this.salt = (seq.getObjectAt(1) as ASN1OctetString).octets

        if (seq.size() == 3) {
            this.iterationCount = (seq.getObjectAt(2) as ASN1Integer).value
        } else {
            this.iterationCount = ONE
        }
    }

    constructor(
            digInfo: DigestInfo,
            salt: ByteArray,
            iterationCount: Int) {
        this.mac = digInfo
        this.salt = salt
        this.iterationCount = BigInteger.valueOf(iterationCount.toLong())
    }

    /**
     *
     * MacData ::= SEQUENCE {
     * mac      DigestInfo,
     * macSalt  OCTET STRING,
     * iterations INTEGER DEFAULT 1
     * -- Note: The default is for historic reasons and its use is deprecated. A
     * -- higher value, like 1024 is recommended.
     *
     * @return the basic ASN1Primitive construction.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(mac)
        v.add(DEROctetString(salt))

        if (iterationCount != ONE) {
            v.add(ASN1Integer(iterationCount))
        }

        return DERSequence(v)
    }

    companion object {
        private val ONE = BigInteger.valueOf(1)

        fun getInstance(
                obj: Any?): MacData? {
            if (obj is MacData) {
                return obj
            } else if (obj != null) {
                return MacData(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

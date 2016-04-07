package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

class PBMParameter : ASN1Object {
    var salt: ASN1OctetString? = null
        private set
    var owf: AlgorithmIdentifier? = null
        private set
    var iterationCount: ASN1Integer? = null
        private set
    var mac: AlgorithmIdentifier? = null
        private set

    private constructor(seq: ASN1Sequence) {
        salt = ASN1OctetString.getInstance(seq.getObjectAt(0))
        owf = AlgorithmIdentifier.getInstance(seq.getObjectAt(1))
        iterationCount = ASN1Integer.getInstance(seq.getObjectAt(2))
        mac = AlgorithmIdentifier.getInstance(seq.getObjectAt(3))
    }

    constructor(
            salt: ByteArray,
            owf: AlgorithmIdentifier,
            iterationCount: Int,
            mac: AlgorithmIdentifier) : this(DEROctetString(salt), owf,
            ASN1Integer(iterationCount.toLong()), mac) {
    }

    constructor(
            salt: ASN1OctetString,
            owf: AlgorithmIdentifier,
            iterationCount: ASN1Integer,
            mac: AlgorithmIdentifier) {
        this.salt = salt
        this.owf = owf
        this.iterationCount = iterationCount
        this.mac = mac
    }

    /**
     *
     * PBMParameter ::= SEQUENCE {
     * salt                OCTET STRING,
     * -- note:  implementations MAY wish to limit acceptable sizes
     * -- of this string to values appropriate for their environment
     * -- in order to reduce the risk of denial-of-service attacks
     * owf                 AlgorithmIdentifier,
     * -- AlgId for a One-Way Function (SHA-1 recommended)
     * iterationCount      INTEGER,
     * -- number of times the OWF is applied
     * -- note:  implementations MAY wish to limit acceptable sizes
     * -- of this integer to values appropriate for their environment
     * -- in order to reduce the risk of denial-of-service attacks
     * mac                 AlgorithmIdentifier
     * -- the MAC AlgId (e.g., DES-MAC, Triple-DES-MAC [PKCS11],
     * }   -- or HMAC [RFC2104, RFC2202])
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(salt)
        v.add(owf)
        v.add(iterationCount)
        v.add(mac)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): PBMParameter? {
            if (o is PBMParameter) {
                return o
            }

            if (o != null) {
                return PBMParameter(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

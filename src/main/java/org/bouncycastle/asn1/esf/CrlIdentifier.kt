package org.bouncycastle.asn1.esf

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1UTCTime
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500Name

/**
 *
 * CrlIdentifier ::= SEQUENCE
 * {
 * crlissuer    Name,
 * crlIssuedTime  UTCTime,
 * crlNumber    INTEGER OPTIONAL
 * }
 *
 */
class CrlIdentifier : ASN1Object {
    var crlIssuer: X500Name? = null
        private set
    var crlIssuedTime: ASN1UTCTime? = null
        private set
    private var crlNumber: ASN1Integer? = null

    private constructor(seq: ASN1Sequence) {
        if (seq.size() < 2 || seq.size() > 3) {
            throw IllegalArgumentException()
        }
        this.crlIssuer = X500Name.getInstance(seq.getObjectAt(0))
        this.crlIssuedTime = ASN1UTCTime.getInstance(seq.getObjectAt(1))
        if (seq.size() > 2) {
            this.crlNumber = ASN1Integer.getInstance(seq.getObjectAt(2))
        }
    }

    @JvmOverloads constructor(crlIssuer: X500Name, crlIssuedTime: ASN1UTCTime,
                              crlNumber: BigInteger? = null) {
        this.crlIssuer = crlIssuer
        this.crlIssuedTime = crlIssuedTime
        if (null != crlNumber) {
            this.crlNumber = ASN1Integer(crlNumber)
        }
    }

    fun getCrlNumber(): BigInteger? {
        if (null == this.crlNumber) {
            return null
        }
        return this.crlNumber!!.value
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(this.crlIssuer!!.toASN1Primitive())
        v.add(this.crlIssuedTime)
        if (null != this.crlNumber) {
            v.add(this.crlNumber)
        }
        return DERSequence(v)
    }

    companion object {

        fun getInstance(obj: Any?): CrlIdentifier? {
            if (obj is CrlIdentifier) {
                return obj
            } else if (obj != null) {
                return CrlIdentifier(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }

}

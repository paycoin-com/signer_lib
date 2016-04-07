package org.bouncycastle.asn1.ess

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.PolicyInformation

class OtherSigningCertificate : ASN1Object {
    internal var certs: ASN1Sequence
    internal var policies: ASN1Sequence? = null

    /**
     * constructeurs
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        this.certs = ASN1Sequence.getInstance(seq.getObjectAt(0))

        if (seq.size() > 1) {
            this.policies = ASN1Sequence.getInstance(seq.getObjectAt(1))
        }
    }

    constructor(
            otherCertID: OtherCertID) {
        certs = DERSequence(otherCertID)
    }

    fun getCerts(): Array<OtherCertID> {
        val cs = arrayOfNulls<OtherCertID>(certs.size())

        for (i in 0..certs.size() - 1) {
            cs[i] = OtherCertID.getInstance(certs.getObjectAt(i))
        }

        return cs
    }

    fun getPolicies(): Array<PolicyInformation>? {
        if (policies == null) {
            return null
        }

        val ps = arrayOfNulls<PolicyInformation>(policies!!.size())

        for (i in 0..policies!!.size() - 1) {
            ps[i] = PolicyInformation.getInstance(policies!!.getObjectAt(i))
        }

        return ps
    }

    /**
     * The definition of OtherSigningCertificate is
     *
     * OtherSigningCertificate ::=  SEQUENCE {
     * certs        SEQUENCE OF OtherCertID,
     * policies     SEQUENCE OF PolicyInformation OPTIONAL
     * }
     *
     * id-aa-ets-otherSigCert OBJECT IDENTIFIER ::= { iso(1)
     * member-body(2) us(840) rsadsi(113549) pkcs(1) pkcs9(9)
     * smime(16) id-aa(2) 19 }
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(certs)

        if (policies != null) {
            v.add(policies)
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): OtherSigningCertificate? {
            if (o is OtherSigningCertificate) {
                return o
            } else if (o != null) {
                return OtherSigningCertificate(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

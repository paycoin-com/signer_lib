package org.bouncycastle.asn1.ess

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.PolicyInformation


class SigningCertificate : ASN1Object {
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
            essCertID: ESSCertID) {
        certs = DERSequence(essCertID)
    }

    fun getCerts(): Array<ESSCertID> {
        val cs = arrayOfNulls<ESSCertID>(certs.size())

        for (i in 0..certs.size() - 1) {
            cs[i] = ESSCertID.getInstance(certs.getObjectAt(i))
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
     * The definition of SigningCertificate is
     *
     * SigningCertificate ::=  SEQUENCE {
     * certs        SEQUENCE OF ESSCertID,
     * policies     SEQUENCE OF PolicyInformation OPTIONAL
     * }
     *
     * id-aa-signingCertificate OBJECT IDENTIFIER ::= { iso(1)
     * member-body(2) us(840) rsadsi(113549) pkcs(1) pkcs9(9)
     * smime(16) id-aa(2) 12 }
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

        fun getInstance(o: Any?): SigningCertificate? {
            if (o is SigningCertificate) {
                return o
            } else if (o != null) {
                return SigningCertificate(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

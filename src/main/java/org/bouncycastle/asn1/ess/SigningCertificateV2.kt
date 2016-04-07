package org.bouncycastle.asn1.ess

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.PolicyInformation

class SigningCertificateV2 : ASN1Object {
    internal var certs: ASN1Sequence
    internal var policies: ASN1Sequence? = null

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        this.certs = ASN1Sequence.getInstance(seq.getObjectAt(0))

        if (seq.size() > 1) {
            this.policies = ASN1Sequence.getInstance(seq.getObjectAt(1))
        }
    }

    constructor(
            cert: ESSCertIDv2) {
        this.certs = DERSequence(cert)
    }

    constructor(
            certs: Array<ESSCertIDv2>) {
        val v = ASN1EncodableVector()
        for (i in certs.indices) {
            v.add(certs[i])
        }
        this.certs = DERSequence(v)
    }

    constructor(
            certs: Array<ESSCertIDv2>,
            policies: Array<PolicyInformation>?) {
        var v = ASN1EncodableVector()
        for (i in certs.indices) {
            v.add(certs[i])
        }
        this.certs = DERSequence(v)

        if (policies != null) {
            v = ASN1EncodableVector()
            for (i in policies.indices) {
                v.add(policies[i])
            }
            this.policies = DERSequence(v)
        }
    }

    fun getCerts(): Array<ESSCertIDv2> {
        val certIds = arrayOfNulls<ESSCertIDv2>(certs.size())
        for (i in 0..certs.size() - 1) {
            certIds[i] = ESSCertIDv2.getInstance(certs.getObjectAt(i))
        }
        return certIds
    }

    fun getPolicies(): Array<PolicyInformation>? {
        if (policies == null) {
            return null
        }

        val policyInformations = arrayOfNulls<PolicyInformation>(policies!!.size())
        for (i in 0..policies!!.size() - 1) {
            policyInformations[i] = PolicyInformation.getInstance(policies!!.getObjectAt(i))
        }
        return policyInformations
    }

    /**
     * The definition of SigningCertificateV2 is
     *
     * SigningCertificateV2 ::=  SEQUENCE {
     * certs        SEQUENCE OF ESSCertIDv2,
     * policies     SEQUENCE OF PolicyInformation OPTIONAL
     * }
     *
     * id-aa-signingCertificateV2 OBJECT IDENTIFIER ::= { iso(1)
     * member-body(2) us(840) rsadsi(113549) pkcs(1) pkcs9(9)
     * smime(16) id-aa(2) 47 }
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

        fun getInstance(
                o: Any?): SigningCertificateV2? {
            if (o == null || o is SigningCertificateV2) {
                return o as SigningCertificateV2?
            } else if (o is ASN1Sequence) {
                return SigningCertificateV2(o as ASN1Sequence?)
            }

            return null
        }
    }
}

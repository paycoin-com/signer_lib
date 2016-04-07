package org.bouncycastle.asn1.crmf

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo

class CertTemplate private constructor(private val seq: ASN1Sequence) : ASN1Object() {

    private var version: ASN1Integer? = null
    var serialNumber: ASN1Integer? = null
        private set
    var signingAlg: AlgorithmIdentifier? = null
        private set
    var issuer: X500Name? = null
        private set
    var validity: OptionalValidity? = null
        private set
    var subject: X500Name? = null
        private set
    var publicKey: SubjectPublicKeyInfo? = null
        private set
    var issuerUID: DERBitString? = null
        private set
    var subjectUID: DERBitString? = null
        private set
    var extensions: Extensions? = null
        private set

    init {

        val en = seq.objects
        while (en.hasMoreElements()) {
            val tObj = en.nextElement() as ASN1TaggedObject

            when (tObj.tagNo) {
                0 -> version = ASN1Integer.getInstance(tObj, false)
                1 -> serialNumber = ASN1Integer.getInstance(tObj, false)
                2 -> signingAlg = AlgorithmIdentifier.getInstance(tObj, false)
                3 -> issuer = X500Name.getInstance(tObj, true) // CHOICE
                4 -> validity = OptionalValidity.getInstance(ASN1Sequence.getInstance(tObj, false))
                5 -> subject = X500Name.getInstance(tObj, true) // CHOICE
                6 -> publicKey = SubjectPublicKeyInfo.getInstance(tObj, false)
                7 -> issuerUID = DERBitString.getInstance(tObj, false)
                8 -> subjectUID = DERBitString.getInstance(tObj, false)
                9 -> extensions = Extensions.getInstance(tObj, false)
                else -> throw IllegalArgumentException("unknown tag: " + tObj.tagNo)
            }
        }
    }

    fun getVersion(): Int {
        return version!!.value.toInt()
    }

    /**
     *
     * CertTemplate ::= SEQUENCE {
     * version      [0] Version               OPTIONAL,
     * serialNumber [1] INTEGER               OPTIONAL,
     * signingAlg   [2] AlgorithmIdentifier   OPTIONAL,
     * issuer       [3] Name                  OPTIONAL,
     * validity     [4] OptionalValidity      OPTIONAL,
     * subject      [5] Name                  OPTIONAL,
     * publicKey    [6] SubjectPublicKeyInfo  OPTIONAL,
     * issuerUID    [7] UniqueIdentifier      OPTIONAL,
     * subjectUID   [8] UniqueIdentifier      OPTIONAL,
     * extensions   [9] Extensions            OPTIONAL }
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return seq
    }

    companion object {

        fun getInstance(o: Any?): CertTemplate? {
            if (o is CertTemplate) {
                return o
            } else if (o != null) {
                return CertTemplate(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

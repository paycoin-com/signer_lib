package org.bouncycastle.asn1.isismtt.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.GeneralName

/**
 * An Admissions structure.
 *
 * Admissions ::= SEQUENCE
 * {
 * admissionAuthority [0] EXPLICIT GeneralName OPTIONAL
 * namingAuthority [1] EXPLICIT NamingAuthority OPTIONAL
 * professionInfos SEQUENCE OF ProfessionInfo
 * }
 *

 * @see org.bouncycastle.asn1.isismtt.x509.AdmissionSyntax

 * @see org.bouncycastle.asn1.isismtt.x509.ProfessionInfo

 * @see org.bouncycastle.asn1.isismtt.x509.NamingAuthority
 */
class Admissions : ASN1Object {

    var admissionAuthority: GeneralName? = null
        private set

    var namingAuthority: NamingAuthority? = null
        private set

    private var professionInfos: ASN1Sequence? = null

    /**
     * Constructor from ASN1Sequence.
     *
     *
     * The sequence is of type ProcurationSyntax:
     *
     * Admissions ::= SEQUENCE
     * {
     * admissionAuthority [0] EXPLICIT GeneralName OPTIONAL
     * namingAuthority [1] EXPLICIT NamingAuthority OPTIONAL
     * professionInfos SEQUENCE OF ProfessionInfo
     * }
     *
     *
     * @param seq The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() > 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }
        val e = seq.objects

        var o = e.nextElement() as ASN1Encodable
        if (o is ASN1TaggedObject) {
            when (o.tagNo) {
                0 -> admissionAuthority = GeneralName.getInstance(o, true)
                1 -> namingAuthority = NamingAuthority.getInstance(o, true)
                else -> throw IllegalArgumentException("Bad tag number: " + o.tagNo)
            }
            o = e.nextElement() as ASN1Encodable
        }
        if (o is ASN1TaggedObject) {
            when (o.tagNo) {
                1 -> namingAuthority = NamingAuthority.getInstance(o, true)
                else -> throw IllegalArgumentException("Bad tag number: " + o.tagNo)
            }
            o = e.nextElement() as ASN1Encodable
        }
        professionInfos = ASN1Sequence.getInstance(o)
        if (e.hasMoreElements()) {
            throw IllegalArgumentException("Bad object encountered: " + e.nextElement().javaClass)
        }
    }

    /**
     * Constructor from a given details.
     *
     *
     * Parameter `professionInfos` is mandatory.

     * @param admissionAuthority The admission authority.
     * *
     * @param namingAuthority    The naming authority.
     * *
     * @param professionInfos    The profession infos.
     */
    constructor(admissionAuthority: GeneralName,
                namingAuthority: NamingAuthority, professionInfos: Array<ProfessionInfo>) {
        this.admissionAuthority = admissionAuthority
        this.namingAuthority = namingAuthority
        this.professionInfos = DERSequence(professionInfos)
    }

    fun getProfessionInfos(): Array<ProfessionInfo> {
        val infos = arrayOfNulls<ProfessionInfo>(professionInfos!!.size())
        var count = 0
        val e = professionInfos!!.objects
        while (e.hasMoreElements()) {
            infos[count++] = ProfessionInfo.getInstance(e.nextElement())
        }
        return infos
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * Admissions ::= SEQUENCE
     * {
     * admissionAuthority [0] EXPLICIT GeneralName OPTIONAL
     * namingAuthority [1] EXPLICIT NamingAuthority OPTIONAL
     * professionInfos SEQUENCE OF ProfessionInfo
     * }
     *

     * @return an ASN1Primitive
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()

        if (admissionAuthority != null) {
            vec.add(DERTaggedObject(true, 0, admissionAuthority))
        }
        if (namingAuthority != null) {
            vec.add(DERTaggedObject(true, 1, namingAuthority))
        }
        vec.add(professionInfos)

        return DERSequence(vec)
    }

    companion object {

        fun getInstance(obj: Any?): Admissions {
            if (obj == null || obj is Admissions) {
                return obj as Admissions?
            }

            if (obj is ASN1Sequence) {
                return Admissions(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }
    }
}

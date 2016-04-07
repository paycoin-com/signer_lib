package org.bouncycastle.asn1.isismtt.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.DirectoryString

/**
 * Professions, specializations, disciplines, fields of activity, etc.

 *
 * ProfessionInfo ::= SEQUENCE
 * {
 * namingAuthority [0] EXPLICIT NamingAuthority OPTIONAL,
 * professionItems SEQUENCE OF DirectoryString (SIZE(1..128)),
 * professionOIDs SEQUENCE OF OBJECT IDENTIFIER OPTIONAL,
 * registrationNumber PrintableString(SIZE(1..128)) OPTIONAL,
 * addProfessionInfo OCTET STRING OPTIONAL
 * }
 *

 * @see org.bouncycastle.asn1.isismtt.x509.AdmissionSyntax
 */
class ProfessionInfo : ASN1Object {

    /**
     * @return Returns the namingAuthority.
     */
    var namingAuthority: NamingAuthority? = null
        private set

    private var professionItems: ASN1Sequence? = null

    private var professionOIDs: ASN1Sequence? = null

    /**
     * @return Returns the registrationNumber.
     */
    var registrationNumber: String? = null
        private set

    /**
     * @return Returns the addProfessionInfo.
     */
    var addProfessionInfo: ASN1OctetString? = null
        private set

    /**
     * Constructor from ASN1Sequence.
     *
     * ProfessionInfo ::= SEQUENCE
     * {
     * namingAuthority [0] EXPLICIT NamingAuthority OPTIONAL,
     * professionItems SEQUENCE OF DirectoryString (SIZE(1..128)),
     * professionOIDs SEQUENCE OF OBJECT IDENTIFIER OPTIONAL,
     * registrationNumber PrintableString(SIZE(1..128)) OPTIONAL,
     * addProfessionInfo OCTET STRING OPTIONAL
     * }
     *

     * @param seq The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {
        if (seq.size() > 5) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        val e = seq.objects

        var o = e.nextElement() as ASN1Encodable

        if (o is ASN1TaggedObject) {
            if (o.tagNo != 0) {
                throw IllegalArgumentException("Bad tag number: " + o.tagNo)
            }
            namingAuthority = NamingAuthority.getInstance(o, true)
            o = e.nextElement() as ASN1Encodable
        }

        professionItems = ASN1Sequence.getInstance(o)

        if (e.hasMoreElements()) {
            o = e.nextElement() as ASN1Encodable
            if (o is ASN1Sequence) {
                professionOIDs = ASN1Sequence.getInstance(o)
            } else if (o is DERPrintableString) {
                registrationNumber = DERPrintableString.getInstance(o).string
            } else if (o is ASN1OctetString) {
                addProfessionInfo = ASN1OctetString.getInstance(o)
            } else {
                throw IllegalArgumentException("Bad object encountered: " + o.javaClass)
            }
        }
        if (e.hasMoreElements()) {
            o = e.nextElement() as ASN1Encodable
            if (o is DERPrintableString) {
                registrationNumber = DERPrintableString.getInstance(o).string
            } else if (o is DEROctetString) {
                addProfessionInfo = o
            } else {
                throw IllegalArgumentException("Bad object encountered: " + o.javaClass)
            }
        }
        if (e.hasMoreElements()) {
            o = e.nextElement() as ASN1Encodable
            if (o is DEROctetString) {
                addProfessionInfo = o
            } else {
                throw IllegalArgumentException("Bad object encountered: " + o.javaClass)
            }
        }

    }

    /**
     * Constructor from given details.
     *
     *
     * `professionItems` is mandatory, all other parameters are
     * optional.

     * @param namingAuthority    The naming authority.
     * *
     * @param professionItems    Directory strings of the profession.
     * *
     * @param professionOIDs     DERObjectIdentfier objects for the
     * *                           profession.
     * *
     * @param registrationNumber Registration number.
     * *
     * @param addProfessionInfo  Additional infos in encoded form.
     */
    constructor(namingAuthority: NamingAuthority,
                professionItems: Array<DirectoryString>, professionOIDs: Array<ASN1ObjectIdentifier>?,
                registrationNumber: String, addProfessionInfo: ASN1OctetString) {
        this.namingAuthority = namingAuthority
        var v = ASN1EncodableVector()
        for (i in professionItems.indices) {
            v.add(professionItems[i])
        }
        this.professionItems = DERSequence(v)
        if (professionOIDs != null) {
            v = ASN1EncodableVector()
            for (i in professionOIDs.indices) {
                v.add(professionOIDs[i])
            }
            this.professionOIDs = DERSequence(v)
        }
        this.registrationNumber = registrationNumber
        this.addProfessionInfo = addProfessionInfo
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * ProfessionInfo ::= SEQUENCE
     * {
     * namingAuthority [0] EXPLICIT NamingAuthority OPTIONAL,
     * professionItems SEQUENCE OF DirectoryString (SIZE(1..128)),
     * professionOIDs SEQUENCE OF OBJECT IDENTIFIER OPTIONAL,
     * registrationNumber PrintableString(SIZE(1..128)) OPTIONAL,
     * addProfessionInfo OCTET STRING OPTIONAL
     * }
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()
        if (namingAuthority != null) {
            vec.add(DERTaggedObject(true, 0, namingAuthority))
        }
        vec.add(professionItems)
        if (professionOIDs != null) {
            vec.add(professionOIDs)
        }
        if (registrationNumber != null) {
            vec.add(DERPrintableString(registrationNumber, true))
        }
        if (addProfessionInfo != null) {
            vec.add(addProfessionInfo)
        }
        return DERSequence(vec)
    }

    /**
     * @return Returns the professionItems.
     */
    fun getProfessionItems(): Array<DirectoryString> {
        val items = arrayOfNulls<DirectoryString>(professionItems!!.size())
        var count = 0
        val e = professionItems!!.objects
        while (e.hasMoreElements()) {
            items[count++] = DirectoryString.getInstance(e.nextElement())
        }
        return items
    }

    /**
     * @return Returns the professionOIDs.
     */
    fun getProfessionOIDs(): Array<ASN1ObjectIdentifier> {
        if (professionOIDs == null) {
            return arrayOfNulls(0)
        }
        val oids = arrayOfNulls<ASN1ObjectIdentifier>(professionOIDs!!.size())
        var count = 0
        val e = professionOIDs!!.objects
        while (e.hasMoreElements()) {
            oids[count++] = ASN1ObjectIdentifier.getInstance(e.nextElement())
        }
        return oids
    }

    companion object {

        /**
         * Rechtsanw�ltin
         */
        val Rechtsanwltin = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".1")

        /**
         * Rechtsanwalt
         */
        val Rechtsanwalt = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".2")

        /**
         * Rechtsbeistand
         */
        val Rechtsbeistand = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".3")

        /**
         * Steuerberaterin
         */
        val Steuerberaterin = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".4")

        /**
         * Steuerberater
         */
        val Steuerberater = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".5")

        /**
         * Steuerbevollm�chtigte
         */
        val Steuerbevollmchtigte = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".6")

        /**
         * Steuerbevollm�chtigter
         */
        val Steuerbevollmchtigter = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".7")

        /**
         * Notarin
         */
        val Notarin = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".8")

        /**
         * Notar
         */
        val Notar = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".9")

        /**
         * Notarvertreterin
         */
        val Notarvertreterin = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".10")

        /**
         * Notarvertreter
         */
        val Notarvertreter = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".11")

        /**
         * Notariatsverwalterin
         */
        val Notariatsverwalterin = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".12")

        /**
         * Notariatsverwalter
         */
        val Notariatsverwalter = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".13")

        /**
         * Wirtschaftspr�ferin
         */
        val Wirtschaftsprferin = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".14")

        /**
         * Wirtschaftspr�fer
         */
        val Wirtschaftsprfer = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".15")

        /**
         * Vereidigte Buchpr�ferin
         */
        val VereidigteBuchprferin = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".16")

        /**
         * Vereidigter Buchpr�fer
         */
        val VereidigterBuchprfer = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".17")

        /**
         * Patentanw�ltin
         */
        val Patentanwltin = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".18")

        /**
         * Patentanwalt
         */
        val Patentanwalt = ASN1ObjectIdentifier(
                NamingAuthority.id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern + ".19")

        fun getInstance(obj: Any?): ProfessionInfo {
            if (obj == null || obj is ProfessionInfo) {
                return obj as ProfessionInfo?
            }

            if (obj is ASN1Sequence) {
                return ProfessionInfo(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }
    }
}

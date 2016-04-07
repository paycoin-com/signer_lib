package org.bouncycastle.asn1.isismtt.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.isismtt.ISISMTTObjectIdentifiers
import org.bouncycastle.asn1.x500.DirectoryString

/**
 * Names of authorities which are responsible for the administration of title
 * registers.

 *
 * NamingAuthority ::= SEQUENCE
 * {
 * namingAuthorityId OBJECT IDENTIFIER OPTIONAL,
 * namingAuthorityUrl IA5String OPTIONAL,
 * namingAuthorityText DirectoryString(SIZE(1..128)) OPTIONAL
 * }
 *
 * @see org.bouncycastle.asn1.isismtt.x509.AdmissionSyntax
 */
class NamingAuthority : ASN1Object {

    /**
     * @return Returns the namingAuthorityId.
     */
    var namingAuthorityId: ASN1ObjectIdentifier? = null
        private set
    /**
     * @return Returns the namingAuthorityUrl.
     */
    var namingAuthorityUrl: String? = null
        private set
    /**
     * @return Returns the namingAuthorityText.
     */
    var namingAuthorityText: DirectoryString? = null
        private set

    /**
     * Constructor from ASN1Sequence.
     *
     * NamingAuthority ::= SEQUENCE
     * {
     * namingAuthorityId OBJECT IDENTIFIER OPTIONAL,
     * namingAuthorityUrl IA5String OPTIONAL,
     * namingAuthorityText DirectoryString(SIZE(1..128)) OPTIONAL
     * }
     *

     * @param seq The ASN.1 sequence.
     */
    private constructor(seq: ASN1Sequence) {

        if (seq.size() > 3) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        val e = seq.objects

        if (e.hasMoreElements()) {
            val o = e.nextElement() as ASN1Encodable
            if (o is ASN1ObjectIdentifier) {
                namingAuthorityId = o
            } else if (o is DERIA5String) {
                namingAuthorityUrl = DERIA5String.getInstance(o).string
            } else if (o is ASN1String) {
                namingAuthorityText = DirectoryString.getInstance(o)
            } else {
                throw IllegalArgumentException("Bad object encountered: " + o.javaClass)
            }
        }
        if (e.hasMoreElements()) {
            val o = e.nextElement() as ASN1Encodable
            if (o is DERIA5String) {
                namingAuthorityUrl = DERIA5String.getInstance(o).string
            } else if (o is ASN1String) {
                namingAuthorityText = DirectoryString.getInstance(o)
            } else {
                throw IllegalArgumentException("Bad object encountered: " + o.javaClass)
            }
        }
        if (e.hasMoreElements()) {
            val o = e.nextElement() as ASN1Encodable
            if (o is ASN1String) {
                namingAuthorityText = DirectoryString.getInstance(o)
            } else {
                throw IllegalArgumentException("Bad object encountered: " + o.javaClass)
            }

        }
    }

    /**
     * Constructor from given details.
     *
     *
     * All parameters can be combined.

     * @param namingAuthorityId   ObjectIdentifier for naming authority.
     * *
     * @param namingAuthorityUrl  URL for naming authority.
     * *
     * @param namingAuthorityText Textual representation of naming authority.
     */
    constructor(namingAuthorityId: ASN1ObjectIdentifier,
                namingAuthorityUrl: String, namingAuthorityText: DirectoryString) {
        this.namingAuthorityId = namingAuthorityId
        this.namingAuthorityUrl = namingAuthorityUrl
        this.namingAuthorityText = namingAuthorityText
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     *
     * Returns:
     *
     * NamingAuthority ::= SEQUENCE
     * {
     * namingAuthorityId OBJECT IDENTIFIER OPTIONAL,
     * namingAuthorityUrl IA5String OPTIONAL,
     * namingAuthorityText DirectoryString(SIZE(1..128)) OPTIONAL
     * }
     *

     * @return a DERObject
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()
        if (namingAuthorityId != null) {
            vec.add(namingAuthorityId)
        }
        if (namingAuthorityUrl != null) {
            vec.add(DERIA5String(namingAuthorityUrl, true))
        }
        if (namingAuthorityText != null) {
            vec.add(namingAuthorityText)
        }
        return DERSequence(vec)
    }

    companion object {

        /**
         * Profession OIDs should always be defined under the OID branch of the
         * responsible naming authority. At the time of this writing, the work group
         * �Recht, Wirtschaft, Steuern� (�Law, Economy, Taxes�) is registered as the
         * first naming authority under the OID id-isismtt-at-namingAuthorities.
         */
        val id_isismtt_at_namingAuthorities_RechtWirtschaftSteuern = ASN1ObjectIdentifier(ISISMTTObjectIdentifiers.id_isismtt_at_namingAuthorities + ".1")

        fun getInstance(obj: Any?): NamingAuthority {
            if (obj == null || obj is NamingAuthority) {
                return obj as NamingAuthority?
            }

            if (obj is ASN1Sequence) {
                return NamingAuthority(obj as ASN1Sequence?)
            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        fun getInstance(obj: ASN1TaggedObject, explicit: Boolean): NamingAuthority {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }
    }
}

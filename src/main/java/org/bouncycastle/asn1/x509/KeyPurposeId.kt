package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive

/**
 * The KeyPurposeId object.
 *
 * KeyPurposeId ::= OBJECT IDENTIFIER

 * id-kp ::= OBJECT IDENTIFIER { iso(1) identified-organization(3)
 * dod(6) internet(1) security(5) mechanisms(5) pkix(7) 3}

 *
 * To create a new KeyPurposeId where none of the below suit, use
 *
 * ASN1ObjectIdentifier newKeyPurposeIdOID = new ASN1ObjectIdentifier("1.3.6.1...");

 * KeyPurposeId newKeyPurposeId = KeyPurposeId.getInstance(newKeyPurposeIdOID);
 *
 */
class KeyPurposeId private constructor(private val id: ASN1ObjectIdentifier) : ASN1Object() {

    /**
     * @param id string representation of an OID.
     */
    @Deprecated("use getInstance and an OID or one of the constants above.\n      ")
    constructor(id: String) : this(ASN1ObjectIdentifier(id)) {
    }

    fun toOID(): ASN1ObjectIdentifier {
        return id
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return id
    }

    fun getId(): String {
        return id.id
    }

    override fun toString(): String {
        return id.toString()
    }

    companion object {
        private val id_kp = ASN1ObjectIdentifier("1.3.6.1.5.5.7.3")

        /**
         * { 2 5 29 37 0 }
         */
        val anyExtendedKeyUsage = KeyPurposeId(Extension.extendedKeyUsage.branch("0"))

        /**
         * { id-kp 1 }
         */
        val id_kp_serverAuth = KeyPurposeId(id_kp.branch("1"))
        /**
         * { id-kp 2 }
         */
        val id_kp_clientAuth = KeyPurposeId(id_kp.branch("2"))
        /**
         * { id-kp 3 }
         */
        val id_kp_codeSigning = KeyPurposeId(id_kp.branch("3"))
        /**
         * { id-kp 4 }
         */
        val id_kp_emailProtection = KeyPurposeId(id_kp.branch("4"))
        /**
         * Usage deprecated by RFC4945 - was { id-kp 5 }
         */
        val id_kp_ipsecEndSystem = KeyPurposeId(id_kp.branch("5"))
        /**
         * Usage deprecated by RFC4945 - was { id-kp 6 }
         */
        val id_kp_ipsecTunnel = KeyPurposeId(id_kp.branch("6"))
        /**
         * Usage deprecated by RFC4945 - was { idkp 7 }
         */
        val id_kp_ipsecUser = KeyPurposeId(id_kp.branch("7"))
        /**
         * { id-kp 8 }
         */
        val id_kp_timeStamping = KeyPurposeId(id_kp.branch("8"))
        /**
         * { id-kp 9 }
         */
        val id_kp_OCSPSigning = KeyPurposeId(id_kp.branch("9"))
        /**
         * { id-kp 10 }
         */
        val id_kp_dvcs = KeyPurposeId(id_kp.branch("10"))
        /**
         * { id-kp 11 }
         */
        val id_kp_sbgpCertAAServerAuth = KeyPurposeId(id_kp.branch("11"))
        /**
         * { id-kp 12 }
         */
        val id_kp_scvp_responder = KeyPurposeId(id_kp.branch("12"))
        /**
         * { id-kp 13 }
         */
        val id_kp_eapOverPPP = KeyPurposeId(id_kp.branch("13"))
        /**
         * { id-kp 14 }
         */
        val id_kp_eapOverLAN = KeyPurposeId(id_kp.branch("14"))
        /**
         * { id-kp 15 }
         */
        val id_kp_scvpServer = KeyPurposeId(id_kp.branch("15"))
        /**
         * { id-kp 16 }
         */
        val id_kp_scvpClient = KeyPurposeId(id_kp.branch("16"))
        /**
         * { id-kp 17 }
         */
        val id_kp_ipsecIKE = KeyPurposeId(id_kp.branch("17"))
        /**
         * { id-kp 18 }
         */
        val id_kp_capwapAC = KeyPurposeId(id_kp.branch("18"))
        /**
         * { id-kp 19 }
         */
        val id_kp_capwapWTP = KeyPurposeId(id_kp.branch("19"))

        //
        // microsoft key purpose ids
        //
        /**
         * { 1 3 6 1 4 1 311 20 2 2 }
         */
        val id_kp_smartcardlogon = KeyPurposeId(ASN1ObjectIdentifier("1.3.6.1.4.1.311.20.2.2"))

        fun getInstance(o: Any?): KeyPurposeId? {
            if (o is KeyPurposeId) {
                return o
            } else if (o != null) {
                return KeyPurposeId(ASN1ObjectIdentifier.getInstance(o))
            }

            return null
        }
    }
}

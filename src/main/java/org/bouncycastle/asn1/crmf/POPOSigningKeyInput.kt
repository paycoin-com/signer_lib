package org.bouncycastle.asn1.crmf

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo

class POPOSigningKeyInput : ASN1Object {
    /**
     * Returns the sender field, or null if authInfo is publicKeyMAC
     */
    var sender: GeneralName? = null
        private set
    /**
     * Returns the publicKeyMAC field, or null if authInfo is sender
     */
    var publicKeyMAC: PKMACValue? = null
        private set
    var publicKey: SubjectPublicKeyInfo? = null
        private set

    private constructor(seq: ASN1Sequence) {

        if (seq.getObjectAt(0) is ASN1TaggedObject) {
            if (seq.getObjectAt(0).tagNo != 0) {
                throw IllegalArgumentException(
                        "Unknown authInfo tag: " + seq.getObjectAt(0).tagNo)
            }
            sender = GeneralName.getInstance(seq.getObjectAt(0).`object`)
        } else {
            publicKeyMAC = PKMACValue.getInstance(seq.getObjectAt(0))
        }

        publicKey = SubjectPublicKeyInfo.getInstance(seq.getObjectAt(1))
    }

    /**
     * Creates a new POPOSigningKeyInput with sender name as authInfo.
     */
    constructor(
            sender: GeneralName,
            spki: SubjectPublicKeyInfo) {
        this.sender = sender
        this.publicKey = spki
    }

    /**
     * Creates a new POPOSigningKeyInput using password-based MAC.
     */
    constructor(
            pkmac: PKMACValue,
            spki: SubjectPublicKeyInfo) {
        this.publicKeyMAC = pkmac
        this.publicKey = spki
    }

    /**
     *
     * POPOSigningKeyInput ::= SEQUENCE {
     * authInfo             CHOICE {
     * sender              [0] GeneralName,
     * -- used only if an authenticated identity has been
     * -- established for the sender (e.g., a DN from a
     * -- previously-issued and currently-valid certificate
     * publicKeyMAC        PKMACValue },
     * -- used if no authenticated GeneralName currently exists for
     * -- the sender; publicKeyMAC contains a password-based MAC
     * -- on the DER-encoded value of publicKey
     * publicKey           SubjectPublicKeyInfo }  -- from CertTemplate
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (sender != null) {
            v.add(DERTaggedObject(false, 0, sender))
        } else {
            v.add(publicKeyMAC)
        }

        v.add(publicKey)

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): POPOSigningKeyInput? {
            if (o is POPOSigningKeyInput) {
                return o
            }

            if (o != null) {
                return POPOSigningKeyInput(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

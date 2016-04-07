package org.bouncycastle.asn1.smime

import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.cms.Attribute
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber
import org.bouncycastle.asn1.cms.RecipientKeyIdentifier

/**
 * The SMIMEEncryptionKeyPreference object.
 *
 * SMIMEEncryptionKeyPreference ::= CHOICE {
 * issuerAndSerialNumber   [0] IssuerAndSerialNumber,
 * receipentKeyId          [1] RecipientKeyIdentifier,
 * subjectAltKeyIdentifier [2] SubjectKeyIdentifier
 * }
 *
 */
class SMIMEEncryptionKeyPreferenceAttribute : Attribute {
    constructor(
            issAndSer: IssuerAndSerialNumber) : super(SMIMEAttributes.encrypKeyPref,
            DERSet(DERTaggedObject(false, 0, issAndSer))) {
    }

    constructor(
            rKeyId: RecipientKeyIdentifier) :

    super(SMIMEAttributes.encrypKeyPref,
            DERSet(DERTaggedObject(false, 1, rKeyId))) {
    }

    /**
     * @param sKeyId the subjectKeyIdentifier value (normally the X.509 one)
     */
    constructor(
            sKeyId: ASN1OctetString) :

    super(SMIMEAttributes.encrypKeyPref,
            DERSet(DERTaggedObject(false, 2, sKeyId))) {
    }
}

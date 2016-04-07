package org.bouncycastle.asn1.x509.qualified

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * The QCStatement object.
 *
 * QCStatement ::= SEQUENCE {
 * statementId        OBJECT IDENTIFIER,
 * statementInfo      ANY DEFINED BY statementId OPTIONAL}
 *
 */

class QCStatement : ASN1Object, ETSIQCObjectIdentifiers, RFC3739QCObjectIdentifiers {
    var statementId: ASN1ObjectIdentifier
        internal set
    var statementInfo: ASN1Encodable? = null
        internal set

    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        // qcStatementId
        statementId = ASN1ObjectIdentifier.getInstance(e.nextElement())
        // qcstatementInfo
        if (e.hasMoreElements()) {
            statementInfo = e.nextElement() as ASN1Encodable
        }
    }

    constructor(
            qcStatementId: ASN1ObjectIdentifier) {
        this.statementId = qcStatementId
        this.statementInfo = null
    }

    constructor(
            qcStatementId: ASN1ObjectIdentifier,
            qcStatementInfo: ASN1Encodable) {
        this.statementId = qcStatementId
        this.statementInfo = qcStatementInfo
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val seq = ASN1EncodableVector()
        seq.add(statementId)

        if (statementInfo != null) {
            seq.add(statementInfo)
        }

        return DERSequence(seq)
    }

    companion object {

        fun getInstance(
                obj: Any?): QCStatement? {
            if (obj is QCStatement) {
                return obj
            }
            if (obj != null) {
                return QCStatement(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

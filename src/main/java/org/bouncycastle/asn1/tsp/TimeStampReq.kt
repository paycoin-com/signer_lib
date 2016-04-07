package org.bouncycastle.asn1.tsp

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Extensions

class TimeStampReq : ASN1Object {
    var version: ASN1Integer
        internal set

    var messageImprint: MessageImprint
        internal set

    var reqPolicy: ASN1ObjectIdentifier? = null
        internal set

    var nonce: ASN1Integer? = null
        internal set

    var certReq: ASN1Boolean? = null
        internal set

    var extensions: Extensions? = null
        internal set

    private constructor(seq: ASN1Sequence) {
        val nbObjects = seq.size()

        var seqStart = 0

        // version
        version = ASN1Integer.getInstance(seq.getObjectAt(seqStart))

        seqStart++

        // messageImprint
        messageImprint = MessageImprint.getInstance(seq.getObjectAt(seqStart))

        seqStart++

        for (opt in seqStart..nbObjects - 1) {
            // tsaPolicy
            if (seq.getObjectAt(opt) is ASN1ObjectIdentifier) {
                reqPolicy = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(opt))
            } else if (seq.getObjectAt(opt) is ASN1Integer) {
                nonce = ASN1Integer.getInstance(seq.getObjectAt(opt))
            } else if (seq.getObjectAt(opt) is ASN1Boolean) {
                certReq = ASN1Boolean.getInstance(seq.getObjectAt(opt))
            } else if (seq.getObjectAt(opt) is ASN1TaggedObject) {
                val tagged = seq.getObjectAt(opt) as ASN1TaggedObject
                if (tagged.tagNo == 0) {
                    extensions = Extensions.getInstance(tagged, false)
                }
            }// extensions
            // certReq
            // nonce
        }
    }

    constructor(
            messageImprint: MessageImprint,
            tsaPolicy: ASN1ObjectIdentifier,
            nonce: ASN1Integer,
            certReq: ASN1Boolean,
            extensions: Extensions) {
        // default
        version = ASN1Integer(1)

        this.messageImprint = messageImprint
        this.reqPolicy = tsaPolicy
        this.nonce = nonce
        this.certReq = certReq
        this.extensions = extensions
    }

    /**
     *
     * TimeStampReq ::= SEQUENCE  {
     * version                      INTEGER  { v1(1) },
     * messageImprint               MessageImprint,
     * --a hash algorithm OID and the hash value of the data to be
     * --time-stamped
     * reqPolicy             TSAPolicyId              OPTIONAL,
     * nonce                 INTEGER                  OPTIONAL,
     * certReq               BOOLEAN                  DEFAULT FALSE,
     * extensions            [0] IMPLICIT Extensions  OPTIONAL
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(version)
        v.add(messageImprint)

        if (reqPolicy != null) {
            v.add(reqPolicy)
        }

        if (nonce != null) {
            v.add(nonce)
        }

        if (certReq != null && certReq!!.isTrue) {
            v.add(certReq)
        }

        if (extensions != null) {
            v.add(DERTaggedObject(false, 0, extensions))
        }

        return DERSequence(v)
    }

    companion object {

        fun getInstance(o: Any?): TimeStampReq? {
            if (o is TimeStampReq) {
                return o
            } else if (o != null) {
                return TimeStampReq(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

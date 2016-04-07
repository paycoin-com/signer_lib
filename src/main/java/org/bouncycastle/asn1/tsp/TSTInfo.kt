package org.bouncycastle.asn1.tsp

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.Extensions
import org.bouncycastle.asn1.x509.GeneralName

class TSTInfo : ASN1Object {
    var version: ASN1Integer? = null
        private set
    var policy: ASN1ObjectIdentifier? = null
        private set
    var messageImprint: MessageImprint? = null
        private set
    var serialNumber: ASN1Integer? = null
        private set
    var genTime: ASN1GeneralizedTime? = null
        private set
    var accuracy: Accuracy? = null
        private set
    var ordering: ASN1Boolean? = null
        private set
    var nonce: ASN1Integer? = null
        private set
    var tsa: GeneralName? = null
        private set
    var extensions: Extensions? = null
        private set

    private constructor(seq: ASN1Sequence) {
        val e = seq.objects

        // version
        version = ASN1Integer.getInstance(e.nextElement())

        // tsaPolicy
        policy = ASN1ObjectIdentifier.getInstance(e.nextElement())

        // messageImprint
        messageImprint = MessageImprint.getInstance(e.nextElement())

        // serialNumber
        serialNumber = ASN1Integer.getInstance(e.nextElement())

        // genTime
        genTime = ASN1GeneralizedTime.getInstance(e.nextElement())

        // default for ordering
        ordering = ASN1Boolean.getInstance(false)

        while (e.hasMoreElements()) {
            val o = e.nextElement() as ASN1Object

            if (o is ASN1TaggedObject) {
                val tagged = o as DERTaggedObject

                when (tagged.tagNo) {
                    0 -> tsa = GeneralName.getInstance(tagged, true)
                    1 -> extensions = Extensions.getInstance(tagged, false)
                    else -> throw IllegalArgumentException("Unknown tag value " + tagged.tagNo)
                }
            } else if (o is ASN1Sequence || o is Accuracy) {
                accuracy = Accuracy.getInstance(o)
            } else if (o is ASN1Boolean) {
                ordering = ASN1Boolean.getInstance(o)
            } else if (o is ASN1Integer) {
                nonce = ASN1Integer.getInstance(o)
            }

        }
    }

    constructor(tsaPolicyId: ASN1ObjectIdentifier, messageImprint: MessageImprint,
                serialNumber: ASN1Integer, genTime: ASN1GeneralizedTime,
                accuracy: Accuracy, ordering: ASN1Boolean, nonce: ASN1Integer,
                tsa: GeneralName, extensions: Extensions) {
        version = ASN1Integer(1)
        this.policy = tsaPolicyId
        this.messageImprint = messageImprint
        this.serialNumber = serialNumber
        this.genTime = genTime

        this.accuracy = accuracy
        this.ordering = ordering
        this.nonce = nonce
        this.tsa = tsa
        this.extensions = extensions
    }

    /**
     *

     * TSTInfo ::= SEQUENCE  {
     * version                      INTEGER  { v1(1) },
     * policy                       TSAPolicyId,
     * messageImprint               MessageImprint,
     * -- MUST have the same value as the similar field in
     * -- TimeStampReq
     * serialNumber                 INTEGER,
     * -- Time-Stamping users MUST be ready to accommodate integers
     * -- up to 160 bits.
     * genTime                      GeneralizedTime,
     * accuracy                     Accuracy                 OPTIONAL,
     * ordering                     BOOLEAN             DEFAULT FALSE,
     * nonce                        INTEGER                  OPTIONAL,
     * -- MUST be present if the similar field was present
     * -- in TimeStampReq.  In that case it MUST have the same value.
     * tsa                          [0] GeneralName          OPTIONAL,
     * extensions                   [1] IMPLICIT Extensions   OPTIONAL  }

     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val seq = ASN1EncodableVector()
        seq.add(version)

        seq.add(policy)
        seq.add(messageImprint)
        seq.add(serialNumber)
        seq.add(genTime)

        if (accuracy != null) {
            seq.add(accuracy)
        }

        if (ordering != null && ordering!!.isTrue) {
            seq.add(ordering)
        }

        if (nonce != null) {
            seq.add(nonce)
        }

        if (tsa != null) {
            seq.add(DERTaggedObject(true, 0, tsa))
        }

        if (extensions != null) {
            seq.add(DERTaggedObject(false, 1, extensions))
        }

        return DERSequence(seq)
    }

    companion object {

        fun getInstance(o: Any?): TSTInfo? {
            if (o is TSTInfo) {
                return o
            } else if (o != null) {
                return TSTInfo(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

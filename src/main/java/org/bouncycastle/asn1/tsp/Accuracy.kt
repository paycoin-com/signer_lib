package org.bouncycastle.asn1.tsp

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject


class Accuracy : ASN1Object {
    var seconds: ASN1Integer? = null
        internal set

    var millis: ASN1Integer? = null
        internal set

    var micros: ASN1Integer? = null
        internal set

    protected constructor() {
    }

    constructor(
            seconds: ASN1Integer,
            millis: ASN1Integer?,
            micros: ASN1Integer?) {
        this.seconds = seconds

        //Verifications
        if (millis != null && (millis.value.toInt() < MIN_MILLIS || millis.value.toInt() > MAX_MILLIS)) {
            throw IllegalArgumentException(
                    "Invalid millis field : not in (1..999)")
        } else {
            this.millis = millis
        }

        if (micros != null && (micros.value.toInt() < MIN_MICROS || micros.value.toInt() > MAX_MICROS)) {
            throw IllegalArgumentException(
                    "Invalid micros field : not in (1..999)")
        } else {
            this.micros = micros
        }

    }

    private constructor(seq: ASN1Sequence) {
        seconds = null
        millis = null
        micros = null

        for (i in 0..seq.size() - 1) {
            // seconds
            if (seq.getObjectAt(i) is ASN1Integer) {
                seconds = seq.getObjectAt(i) as ASN1Integer
            } else if (seq.getObjectAt(i) is DERTaggedObject) {
                val extra = seq.getObjectAt(i) as DERTaggedObject

                when (extra.tagNo) {
                    0 -> {
                        millis = ASN1Integer.getInstance(extra, false)
                        if (millis!!.value.toInt() < MIN_MILLIS || millis!!.value.toInt() > MAX_MILLIS) {
                            throw IllegalArgumentException(
                                    "Invalid millis field : not in (1..999).")
                        }
                    }
                    1 -> {
                        micros = ASN1Integer.getInstance(extra, false)
                        if (micros!!.value.toInt() < MIN_MICROS || micros!!.value.toInt() > MAX_MICROS) {
                            throw IllegalArgumentException(
                                    "Invalid micros field : not in (1..999).")
                        }
                    }
                    else -> throw IllegalArgumentException("Invalig tag number")
                }
            }
        }
    }

    /**
     *
     * Accuracy ::= SEQUENCE {
     * seconds        INTEGER              OPTIONAL,
     * millis     [0] INTEGER  (1..999)    OPTIONAL,
     * micros     [1] INTEGER  (1..999)    OPTIONAL
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {

        val v = ASN1EncodableVector()

        if (seconds != null) {
            v.add(seconds)
        }

        if (millis != null) {
            v.add(DERTaggedObject(false, 0, millis))
        }

        if (micros != null) {
            v.add(DERTaggedObject(false, 1, micros))
        }

        return DERSequence(v)
    }

    companion object {

        // constantes
        protected val MIN_MILLIS = 1

        protected val MAX_MILLIS = 999

        protected val MIN_MICROS = 1

        protected val MAX_MICROS = 999

        fun getInstance(o: Any?): Accuracy? {
            if (o is Accuracy) {
                return o
            }

            if (o != null) {
                return Accuracy(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

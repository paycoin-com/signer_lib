package org.bouncycastle.asn1.dvcs

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x509.DigestInfo

/**
 *
 * Data ::= CHOICE {
 * message           OCTET STRING ,
 * messageImprint    DigestInfo,
 * certs             [0] SEQUENCE SIZE (1..MAX) OF
 * TargetEtcChain
 * }
 *
 */

class Data : ASN1Object, ASN1Choice {
    var message: ASN1OctetString? = null
        private set
    val messageImprint: DigestInfo?
    private var certs: ASN1Sequence? = null

    constructor(messageBytes: ByteArray) {
        this.message = DEROctetString(messageBytes)
    }

    constructor(message: ASN1OctetString) {
        this.message = message
    }

    constructor(messageImprint: DigestInfo) {
        this.messageImprint = messageImprint
    }

    constructor(cert: TargetEtcChain) {
        this.certs = DERSequence(cert)
    }

    constructor(certs: Array<TargetEtcChain>) {
        this.certs = DERSequence(certs)
    }

    private constructor(certs: ASN1Sequence) {
        this.certs = certs
    }

    override fun toASN1Primitive(): ASN1Primitive {
        if (message != null) {
            return message!!.toASN1Primitive()
        }
        if (messageImprint != null) {
            return messageImprint.toASN1Primitive()
        } else {
            return DERTaggedObject(false, 0, certs)
        }
    }

    override fun toString(): String {
        if (message != null) {
            return "Data {\n$message}\n"
        }
        if (messageImprint != null) {
            return "Data {\n$messageImprint}\n"
        } else {
            return "Data {\n$certs}\n"
        }
    }

    fun getCerts(): Array<TargetEtcChain>? {
        if (certs == null) {
            return null
        }

        val tmp = arrayOfNulls<TargetEtcChain>(certs!!.size())

        for (i in tmp.indices) {
            tmp[i] = TargetEtcChain.getInstance(certs!!.getObjectAt(i))
        }

        return tmp
    }

    companion object {

        fun getInstance(obj: Any): Data {
            if (obj is Data) {
                return obj
            } else if (obj is ASN1OctetString) {
                return Data(obj)
            } else if (obj is ASN1Sequence) {
                return Data(DigestInfo.getInstance(obj))
            } else if (obj is ASN1TaggedObject) {
                return Data(ASN1Sequence.getInstance(obj, false))
            }
            throw IllegalArgumentException("Unknown object submitted to getInstance: " + obj.javaClass.name)
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): Data {
            return getInstance(obj.`object`)
        }
    }
}

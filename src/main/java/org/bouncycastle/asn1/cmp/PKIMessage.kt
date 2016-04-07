package org.bouncycastle.asn1.cmp

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

class PKIMessage : ASN1Object {
    var header: PKIHeader? = null
        private set
    var body: PKIBody? = null
        private set
    var protection: DERBitString? = null
        private set
    private var extraCerts: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        val en = seq.objects

        header = PKIHeader.getInstance(en.nextElement())
        body = PKIBody.getInstance(en.nextElement())

        while (en.hasMoreElements()) {
            val tObj = en.nextElement() as ASN1TaggedObject

            if (tObj.tagNo == 0) {
                protection = DERBitString.getInstance(tObj, true)
            } else {
                extraCerts = ASN1Sequence.getInstance(tObj, true)
            }
        }
    }

    /**
     * Creates a new PKIMessage.

     * @param header     message header
     * *
     * @param body       message body
     * *
     * @param protection message protection (may be null)
     * *
     * @param extraCerts extra certificates (may be null)
     */
    @JvmOverloads constructor(
            header: PKIHeader,
            body: PKIBody,
            protection: DERBitString? = null,
            extraCerts: Array<CMPCertificate>? = null) {
        this.header = header
        this.body = body
        this.protection = protection
        if (extraCerts != null) {
            val v = ASN1EncodableVector()
            for (i in extraCerts.indices) {
                v.add(extraCerts[i])
            }
            this.extraCerts = DERSequence(v)
        }
    }

    fun getExtraCerts(): Array<CMPCertificate>? {
        if (extraCerts == null) {
            return null
        }

        val results = arrayOfNulls<CMPCertificate>(extraCerts!!.size())

        for (i in results.indices) {
            results[i] = CMPCertificate.getInstance(extraCerts!!.getObjectAt(i))
        }
        return results
    }

    /**
     *
     * PKIMessage ::= SEQUENCE {
     * header           PKIHeader,
     * body             PKIBody,
     * protection   [0] PKIProtection OPTIONAL,
     * extraCerts   [1] SEQUENCE SIZE (1..MAX) OF CMPCertificate
     * OPTIONAL
     * }
     *

     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(header)
        v.add(body)

        addOptional(v, 0, protection)
        addOptional(v, 1, extraCerts)

        return DERSequence(v)
    }

    private fun addOptional(v: ASN1EncodableVector, tagNo: Int, obj: ASN1Encodable?) {
        if (obj != null) {
            v.add(DERTaggedObject(true, tagNo, obj))
        }
    }

    companion object {

        fun getInstance(o: Any?): PKIMessage? {
            if (o is PKIMessage) {
                return o
            } else if (o != null) {
                return PKIMessage(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

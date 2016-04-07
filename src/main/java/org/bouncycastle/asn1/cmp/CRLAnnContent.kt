package org.bouncycastle.asn1.cmp

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.CertificateList

class CRLAnnContent : ASN1Object {
    private var content: ASN1Sequence? = null

    private constructor(seq: ASN1Sequence) {
        content = seq
    }

    constructor(crl: CertificateList) {
        this.content = DERSequence(crl)
    }

    val certificateLists: Array<CertificateList>
        get() {
            val result = arrayOfNulls<CertificateList>(content!!.size())

            for (i in result.indices) {
                result[i] = CertificateList.getInstance(content!!.getObjectAt(i))
            }

            return result
        }

    /**
     *
     * CRLAnnContent ::= SEQUENCE OF CertificateList
     *
     * @return a basic ASN.1 object representation.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return content
    }

    companion object {

        fun getInstance(o: Any?): CRLAnnContent? {
            if (o is CRLAnnContent) {
                return o
            }

            if (o != null) {
                return CRLAnnContent(ASN1Sequence.getInstance(o))
            }

            return null
        }
    }
}

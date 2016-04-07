package org.bouncycastle.asn1.dvcs

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 *
 * TargetEtcChain ::= SEQUENCE {
 * target                       CertEtcToken,
 * chain                        SEQUENCE SIZE (1..MAX) OF
 * CertEtcToken OPTIONAL,
 * pathProcInput                [0] PathProcInput OPTIONAL
 * }
 *
 */

class TargetEtcChain : ASN1Object {
    var target: CertEtcToken? = null
        private set
    private var chain: ASN1Sequence? = null
    var pathProcInput: PathProcInput? = null
        private set(pathProcInput) {
            this.pathProcInput = pathProcInput
        }

    constructor(target: CertEtcToken, pathProcInput: PathProcInput) : this(target, null, pathProcInput) {
    }

    @JvmOverloads constructor(target: CertEtcToken, chain: Array<CertEtcToken>? = null, pathProcInput: PathProcInput? = null) {
        this.target = target

        if (chain != null) {
            this.chain = DERSequence(chain)
        }

        this.pathProcInput = pathProcInput
    }

    private constructor(seq: ASN1Sequence) {
        var i = 0
        var obj = seq.getObjectAt(i++)
        this.target = CertEtcToken.getInstance(obj)

        try {
            obj = seq.getObjectAt(i++)
            this.chain = ASN1Sequence.getInstance(obj)
        } catch (e: IllegalArgumentException) {
        } catch (e: IndexOutOfBoundsException) {
            return
        }

        try {
            obj = seq.getObjectAt(i++)
            val tagged = ASN1TaggedObject.getInstance(obj)
            when (tagged.tagNo) {
                0 -> this.pathProcInput = PathProcInput.getInstance(tagged, false)
            }
        } catch (e: IllegalArgumentException) {
        } catch (e: IndexOutOfBoundsException) {
        }

    }

    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        v.add(target)
        if (chain != null) {
            v.add(chain)
        }
        if (pathProcInput != null) {
            v.add(DERTaggedObject(false, 0, pathProcInput))
        }

        return DERSequence(v)
    }

    override fun toString(): String {
        val s = StringBuffer()
        s.append("TargetEtcChain {\n")
        s.append("target: " + target + "\n")
        if (chain != null) {
            s.append("chain: " + chain + "\n")
        }
        if (pathProcInput != null) {
            s.append("pathProcInput: " + pathProcInput + "\n")
        }
        s.append("}\n")
        return s.toString()
    }

    val chain: Array<CertEtcToken>?
        get() {
            if (chain != null) {
                return CertEtcToken.arrayFromSequence(chain)
            }

            return null
        }

    private fun setChain(chain: ASN1Sequence) {
        this.chain = chain
    }

    companion object {

        fun getInstance(obj: Any?): TargetEtcChain? {
            if (obj is TargetEtcChain) {
                return obj
            } else if (obj != null) {
                return TargetEtcChain(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): TargetEtcChain {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun arrayFromSequence(seq: ASN1Sequence): Array<TargetEtcChain> {
            val tmp = arrayOfNulls<TargetEtcChain>(seq.size())

            for (i in tmp.indices) {
                tmp[i] = TargetEtcChain.getInstance(seq.getObjectAt(i))
            }

            return tmp
        }
    }
}

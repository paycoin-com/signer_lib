package org.bouncycastle.asn1.icao

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * The LDSSecurityObject object (V1.8).
 *
 * LDSSecurityObject ::= SEQUENCE {
 * version                LDSSecurityObjectVersion,
 * hashAlgorithm          DigestAlgorithmIdentifier,
 * dataGroupHashValues    SEQUENCE SIZE (2..ub-DataGroups) OF DataHashGroup,
 * ldsVersionInfo         LDSVersionInfo OPTIONAL
 * -- if present, version MUST be v1 }

 * DigestAlgorithmIdentifier ::= AlgorithmIdentifier,

 * LDSSecurityObjectVersion :: INTEGER {V0(0)}
 *
 */

class LDSSecurityObject : ASN1Object, ICAOObjectIdentifiers {

    private var version = ASN1Integer(0)
    var digestAlgorithmIdentifier: AlgorithmIdentifier? = null
        private set
    var datagroupHash: Array<DataGroupHash>? = null
        private set
    var versionInfo: LDSVersionInfo? = null
        private set

    private constructor(
            seq: ASN1Sequence?) {
        if (seq == null || seq.size() == 0) {
            throw IllegalArgumentException("null or empty sequence passed.")
        }

        val e = seq.objects

        // version
        version = ASN1Integer.getInstance(e.nextElement())
        // digestAlgorithmIdentifier
        digestAlgorithmIdentifier = AlgorithmIdentifier.getInstance(e.nextElement())

        val datagroupHashSeq = ASN1Sequence.getInstance(e.nextElement())

        if (version.value.toInt() == 1) {
            versionInfo = LDSVersionInfo.getInstance(e.nextElement())
        }

        checkDatagroupHashSeqSize(datagroupHashSeq.size())

        datagroupHash = arrayOfNulls<DataGroupHash>(datagroupHashSeq.size())
        for (i in 0..datagroupHashSeq.size() - 1) {
            datagroupHash[i] = DataGroupHash.getInstance(datagroupHashSeq.getObjectAt(i))
        }
    }

    constructor(
            digestAlgorithmIdentifier: AlgorithmIdentifier,
            datagroupHash: Array<DataGroupHash>) {
        this.version = ASN1Integer(0)
        this.digestAlgorithmIdentifier = digestAlgorithmIdentifier
        this.datagroupHash = datagroupHash

        checkDatagroupHashSeqSize(datagroupHash.size)
    }

    constructor(
            digestAlgorithmIdentifier: AlgorithmIdentifier,
            datagroupHash: Array<DataGroupHash>,
            versionInfo: LDSVersionInfo) {
        this.version = ASN1Integer(1)
        this.digestAlgorithmIdentifier = digestAlgorithmIdentifier
        this.datagroupHash = datagroupHash
        this.versionInfo = versionInfo

        checkDatagroupHashSeqSize(datagroupHash.size)
    }

    private fun checkDatagroupHashSeqSize(size: Int) {
        if (size < 2 || size > ub_DataGroups) {
            throw IllegalArgumentException("wrong size in DataGroupHashValues : not in (2..$ub_DataGroups)")
        }
    }

    fun getVersion(): Int {
        return version.value.toInt()
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val seq = ASN1EncodableVector()

        seq.add(version)
        seq.add(digestAlgorithmIdentifier)

        val seqname = ASN1EncodableVector()
        for (i in datagroupHash!!.indices) {
            seqname.add(datagroupHash!![i])
        }
        seq.add(DERSequence(seqname))

        if (versionInfo != null) {
            seq.add(versionInfo)
        }

        return DERSequence(seq)
    }

    companion object {
        val ub_DataGroups = 16

        fun getInstance(
                obj: Any?): LDSSecurityObject? {
            if (obj is LDSSecurityObject) {
                return obj
            } else if (obj != null) {
                return LDSSecurityObject(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

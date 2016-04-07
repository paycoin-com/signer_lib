package org.bouncycastle.asn1.icao

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.x509.Certificate

/**
 * The CscaMasterList object. This object can be wrapped in a
 * CMSSignedData to be published in LDAP.

 *
 * CscaMasterList ::= SEQUENCE {
 * version                CscaMasterListVersion,
 * certList               SET OF Certificate }

 * CscaMasterListVersion :: INTEGER {v0(0)}
 *
 */

class CscaMasterList : ASN1Object {
    private var version = ASN1Integer(0)
    private var certList: Array<Certificate>? = null

    private constructor(
            seq: ASN1Sequence?) {
        if (seq == null || seq.size() == 0) {
            throw IllegalArgumentException(
                    "null or empty sequence passed.")
        }
        if (seq.size() != 2) {
            throw IllegalArgumentException(
                    "Incorrect sequence size: " + seq.size())
        }

        version = ASN1Integer.getInstance(seq.getObjectAt(0))
        val certSet = ASN1Set.getInstance(seq.getObjectAt(1))
        certList = arrayOfNulls<Certificate>(certSet.size())
        for (i in certList!!.indices) {
            certList[i] = Certificate.getInstance(certSet.getObjectAt(i))
        }
    }

    constructor(
            certStructs: Array<Certificate>) {
        certList = copyCertList(certStructs)
    }

    fun getVersion(): Int {
        return version.value.toInt()
    }

    val certStructs: Array<Certificate>
        get() = copyCertList(certList)

    private fun copyCertList(orig: Array<Certificate>): Array<Certificate> {
        val certs = arrayOfNulls<Certificate>(orig.size)

        for (i in certs.indices) {
            certs[i] = orig[i]
        }

        return certs
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val seq = ASN1EncodableVector()

        seq.add(version)

        val certSet = ASN1EncodableVector()
        for (i in certList!!.indices) {
            certSet.add(certList!![i])
        }
        seq.add(DERSet(certSet))

        return DERSequence(seq)
    }

    companion object {

        fun getInstance(
                obj: Any?): CscaMasterList? {
            if (obj is CscaMasterList) {
                return obj
            } else if (obj != null) {
                return CscaMasterList(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

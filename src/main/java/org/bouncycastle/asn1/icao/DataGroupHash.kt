package org.bouncycastle.asn1.icao

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * The DataGroupHash object.
 *
 * DataGroupHash  ::=  SEQUENCE {
 * dataGroupNumber         DataGroupNumber,
 * dataGroupHashValue     OCTET STRING }

 * DataGroupNumber ::= INTEGER {
 * dataGroup1    (1),
 * dataGroup1    (2),
 * dataGroup1    (3),
 * dataGroup1    (4),
 * dataGroup1    (5),
 * dataGroup1    (6),
 * dataGroup1    (7),
 * dataGroup1    (8),
 * dataGroup1    (9),
 * dataGroup1    (10),
 * dataGroup1    (11),
 * dataGroup1    (12),
 * dataGroup1    (13),
 * dataGroup1    (14),
 * dataGroup1    (15),
 * dataGroup1    (16) }

 *
 */
class DataGroupHash : ASN1Object {
    internal var dataGroupNumber: ASN1Integer
    var dataGroupHashValue: ASN1OctetString
        internal set

    private constructor(seq: ASN1Sequence) {
        val e = seq.objects

        // dataGroupNumber
        dataGroupNumber = ASN1Integer.getInstance(e.nextElement())
        // dataGroupHashValue
        dataGroupHashValue = ASN1OctetString.getInstance(e.nextElement())
    }

    constructor(
            dataGroupNumber: Int,
            dataGroupHashValue: ASN1OctetString) {
        this.dataGroupNumber = ASN1Integer(dataGroupNumber.toLong())
        this.dataGroupHashValue = dataGroupHashValue
    }

    fun getDataGroupNumber(): Int {
        return dataGroupNumber.value.toInt()
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val seq = ASN1EncodableVector()
        seq.add(dataGroupNumber)
        seq.add(dataGroupHashValue)

        return DERSequence(seq)
    }

    companion object {

        fun getInstance(
                obj: Any?): DataGroupHash? {
            if (obj is DataGroupHash) {
                return obj
            } else if (obj != null) {
                return DataGroupHash(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

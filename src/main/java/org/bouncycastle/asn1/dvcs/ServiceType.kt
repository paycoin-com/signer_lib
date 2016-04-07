package org.bouncycastle.asn1.dvcs

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject


/**
 * ServiceType ::= ENUMERATED { cpd(1), vsd(2), cpkc(3), ccpd(4) }
 */

class ServiceType : ASN1Object {

    private var value: ASN1Enumerated? = null

    constructor(value: Int) {
        this.value = ASN1Enumerated(value)
    }

    private constructor(value: ASN1Enumerated) {
        this.value = value
    }

    fun getValue(): BigInteger {
        return value!!.value
    }

    override fun toASN1Primitive(): ASN1Primitive {
        return value
    }

    override fun toString(): String {
        val num = value!!.value.toInt()
        return "" + num + if (num == CPD.getValue().toInt())
            "(CPD)"
        else if (num == VSD.getValue().toInt())
            "(VSD)"
        else if (num == VPKC.getValue().toInt())
            "(VPKC)"
        else if (num == CCPD.getValue().toInt())
            "(CCPD)"
        else
            "?"
    }

    companion object {
        /**
         * Identifier of CPD service (Certify Possession of Data).
         */
        val CPD = ServiceType(1)

        /**
         * Identifier of VSD service (Verify Signed Document).
         */
        val VSD = ServiceType(2)

        /**
         * Identifier of VPKC service (Verify Public Key Certificates (also referred to as CPKC)).
         */
        val VPKC = ServiceType(3)

        /**
         * Identifier of CCPD service (Certify Claim of Possession of Data).
         */
        val CCPD = ServiceType(4)

        fun getInstance(obj: Any?): ServiceType? {
            if (obj is ServiceType) {
                return obj
            } else if (obj != null) {
                return ServiceType(ASN1Enumerated.getInstance(obj))
            }

            return null
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ServiceType {
            return getInstance(ASN1Enumerated.getInstance(obj, explicit))
        }
    }

}

package org.bouncycastle.asn1.x509.qualified

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive

/**
 * The TypeOfBiometricData object.
 *
 * TypeOfBiometricData ::= CHOICE {
 * predefinedBiometricType   PredefinedBiometricType,
 * biometricDataOid          OBJECT IDENTIFIER }

 * PredefinedBiometricType ::= INTEGER {
 * picture(0),handwritten-signature(1)}
 * (picture|handwritten-signature)
 *
 */
class TypeOfBiometricData : ASN1Object, ASN1Choice {

    internal var obj: ASN1Encodable

    constructor(predefinedBiometricType: Int) {
        if (predefinedBiometricType == PICTURE || predefinedBiometricType == HANDWRITTEN_SIGNATURE) {
            obj = ASN1Integer(predefinedBiometricType.toLong())
        } else {
            throw IllegalArgumentException("unknow PredefinedBiometricType : " + predefinedBiometricType)
        }
    }

    constructor(BiometricDataID: ASN1ObjectIdentifier) {
        obj = BiometricDataID
    }

    val isPredefined: Boolean
        get() = obj is ASN1Integer

    val predefinedBiometricType: Int
        get() = (obj as ASN1Integer).value.toInt()

    val biometricDataOid: ASN1ObjectIdentifier
        get() = obj as ASN1ObjectIdentifier

    override fun toASN1Primitive(): ASN1Primitive {
        return obj.toASN1Primitive()
    }

    companion object {
        val PICTURE = 0
        val HANDWRITTEN_SIGNATURE = 1

        fun getInstance(obj: Any?): TypeOfBiometricData {
            if (obj == null || obj is TypeOfBiometricData) {
                return obj as TypeOfBiometricData?
            }

            if (obj is ASN1Integer) {
                val predefinedBiometricTypeObj = ASN1Integer.getInstance(obj)
                val predefinedBiometricType = predefinedBiometricTypeObj.value.toInt()

                return TypeOfBiometricData(predefinedBiometricType)
            } else if (obj is ASN1ObjectIdentifier) {
                val BiometricDataID = ASN1ObjectIdentifier.getInstance(obj)
                return TypeOfBiometricData(BiometricDataID)
            }

            throw IllegalArgumentException("unknown object in getInstance")
        }
    }
}

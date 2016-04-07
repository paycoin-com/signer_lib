package org.bouncycastle.asn1.x509.qualified

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x509.AlgorithmIdentifier

/**
 * The BiometricData object.
 *
 * BiometricData  ::=  SEQUENCE {
 * typeOfBiometricData  TypeOfBiometricData,
 * hashAlgorithm        AlgorithmIdentifier,
 * biometricDataHash    OCTET STRING,
 * sourceDataUri        IA5String OPTIONAL  }
 *
 */
class BiometricData : ASN1Object {
    var typeOfBiometricData: TypeOfBiometricData? = null
        private set
    var hashAlgorithm: AlgorithmIdentifier? = null
        private set
    var biometricDataHash: ASN1OctetString? = null
        private set
    var sourceDataUri: DERIA5String? = null
        private set

    private constructor(seq: ASN1Sequence) {
        val e = seq.objects

        // typeOfBiometricData
        typeOfBiometricData = TypeOfBiometricData.getInstance(e.nextElement())
        // hashAlgorithm
        hashAlgorithm = AlgorithmIdentifier.getInstance(e.nextElement())
        // biometricDataHash
        biometricDataHash = ASN1OctetString.getInstance(e.nextElement())
        // sourceDataUri
        if (e.hasMoreElements()) {
            sourceDataUri = DERIA5String.getInstance(e.nextElement())
        }
    }

    constructor(
            typeOfBiometricData: TypeOfBiometricData,
            hashAlgorithm: AlgorithmIdentifier,
            biometricDataHash: ASN1OctetString,
            sourceDataUri: DERIA5String) {
        this.typeOfBiometricData = typeOfBiometricData
        this.hashAlgorithm = hashAlgorithm
        this.biometricDataHash = biometricDataHash
        this.sourceDataUri = sourceDataUri
    }

    constructor(
            typeOfBiometricData: TypeOfBiometricData,
            hashAlgorithm: AlgorithmIdentifier,
            biometricDataHash: ASN1OctetString) {
        this.typeOfBiometricData = typeOfBiometricData
        this.hashAlgorithm = hashAlgorithm
        this.biometricDataHash = biometricDataHash
        this.sourceDataUri = null
    }

    override fun toASN1Primitive(): ASN1Primitive {
        val seq = ASN1EncodableVector()
        seq.add(typeOfBiometricData)
        seq.add(hashAlgorithm)
        seq.add(biometricDataHash)

        if (sourceDataUri != null) {
            seq.add(sourceDataUri)
        }

        return DERSequence(seq)
    }

    companion object {

        fun getInstance(
                obj: Any?): BiometricData? {
            if (obj is BiometricData) {
                return obj
            }

            if (obj != null) {
                return BiometricData(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

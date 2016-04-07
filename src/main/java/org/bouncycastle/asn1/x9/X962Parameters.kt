package org.bouncycastle.asn1.x9

import java.io.IOException

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Null
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject

/**
 * The Parameters ASN.1 CHOICE from X9.62.
 */
class X962Parameters : ASN1Object, ASN1Choice {
    var parameters: ASN1Primitive? = null
        private set

    constructor(
            ecParameters: X9ECParameters) {
        this.parameters = ecParameters.toASN1Primitive()
    }

    constructor(
            namedCurve: ASN1ObjectIdentifier) {
        this.parameters = namedCurve
    }

    constructor(
            obj: ASN1Null) {
        this.parameters = obj
    }


    @Deprecated("use getInstance()")
    constructor(
            obj: ASN1Primitive) {
        this.parameters = obj
    }

    val isNamedCurve: Boolean
        get() = parameters is ASN1ObjectIdentifier

    val isImplicitlyCA: Boolean
        get() = parameters is ASN1Null

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * Parameters ::= CHOICE {
     * ecParameters ECParameters,
     * namedCurve   CURVES.&amp;id({CurveNames}),
     * implicitlyCA NULL
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return parameters
    }

    companion object {

        fun getInstance(
                obj: Any?): X962Parameters {
            if (obj == null || obj is X962Parameters) {
                return obj as X962Parameters?
            }

            if (obj is ASN1Primitive) {
                return X962Parameters(obj as ASN1Primitive?)
            }

            if (obj is ByteArray) {
                try {
                    return X962Parameters(ASN1Primitive.fromByteArray(obj as ByteArray?))
                } catch (e: IOException) {
                    throw IllegalArgumentException("unable to parse encoded data: " + e.message)
                }

            }

            throw IllegalArgumentException("unknown object in getInstance()")
        }

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): X962Parameters {
            return getInstance(obj.`object`) // must be explicitly tagged
        }
    }
}

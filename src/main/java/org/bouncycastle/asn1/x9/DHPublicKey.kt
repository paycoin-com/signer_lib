package org.bouncycastle.asn1.x9

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject

/**
 * X9.42 definition of a DHPublicKey
 *
 * DHPublicKey ::= INTEGER
 *
 */
class DHPublicKey : ASN1Object {
    private var y: ASN1Integer? = null

    private constructor(y: ASN1Integer?) {
        if (y == null) {
            throw IllegalArgumentException("'y' cannot be null")
        }

        this.y = y
    }

    /**
     * Base constructor.

     * @param y the public value Y.
     */
    constructor(y: BigInteger?) {
        if (y == null) {
            throw IllegalArgumentException("'y' cannot be null")
        }

        this.y = ASN1Integer(y)
    }

    /**
     * Return the public value Y for the key.

     * @return the Y value.
     */
    fun getY(): BigInteger {
        return this.y!!.positiveValue
    }

    /**
     * Return an ASN.1 primitive representation of this object.

     * @return an ASN1Integer.
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return this.y
    }

    companion object {

        /**
         * Return a DHPublicKey from the passed in tagged object.

         * @param obj a tagged object.
         * *
         * @param explicit true if the contents of the object is explictly tagged, false otherwise.
         * *
         * @return a DHPublicKey
         */
        fun getInstance(obj: ASN1TaggedObject, explicit: Boolean): DHPublicKey {
            return getInstance(ASN1Integer.getInstance(obj, explicit))
        }

        /**
         * Return a DHPublicKey from the passed in object.

         * @param obj an object for conversion or a byte[].
         * *
         * @return a DHPublicKey
         */
        fun getInstance(obj: Any?): DHPublicKey {
            if (obj == null || obj is DHPublicKey) {
                return obj as DHPublicKey?
            }

            if (obj is ASN1Integer) {
                return DHPublicKey(obj as ASN1Integer?)
            }

            throw IllegalArgumentException("Invalid DHPublicKey: " + obj.javaClass.name)
        }
    }
}
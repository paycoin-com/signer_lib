package org.bouncycastle.asn1.x509

import java.math.BigInteger

import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class BasicConstraints : ASN1Object {
    internal var cA: ASN1Boolean? = ASN1Boolean.getInstance(false)
    internal var pathLenConstraint: ASN1Integer? = null

    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() == 0) {
            this.cA = null
            this.pathLenConstraint = null
        } else {
            if (seq.getObjectAt(0) is ASN1Boolean) {
                this.cA = ASN1Boolean.getInstance(seq.getObjectAt(0))
            } else {
                this.cA = null
                this.pathLenConstraint = ASN1Integer.getInstance(seq.getObjectAt(0))
            }
            if (seq.size() > 1) {
                if (this.cA != null) {
                    this.pathLenConstraint = ASN1Integer.getInstance(seq.getObjectAt(1))
                } else {
                    throw IllegalArgumentException("wrong sequence in constructor")
                }
            }
        }
    }

    constructor(
            cA: Boolean) {
        if (cA) {
            this.cA = ASN1Boolean.getInstance(true)
        } else {
            this.cA = null
        }
        this.pathLenConstraint = null
    }

    /**
     * create a cA=true object for the given path length constraint.

     * @param pathLenConstraint
     */
    constructor(
            pathLenConstraint: Int) {
        this.cA = ASN1Boolean.getInstance(true)
        this.pathLenConstraint = ASN1Integer(pathLenConstraint.toLong())
    }

    val isCA: Boolean
        get() = cA != null && cA!!.isTrue

    fun getPathLenConstraint(): BigInteger? {
        if (pathLenConstraint != null) {
            return pathLenConstraint!!.value
        }

        return null
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * BasicConstraints := SEQUENCE {
     * cA                  BOOLEAN DEFAULT FALSE,
     * pathLenConstraint   INTEGER (0..MAX) OPTIONAL
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        if (cA != null) {
            v.add(cA)
        }

        if (pathLenConstraint != null)
        // yes some people actually do this when cA is false...
        {
            v.add(pathLenConstraint)
        }

        return DERSequence(v)
    }

    override fun toString(): String {
        if (pathLenConstraint == null) {
            if (cA == null) {
                return "BasicConstraints: isCa(false)"
            }
            return "BasicConstraints: isCa(" + this.isCA + ")"
        }
        return "BasicConstraints: isCa(" + this.isCA + "), pathLenConstraint = " + pathLenConstraint!!.value
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): BasicConstraints {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): BasicConstraints? {
            if (obj is BasicConstraints) {
                return obj
            }
            if (obj is X509Extension) {
                return getInstance(X509Extension.convertValueToObject(obj as X509Extension?))
            }
            if (obj != null) {
                return BasicConstraints(ASN1Sequence.getInstance(obj))
            }

            return null
        }

        fun fromExtensions(extensions: Extensions): BasicConstraints {
            return BasicConstraints.getInstance(extensions.getExtensionParsedValue(Extension.basicConstraints))
        }
    }
}

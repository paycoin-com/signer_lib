package org.bouncycastle.asn1.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * Targets structure used in target information extension for attribute
 * certificates from RFC 3281.

 *
 * Targets ::= SEQUENCE OF Target

 * Target  ::= CHOICE {
 * targetName          [0] GeneralName,
 * targetGroup         [1] GeneralName,
 * targetCert          [2] TargetCert
 * }

 * TargetCert  ::= SEQUENCE {
 * targetCertificate    IssuerSerial,
 * targetName           GeneralName OPTIONAL,
 * certDigestInfo       ObjectDigestInfo OPTIONAL
 * }
 *

 * @see org.bouncycastle.asn1.x509.Target

 * @see org.bouncycastle.asn1.x509.TargetInformation
 */
class Targets : ASN1Object {
    private var targets: ASN1Sequence? = null

    /**
     * Constructor from ASN1Sequence.

     * @param targets The ASN.1 SEQUENCE.
     * *
     * @throws IllegalArgumentException if the contents of the sequence are
     * *             invalid.
     */
    private constructor(targets: ASN1Sequence) {
        this.targets = targets
    }

    /**
     * Constructor from given targets.
     *
     *
     * The vector is copied.

     * @param targets A `Vector` of [Target]s.
     * *
     * @see Target

     * @throws IllegalArgumentException if the vector contains not only Targets.
     */
    constructor(targets: Array<Target>) {
        this.targets = DERSequence(targets)
    }

    /**
     * Returns the targets in a `Vector`.
     *
     *
     * The vector is cloned before it is returned.

     * @return Returns the targets.
     */
    fun getTargets(): Array<Target> {
        val targs = arrayOfNulls<Target>(targets!!.size())
        var count = 0
        val e = targets!!.objects
        while (e.hasMoreElements()) {
            targs[count++] = Target.getInstance(e.nextElement())
        }
        return targs
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.

     * Returns:

     *
     * Targets ::= SEQUENCE OF Target
     *

     * @return a ASN1Primitive
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return targets
    }

    companion object {

        /**
         * Creates an instance of a Targets from the given object.
         *
         *
         * `obj` can be a Targets or a [ASN1Sequence]

         * @param obj The object.
         * *
         * @return A Targets instance.
         * *
         * @throws IllegalArgumentException if the given object cannot be
         * *             interpreted as Target.
         */
        fun getInstance(obj: Any?): Targets? {
            if (obj is Targets) {
                return obj
            } else if (obj != null) {
                return Targets(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

package org.bouncycastle.asn1.x509

import java.util.Enumeration

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence

/**
 * Target information extension for attributes certificates according to RFC
 * 3281.

 *
 * SEQUENCE OF Targets
 *

 */
class TargetInformation : ASN1Object {
    private var targets: ASN1Sequence? = null

    /**
     * Constructor from a ASN1Sequence.

     * @param seq The ASN1Sequence.
     * *
     * @throws IllegalArgumentException if the sequence does not contain
     * *             correctly encoded Targets elements.
     */
    private constructor(seq: ASN1Sequence) {
        targets = seq
    }

    /**
     * Returns the targets in this target information extension.

     * @return Returns the targets.
     */
    val targetsObjects: Array<Targets>
        get() {
            val copy = arrayOfNulls<Targets>(targets!!.size())
            var count = 0
            val e = targets!!.objects
            while (e.hasMoreElements()) {
                copy[count++] = Targets.getInstance(e.nextElement())
            }
            return copy
        }

    /**
     * Constructs a target information from a single targets element.
     * According to RFC 3281 only one targets element must be produced.

     * @param targets A Targets instance.
     */
    constructor(targets: Targets) {
        this.targets = DERSequence(targets)
    }

    /**
     * According to RFC 3281 only one targets element must be produced. If
     * multiple targets are given they must be merged in
     * into one targets element.

     * @param targets An array with [Targets].
     */
    constructor(targets: Array<Target>) : this(Targets(targets)) {
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.

     * Returns:

     *
     * SEQUENCE OF Targets
     *

     *
     *
     * According to RFC 3281 only one targets element must be produced. If
     * multiple targets are given in the constructor they are merged into one
     * targets element. If this was produced from a
     * [org.bouncycastle.asn1.ASN1Sequence] the encoding is kept.

     * @return a ASN1Primitive
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return targets
    }

    companion object {

        /**
         * Creates an instance of a TargetInformation from the given object.
         *
         *
         * `obj` can be a TargetInformation or a [ASN1Sequence]

         * @param obj The object.
         * *
         * @return A TargetInformation instance.
         * *
         * @throws IllegalArgumentException if the given object cannot be
         * *             interpreted as TargetInformation.
         */
        fun getInstance(obj: Any?): TargetInformation? {
            if (obj is TargetInformation) {
                return obj
            } else if (obj != null) {
                return TargetInformation(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

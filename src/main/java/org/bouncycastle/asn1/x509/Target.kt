package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERTaggedObject

/**
 * Target structure used in target information extension for attribute
 * certificates from RFC 3281.

 *
 * Target  ::= CHOICE {
 * targetName          [0] GeneralName,
 * targetGroup         [1] GeneralName,
 * targetCert          [2] TargetCert
 * }
 *

 *
 *
 * The targetCert field is currently not supported and must not be used
 * according to RFC 3281.
 */
class Target
/**
 * Constructor from ASN1TaggedObject.

 * @param tagObj The tagged object.
 * *
 * @throws IllegalArgumentException if the encoding is wrong.
 */
private constructor(tagObj: ASN1TaggedObject) : ASN1Object(), ASN1Choice {

    private var targName: GeneralName? = null
    private var targGroup: GeneralName? = null

    init {
        when (tagObj.tagNo) {
            targetName     // GeneralName is already a choice so explicit
            -> targName = GeneralName.getInstance(tagObj, true)
            targetGroup -> targGroup = GeneralName.getInstance(tagObj, true)
            else -> throw IllegalArgumentException("unknown tag: " + tagObj.tagNo)
        }
    }

    /**
     * Constructor from given details.
     *
     *
     * Exactly one of the parameters must be not `null`.

     * @param type the choice type to apply to the name.
     * *
     * @param name the general name.
     * *
     * @throws IllegalArgumentException if type is invalid.
     */
    constructor(type: Int, name: GeneralName) : this(DERTaggedObject(type, name)) {
    }

    /**
     * @return Returns the targetGroup.
     */
    fun getTargetGroup(): GeneralName {
        return targGroup
    }

    /**
     * @return Returns the targetName.
     */
    fun getTargetName(): GeneralName {
        return targName
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.

     * Returns:

     *
     * Target  ::= CHOICE {
     * targetName          [0] GeneralName,
     * targetGroup         [1] GeneralName,
     * targetCert          [2] TargetCert
     * }
     *

     * @return a ASN1Primitive
     */
    override fun toASN1Primitive(): ASN1Primitive {
        // GeneralName is a choice already so most be explicitly tagged
        if (targName != null) {
            return DERTaggedObject(true, 0, targName)
        } else {
            return DERTaggedObject(true, 1, targGroup)
        }
    }

    companion object {
        val targetName = 0
        val targetGroup = 1

        /**
         * Creates an instance of a Target from the given object.
         *
         *
         * `obj` can be a Target or a [ASN1TaggedObject]

         * @param obj The object.
         * *
         * @return A Target instance.
         * *
         * @throws IllegalArgumentException if the given object cannot be
         * *             interpreted as Target.
         */
        fun getInstance(obj: Any?): Target {
            if (obj == null || obj is Target) {
                return obj as Target?
            } else if (obj is ASN1TaggedObject) {
                return Target(obj as ASN1TaggedObject?)
            }

            throw IllegalArgumentException("unknown object in factory: " + obj.javaClass)
        }
    }
}

package org.bouncycastle.asn1.x509

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERTaggedObject

/**
 * Implementation of the RoleSyntax object as specified by the RFC3281.

 *
 * RoleSyntax ::= SEQUENCE {
 * roleAuthority  [0] GeneralNames OPTIONAL,
 * roleName       [1] GeneralName
 * }
 *
 */
class RoleSyntax : ASN1Object {
    /**
     * Gets the role authority of this RoleSyntax.
     * @return    an instance of `GeneralNames` holding the
     * * role authority of this RoleSyntax.
     */
    var roleAuthority: GeneralNames? = null
        private set
    /**
     * Gets the role name of this RoleSyntax.
     * @return    an instance of `GeneralName` holding the
     * * role name of this RoleSyntax.
     */
    var roleName: GeneralName? = null
        private set

    /**
     * Constructor.
     * @param roleAuthority the role authority of this RoleSyntax.
     * *
     * @param roleName    the role name of this RoleSyntax.
     */
    constructor(
            roleAuthority: GeneralNames?,
            roleName: GeneralName?) {
        if (roleName == null ||
                roleName.tagNo != GeneralName.uniformResourceIdentifier ||
                (roleName.name as ASN1String).string == "") {
            throw IllegalArgumentException("the role name MUST be non empty and MUST " + "use the URI option of GeneralName")
        }
        this.roleAuthority = roleAuthority
        this.roleName = roleName
    }

    /**
     * Constructor. Invoking this constructor is the same as invoking
     * `new RoleSyntax(null, roleName)`.
     * @param roleName    the role name of this RoleSyntax.
     */
    constructor(
            roleName: GeneralName) : this(null, roleName) {
    }

    /**
     * Utility constructor. Takes a `String` argument representing
     * the role name, builds a `GeneralName` to hold the role name
     * and calls the constructor that takes a `GeneralName`.
     * @param roleName
     */
    constructor(
            roleName: String?) : this(GeneralName(GeneralName.uniformResourceIdentifier,
            roleName ?: "")) {
    }

    /**
     * Constructor that builds an instance of `RoleSyntax` by
     * extracting the encoded elements from the `ASN1Sequence`
     * object supplied.
     * @param seq    an instance of `ASN1Sequence` that holds
     * * the encoded elements used to build this `RoleSyntax`.
     */
    private constructor(
            seq: ASN1Sequence) {
        if (seq.size() < 1 || seq.size() > 2) {
            throw IllegalArgumentException("Bad sequence size: " + seq.size())
        }

        for (i in 0..seq.size() - 1) {
            val taggedObject = ASN1TaggedObject.getInstance(seq.getObjectAt(i))
            when (taggedObject.tagNo) {
                0 -> roleAuthority = GeneralNames.getInstance(taggedObject, false)
                1 -> roleName = GeneralName.getInstance(taggedObject, true)
                else -> throw IllegalArgumentException("Unknown tag in RoleSyntax")
            }
        }
    }

    /**
     * Gets the role name as a `java.lang.String` object.
     * @return    the role name of this RoleSyntax represented as a
     * * `java.lang.String` object.
     */
    val roleNameAsString: String
        get() {
            val str = this.roleName!!.name as ASN1String

            return str.string
        }

    /**
     * Gets the role authority as a `String[]` object.
     * @return the role authority of this RoleSyntax represented as a
     * * `String[]` array.
     */
    val roleAuthorityAsString: Array<String>
        get() {
            if (roleAuthority == null) {
                return arrayOfNulls(0)
            }

            val names = roleAuthority!!.names
            val namesString = arrayOfNulls<String>(names.size)
            for (i in names.indices) {
                val value = names[i].name
                if (value is ASN1String) {
                    namesString[i] = value.string
                } else {
                    namesString[i] = value.toString()
                }
            }
            return namesString
        }

    /**
     * Implementation of the method `toASN1Object` as
     * required by the superclass `ASN1Encodable`.

     *
     * RoleSyntax ::= SEQUENCE {
     * roleAuthority  [0] GeneralNames OPTIONAL,
     * roleName       [1] GeneralName
     * }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()
        if (this.roleAuthority != null) {
            v.add(DERTaggedObject(false, 0, roleAuthority))
        }
        v.add(DERTaggedObject(true, 1, roleName))

        return DERSequence(v)
    }

    override fun toString(): String {
        val buff = StringBuffer("Name: " + this.roleNameAsString +
                " - Auth: ")
        if (this.roleAuthority == null || roleAuthority!!.names.size == 0) {
            buff.append("N/A")
        } else {
            val names = this.roleAuthorityAsString
            buff.append('[').append(names[0])
            for (i in 1..names.size - 1) {
                buff.append(", ").append(names[i])
            }
            buff.append(']')
        }
        return buff.toString()
    }

    companion object {

        /**
         * RoleSyntax factory method.
         * @param obj the object used to construct an instance of `
         * * RoleSyntax`. It must be an instance of `RoleSyntax
         * * ` or `ASN1Sequence`.
         * *
         * @return the instance of `RoleSyntax` built from the
         * * supplied object.
         * *
         * @throws java.lang.IllegalArgumentException if the object passed
         * * to the factory is not an instance of `RoleSyntax` or
         * * `ASN1Sequence`.
         */
        fun getInstance(
                obj: Any?): RoleSyntax? {

            if (obj is RoleSyntax) {
                return obj
            } else if (obj != null) {
                return RoleSyntax(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

package org.bouncycastle.asn1.x509

import java.util.Enumeration
import java.util.Hashtable
import java.util.Vector

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence

class Extensions : ASN1Object {
    private val extensions = Hashtable()
    private val ordering = Vector()

    /**
     * Constructor from ASN1Sequence.
     *
     *
     * The extensions are a list of constructed sequences, either with (OID, OctetString) or (OID, Boolean, OctetString)
     *
     */
    private constructor(
            seq: ASN1Sequence) {
        val e = seq.objects

        while (e.hasMoreElements()) {
            val ext = Extension.getInstance(e.nextElement())

            extensions.put(ext.extnId, ext)
            ordering.addElement(ext.extnId)
        }
    }

    /**
     * Base Constructor

     * @param extension a single extension.
     */
    constructor(
            extension: Extension) {
        this.ordering.addElement(extension.extnId)
        this.extensions.put(extension.extnId, extension)
    }

    /**
     * Base Constructor

     * @param extensions an array of extensions.
     */
    constructor(
            extensions: Array<Extension>) {
        for (i in extensions.indices) {
            val ext = extensions[i]

            this.ordering.addElement(ext.extnId)
            this.extensions.put(ext.extnId, ext)
        }
    }

    /**
     * return an Enumeration of the extension field's object ids.
     */
    fun oids(): Enumeration<Any> {
        return ordering.elements()
    }

    /**
     * return the extension represented by the object identifier
     * passed in.

     * @return the extension if it's present, null otherwise.
     */
    fun getExtension(
            oid: ASN1ObjectIdentifier): Extension? {
        return extensions.get(oid)
    }

    /**
     * return the parsed value of the extension represented by the object identifier
     * passed in.

     * @return the parsed value of the extension if it's present, null otherwise.
     */
    fun getExtensionParsedValue(oid: ASN1ObjectIdentifier): ASN1Encodable? {
        val ext = this.getExtension(oid)

        if (ext != null) {
            return ext.parsedValue
        }

        return null
    }

    /**
     *
     * Extensions        ::=   SEQUENCE SIZE (1..MAX) OF Extension

     * Extension         ::=   SEQUENCE {
     * extnId            EXTENSION.&amp;id ({ExtensionSet}),
     * critical          BOOLEAN DEFAULT FALSE,
     * extnValue         OCTET STRING }
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val vec = ASN1EncodableVector()
        val e = ordering.elements()

        while (e.hasMoreElements()) {

            vec.add(extensions.get(e.nextElement()))
        }

        return DERSequence(vec)
    }

    fun equivalent(
            other: Extensions): Boolean {
        if (extensions.size != other.extensions.size) {
            return false
        }

        val e1 = extensions.keys()

        while (e1.hasMoreElements()) {
            val key = e1.nextElement()

            if (extensions.get(key) != other.extensions.get(key)) {
                return false
            }
        }

        return true
    }

    val extensionOIDs: Array<ASN1ObjectIdentifier>
        get() = toOidArray(ordering)

    val nonCriticalExtensionOIDs: Array<ASN1ObjectIdentifier>
        get() = getExtensionOIDs(false)

    val criticalExtensionOIDs: Array<ASN1ObjectIdentifier>
        get() = getExtensionOIDs(true)

    private fun getExtensionOIDs(isCritical: Boolean): Array<ASN1ObjectIdentifier> {
        val oidVec = Vector()

        for (i in ordering.indices) {
            val oid = ordering.elementAt(i)

            if ((extensions.get(oid) as Extension).isCritical == isCritical) {
                oidVec.addElement(oid)
            }
        }

        return toOidArray(oidVec)
    }

    private fun toOidArray(oidVec: Vector<Any>): Array<ASN1ObjectIdentifier> {
        val oids = arrayOfNulls<ASN1ObjectIdentifier>(oidVec.size)

        for (i in oids.indices) {
            oids[i] = oidVec.elementAt(i) as ASN1ObjectIdentifier
        }
        return oids
    }

    companion object {

        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): Extensions {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): Extensions? {
            if (obj is Extensions) {
                return obj
            } else if (obj != null) {
                return Extensions(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}

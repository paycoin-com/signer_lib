package org.bouncycastle.asn1.smime

import java.util.Enumeration
import java.util.Vector

import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.cms.Attribute
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers

/**
 * Handler class for dealing with S/MIME Capabilities
 */
class SMIMECapabilities(
        private val capabilities: ASN1Sequence) : ASN1Object() {

    /**
     * returns a vector with 0 or more objects of all the capabilities
     * matching the passed in capability OID. If the OID passed is null the
     * entire set is returned.
     */
    fun getCapabilities(
            capability: ASN1ObjectIdentifier?): Vector<Any> {
        val e = capabilities.objects
        val list = Vector()

        if (capability == null) {
            while (e.hasMoreElements()) {
                val cap = SMIMECapability.getInstance(e.nextElement())

                list.addElement(cap)
            }
        } else {
            while (e.hasMoreElements()) {
                val cap = SMIMECapability.getInstance(e.nextElement())

                if (capability == cap.capabilityID) {
                    list.addElement(cap)
                }
            }
        }

        return list
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     *
     * SMIMECapabilities ::= SEQUENCE OF SMIMECapability
     *
     */
    override fun toASN1Primitive(): ASN1Primitive {
        return capabilities
    }

    companion object {
        /**
         * general preferences
         */
        val preferSignedData = PKCSObjectIdentifiers.preferSignedData
        val canNotDecryptAny = PKCSObjectIdentifiers.canNotDecryptAny
        val sMIMECapabilitesVersions = PKCSObjectIdentifiers.sMIMECapabilitiesVersions

        /**
         * encryption algorithms preferences
         */
        val aes256_CBC = NISTObjectIdentifiers.id_aes256_CBC
        val aes192_CBC = NISTObjectIdentifiers.id_aes192_CBC
        val aes128_CBC = NISTObjectIdentifiers.id_aes128_CBC
        val idea_CBC = ASN1ObjectIdentifier("1.3.6.1.4.1.188.7.1.1.2")
        val cast5_CBC = ASN1ObjectIdentifier("1.2.840.113533.7.66.10")
        val dES_CBC = ASN1ObjectIdentifier("1.3.14.3.2.7")
        val dES_EDE3_CBC = PKCSObjectIdentifiers.des_EDE3_CBC
        val rC2_CBC = PKCSObjectIdentifiers.RC2_CBC

        /**
         * return an Attribute object from the given object.

         * @param o the object we want converted.
         * *
         * @exception IllegalArgumentException if the object cannot be converted.
         */
        fun getInstance(
                o: Any?): SMIMECapabilities {
            if (o == null || o is SMIMECapabilities) {
                return o as SMIMECapabilities?
            }

            if (o is ASN1Sequence) {
                return SMIMECapabilities(o as ASN1Sequence?)
            }

            if (o is Attribute) {
                return SMIMECapabilities(
                        o.attrValues!!.getObjectAt(0) as ASN1Sequence)
            }

            throw IllegalArgumentException("unknown object in factory: " + o.javaClass.name)
        }
    }
}

package org.bouncycastle.asn1.eac

import java.io.IOException
import java.util.Hashtable

import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.DERApplicationSpecific
import org.bouncycastle.util.Integers

/**
 * an Iso7816CertificateHolderAuthorization structure.
 *
 * Certificate Holder Authorization ::= SEQUENCE {
 * // specifies the format and the rules for the evaluation of the authorization
 * // level
 * ASN1ObjectIdentifier        oid,
 * // access rights
 * DERApplicationSpecific    accessRights,
 * }
 *
 */
class CertificateHolderAuthorization : ASN1Object {
    /**
     * @return the Object identifier
     */
    /**
     * set the Object Identifier

     * @param oid [ASN1ObjectIdentifier] containing the Object Identifier
     */
    var oid: ASN1ObjectIdentifier
        private set(oid) {
            this.oid = oid
        }
    internal var accessRights: DERApplicationSpecific

    @Throws(IOException::class)
    private fun setPrivateData(cha: ASN1InputStream) {
        var obj: ASN1Primitive
        obj = cha.readObject()
        if (obj is ASN1ObjectIdentifier) {
            this.oid = obj
        } else {
            throw IllegalArgumentException("no Oid in CerticateHolderAuthorization")
        }
        obj = cha.readObject()
        if (obj is DERApplicationSpecific) {
            this.accessRights = obj
        } else {
            throw IllegalArgumentException("No access rights in CerticateHolderAuthorization")
        }
    }


    /**
     * create an Iso7816CertificateHolderAuthorization according to the parameters

     * @param oid    Object Identifier : specifies the format and the rules for the
     * *               evaluatioin of the authorization level.
     * *
     * @param rights specifies the access rights
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    constructor(oid: ASN1ObjectIdentifier, rights: Int) {
        oid = oid
        setAccessRights(rights.toByte())
    }

    /**
     * create an Iso7816CertificateHolderAuthorization according to the [DERApplicationSpecific]

     * @param aSpe the DERApplicationSpecific containing the data
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    constructor(aSpe: DERApplicationSpecific) {
        if (aSpe.applicationTag == EACTags.CERTIFICATE_HOLDER_AUTHORIZATION_TEMPLATE) {
            setPrivateData(ASN1InputStream(aSpe.contents))
        }
    }

    /**
     * @return containing the access rights
     */
    fun getAccessRights(): Int {
        return accessRights.contents[0] and 0xff
    }

    /**
     * create a DERApplicationSpecific and set the access rights to "rights"

     * @param rights byte containing the rights.
     */
    private fun setAccessRights(rights: Byte) {
        val accessRights = ByteArray(1)
        accessRights[0] = rights
        this.accessRights = DERApplicationSpecific(
                EACTags.getTag(EACTags.DISCRETIONARY_DATA), accessRights)
    }

    /**
     * return the Certificate Holder Authorization as a DERApplicationSpecific Object
     */
    override fun toASN1Primitive(): ASN1Primitive {
        val v = ASN1EncodableVector()

        v.add(oid)
        v.add(accessRights)

        return DERApplicationSpecific(EACTags.CERTIFICATE_HOLDER_AUTHORIZATION_TEMPLATE, v)
    }

    companion object {
        val id_role_EAC = EACObjectIdentifiers.bsi_de.branch("3.1.2.1")
        val CVCA = 0xC0
        val DV_DOMESTIC = 0x80
        val DV_FOREIGN = 0x40
        val IS = 0
        val RADG4 = 0x02//Read Access to DG4 (Iris)
        val RADG3 = 0x01//Read Access to DG3 (fingerprint)

        internal var RightsDecodeMap = Hashtable()
        internal var AuthorizationRole = BidirectionalMap()
        internal var ReverseMap = Hashtable()

        init {
            RightsDecodeMap.put(Integers.valueOf(RADG4), "RADG4")
            RightsDecodeMap.put(Integers.valueOf(RADG3), "RADG3")

            AuthorizationRole.put(Integers.valueOf(CVCA), "CVCA")
            AuthorizationRole.put(Integers.valueOf(DV_DOMESTIC), "DV_DOMESTIC")
            AuthorizationRole.put(Integers.valueOf(DV_FOREIGN), "DV_FOREIGN")
            AuthorizationRole.put(Integers.valueOf(IS), "IS")

            /*
          for (int i : RightsDecodeMap.keySet())
              ReverseMap.put(RightsDecodeMap.get(i), i);

          for (int i : AuthorizationRole.keySet())
              ReverseMap.put(AuthorizationRole.get(i), i);
          */
        }

        fun getRoleDescription(i: Int): String {
            return AuthorizationRole[Integers.valueOf(i)] as String
        }

        fun getFlag(description: String): Int {
            val i = AuthorizationRole.getReverse(description) as Int ?: throw IllegalArgumentException("Unknown value " + description)

            return i.toInt()
        }
    }
}

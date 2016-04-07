package org.bouncycastle.asn1.x509

import java.io.IOException
import java.util.StringTokenizer

import org.bouncycastle.asn1.ASN1Choice
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DERTaggedObject
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.util.IPAddress

/**
 * The GeneralName object.
 *
 * GeneralName ::= CHOICE {
 * otherName                       [0]     OtherName,
 * rfc822Name                      [1]     IA5String,
 * dNSName                         [2]     IA5String,
 * x400Address                     [3]     ORAddress,
 * directoryName                   [4]     Name,
 * ediPartyName                    [5]     EDIPartyName,
 * uniformResourceIdentifier       [6]     IA5String,
 * iPAddress                       [7]     OCTET STRING,
 * registeredID                    [8]     OBJECT IDENTIFIER}

 * OtherName ::= SEQUENCE {
 * type-id    OBJECT IDENTIFIER,
 * value      [0] EXPLICIT ANY DEFINED BY type-id }

 * EDIPartyName ::= SEQUENCE {
 * nameAssigner            [0]     DirectoryString OPTIONAL,
 * partyName               [1]     DirectoryString }

 * Name ::= CHOICE { RDNSequence }
 *
 */
class GeneralName : ASN1Object, ASN1Choice {

    var name: ASN1Encodable? = null
        private set
    var tagNo: Int = 0
        private set

    /**
     * @param dirName
     */
    @Deprecated("use X500Name constructor.\n      ")
    constructor(
            dirName: X509Name) {
        this.name = X500Name.getInstance(dirName)
        this.tagNo = 4
    }

    constructor(
            dirName: X500Name) {
        this.name = dirName
        this.tagNo = 4
    }

    /**
     * When the subjectAltName extension contains an Internet mail address,
     * the address MUST be included as an rfc822Name. The format of an
     * rfc822Name is an "addr-spec" as defined in RFC 822 [RFC 822].

     * When the subjectAltName extension contains a domain name service
     * label, the domain name MUST be stored in the dNSName (an IA5String).
     * The name MUST be in the "preferred name syntax," as specified by RFC
     * 1034 [RFC 1034].

     * When the subjectAltName extension contains a URI, the name MUST be
     * stored in the uniformResourceIdentifier (an IA5String). The name MUST
     * be a non-relative URL, and MUST follow the URL syntax and encoding
     * rules specified in [RFC 1738].  The name must include both a scheme
     * (e.g., "http" or "ftp") and a scheme-specific-part.  The scheme-
     * specific-part must include a fully qualified domain name or IP
     * address as the host.

     * When the subjectAltName extension contains a iPAddress, the address
     * MUST be stored in the octet string in "network byte order," as
     * specified in RFC 791 [RFC 791]. The least significant bit (LSB) of
     * each octet is the LSB of the corresponding byte in the network
     * address. For IP Version 4, as specified in RFC 791, the octet string
     * MUST contain exactly four octets.  For IP Version 6, as specified in
     * RFC 1883, the octet string MUST contain exactly sixteen octets [RFC
     * 1883].
     */
    constructor(
            tag: Int,
            name: ASN1Encodable) {
        this.name = name
        this.tagNo = tag
    }

    /**
     * Create a GeneralName for the given tag from the passed in String.
     *
     *
     * This constructor can handle:
     *
     *  * rfc822Name
     *  * iPAddress
     *  * directoryName
     *  * dNSName
     *  * uniformResourceIdentifier
     *  * registeredID
     *
     * For x400Address, otherName and ediPartyName there is no common string
     * format defined.
     *
     *
     * Note: A directory name can be encoded in different ways into a byte
     * representation. Be aware of this if the byte representation is used for
     * comparing results.

     * @param tag tag number
     * *
     * @param name string representation of name
     * *
     * @throws IllegalArgumentException if the string encoding is not correct or     *             not supported.
     */
    constructor(
            tag: Int,
            name: String) {
        this.tagNo = tag

        if (tag == rfc822Name || tag == dNSName || tag == uniformResourceIdentifier) {
            this.name = DERIA5String(name)
        } else if (tag == registeredID) {
            this.name = ASN1ObjectIdentifier(name)
        } else if (tag == directoryName) {
            this.name = X500Name(name)
        } else if (tag == iPAddress) {
            val enc = toGeneralNameEncoding(name)
            if (enc != null) {
                this.name = DEROctetString(enc)
            } else {
                throw IllegalArgumentException("IP Address is invalid")
            }
        } else {
            throw IllegalArgumentException("can't process String for tag: " + tag)
        }
    }

    override fun toString(): String {
        val buf = StringBuffer()

        buf.append(tagNo)
        buf.append(": ")
        when (tagNo) {
            rfc822Name, dNSName, uniformResourceIdentifier -> buf.append(DERIA5String.getInstance(name).string)
            directoryName -> buf.append(X500Name.getInstance(name).toString())
            else -> buf.append(name!!.toString())
        }
        return buf.toString()
    }

    private fun toGeneralNameEncoding(ip: String): ByteArray? {
        if (IPAddress.isValidIPv6WithNetmask(ip) || IPAddress.isValidIPv6(ip)) {
            val slashIndex = ip.indexOf('/')

            if (slashIndex < 0) {
                val addr = ByteArray(16)
                val parsedIp = parseIPv6(ip)
                copyInts(parsedIp, addr, 0)

                return addr
            } else {
                val addr = ByteArray(32)
                var parsedIp = parseIPv6(ip.substring(0, slashIndex))
                copyInts(parsedIp, addr, 0)
                val mask = ip.substring(slashIndex + 1)
                if (mask.indexOf(':') > 0) {
                    parsedIp = parseIPv6(mask)
                } else {
                    parsedIp = parseMask(mask)
                }
                copyInts(parsedIp, addr, 16)

                return addr
            }
        } else if (IPAddress.isValidIPv4WithNetmask(ip) || IPAddress.isValidIPv4(ip)) {
            val slashIndex = ip.indexOf('/')

            if (slashIndex < 0) {
                val addr = ByteArray(4)

                parseIPv4(ip, addr, 0)

                return addr
            } else {
                val addr = ByteArray(8)

                parseIPv4(ip.substring(0, slashIndex), addr, 0)

                val mask = ip.substring(slashIndex + 1)
                if (mask.indexOf('.') > 0) {
                    parseIPv4(mask, addr, 4)
                } else {
                    parseIPv4Mask(mask, addr, 4)
                }

                return addr
            }
        }

        return null
    }

    private fun parseIPv4Mask(mask: String, addr: ByteArray, offset: Int) {
        val maskVal = Integer.parseInt(mask)

        for (i in 0..maskVal - 1) {
            addr[i / 8 + offset] = addr[i / 8 + offset] or (1 shl 7 - i % 8).toByte()
        }
    }

    private fun parseIPv4(ip: String, addr: ByteArray, offset: Int) {
        val sTok = StringTokenizer(ip, "./")
        var index = 0

        while (sTok.hasMoreTokens()) {
            addr[offset + index++] = Integer.parseInt(sTok.nextToken()).toByte()
        }
    }

    private fun parseMask(mask: String): IntArray {
        val res = IntArray(8)
        val maskVal = Integer.parseInt(mask)

        for (i in 0..maskVal - 1) {
            res[i / 16] = res[i / 16] or (1 shl 15 - i % 16)
        }
        return res
    }

    private fun copyInts(parsedIp: IntArray, addr: ByteArray, offSet: Int) {
        for (i in parsedIp.indices) {
            addr[i * 2 + offSet] = (parsedIp[i] shr 8).toByte()
            addr[i * 2 + 1 + offSet] = parsedIp[i].toByte()
        }
    }

    private fun parseIPv6(ip: String): IntArray {
        val sTok = StringTokenizer(ip, ":", true)
        var index = 0
        val `val` = IntArray(8)

        if (ip[0] == ':' && ip[1] == ':') {
            sTok.nextToken() // skip the first one
        }

        var doubleColon = -1

        while (sTok.hasMoreTokens()) {
            val e = sTok.nextToken()

            if (e == ":") {
                doubleColon = index
                `val`[index++] = 0
            } else {
                if (e.indexOf('.') < 0) {
                    `val`[index++] = Integer.parseInt(e, 16)
                    if (sTok.hasMoreTokens()) {
                        sTok.nextToken()
                    }
                } else {
                    val eTok = StringTokenizer(e, ".")

                    `val`[index++] = Integer.parseInt(eTok.nextToken()) shl 8 or Integer.parseInt(eTok.nextToken())
                    `val`[index++] = Integer.parseInt(eTok.nextToken()) shl 8 or Integer.parseInt(eTok.nextToken())
                }
            }
        }

        if (index != `val`.size) {
            System.arraycopy(`val`, doubleColon, `val`, `val`.size - (index - doubleColon), index - doubleColon)
            for (i in doubleColon..`val`.size - (index - doubleColon) - 1) {
                `val`[i] = 0
            }
        }

        return `val`
    }

    override fun toASN1Primitive(): ASN1Primitive {
        if (tagNo == directoryName)
        // directoryName is explicitly tagged as it is a CHOICE
        {
            return DERTaggedObject(true, tagNo, name)
        } else {
            return DERTaggedObject(false, tagNo, name)
        }
    }

    companion object {
        val otherName = 0
        val rfc822Name = 1
        val dNSName = 2
        val x400Address = 3
        val directoryName = 4
        val ediPartyName = 5
        val uniformResourceIdentifier = 6
        val iPAddress = 7
        val registeredID = 8

        fun getInstance(
                obj: Any?): GeneralName {
            if (obj == null || obj is GeneralName) {
                return obj as GeneralName?
            }

            if (obj is ASN1TaggedObject) {
                val tag = obj.tagNo

                when (tag) {
                    otherName -> return GeneralName(tag, ASN1Sequence.getInstance(obj, false))
                    rfc822Name -> return GeneralName(tag, DERIA5String.getInstance(obj, false))
                    dNSName -> return GeneralName(tag, DERIA5String.getInstance(obj, false))
                    x400Address -> throw IllegalArgumentException("unknown tag: " + tag)
                    directoryName -> return GeneralName(tag, X500Name.getInstance(obj, true))
                    ediPartyName -> return GeneralName(tag, ASN1Sequence.getInstance(obj, false))
                    uniformResourceIdentifier -> return GeneralName(tag, DERIA5String.getInstance(obj, false))
                    iPAddress -> return GeneralName(tag, ASN1OctetString.getInstance(obj, false))
                    registeredID -> return GeneralName(tag, ASN1ObjectIdentifier.getInstance(obj, false))
                }
            }

            if (obj is ByteArray) {
                try {
                    return getInstance(ASN1Primitive.fromByteArray(obj as ByteArray?))
                } catch (e: IOException) {
                    throw IllegalArgumentException("unable to parse encoded general name")
                }

            }

            throw IllegalArgumentException("unknown object in getInstance: " + obj.javaClass.name)
        }

        fun getInstance(
                tagObj: ASN1TaggedObject,
                explicit: Boolean): GeneralName {
            return GeneralName.getInstance(ASN1TaggedObject.getInstance(tagObj, true))
        }
    }
}

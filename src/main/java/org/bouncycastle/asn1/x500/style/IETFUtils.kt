package org.bouncycastle.asn1.x500.style

import java.io.IOException
import java.util.Enumeration
import java.util.Hashtable
import java.util.Vector

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.DERUniversalString
import org.bouncycastle.asn1.x500.AttributeTypeAndValue
import org.bouncycastle.asn1.x500.RDN
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.X500NameStyle
import org.bouncycastle.util.Strings
import org.bouncycastle.util.encoders.Hex

object IETFUtils {
    private fun unescape(elt: String): String {
        if (elt.length == 0 || elt.indexOf('\\') < 0 && elt.indexOf('"') < 0) {
            return elt.trim { it <= ' ' }
        }

        val elts = elt.toCharArray()
        var escaped = false
        var quoted = false
        val buf = StringBuffer(elt.length)
        var start = 0

        // if it's an escaped hash string and not an actual encoding in string form
        // we need to leave it escaped.
        if (elts[0] == '\\') {
            if (elts[1] == '#') {
                start = 2
                buf.append("\\#")
            }
        }

        var nonWhiteSpaceEncountered = false
        var lastEscaped = 0
        var hex1: Char = 0.toChar()

        for (i in start..elts.size - 1) {
            val c = elts[i]

            if (c != ' ') {
                nonWhiteSpaceEncountered = true
            }

            if (c == '"') {
                if (!escaped) {
                    quoted = !quoted
                } else {
                    buf.append(c)
                }
                escaped = false
            } else if (c == '\\' && !(escaped || quoted)) {
                escaped = true
                lastEscaped = buf.length
            } else {
                if (c == ' ' && !escaped && !nonWhiteSpaceEncountered) {
                    continue
                }
                if (escaped && isHexDigit(c)) {
                    if (hex1.toInt() != 0) {
                        buf.append((convertHex(hex1) * 16 + convertHex(c)).toChar())
                        escaped = false
                        hex1 = 0.toChar()
                        continue
                    }
                    hex1 = c
                    continue
                }
                buf.append(c)
                escaped = false
            }
        }

        if (buf.length > 0) {
            while (buf[buf.length - 1] == ' ' && lastEscaped != buf.length - 1) {
                buf.setLength(buf.length - 1)
            }
        }

        return buf.toString()
    }

    private fun isHexDigit(c: Char): Boolean {
        return '0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F'
    }

    private fun convertHex(c: Char): Int {
        if ('0' <= c && c <= '9') {
            return c - '0'
        }
        if ('a' <= c && c <= 'f') {
            return c - 'a' + 10
        }
        return c - 'A' + 10
    }

    fun rDNsFromString(name: String, x500Style: X500NameStyle): Array<RDN> {
        val nTok = X500NameTokenizer(name)
        val builder = X500NameBuilder(x500Style)

        while (nTok.hasMoreTokens()) {
            val token = nTok.nextToken()

            if (token.indexOf('+') > 0) {
                val pTok = X500NameTokenizer(token, '+')
                var vTok = X500NameTokenizer(pTok.nextToken(), '=')

                var attr: String = vTok.nextToken()

                if (!vTok.hasMoreTokens()) {
                    throw IllegalArgumentException("badly formatted directory string")
                }

                var value: String = vTok.nextToken()
                var oid = x500Style.attrNameToOID(attr.trim { it <= ' ' })

                if (pTok.hasMoreTokens()) {
                    val oids = Vector()
                    val values = Vector()

                    oids.addElement(oid)
                    values.addElement(unescape(value))

                    while (pTok.hasMoreTokens()) {
                        vTok = X500NameTokenizer(pTok.nextToken(), '=')

                        attr = vTok.nextToken()

                        if (!vTok.hasMoreTokens()) {
                            throw IllegalArgumentException("badly formatted directory string")
                        }

                        value = vTok.nextToken()
                        oid = x500Style.attrNameToOID(attr.trim { it <= ' ' })


                        oids.addElement(oid)
                        values.addElement(unescape(value))
                    }

                    builder.addMultiValuedRDN(toOIDArray(oids), toValueArray(values))
                } else {
                    builder.addRDN(oid, unescape(value))
                }
            } else {
                val vTok = X500NameTokenizer(token, '=')

                val attr = vTok.nextToken()

                if (!vTok.hasMoreTokens()) {
                    throw IllegalArgumentException("badly formatted directory string")
                }

                val value = vTok.nextToken()
                val oid = x500Style.attrNameToOID(attr.trim { it <= ' ' })

                builder.addRDN(oid, unescape(value))
            }
        }

        return builder.build().rdNs
    }

    private fun toValueArray(values: Vector<Any>): Array<String> {
        val tmp = arrayOfNulls<String>(values.size)

        for (i in tmp.indices) {
            tmp[i] = values.elementAt(i) as String
        }

        return tmp
    }

    private fun toOIDArray(oids: Vector<Any>): Array<ASN1ObjectIdentifier> {
        val tmp = arrayOfNulls<ASN1ObjectIdentifier>(oids.size)

        for (i in tmp.indices) {
            tmp[i] = oids.elementAt(i) as ASN1ObjectIdentifier
        }

        return tmp
    }

    fun findAttrNamesForOID(
            oid: ASN1ObjectIdentifier,
            lookup: Hashtable<Any, Any>): Array<String> {
        var count = 0
        run {
            val en = lookup.elements()
            while (en.hasMoreElements()) {
                if (oid == en.nextElement()) {
                    count++
                }
            }
        }

        val aliases = arrayOfNulls<String>(count)
        count = 0

        val en = lookup.keys()
        while (en.hasMoreElements()) {
            val key = en.nextElement() as String
            if (oid == lookup[key]) {
                aliases[count++] = key
            }
        }

        return aliases
    }

    fun decodeAttrName(
            name: String,
            lookUp: Hashtable<Any, Any>): ASN1ObjectIdentifier {
        if (Strings.toUpperCase(name).startsWith("OID.")) {
            return ASN1ObjectIdentifier(name.substring(4))
        } else if (name[0] >= '0' && name[0] <= '9') {
            return ASN1ObjectIdentifier(name)
        }

        val oid = lookUp[Strings.toLowerCase(name)] as ASN1ObjectIdentifier ?: throw IllegalArgumentException("Unknown object id - $name - passed to distinguished name")

        return oid
    }

    @Throws(IOException::class)
    fun valueFromHexString(
            str: String,
            off: Int): ASN1Encodable {
        val data = ByteArray((str.length - off) / 2)
        for (index in data.indices) {
            val left = str[index * 2 + off]
            val right = str[index * 2 + off + 1]

            data[index] = (convertHex(left) shl 4 or convertHex(right)).toByte()
        }

        return ASN1Primitive.fromByteArray(data)
    }

    fun appendRDN(
            buf: StringBuffer,
            rdn: RDN,
            oidSymbols: Hashtable<Any, Any>) {
        if (rdn.isMultiValued) {
            val atv = rdn.typesAndValues
            var firstAtv = true

            for (j in atv.indices) {
                if (firstAtv) {
                    firstAtv = false
                } else {
                    buf.append('+')
                }

                IETFUtils.appendTypeAndValue(buf, atv[j], oidSymbols)
            }
        } else {
            if (rdn.first != null) {
                IETFUtils.appendTypeAndValue(buf, rdn.first, oidSymbols)
            }
        }
    }

    fun appendTypeAndValue(
            buf: StringBuffer,
            typeAndValue: AttributeTypeAndValue,
            oidSymbols: Hashtable<Any, Any>) {
        val sym = oidSymbols[typeAndValue.type] as String

        if (sym != null) {
            buf.append(sym)
        } else {
            buf.append(typeAndValue.type.id)
        }

        buf.append('=')

        buf.append(valueToString(typeAndValue.value))
    }

    fun valueToString(value: ASN1Encodable): String {
        val vBuf = StringBuffer()

        if (value is ASN1String && value !is DERUniversalString) {
            val v = value.string
            if (v.length > 0 && v[0] == '#') {
                vBuf.append("\\" + v)
            } else {
                vBuf.append(v)
            }
        } else {
            try {
                vBuf.append("#" + bytesToString(Hex.encode(value.toASN1Primitive().getEncoded(ASN1Encoding.DER))))
            } catch (e: IOException) {
                throw IllegalArgumentException("Other value has no encoded form")
            }

        }

        var end = vBuf.length
        var index = 0

        if (vBuf.length >= 2 && vBuf[0] == '\\' && vBuf[1] == '#') {
            index += 2
        }

        while (index != end) {
            if (vBuf[index] == ','
                    || vBuf[index] == '"'
                    || vBuf[index] == '\\'
                    || vBuf[index] == '+'
                    || vBuf[index] == '='
                    || vBuf[index] == '<'
                    || vBuf[index] == '>'
                    || vBuf[index] == ';') {
                vBuf.insert(index, "\\")
                index++
                end++
            }

            index++
        }

        var start = 0
        if (vBuf.length > 0) {
            while (vBuf.length > start && vBuf[start] == ' ') {
                vBuf.insert(start, "\\")
                start += 2
            }
        }

        var endBuf = vBuf.length - 1

        while (endBuf >= 0 && vBuf[endBuf] == ' ') {
            vBuf.insert(endBuf, '\\')
            endBuf--
        }

        return vBuf.toString()
    }

    private fun bytesToString(
            data: ByteArray): String {
        val cs = CharArray(data.size)

        for (i in cs.indices) {
            cs[i] = (data[i] and 0xff).toChar()
        }

        return String(cs)
    }

    fun canonicalize(s: String): String {
        var value = Strings.toLowerCase(s)

        if (value.length > 0 && value[0] == '#') {
            val obj = decodeObject(value)

            if (obj is ASN1String) {
                value = Strings.toLowerCase(obj.string)
            }
        }

        if (value.length > 1) {
            var start = 0
            while (start + 1 < value.length && value[start] == '\\' && value[start + 1] == ' ') {
                start += 2
            }

            var end = value.length - 1
            while (end - 1 > 0 && value[end - 1] == '\\' && value[end] == ' ') {
                end -= 2
            }

            if (start > 0 || end < value.length - 1) {
                value = value.substring(start, end + 1)
            }
        }

        value = stripInternalSpaces(value)

        return value
    }

    private fun decodeObject(oValue: String): ASN1Primitive {
        try {
            return ASN1Primitive.fromByteArray(Hex.decode(oValue.substring(1)))
        } catch (e: IOException) {
            throw IllegalStateException("unknown encoding in name: " + e)
        }

    }

    fun stripInternalSpaces(
            str: String): String {
        val res = StringBuffer()

        if (str.length != 0) {
            var c1 = str[0]

            res.append(c1)

            for (k in 1..str.length - 1) {
                val c2 = str[k]
                if (!(c1 == ' ' && c2 == ' ')) {
                    res.append(c2)
                }
                c1 = c2
            }
        }

        return res.toString()
    }

    fun rDNAreEqual(rdn1: RDN, rdn2: RDN): Boolean {
        if (rdn1.isMultiValued) {
            if (rdn2.isMultiValued) {
                val atvs1 = rdn1.typesAndValues
                val atvs2 = rdn2.typesAndValues

                if (atvs1.size != atvs2.size) {
                    return false
                }

                for (i in atvs1.indices) {
                    if (!atvAreEqual(atvs1[i], atvs2[i])) {
                        return false
                    }
                }
            } else {
                return false
            }
        } else {
            if (!rdn2.isMultiValued) {
                return atvAreEqual(rdn1.first, rdn2.first)
            } else {
                return false
            }
        }

        return true
    }

    private fun atvAreEqual(atv1: AttributeTypeAndValue?, atv2: AttributeTypeAndValue?): Boolean {
        if (atv1 === atv2) {
            return true
        }

        if (atv1 == null) {
            return false
        }

        if (atv2 == null) {
            return false
        }

        val o1 = atv1.type
        val o2 = atv2.type

        if (o1 != o2) {
            return false
        }

        val v1 = IETFUtils.canonicalize(IETFUtils.valueToString(atv1.value))
        val v2 = IETFUtils.canonicalize(IETFUtils.valueToString(atv2.value))

        if (v1 != v2) {
            return false
        }

        return true
    }
}

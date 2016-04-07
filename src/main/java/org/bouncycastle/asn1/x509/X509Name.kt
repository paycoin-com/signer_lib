package org.bouncycastle.asn1.x509

import java.io.IOException
import java.util.Enumeration
import java.util.Hashtable
import java.util.Vector

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1EncodableVector
import org.bouncycastle.asn1.ASN1Encoding
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1String
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERSet
import org.bouncycastle.asn1.DERUniversalString
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.util.Strings
import org.bouncycastle.util.encoders.Hex

/**
 *
 * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName

 * RelativeDistinguishedName ::= SET SIZE (1..MAX) OF AttributeTypeAndValue

 * AttributeTypeAndValue ::= SEQUENCE {
 * type  OBJECT IDENTIFIER,
 * value ANY }
 *
 */
@Deprecated("use org.bouncycastle.asn1.x500.X500Name.")
open class X509Name : ASN1Object {

    private var converter: X509NameEntryConverter? = null
    private var ordering = Vector()
    private var values = Vector()
    private var added = Vector()

    private var seq: ASN1Sequence? = null

    private var isHashCodeCalculated: Boolean = false
    private var hashCodeValue: Int = 0

    protected constructor() {
        // constructure use by new X500 Name class
    }

    /**
     * Constructor from ASN1Sequence

     * the principal will be a list of constructed sets, each containing an (OID, String) pair.
     */
    @Deprecated("use X500Name.getInstance()")
    constructor(
            seq: ASN1Sequence) {
        this.seq = seq

        val e = seq.objects

        while (e.hasMoreElements()) {
            val set = ASN1Set.getInstance((e.nextElement() as ASN1Encodable).toASN1Primitive())

            for (i in 0..set.size() - 1) {
                val s = ASN1Sequence.getInstance(set.getObjectAt(i).toASN1Primitive())

                if (s.size() != 2) {
                    throw IllegalArgumentException("badly sized pair")
                }

                ordering.addElement(ASN1ObjectIdentifier.getInstance(s.getObjectAt(0)))

                val value = s.getObjectAt(1)
                if (value is ASN1String && value !is DERUniversalString) {
                    val v = value.string
                    if (v.length > 0 && v[0] == '#') {
                        values.addElement("\\" + v)
                    } else {
                        values.addElement(v)
                    }
                } else {
                    try {
                        values.addElement("#" + bytesToString(Hex.encode(value.toASN1Primitive().getEncoded(ASN1Encoding.DER))))
                    } catch (e1: IOException) {
                        throw IllegalArgumentException("cannot encode value")
                    }

                }
                added.addElement(if (i != 0) TRUE else FALSE)  // to allow earlier JDK compatibility
            }
        }
    }

    /**
     * constructor from a table of attributes.
     *
     *
     * it's is assumed the table contains OID/String pairs, and the contents
     * of the table are copied into an internal table as part of the
     * construction process.
     *
     *
     * **Note:** if the name you are trying to generate should be
     * following a specific ordering, you should use the constructor
     * with the ordering specified below.
     */
    @Deprecated("use an ordered constructor! The hashtable ordering is rarely correct")
    constructor(
            attributes: Hashtable<Any, Any>) : this(null, attributes) {
    }

    /**
     * Constructor from a table of attributes with ordering.
     *
     *
     * it's is assumed the table contains OID/String pairs, and the contents
     * of the table are copied into an internal table as part of the
     * construction process. The ordering vector should contain the OIDs
     * in the order they are meant to be encoded or printed in toString.
     *
     *
     * The passed in converter will be used to convert the strings into their
     * ASN.1 counterparts.
     */
    @Deprecated("use X500Name, X500NameBuilder")
    @JvmOverloads constructor(
            ordering: Vector<Any>?,
            attributes: Hashtable<Any, Any>,
            converter: X509NameEntryConverter = X509DefaultEntryConverter()) {
        this.converter = converter

        if (ordering != null) {
            for (i in ordering.indices) {
                this.ordering.addElement(ordering.elementAt(i))
                this.added.addElement(FALSE)
            }
        } else {
            val e = attributes.keys()

            while (e.hasMoreElements()) {
                this.ordering.addElement(e.nextElement())
                this.added.addElement(FALSE)
            }
        }

        for (i in this.ordering.indices) {

            if (attributes[this.ordering.elementAt(i)] == null) {
                throw IllegalArgumentException("No attribute for object id - " + this.ordering.elementAt(i).id + " - passed to distinguished name")
            }

            this.values.addElement(attributes[this.ordering.elementAt(i)]) // copy the hash table
        }
    }

    /**
     * Takes two vectors one of the oids and the other of the values.
     *
     *
     * The passed in converter will be used to convert the strings into their
     * ASN.1 counterparts.
     */
    @Deprecated("use X500Name, X500NameBuilder")
    @JvmOverloads constructor(
            oids: Vector<Any>,
            values: Vector<Any>,
            converter: X509NameEntryConverter = X509DefaultEntryConverter()) {
        this.converter = converter

        if (oids.size != values.size) {
            throw IllegalArgumentException("oids vector must be same length as values.")
        }

        for (i in oids.indices) {
            this.ordering.addElement(oids.elementAt(i))
            this.values.addElement(values.elementAt(i))
            this.added.addElement(FALSE)
        }
    }

    //    private Boolean isEncoded(String s)
    //    {
    //        if (s.charAt(0) == '#')
    //        {
    //            return TRUE;
    //        }
    //
    //        return FALSE;
    //    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes.
     */
    @Deprecated("use X500Name, X500NameBuilder")
    constructor(
            dirName: String) : this(DefaultReverse, DefaultLookUp, dirName) {
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes with each
     * string value being converted to its associated ASN.1 type using the passed
     * in converter.
     */
    @Deprecated("use X500Name, X500NameBuilder")
    constructor(
            dirName: String,
            converter: X509NameEntryConverter) : this(DefaultReverse, DefaultLookUp, dirName, converter) {
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes. If reverse
     * is true, create the encoded version of the sequence starting from the
     * last element in the string.
     */
    @Deprecated("use X500Name, X500NameBuilder")
    constructor(
            reverse: Boolean,
            dirName: String) : this(reverse, DefaultLookUp, dirName) {
    }

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes with each
     * string value being converted to its associated ASN.1 type using the passed
     * in converter. If reverse is true the ASN.1 sequence representing the DN will
     * be built by starting at the end of the string, rather than the start.
     */
    @Deprecated("use X500Name, X500NameBuilder")
    constructor(
            reverse: Boolean,
            dirName: String,
            converter: X509NameEntryConverter) : this(reverse, DefaultLookUp, dirName, converter) {
    }

    private fun decodeOID(
            name: String,
            lookUp: Hashtable<Any, Any>): ASN1ObjectIdentifier {
        var name = name
        name = name.trim { it <= ' ' }
        if (Strings.toUpperCase(name).startsWith("OID.")) {
            return ASN1ObjectIdentifier(name.substring(4))
        } else if (name[0] >= '0' && name[0] <= '9') {
            return ASN1ObjectIdentifier(name)
        }

        val oid = lookUp[Strings.toLowerCase(name)] as ASN1ObjectIdentifier ?: throw IllegalArgumentException("Unknown object id - $name - passed to distinguished name")

        return oid
    }

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

    /**
     * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
     * some such, converting it into an ordered set of name attributes. lookUp
     * should provide a table of lookups, indexed by lowercase only strings and
     * yielding a ASN1ObjectIdentifier, other than that OID. and numeric oids
     * will be processed automatically. The passed in converter is used to convert the
     * string values to the right of each equals sign to their ASN.1 counterparts.
     *
     * @param reverse true if we should start scanning from the end, false otherwise.
     * *
     * @param lookUp table of names and oids.
     * *
     * @param dirName the string dirName
     * *
     * @param converter the converter to convert string values into their ASN.1 equivalents
     */
    @JvmOverloads constructor(
            reverse: Boolean,
            lookUp: Hashtable<Any, Any>,
            dirName: String,
            converter: X509NameEntryConverter = X509DefaultEntryConverter()) {
        this.converter = converter
        val nTok = X509NameTokenizer(dirName)

        while (nTok.hasMoreTokens()) {
            val token = nTok.nextToken()

            if (token.indexOf('+') > 0) {
                val pTok = X509NameTokenizer(token, '+')

                addEntry(lookUp, pTok.nextToken(), FALSE)

                while (pTok.hasMoreTokens()) {
                    addEntry(lookUp, pTok.nextToken(), TRUE)
                }
            } else {
                addEntry(lookUp, token, FALSE)
            }
        }

        if (reverse) {
            val o = Vector()
            val v = Vector()
            val a = Vector()

            var count = 1

            for (i in this.ordering.indices) {
                if ((this.added.elementAt(i) as Boolean).booleanValue()) {
                    o.insertElementAt(this.ordering.elementAt(i), count)
                    v.insertElementAt(this.values.elementAt(i), count)
                    a.insertElementAt(this.added.elementAt(i), count)
                    count++
                } else {
                    o.insertElementAt(this.ordering.elementAt(i), 0)
                    v.insertElementAt(this.values.elementAt(i), 0)
                    a.insertElementAt(this.added.elementAt(i), 0)
                    count = 1
                }
            }

            this.ordering = o
            this.values = v
            this.added = a
        }
    }

    private fun addEntry(lookUp: Hashtable<Any, Any>, token: String, isAdded: Boolean?) {
        val vTok: X509NameTokenizer
        val name: String
        val value: String
        val oid: ASN1ObjectIdentifier
        vTok = X509NameTokenizer(token, '=')

        name = vTok.nextToken()

        if (!vTok.hasMoreTokens()) {
            throw IllegalArgumentException("badly formatted directory string")
        }

        value = vTok.nextToken()

        oid = decodeOID(name, lookUp)

        this.ordering.addElement(oid)
        this.values.addElement(unescape(value))
        this.added.addElement(isAdded)
    }

    /**
     * return a vector of the oids in the name, in the order they were found.
     */
    val oiDs: Vector<Any>
        get() {
            val v = Vector()

            for (i in ordering.indices) {
                v.addElement(ordering.elementAt(i))
            }

            return v
        }

    /**
     * return a vector of the values found in the name, in the order they
     * were found.
     */
    fun getValues(): Vector<Any> {
        val v = Vector()

        for (i in values.indices) {
            v.addElement(values.elementAt(i))
        }

        return v
    }

    /**
     * return a vector of the values found in the name, in the order they
     * were found, with the DN label corresponding to passed in oid.
     */
    fun getValues(
            oid: ASN1ObjectIdentifier): Vector<Any> {
        val v = Vector()

        for (i in values.indices) {
            if (ordering.elementAt(i) == oid) {

                if (values.elementAt(i).length > 2 && values.elementAt(i)[0] == '\\' && values.elementAt(i)[1] == '#') {
                    v.addElement(values.elementAt(i).substring(1))
                } else {
                    v.addElement(values.elementAt(i))
                }
            }
        }

        return v
    }

    override fun toASN1Primitive(): ASN1Primitive {
        if (seq == null) {
            val vec = ASN1EncodableVector()
            var sVec = ASN1EncodableVector()
            var lstOid: ASN1ObjectIdentifier? = null

            for (i in ordering.indices) {
                val v = ASN1EncodableVector()

                v.add(ordering.elementAt(i))

                v.add(converter!!.getConvertedValue(ordering.elementAt(i), values.elementAt(i)))

                if (lstOid == null || (this.added.elementAt(i) as Boolean).booleanValue()) {
                    sVec.add(DERSequence(v))
                } else {
                    vec.add(DERSet(sVec))
                    sVec = ASN1EncodableVector()

                    sVec.add(DERSequence(v))
                }

                lstOid = ordering.elementAt(i)
            }

            vec.add(DERSet(sVec))

            seq = DERSequence(vec)
        }

        return seq
    }

    /**
     * @param inOrder if true the order of both X509 names must be the same,
     * * as well as the values associated with each element.
     */
    fun equals(obj: Any, inOrder: Boolean): Boolean {
        if (!inOrder) {
            return this == obj
        }

        if (obj === this) {
            return true
        }

        if (!(obj is X509Name || obj is ASN1Sequence)) {
            return false
        }

        val derO = (obj as ASN1Encodable).toASN1Primitive()

        if (this.toASN1Primitive() == derO) {
            return true
        }

        val other: X509Name

        try {
            other = X509Name.getInstance(obj)
        } catch (e: IllegalArgumentException) {
            return false
        }

        val orderingSize = ordering.size

        if (orderingSize != other.ordering.size) {
            return false
        }

        for (i in 0..orderingSize - 1) {

            if (ordering.elementAt(i) == other.ordering.elementAt(i)) {

                if (!equivalentStrings(values.elementAt(i), other.values.elementAt(i))) {
                    return false
                }
            } else {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        if (isHashCodeCalculated) {
            return hashCodeValue
        }

        isHashCodeCalculated = true

        // this needs to be order independent, like equals
        var i = 0
        while (i != ordering.size) {

            values.elementAt(i) = canonicalize(values.elementAt(i))
            values.elementAt(i) = stripInternalSpaces(values.elementAt(i))

            hashCodeValue = hashCodeValue xor ordering.elementAt(i).hashCode()
            hashCodeValue = hashCodeValue xor values.elementAt(i).hashCode()
            i += 1
        }

        return hashCodeValue
    }

    /**
     * test for equality - note: case is ignored.
     */
    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }

        if (!(obj is X509Name || obj is ASN1Sequence)) {
            return false
        }

        val derO = (obj as ASN1Encodable).toASN1Primitive()

        if (this.toASN1Primitive() == derO) {
            return true
        }

        val other: X509Name

        try {
            other = X509Name.getInstance(obj)
        } catch (e: IllegalArgumentException) {
            return false
        }

        val orderingSize = ordering.size

        if (orderingSize != other.ordering.size) {
            return false
        }

        val indexes = BooleanArray(orderingSize)
        val start: Int
        val end: Int
        val delta: Int

        if (ordering.elementAt(0) == other.ordering.elementAt(0))
        // guess forward
        {
            start = 0
            end = orderingSize
            delta = 1
        } else
        // guess reversed - most common problem
        {
            start = orderingSize - 1
            end = -1
            delta = -1
        }

        var i = start
        while (i != end) {
            var found = false

            for (j in 0..orderingSize - 1) {
                if (indexes[j]) {
                    continue
                }

                if (ordering.elementAt(i) == other.ordering.elementAt(j)) {

                    if (equivalentStrings(values.elementAt(i), other.values.elementAt(j))) {
                        indexes[j] = true
                        found = true
                        break
                    }
                }
            }

            if (!found) {
                return false
            }
            i += delta
        }

        return true
    }

    private fun equivalentStrings(s1: String, s2: String): Boolean {
        var value = canonicalize(s1)
        var oValue = canonicalize(s2)

        if (value != oValue) {
            value = stripInternalSpaces(value)
            oValue = stripInternalSpaces(oValue)

            if (value != oValue) {
                return false
            }
        }

        return true
    }

    private fun canonicalize(s: String): String {
        var value = Strings.toLowerCase(s.trim { it <= ' ' })

        if (value.length > 0 && value[0] == '#') {
            val obj = decodeObject(value)

            if (obj is ASN1String) {
                value = Strings.toLowerCase(obj.string.trim { it <= ' ' })
            }
        }

        return value
    }

    private fun decodeObject(oValue: String): ASN1Primitive {
        try {
            return ASN1Primitive.fromByteArray(Hex.decode(oValue.substring(1)))
        } catch (e: IOException) {
            throw IllegalStateException("unknown encoding in name: " + e)
        }

    }

    private fun stripInternalSpaces(
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

    private fun appendValue(
            buf: StringBuffer,
            oidSymbols: Hashtable<Any, Any>,
            oid: ASN1ObjectIdentifier,
            value: String) {
        val sym = oidSymbols[oid] as String

        if (sym != null) {
            buf.append(sym)
        } else {
            buf.append(oid.id)
        }

        buf.append('=')

        var start = buf.length
        buf.append(value)
        var end = buf.length

        if (value.length >= 2 && value[0] == '\\' && value[1] == '#') {
            start += 2
        }

        while (start < end && buf[start] == ' ') {
            buf.insert(start, "\\")
            start += 2
            ++end
        }

        while (--end > start && buf[end] == ' ') {
            buf.insert(end, '\\')
        }

        while (start <= end) {
            when (buf[start]) {
                ',', '"', '\\', '+', '=', '<', '>', ';' -> {
                    buf.insert(start, "\\")
                    start += 2
                    ++end
                }
                else -> ++start
            }
        }
    }

    /**
     * convert the structure to a string - if reverse is true the
     * oids and values are listed out starting with the last element
     * in the sequence (ala RFC 2253), otherwise the string will begin
     * with the first element of the structure. If no string definition
     * for the oid is found in oidSymbols the string value of the oid is
     * added. Two standard symbol tables are provided DefaultSymbols, and
     * RFC2253Symbols as part of this class.

     * @param reverse if true start at the end of the sequence and work back.
     * *
     * @param oidSymbols look up table strings for oids.
     */
    fun toString(
            reverse: Boolean,
            oidSymbols: Hashtable<Any, Any>): String {
        val buf = StringBuffer()
        val components = Vector()
        var first = true

        var ava: StringBuffer? = null

        for (i in ordering.indices) {
            if ((added.elementAt(i) as Boolean).booleanValue()) {
                ava!!.append('+')
                appendValue(ava, oidSymbols,
                        ordering.elementAt(i) as ASN1ObjectIdentifier,
                        values.elementAt(i) as String)
            } else {
                ava = StringBuffer()
                appendValue(ava, oidSymbols,
                        ordering.elementAt(i) as ASN1ObjectIdentifier,
                        values.elementAt(i) as String)
                components.addElement(ava)
            }
        }

        if (reverse) {
            for (i in components.indices.reversed()) {
                if (first) {
                    first = false
                } else {
                    buf.append(',')
                }

                buf.append(components.elementAt(i).toString())
            }
        } else {
            for (i in components.indices) {
                if (first) {
                    first = false
                } else {
                    buf.append(',')
                }

                buf.append(components.elementAt(i).toString())
            }
        }

        return buf.toString()
    }

    private fun bytesToString(
            data: ByteArray): String {
        val cs = CharArray(data.size)

        for (i in cs.indices) {
            cs[i] = (data[i] and 0xff).toChar()
        }

        return String(cs)
    }

    override fun toString(): String {
        return toString(DefaultReverse, DefaultSymbols)
    }

    companion object {
        /**
         * country code - StringType(SIZE(2))
         */
        @Deprecated("use a X500NameStyle")
        val C = ASN1ObjectIdentifier("2.5.4.6")

        /**
         * organization - StringType(SIZE(1..64))
         */
        @Deprecated("use a X500NameStyle")
        val O = ASN1ObjectIdentifier("2.5.4.10")

        /**
         * organizational unit name - StringType(SIZE(1..64))
         */
        @Deprecated("use a X500NameStyle")
        val OU = ASN1ObjectIdentifier("2.5.4.11")

        /**
         * Title
         */
        @Deprecated("use a X500NameStyle")
        val T = ASN1ObjectIdentifier("2.5.4.12")

        /**
         * common name - StringType(SIZE(1..64))
         */
        @Deprecated("use a X500NameStyle")
        val CN = ASN1ObjectIdentifier("2.5.4.3")

        /**
         * device serial number name - StringType(SIZE(1..64))
         */
        val SN = ASN1ObjectIdentifier("2.5.4.5")

        /**
         * street - StringType(SIZE(1..64))
         */
        val STREET = ASN1ObjectIdentifier("2.5.4.9")

        /**
         * device serial number name - StringType(SIZE(1..64))
         */
        val SERIALNUMBER = SN

        /**
         * locality name - StringType(SIZE(1..64))
         */
        val L = ASN1ObjectIdentifier("2.5.4.7")

        /**
         * state, or province name - StringType(SIZE(1..64))
         */
        val ST = ASN1ObjectIdentifier("2.5.4.8")

        /**
         * Naming attributes of type X520name
         */
        val SURNAME = ASN1ObjectIdentifier("2.5.4.4")
        val GIVENNAME = ASN1ObjectIdentifier("2.5.4.42")
        val INITIALS = ASN1ObjectIdentifier("2.5.4.43")
        val GENERATION = ASN1ObjectIdentifier("2.5.4.44")
        val UNIQUE_IDENTIFIER = ASN1ObjectIdentifier("2.5.4.45")

        /**
         * businessCategory - DirectoryString(SIZE(1..128)
         */
        val BUSINESS_CATEGORY = ASN1ObjectIdentifier(
                "2.5.4.15")

        /**
         * postalCode - DirectoryString(SIZE(1..40)
         */
        val POSTAL_CODE = ASN1ObjectIdentifier(
                "2.5.4.17")

        /**
         * dnQualifier - DirectoryString(SIZE(1..64)
         */
        val DN_QUALIFIER = ASN1ObjectIdentifier(
                "2.5.4.46")

        /**
         * RFC 3039 Pseudonym - DirectoryString(SIZE(1..64)
         */
        val PSEUDONYM = ASN1ObjectIdentifier(
                "2.5.4.65")


        /**
         * RFC 3039 DateOfBirth - GeneralizedTime - YYYYMMDD000000Z
         */
        val DATE_OF_BIRTH = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.1")

        /**
         * RFC 3039 PlaceOfBirth - DirectoryString(SIZE(1..128)
         */
        val PLACE_OF_BIRTH = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.2")

        /**
         * RFC 3039 Gender - PrintableString (SIZE(1)) -- "M", "F", "m" or "f"
         */
        val GENDER = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.3")

        /**
         * RFC 3039 CountryOfCitizenship - PrintableString (SIZE (2)) -- ISO 3166
         * codes only
         */
        val COUNTRY_OF_CITIZENSHIP = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.4")

        /**
         * RFC 3039 CountryOfResidence - PrintableString (SIZE (2)) -- ISO 3166
         * codes only
         */
        val COUNTRY_OF_RESIDENCE = ASN1ObjectIdentifier(
                "1.3.6.1.5.5.7.9.5")


        /**
         * ISIS-MTT NameAtBirth - DirectoryString(SIZE(1..64)
         */
        val NAME_AT_BIRTH = ASN1ObjectIdentifier("1.3.36.8.3.14")

        /**
         * RFC 3039 PostalAddress - SEQUENCE SIZE (1..6) OF
         * DirectoryString(SIZE(1..30))
         */
        val POSTAL_ADDRESS = ASN1ObjectIdentifier("2.5.4.16")

        /**
         * RFC 2256 dmdName
         */
        val DMD_NAME = ASN1ObjectIdentifier("2.5.4.54")

        /**
         * id-at-telephoneNumber
         */
        val TELEPHONE_NUMBER = X509ObjectIdentifiers.id_at_telephoneNumber

        /**
         * id-at-name
         */
        val NAME = X509ObjectIdentifiers.id_at_name

        /**
         * Email address (RSA PKCS#9 extension) - IA5String.
         *
         * Note: if you're trying to be ultra orthodox, don't use this! It shouldn't be in here.
         */
        @Deprecated("use a X500NameStyle")
        val EmailAddress = PKCSObjectIdentifiers.pkcs_9_at_emailAddress

        /**
         * more from PKCS#9
         */
        val UnstructuredName = PKCSObjectIdentifiers.pkcs_9_at_unstructuredName
        val UnstructuredAddress = PKCSObjectIdentifiers.pkcs_9_at_unstructuredAddress

        /**
         * email address in Verisign certificates
         */
        val E = EmailAddress

        /*
     * others...
     */
        val DC = ASN1ObjectIdentifier("0.9.2342.19200300.100.1.25")

        /**
         * LDAP User id.
         */
        val UID = ASN1ObjectIdentifier("0.9.2342.19200300.100.1.1")

        /**
         * determines whether or not strings should be processed and printed
         * from back to front.
         */
        var DefaultReverse = false

        /**
         * default look up table translating OID values into their common symbols following
         * the convention in RFC 2253 with a few extras
         */
        val DefaultSymbols = Hashtable()

        /**
         * look up table translating OID values into their common symbols following the convention in RFC 2253

         */
        val RFC2253Symbols = Hashtable()

        /**
         * look up table translating OID values into their common symbols following the convention in RFC 1779

         */
        val RFC1779Symbols = Hashtable()

        /**
         * look up table translating common symbols into their OIDS.
         */
        val DefaultLookUp = Hashtable()

        /**
         * look up table translating OID values into their common symbols
         */
        @Deprecated("use DefaultSymbols")
        val OIDLookUp: Hashtable<Any, Any> = DefaultSymbols

        /**
         * look up table translating string values into their OIDS -
         */
        @Deprecated("use DefaultLookUp")
        val SymbolLookUp: Hashtable<Any, Any> = DefaultLookUp

        private val TRUE = true // for J2ME compatibility
        private val FALSE = false

        init {
            DefaultSymbols.put(C, "C")
            DefaultSymbols.put(O, "O")
            DefaultSymbols.put(T, "T")
            DefaultSymbols.put(OU, "OU")
            DefaultSymbols.put(CN, "CN")
            DefaultSymbols.put(L, "L")
            DefaultSymbols.put(ST, "ST")
            DefaultSymbols.put(SN, "SERIALNUMBER")
            DefaultSymbols.put(EmailAddress, "E")
            DefaultSymbols.put(DC, "DC")
            DefaultSymbols.put(UID, "UID")
            DefaultSymbols.put(STREET, "STREET")
            DefaultSymbols.put(SURNAME, "SURNAME")
            DefaultSymbols.put(GIVENNAME, "GIVENNAME")
            DefaultSymbols.put(INITIALS, "INITIALS")
            DefaultSymbols.put(GENERATION, "GENERATION")
            DefaultSymbols.put(UnstructuredAddress, "unstructuredAddress")
            DefaultSymbols.put(UnstructuredName, "unstructuredName")
            DefaultSymbols.put(UNIQUE_IDENTIFIER, "UniqueIdentifier")
            DefaultSymbols.put(DN_QUALIFIER, "DN")
            DefaultSymbols.put(PSEUDONYM, "Pseudonym")
            DefaultSymbols.put(POSTAL_ADDRESS, "PostalAddress")
            DefaultSymbols.put(NAME_AT_BIRTH, "NameAtBirth")
            DefaultSymbols.put(COUNTRY_OF_CITIZENSHIP, "CountryOfCitizenship")
            DefaultSymbols.put(COUNTRY_OF_RESIDENCE, "CountryOfResidence")
            DefaultSymbols.put(GENDER, "Gender")
            DefaultSymbols.put(PLACE_OF_BIRTH, "PlaceOfBirth")
            DefaultSymbols.put(DATE_OF_BIRTH, "DateOfBirth")
            DefaultSymbols.put(POSTAL_CODE, "PostalCode")
            DefaultSymbols.put(BUSINESS_CATEGORY, "BusinessCategory")
            DefaultSymbols.put(TELEPHONE_NUMBER, "TelephoneNumber")
            DefaultSymbols.put(NAME, "Name")

            RFC2253Symbols.put(C, "C")
            RFC2253Symbols.put(O, "O")
            RFC2253Symbols.put(OU, "OU")
            RFC2253Symbols.put(CN, "CN")
            RFC2253Symbols.put(L, "L")
            RFC2253Symbols.put(ST, "ST")
            RFC2253Symbols.put(STREET, "STREET")
            RFC2253Symbols.put(DC, "DC")
            RFC2253Symbols.put(UID, "UID")

            RFC1779Symbols.put(C, "C")
            RFC1779Symbols.put(O, "O")
            RFC1779Symbols.put(OU, "OU")
            RFC1779Symbols.put(CN, "CN")
            RFC1779Symbols.put(L, "L")
            RFC1779Symbols.put(ST, "ST")
            RFC1779Symbols.put(STREET, "STREET")

            DefaultLookUp.put("c", C)
            DefaultLookUp.put("o", O)
            DefaultLookUp.put("t", T)
            DefaultLookUp.put("ou", OU)
            DefaultLookUp.put("cn", CN)
            DefaultLookUp.put("l", L)
            DefaultLookUp.put("st", ST)
            DefaultLookUp.put("sn", SN)
            DefaultLookUp.put("serialnumber", SN)
            DefaultLookUp.put("street", STREET)
            DefaultLookUp.put("emailaddress", E)
            DefaultLookUp.put("dc", DC)
            DefaultLookUp.put("e", E)
            DefaultLookUp.put("uid", UID)
            DefaultLookUp.put("surname", SURNAME)
            DefaultLookUp.put("givenname", GIVENNAME)
            DefaultLookUp.put("initials", INITIALS)
            DefaultLookUp.put("generation", GENERATION)
            DefaultLookUp.put("unstructuredaddress", UnstructuredAddress)
            DefaultLookUp.put("unstructuredname", UnstructuredName)
            DefaultLookUp.put("uniqueidentifier", UNIQUE_IDENTIFIER)
            DefaultLookUp.put("dn", DN_QUALIFIER)
            DefaultLookUp.put("pseudonym", PSEUDONYM)
            DefaultLookUp.put("postaladdress", POSTAL_ADDRESS)
            DefaultLookUp.put("nameofbirth", NAME_AT_BIRTH)
            DefaultLookUp.put("countryofcitizenship", COUNTRY_OF_CITIZENSHIP)
            DefaultLookUp.put("countryofresidence", COUNTRY_OF_RESIDENCE)
            DefaultLookUp.put("gender", GENDER)
            DefaultLookUp.put("placeofbirth", PLACE_OF_BIRTH)
            DefaultLookUp.put("dateofbirth", DATE_OF_BIRTH)
            DefaultLookUp.put("postalcode", POSTAL_CODE)
            DefaultLookUp.put("businesscategory", BUSINESS_CATEGORY)
            DefaultLookUp.put("telephonenumber", TELEPHONE_NUMBER)
            DefaultLookUp.put("name", NAME)
        }

        /**
         * Return a X509Name based on the passed in tagged object.

         * @param obj tag object holding name.
         * *
         * @param explicit true if explicitly tagged false otherwise.
         * *
         * @return the X509Name
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): X509Name {
            return getInstance(ASN1Sequence.getInstance(obj, explicit))
        }

        fun getInstance(
                obj: Any?): X509Name? {
            if (obj == null || obj is X509Name) {
                return obj as X509Name?
            } else if (obj is X500Name) {
                return X509Name(ASN1Sequence.getInstance(obj.toASN1Primitive()))
            } else if (obj != null) {
                return X509Name(ASN1Sequence.getInstance(obj))
            }

            return null
        }
    }
}
/**
 * Constructor from a table of attributes with ordering.
 *
 *
 * it's is assumed the table contains OID/String pairs, and the contents
 * of the table are copied into an internal table as part of the
 * construction process. The ordering vector should contain the OIDs
 * in the order they are meant to be encoded or printed in toString.
 */
/**
 * Takes two vectors one of the oids and the other of the values.
 */
/**
 * Takes an X509 dir name as a string of the format "C=AU, ST=Victoria", or
 * some such, converting it into an ordered set of name attributes. lookUp
 * should provide a table of lookups, indexed by lowercase only strings and
 * yielding a ASN1ObjectIdentifier, other than that OID. and numeric oids
 * will be processed automatically.
 *
 * If reverse is true, create the encoded version of the sequence
 * starting from the last element in the string.
 * @param reverse true if we should start scanning from the end (RFC 2553).
 * *
 * @param lookUp table of names and their oids.
 * *
 * @param dirName the X.500 string to be parsed.
 * *
 */

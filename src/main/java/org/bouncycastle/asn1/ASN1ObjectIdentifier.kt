package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.util.HashMap

import org.bouncycastle.util.Arrays

/**
 * Class representing the ASN.1 OBJECT IDENTIFIER type.
 */
open class ASN1ObjectIdentifier : ASN1Primitive {
    /**
     * Return the OID as a string.

     * @return the string representation of the OID carried by this object.
     */
    val id: String

    private var body: ByteArray? = null

    internal constructor(
            bytes: ByteArray) {
        val objId = StringBuffer()
        var value: Long = 0
        var bigValue: BigInteger? = null
        var first = true

        for (i in bytes.indices) {
            val b = bytes[i] and 0xff

            if (value <= LONG_LIMIT) {
                value += (b and 0x7f).toLong()
                if (b and 0x80 == 0)
                // end of number reached
                {
                    if (first) {
                        if (value < 40) {
                            objId.append('0')
                        } else if (value < 80) {
                            objId.append('1')
                            value -= 40
                        } else {
                            objId.append('2')
                            value -= 80
                        }
                        first = false
                    }

                    objId.append('.')
                    objId.append(value)
                    value = 0
                } else {
                    value = value shl 7
                }
            } else {
                if (bigValue == null) {
                    bigValue = BigInteger.valueOf(value)
                }
                bigValue = bigValue!!.or(BigInteger.valueOf((b and 0x7f).toLong()))
                if (b and 0x80 == 0) {
                    if (first) {
                        objId.append('2')
                        bigValue = bigValue!!.subtract(BigInteger.valueOf(80))
                        first = false
                    }

                    objId.append('.')
                    objId.append(bigValue)
                    bigValue = null
                    value = 0
                } else {
                    bigValue = bigValue!!.shiftLeft(7)
                }
            }
        }

        this.id = objId.toString()
        this.body = Arrays.clone(bytes)
    }

    /**
     * Create an OID based on the passed in String.

     * @param identifier a string representation of an OID.
     */
    constructor(
            identifier: String?) {
        if (identifier == null) {
            throw IllegalArgumentException("'identifier' cannot be null")
        }
        if (!isValidIdentifier(identifier)) {
            throw IllegalArgumentException("string $identifier not an OID")
        }

        this.id = identifier
    }

    /**
     * Create an OID that creates a branch under the current one.

     * @param branchID node numbers for the new branch.
     * *
     * @return the OID for the new created branch.
     */
    internal constructor(oid: ASN1ObjectIdentifier, branchID: String) {
        if (!isValidBranchID(branchID, 0)) {
            throw IllegalArgumentException("string $branchID not a valid OID branch")
        }

        this.id = oid.id + "." + branchID
    }

    /**
     * Return an OID that creates a branch under the current one.

     * @param branchID node numbers for the new branch.
     * *
     * @return the OID for the new created branch.
     */
    fun branch(branchID: String): ASN1ObjectIdentifier {
        return ASN1ObjectIdentifier(this, branchID)
    }

    /**
     * Return  true if this oid is an extension of the passed in branch, stem.

     * @param stem the arc or branch that is a possible parent.
     * *
     * @return true if the branch is on the passed in stem, false otherwise.
     */
    fun on(stem: ASN1ObjectIdentifier): Boolean {
        val id = id
        val stemId = stem.id
        return id.length > stemId.length && id[stemId.length] == '.' && id.startsWith(stemId)
    }

    private fun writeField(
            out: ByteArrayOutputStream,
            fieldValue: Long) {
        var fieldValue = fieldValue
        val result = ByteArray(9)
        var pos = 8
        result[pos] = (fieldValue.toInt() and 0x7f).toByte()
        while (fieldValue >= 1L shl 7) {
            fieldValue = fieldValue shr 7
            result[--pos] = (fieldValue.toInt() and 0x7f or 0x80).toByte()
        }
        out.write(result, pos, 9 - pos)
    }

    private fun writeField(
            out: ByteArrayOutputStream,
            fieldValue: BigInteger) {
        val byteCount = (fieldValue.bitLength() + 6) / 7
        if (byteCount == 0) {
            out.write(0)
        } else {
            var tmpValue = fieldValue
            val tmp = ByteArray(byteCount)
            for (i in byteCount - 1 downTo 0) {
                tmp[i] = (tmpValue.toInt() and 0x7f or 0x80).toByte()
                tmpValue = tmpValue.shiftRight(7)
            }
            tmp[byteCount - 1] = tmp[byteCount - 1] and 0x7f
            out.write(tmp, 0, tmp.size)
        }
    }

    private fun doOutput(aOut: ByteArrayOutputStream) {
        val tok = OIDTokenizer(id)
        val first = Integer.parseInt(tok.nextToken()) * 40

        val secondToken = tok.nextToken()
        if (secondToken.length <= 18) {
            writeField(aOut, first + java.lang.Long.parseLong(secondToken))
        } else {
            writeField(aOut, BigInteger(secondToken).add(BigInteger.valueOf(first.toLong())))
        }

        while (tok.hasMoreTokens()) {
            val token = tok.nextToken()
            if (token.length <= 18) {
                writeField(aOut, java.lang.Long.parseLong(token))
            } else {
                writeField(aOut, BigInteger(token))
            }
        }
    }

    @Synchronized private fun getBody(): ByteArray {
        if (body == null) {
            val bOut = ByteArrayOutputStream()

            doOutput(bOut)

            body = bOut.toByteArray()
        }

        return body
    }

    internal override val isConstructed: Boolean
        get() = false

    @Throws(IOException::class)
    internal override fun encodedLength(): Int {
        val length = getBody().size

        return 1 + StreamUtil.calculateBodyLength(length) + length
    }

    @Throws(IOException::class)
    internal override fun encode(
            out: ASN1OutputStream) {
        val enc = getBody()

        out.write(BERTags.OBJECT_IDENTIFIER)
        out.writeLength(enc.size)
        out.write(enc)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    internal override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o === this) {
            return true
        }

        if (o !is ASN1ObjectIdentifier) {
            return false
        }

        return id == o.id
    }

    override fun toString(): String {
        return id
    }

    /**
     * Intern will return a reference to a pooled version of this object, unless it
     * is not present in which case intern will add it.
     *
     *
     * The pool is also used by the ASN.1 parsers to limit the number of duplicated OID
     * objects in circulation.
     *
     * @return a reference to the identifier in the pool.
     */
    fun intern(): ASN1ObjectIdentifier {
        synchronized (pool) {
            val hdl = OidHandle(getBody())

            if (pool.get(hdl) != null) {
                return pool.get(hdl)
            } else {
                pool.put(hdl, this)
                return this
            }
        }
    }

    private class OidHandle internal constructor(private val enc: ByteArray) {
        private val key: Int

        init {
            this.key = Arrays.hashCode(enc)
        }

        override fun hashCode(): Int {
            return key
        }

        override fun equals(o: Any?): Boolean {
            if (o is OidHandle) {
                return Arrays.areEqual(enc, o.enc)
            }

            return false
        }
    }

    companion object {

        /**
         * return an OID from the passed in object
         * @param obj an ASN1ObjectIdentifier or an object that can be converted into one.
         * *
         * @throws IllegalArgumentException if the object cannot be converted.
         * *
         * @return an ASN1ObjectIdentifier instance, or null.
         */
        fun getInstance(
                obj: Any?): ASN1ObjectIdentifier {
            if (obj == null || obj is ASN1ObjectIdentifier) {
                return obj as ASN1ObjectIdentifier?
            }

            if (obj is ASN1Encodable && obj.toASN1Primitive() is ASN1ObjectIdentifier) {
                return obj.toASN1Primitive() as ASN1ObjectIdentifier
            }

            if (obj is ByteArray) {
                try {
                    return ASN1Primitive.fromByteArray(obj) as ASN1ObjectIdentifier
                } catch (e: IOException) {
                    throw IllegalArgumentException("failed to construct object identifier from byte[]: " + e.message)
                }

            }

            throw IllegalArgumentException("illegal object in getInstance: " + obj.javaClass.name)
        }

        /**
         * return an Object Identifier from a tagged object.

         * @param obj      the tagged object holding the object we want
         * *
         * @param explicit true if the object is meant to be explicitly
         * *                 tagged false otherwise.
         * *
         * @throws IllegalArgumentException if the tagged object cannot
         * * be converted.
         * *
         * @return an ASN1ObjectIdentifier instance, or null.
         */
        fun getInstance(
                obj: ASN1TaggedObject,
                explicit: Boolean): ASN1ObjectIdentifier {
            val o = obj.`object`

            if (explicit || o is ASN1ObjectIdentifier) {
                return getInstance(o)
            } else {
                return ASN1ObjectIdentifier.fromOctetString(ASN1OctetString.getInstance(obj.`object`).octets)
            }
        }

        private val LONG_LIMIT = (java.lang.Long.MAX_VALUE shr 7) - 0x7f

        private fun isValidBranchID(
                branchID: String, start: Int): Boolean {
            var periodAllowed = false

            var pos = branchID.length
            while (--pos >= start) {
                val ch = branchID[pos]

                // TODO Leading zeroes?
                if ('0' <= ch && ch <= '9') {
                    periodAllowed = true
                    continue
                }

                if (ch == '.') {
                    if (!periodAllowed) {
                        return false
                    }

                    periodAllowed = false
                    continue
                }

                return false
            }

            return periodAllowed
        }

        private fun isValidIdentifier(
                identifier: String): Boolean {
            if (identifier.length < 3 || identifier[1] != '.') {
                return false
            }

            val first = identifier[0]
            if (first < '0' || first > '2') {
                return false
            }

            return isValidBranchID(identifier, 2)
        }

        private val pool = HashMap()

        internal fun fromOctetString(enc: ByteArray): ASN1ObjectIdentifier {
            val hdl = OidHandle(enc)

            synchronized (pool) {
                if (pool.get(hdl) != null) {
                    return pool.get(hdl)
                }
            }

            return ASN1ObjectIdentifier(enc)
        }
    }
}

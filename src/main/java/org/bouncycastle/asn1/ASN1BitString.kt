package org.bouncycastle.asn1

import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream

import org.bouncycastle.util.Arrays
import org.bouncycastle.util.io.Streams

/**
 * Base class for BIT STRING objects
 */
abstract class ASN1BitString
/**
 * Base constructor.

 * @param data the octets making up the bit string.
 * *
 * @param padBits the number of extra bits at the end of the string.
 */
(
        data: ByteArray?,
        val padBits: Int) : ASN1Primitive(), ASN1String {

    protected val data: ByteArray

    init {
        if (data == null) {
            throw NullPointerException("data cannot be null")
        }
        if (data.size == 0 && padBits != 0) {
            throw IllegalArgumentException("zero length data with non-zero pad bits")
        }
        if (padBits > 7 || padBits < 0) {
            throw IllegalArgumentException("pad bits cannot be greater than 7 or less than 0")
        }

        this.data = Arrays.clone(data)
    }

    /**
     * Return a String representation of this BIT STRING

     * @return a String representation.
     */
    override val string: String
        get() {
            val buf = StringBuffer("#")
            val bOut = ByteArrayOutputStream()
            val aOut = ASN1OutputStream(bOut)

            try {
                aOut.writeObject(this)
            } catch (e: IOException) {
                throw ASN1ParsingException("Internal error encoding BitString: " + e.message, e)
            }

            val string = bOut.toByteArray()

            for (i in string.indices) {
                buf.append(table[string[i].ushr(4) and 0xf])
                buf.append(table[string[i] and 0xf])
            }

            return buf.toString()
        }

    /**
     * @return the value of the bit string as an int (truncating if necessary)
     */
    fun intValue(): Int {
        var value = 0
        var string = data

        if (padBits > 0 && data.size <= 4) {
            string = derForm(data, padBits)
        }

        var i = 0
        while (i != string.size && i != 4) {
            value = value or (string[i] and 0xff shl 8 * i)
            i++
        }

        return value
    }

    /**
     * Return the octets contained in this BIT STRING, checking that this BIT STRING really
     * does represent an octet aligned string. Only use this method when the standard you are
     * following dictates that the BIT STRING will be octet aligned.

     * @return a copy of the octet aligned data.
     */
    val octets: ByteArray
        get() {
            if (padBits != 0) {
                throw IllegalStateException("attempt to get non-octet aligned data from BIT STRING")
            }

            return Arrays.clone(data)
        }

    val bytes: ByteArray
        get() = derForm(data, padBits)

    override fun toString(): String {
        return string
    }

    override fun hashCode(): Int {
        return padBits xor Arrays.hashCode(this.bytes)
    }

    override fun asn1Equals(
            o: ASN1Primitive): Boolean {
        if (o !is ASN1BitString) {
            return false
        }

        return this.padBits == o.padBits && Arrays.areEqual(this.bytes, o.bytes)
    }

    val loadedObject: ASN1Primitive
        get() = this.toASN1Primitive()

    internal override fun toDERObject(): ASN1Primitive {
        return DERBitString(data, padBits)
    }

    internal override fun toDLObject(): ASN1Primitive {
        return DLBitString(data, padBits)
    }

    @Throws(IOException::class)
    internal abstract override fun encode(out: ASN1OutputStream)

    companion object {
        private val table = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

        /**
         * @param bitString an int containing the BIT STRING
         * *
         * @return the correct number of pad bits for a bit string defined in
         * * a 32 bit constant
         */
        protected fun getPadBits(
                bitString: Int): Int {
            var `val` = 0
            for (i in 3 downTo 0) {
                //
                // this may look a little odd, but if it isn't done like this pre jdk1.2
                // JVM's break!
                //
                if (i != 0) {
                    if (bitString shr i * 8 != 0) {
                        `val` = bitString shr i * 8 and 0xFF
                        break
                    }
                } else {
                    if (bitString != 0) {
                        `val` = bitString and 0xFF
                        break
                    }
                }
            }

            if (`val` == 0) {
                return 0
            }

            var bits = 1

            while ((`val` = `val` shl 1) and 0xFF != 0) {
                bits++
            }

            return 8 - bits
        }

        /**
         * @param bitString an int containing the BIT STRING
         * *
         * @return the correct number of bytes for a bit string defined in
         * * a 32 bit constant
         */
        protected fun getBytes(bitString: Int): ByteArray {
            if (bitString == 0) {
                return ByteArray(0)
            }

            var bytes = 4
            for (i in 3 downTo 1) {
                if (bitString and (0xFF shl i * 8) != 0) {
                    break
                }
                bytes--
            }

            val result = ByteArray(bytes)
            for (i in 0..bytes - 1) {
                result[i] = (bitString shr i * 8 and 0xFF).toByte()
            }

            return result
        }

        protected fun derForm(data: ByteArray, padBits: Int): ByteArray {
            val rv = Arrays.clone(data)
            // DER requires pad bits be zero
            if (padBits > 0) {
                rv[data.size - 1] = rv[data.size - 1] and (0xff shl padBits).toByte()
            }

            return rv
        }

        @Throws(IOException::class)
        internal fun fromInputStream(length: Int, stream: InputStream): ASN1BitString {
            if (length < 1) {
                throw IllegalArgumentException("truncated BIT STRING detected")
            }

            val padBits = stream.read()
            val data = ByteArray(length - 1)

            if (data.size != 0) {
                if (Streams.readFully(stream, data) != data.size) {
                    throw EOFException("EOF encountered in middle of BIT STRING")
                }

                if (padBits > 0 && padBits < 8) {
                    if (data[data.size - 1] != (data[data.size - 1] and (0xff shl padBits)).toByte()) {
                        return DLBitString(data, padBits)
                    }
                }
            }

            return DERBitString(data, padBits)
        }
    }
}

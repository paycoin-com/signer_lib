//Copyright (c) 2006, Adobe Systems Incorporated
//All rights reserved.
//
//        Redistribution and use in source and binary forms, with or without
//        modification, are permitted provided that the following conditions are met:
//        1. Redistributions of source code must retain the above copyright
//        notice, this list of conditions and the following disclaimer.
//        2. Redistributions in binary form must reproduce the above copyright
//        notice, this list of conditions and the following disclaimer in the
//        documentation and/or other materials provided with the distribution.
//        3. All advertising materials mentioning features or use of this software
//        must display the following acknowledgement:
//        This product includes software developed by the Adobe Systems Incorporated.
//        4. Neither the name of the Adobe Systems Incorporated nor the
//        names of its contributors may be used to endorse or promote products
//        derived from this software without specific prior written permission.
//
//        THIS SOFTWARE IS PROVIDED BY ADOBE SYSTEMS INCORPORATED ''AS IS'' AND ANY
//        EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//        WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//        DISCLAIMED. IN NO EVENT SHALL ADOBE SYSTEMS INCORPORATED BE LIABLE FOR ANY
//        DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//        (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
//        LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
//        ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//        (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//        SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//        http://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html

package com.itextpdf.xmp.impl

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream


/**
 * Byte buffer container including length of valid data.

 * @since   11.10.2006
 */
class ByteBuffer {
    /**  */
    private var buffer: ByteArray? = null
    /**  */
    private var length: Int = 0
    /**  */
    private var encoding: String? = null


    /**
     * @param initialCapacity the initial capacity for this buffer
     */
    constructor(initialCapacity: Int) {
        this.buffer = ByteArray(initialCapacity)
        this.length = 0
    }


    /**
     * @param buffer a byte array that will be wrapped with `ByteBuffer`.
     */
    constructor(buffer: ByteArray) {
        this.buffer = buffer
        this.length = buffer.size
    }


    /**
     * @param buffer a byte array that will be wrapped with `ByteBuffer`.
     * *
     * @param length the length of valid bytes in the array
     */
    constructor(buffer: ByteArray, length: Int) {
        if (length > buffer.size) {
            throw ArrayIndexOutOfBoundsException("Valid length exceeds the buffer length.")
        }
        this.buffer = buffer
        this.length = length
    }


    /**
     * Loads the stream into a buffer.

     * @param in an InputStream
     * *
     * @throws IOException If the stream cannot be read.
     */
    @Throws(IOException::class)
    constructor(`in`: InputStream) {
        // load stream into buffer
        val chunk = 16384
        this.length = 0
        this.buffer = ByteArray(chunk)

        var read: Int
        while ((read = `in`.read(this.buffer, this.length, chunk)) > 0) {
            this.length += read
            if (read == chunk) {
                ensureCapacity(length + chunk)
            } else {
                break
            }
        }
    }


    /**
     * @param buffer a byte array that will be wrapped with `ByteBuffer`.
     * *
     * @param offset the offset of the provided buffer.
     * *
     * @param length the length of valid bytes in the array
     */
    constructor(buffer: ByteArray, offset: Int, length: Int) {
        if (length > buffer.size - offset) {
            throw ArrayIndexOutOfBoundsException("Valid length exceeds the buffer length.")
        }
        this.buffer = ByteArray(length)
        System.arraycopy(buffer, offset, this.buffer, 0, length)
        this.length = length
    }


    /**
     * @return Returns a byte stream that is limited to the valid amount of bytes.
     */
    val byteStream: InputStream
        get() = ByteArrayInputStream(buffer, 0, length)


    /**
     * @return Returns the length, that means the number of valid bytes, of the buffer;
     * * the inner byte array might be bigger than that.
     */
    fun length(): Int {
        return length
    }


    //	/**
    //	 * <em>Note:</em> Only the byte up to length are valid!
    //	 * @return Returns the inner byte buffer.
    //	 */
    //	public byte[] getBuffer()
    //	{
    //		return buffer;
    //	}


    /**
     * @param index the index to retrieve the byte from
     * *
     * @return Returns a byte from the buffer
     */
    fun byteAt(index: Int): Byte {
        if (index < length) {
            return buffer!![index]
        } else {
            throw IndexOutOfBoundsException("The index exceeds the valid buffer area")
        }
    }


    /**
     * @param index the index to retrieve a byte as int or char.
     * *
     * @return Returns a byte from the buffer
     */
    fun charAt(index: Int): Int {
        if (index < length) {
            return buffer!![index] and 0xFF
        } else {
            throw IndexOutOfBoundsException("The index exceeds the valid buffer area")
        }
    }


    /**
     * Appends a byte to the buffer.
     * @param b a byte
     */
    fun append(b: Byte) {
        ensureCapacity(length + 1)
        buffer[length++] = b
    }


    /**
     * Appends a byte array or part of to the buffer.

     * @param bytes a byte array
     * *
     * @param offset an offset with
     * *
     * @param len
     */
    @JvmOverloads fun append(bytes: ByteArray, offset: Int = 0, len: Int = bytes.size) {
        ensureCapacity(length + len)
        System.arraycopy(bytes, offset, buffer, length, len)
        length += len
    }


    /**
     * Append another buffer to this buffer.
     * @param anotherBuffer another `ByteBuffer`
     */
    fun append(anotherBuffer: ByteBuffer) {
        append(anotherBuffer.buffer, 0, anotherBuffer.length)
    }


    /**
     * Detects the encoding of the byte buffer, stores and returns it.
     * Only UTF-8, UTF-16LE/BE and UTF-32LE/BE are recognized.
     * *Note:* UTF-32 flavors are not supported by Java, the XML-parser will complain.

     * @return Returns the encoding string.
     */
    fun getEncoding(): String {
        if (encoding == null) {
            // needs four byte at maximum to determine encoding
            if (length < 2) {
                // only one byte length must be UTF-8
                encoding = "UTF-8"
            } else if (buffer!![0].toInt() == 0) {
                // These cases are:
                //   00 nn -- -- - Big endian UTF-16
                //   00 00 00 nn - Big endian UTF-32
                //   00 00 FE FF - Big endian UTF 32

                if (length < 4 || buffer!![1].toInt() != 0) {
                    encoding = "UTF-16BE"
                } else if (buffer!![2] and 0xFF == 0xFE && buffer!![3] and 0xFF == 0xFF) {
                    encoding = "UTF-32BE"
                } else {
                    encoding = "UTF-32"
                }
            } else if (buffer!![0] and 0xFF < 0x80) {
                // These cases are:
                //   nn mm -- -- - UTF-8, includes EF BB BF case
                //   nn 00 -- -- - Little endian UTF-16

                if (buffer!![1].toInt() != 0) {
                    encoding = "UTF-8"
                } else if (length < 4 || buffer!![2].toInt() != 0) {
                    encoding = "UTF-16LE"
                } else {
                    encoding = "UTF-32LE"
                }
            } else {
                // These cases are:
                //   EF BB BF -- - UTF-8
                //   FE FF -- -- - Big endian UTF-16
                //   FF FE 00 00 - Little endian UTF-32
                //   FF FE -- -- - Little endian UTF-16

                if (buffer!![0] and 0xFF == 0xEF) {
                    encoding = "UTF-8"
                } else if (buffer!![0] and 0xFF == 0xFE) {
                    encoding = "UTF-16" // in fact BE 
                } else if (length < 4 || buffer!![2].toInt() != 0) {
                    encoding = "UTF-16" // in fact LE
                } else {
                    encoding = "UTF-32" // in fact LE
                }
            }
        }

        return encoding
    }


    /**
     * Ensures the requested capacity by increasing the buffer size when the
     * current length is exceeded.

     * @param requestedLength requested new buffer length
     */
    private fun ensureCapacity(requestedLength: Int) {
        if (requestedLength > buffer!!.size) {
            val oldBuf = buffer
            buffer = ByteArray(oldBuf.size * 2)
            System.arraycopy(oldBuf, 0, buffer, 0, oldBuf.size)
        }
    }
}
/**
 * Append a byte array to the buffer
 * @param bytes a byte array
 */
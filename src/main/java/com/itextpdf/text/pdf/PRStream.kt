/*
 * $Id: 1e7daf8781fa833fa52c68e83de53d435062913f $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Bruno Lowagie, Paulo Soares, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.text.pdf

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

import com.itextpdf.text.Document
import com.itextpdf.text.ExceptionConverter

class PRStream : PdfStream {

    var reader: PdfReader
        protected set
    var offset: Long = 0
        protected set
    var length: Int = 0
        set(length) {
            this.length = length
            put(PdfName.LENGTH, PdfNumber(length))
        }

    //added by ujihara for decryption
    internal var objNum = 0
        protected set
    internal var objGen = 0
        protected set

    constructor(stream: PRStream, newDic: PdfDictionary?) {
        reader = stream.reader
        offset = stream.offset
        length = stream.length
        compressed = stream.compressed
        compressionLevel = stream.compressionLevel
        streamBytes = stream.streamBytes
        bytes = stream.bytes
        objNum = stream.objNum
        objGen = stream.objGen
        if (newDic != null)
            putAll(newDic)
        else
            hashMap.putAll(stream.hashMap)
    }

    constructor(stream: PRStream, newDic: PdfDictionary, reader: PdfReader) : this(stream, newDic) {
        this.reader = reader
    }

    constructor(reader: PdfReader, offset: Long) {
        this.reader = reader
        this.offset = offset
    }

    /**
     * Creates a new PDF stream object that will replace a stream
     * in a existing PDF file.
     * @param    reader    the reader that holds the existing PDF
     * *
     * @param    conts    the new content
     * *
     * @param    compressionLevel    the compression level for the content
     * *
     * @since    2.1.3 (replacing the existing constructor without param compressionLevel)
     */
    @JvmOverloads constructor(reader: PdfReader, conts: ByteArray, compressionLevel: Int = PdfStream.DEFAULT_COMPRESSION) {
        this.reader = reader
        this.offset = -1
        if (Document.compress) {
            try {
                val stream = ByteArrayOutputStream()
                val deflater = Deflater(compressionLevel)
                val zip = DeflaterOutputStream(stream, deflater)
                zip.write(conts)
                zip.close()
                deflater.end()
                bytes = stream.toByteArray()
            } catch (ioe: IOException) {
                throw ExceptionConverter(ioe)
            }

            put(PdfName.FILTER, PdfName.FLATEDECODE)
        } else
            bytes = conts
        length = bytes!!.size
    }

    /**
     * Sets the data associated with the stream, either compressed or
     * uncompressed. Note that the data will never be compressed if
     * Document.compress is set to false.

     * @param data raw data, decrypted and uncompressed.
     * *
     * @param compress true if you want the stream to be compressed.
     * *
     * @param compressionLevel    a value between -1 and 9 (ignored if compress == false)
     * *
     * @since    iText 2.1.3
     */
    @JvmOverloads fun setData(data: ByteArray, compress: Boolean = true, compressionLevel: Int = PdfStream.DEFAULT_COMPRESSION) {
        remove(PdfName.FILTER)
        this.offset = -1
        if (Document.compress && compress) {
            try {
                val stream = ByteArrayOutputStream()
                val deflater = Deflater(compressionLevel)
                val zip = DeflaterOutputStream(stream, deflater)
                zip.write(data)
                zip.close()
                deflater.end()
                bytes = stream.toByteArray()
                this.compressionLevel = compressionLevel
            } catch (ioe: IOException) {
                throw ExceptionConverter(ioe)
            }

            put(PdfName.FILTER, PdfName.FLATEDECODE)
        } else
            bytes = data
        length = bytes!!.size
    }

    /**
     * Sets the data associated with the stream, as-is.  This method will not
     * remove or change any existing filter: the data has to match an existing
     * filter or an appropriate filter has to be set.

     * @param data data, possibly encrypted and/or compressed
     * *
     * @since 5.5.0
     */
    fun setDataRaw(data: ByteArray) {
        this.offset = -1
        bytes = data
        length = bytes!!.size
    }

    override var bytes: ByteArray
        get() = bytes
        set(value: ByteArray) {
            super.bytes = value
        }

    fun setObjNum(objNum: Int, objGen: Int) {
        this.objNum = objNum
        this.objGen = objGen
    }

    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter?, os: OutputStream) {
        var b = PdfReader.getStreamBytesRaw(this)
        var crypto: PdfEncryption? = null
        if (writer != null)
            crypto = writer.encryption
        val objLen = get(PdfName.LENGTH)
        var nn = b.size
        if (crypto != null)
            nn = crypto.calculateStreamSize(nn)
        put(PdfName.LENGTH, PdfNumber(nn))
        superToPdf(writer, os)
        put(PdfName.LENGTH, objLen)
        os.write(PdfStream.STARTSTREAM)
        if (length > 0) {
            if (crypto != null && !crypto.isEmbeddedFilesOnly)
                b = crypto.encryptByteArray(b)
            os.write(b)
        }
        os.write(PdfStream.ENDSTREAM)
    }
}
/**
 * Sets the data associated with the stream, either compressed or
 * uncompressed. Note that the data will never be compressed if
 * Document.compress is set to false.

 * @param data raw data, decrypted and uncompressed.
 * *
 * @param compress true if you want the stream to be compressed.
 * *
 * @since    iText 2.1.1
 */
/**Sets the data associated with the stream
 * @param data raw data, decrypted and uncompressed.
 */

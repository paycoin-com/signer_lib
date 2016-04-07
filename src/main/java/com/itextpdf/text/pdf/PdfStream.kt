/*
 * $Id: 4cad7785e828823a237f521d9b26a963fad4f567 $
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
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.DocWriter
import com.itextpdf.text.Document
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.pdf.internal.PdfIsoKeys

/**
 * PdfStream is the Pdf stream object.
 *
 * A stream, like a string, is a sequence of characters. However, an application can
 * read a small portion of a stream at a time, while a string must be read in its entirety.
 * For this reason, objects with potentially large amounts of data, such as images and
 * page descriptions, are represented as streams.
 * A stream consists of a dictionary that describes a sequence of characters, followed by
 * the keyword stream, followed by zero or more lines of characters, followed by
 * the keyword endstream.
 * All streams must be PdfIndirectObjects. The stream dictionary must be a direct
 * object. The keyword stream that follows the stream dictionary should be followed by
 * a carriage return and linefeed or just a linefeed.
 * Remark: In this version only the FLATEDECODE-filter is supported.
 * This object is described in the 'Portable Document Format Reference Manual version 1.7'
 * section 3.2.7 (page 60-63).

 * @see PdfObject

 * @see PdfDictionary
 */

open class PdfStream : PdfDictionary {


    /** is the stream compressed?  */
    protected var compressed = false
    /**
     * The level of compression.
     * @since    2.1.3
     */
    protected var compressionLevel = NO_COMPRESSION

    protected var streamBytes: ByteArrayOutputStream? = null
    protected var inputStream: InputStream? = null
    protected var ref: PdfIndirectReference
    protected var inputStreamLength = -1
    protected var writer: PdfWriter
    /**
     * Gets the raw length of the stream.
     * @return the raw length of the stream
     */
    var rawLength: Int = 0
        protected set

    // constructors

    /**
     * Constructs a PdfStream-object.

     * @param        bytes            content of the new PdfObject as an array of byte.
     */

    constructor(bytes: ByteArray) : super() {
        type = PdfObject.STREAM
        this.bytes = bytes
        rawLength = bytes.size
        put(PdfName.LENGTH, PdfNumber(bytes.size))
    }

    /**
     * Creates an efficient stream. No temporary array is ever created. The InputStream
     * is totally consumed but is not closed. The general usage is:
     *
     *
     *
     * InputStream in = ...;
     * PdfStream stream = new PdfStream(in, writer);
     * stream.flateCompress();
     * writer.addToBody(stream);
     * stream.writeLength();
     * in.close();
     *
     * @param inputStream the data to write to this stream
     * *
     * @param writer the PdfWriter for this stream
     */
    constructor(inputStream: InputStream, writer: PdfWriter) : super() {
        type = PdfObject.STREAM
        this.inputStream = inputStream
        this.writer = writer
        ref = writer.pdfIndirectReference
        put(PdfName.LENGTH, ref)
    }

    /**
     * Constructs a PdfStream-object.
     */

    protected constructor() : super() {
        type = PdfObject.STREAM
    }

    /**
     * Writes the stream length to the PdfWriter.
     *
     *
     * This method must be called and can only be called if the constructor [.PdfStream]
     * is used to create the stream.
     * @throws IOException on error
     * *
     * @see .PdfStream
     */
    @Throws(IOException::class)
    fun writeLength() {
        if (inputStream == null)
            throw UnsupportedOperationException(MessageLocalization.getComposedMessage("writelength.can.only.be.called.in.a.contructed.pdfstream.inputstream.pdfwriter"))
        if (inputStreamLength == -1)
            throw IOException(MessageLocalization.getComposedMessage("writelength.can.only.be.called.after.output.of.the.stream.body"))
        writer.addToBody(PdfNumber(inputStreamLength), ref, false)
    }

    /**
     * Compresses the stream.
     * @param compressionLevel the compression level (0 = best speed, 9 = best compression, -1 is default)
     * *
     * @since    2.1.3
     */
    @JvmOverloads fun flateCompress(compressionLevel: Int = DEFAULT_COMPRESSION) {
        if (!Document.compress)
            return
        // check if the flateCompress-method has already been
        if (compressed) {
            return
        }
        this.compressionLevel = compressionLevel
        if (inputStream != null) {
            compressed = true
            return
        }
        // check if a filter already exists
        val filter = PdfReader.getPdfObject(get(PdfName.FILTER))
        if (filter != null) {
            if (filter.isName) {
                if (PdfName.FLATEDECODE == filter)
                    return
            } else if (filter.isArray) {
                if ((filter as PdfArray).contains(PdfName.FLATEDECODE))
                    return
            } else {
                throw RuntimeException(MessageLocalization.getComposedMessage("stream.could.not.be.compressed.filter.is.not.a.name.or.array"))
            }
        }
        try {
            // compress
            val stream = ByteArrayOutputStream()
            val deflater = Deflater(compressionLevel)
            val zip = DeflaterOutputStream(stream, deflater)
            if (streamBytes != null)
                streamBytes!!.writeTo(zip)
            else
                zip.write(bytes)
            zip.close()
            deflater.end()
            // update the object
            streamBytes = stream
            bytes = null
            put(PdfName.LENGTH, PdfNumber(streamBytes!!.size()))
            if (filter == null) {
                put(PdfName.FILTER, PdfName.FLATEDECODE)
            } else {
                val filters = PdfArray(filter)
                filters.add(0, PdfName.FLATEDECODE)
                put(PdfName.FILTER, filters)
            }
            compressed = true
        } catch (ioe: IOException) {
            throw ExceptionConverter(ioe)
        }

    }

    //    public int getStreamLength(PdfWriter writer) {
    //        if (dicBytes == null)
    //            toPdf(writer);
    //        if (streamBytes != null)
    //            return streamBytes.size() + dicBytes.length + SIZESTREAM;
    //        else
    //            return bytes.length + dicBytes.length + SIZESTREAM;
    //    }

    @Throws(IOException::class)
    protected fun superToPdf(writer: PdfWriter, os: OutputStream) {
        super.toPdf(writer, os)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfDictionary.toPdf
     */
    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter?, os: OutputStream) {
        if (inputStream != null && compressed)
            put(PdfName.FILTER, PdfName.FLATEDECODE)
        var crypto: PdfEncryption? = null
        if (writer != null)
            crypto = writer.encryption
        if (crypto != null) {
            val filter = get(PdfName.FILTER)
            if (filter != null) {
                if (PdfName.CRYPT == filter)
                    crypto = null
                else if (filter.isArray) {
                    val a = filter as PdfArray?
                    if (!a.isEmpty && PdfName.CRYPT == a.getPdfObject(0))
                        crypto = null
                }
            }
        }
        val nn = get(PdfName.LENGTH)
        if (crypto != null && nn != null && nn.isNumber) {
            val sz = (nn as PdfNumber).intValue()
            put(PdfName.LENGTH, PdfNumber(crypto.calculateStreamSize(sz)))
            superToPdf(writer, os)
            put(PdfName.LENGTH, nn)
        } else
            superToPdf(writer, os)
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_STREAM, this)
        os.write(STARTSTREAM)
        if (inputStream != null) {
            rawLength = 0
            var def: DeflaterOutputStream? = null
            val osc = OutputStreamCounter(os)
            var ose: OutputStreamEncryption? = null
            var fout: OutputStream = osc
            if (crypto != null && !crypto.isEmbeddedFilesOnly)
                fout = ose = crypto.getEncryptionStream(fout)
            var deflater: Deflater? = null
            if (compressed) {
                deflater = Deflater(compressionLevel)
                fout = def = DeflaterOutputStream(fout, deflater, 0x8000)
            }

            val buf = ByteArray(4192)
            while (true) {
                val n = inputStream!!.read(buf)
                if (n <= 0)
                    break
                fout.write(buf, 0, n)
                rawLength += n
            }
            if (def != null) {
                def.finish()
                deflater!!.end()
            }
            if (ose != null)
                ose.finish()
            inputStreamLength = osc.counter.toInt()
        } else {
            if (crypto != null && !crypto.isEmbeddedFilesOnly) {
                val b: ByteArray
                if (streamBytes != null) {
                    b = crypto.encryptByteArray(streamBytes!!.toByteArray())
                } else {
                    b = crypto.encryptByteArray(bytes)
                }
                os.write(b)
            } else {
                if (streamBytes != null)
                    streamBytes!!.writeTo(os)
                else
                    os.write(bytes)
            }
        }
        os.write(ENDSTREAM)
    }

    /**
     * Writes the data content to an OutputStream.
     * @param os the destination to write to
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    fun writeContent(os: OutputStream) {
        if (streamBytes != null)
            streamBytes!!.writeTo(os)
        else if (bytes != null)
            os.write(bytes)
    }

    /**
     * @see com.itextpdf.text.pdf.PdfObject.toString
     */
    override fun toString(): String {
        if (get(PdfName.TYPE) == null) return "Stream"
        return "Stream of type: " + get(PdfName.TYPE)!!
    }

    companion object {

        // membervariables

        /**
         * A possible compression level.
         * @since    2.1.3
         */
        val DEFAULT_COMPRESSION = -1
        /**
         * A possible compression level.
         * @since    2.1.3
         */
        val NO_COMPRESSION = 0
        /**
         * A possible compression level.
         * @since    2.1.3
         */
        val BEST_SPEED = 1
        /**
         * A possible compression level.
         * @since    2.1.3
         */
        val BEST_COMPRESSION = 9

        internal val STARTSTREAM = DocWriter.getISOBytes("stream\n")
        internal val ENDSTREAM = DocWriter.getISOBytes("\nendstream")
        internal val SIZESTREAM = STARTSTREAM.size + ENDSTREAM.size
    }
}
/**
 * Compresses the stream.
 */

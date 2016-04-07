/*
 * $Id: d699c887eaa48d534ca38f969f4a62eac11851b1 $
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

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

/**
 * Extends PdfStream and should be used to create Streams for Embedded Files
 * (file attachments).
 * @since    2.1.3
 */

class PdfEFStream : PdfStream {

    /**
     * Creates a Stream object using an InputStream and a PdfWriter object
     * @param    in    the InputStream that will be read to get the Stream object
     * *
     * @param    writer    the writer to which the stream will be added
     */
    constructor(`in`: InputStream, writer: PdfWriter) : super(`in`, writer) {
    }

    /**
     * Creates a Stream object using a byte array
     * @param    fileStore    the bytes for the stream
     */
    constructor(fileStore: ByteArray) : super(fileStore) {
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
        if (crypto != null && crypto.isEmbeddedFilesOnly) {
            val filter = PdfArray()
            val decodeparms = PdfArray()
            val crypt = PdfDictionary()
            crypt.put(PdfName.NAME, PdfName.STDCF)
            filter.add(PdfName.CRYPT)
            decodeparms.add(crypt)
            if (compressed) {
                filter.add(PdfName.FLATEDECODE)
                decodeparms.add(PdfNull())
            }
            put(PdfName.FILTER, filter)
            put(PdfName.DECODEPARMS, decodeparms)
        }
        val nn = get(PdfName.LENGTH)
        if (crypto != null && nn != null && nn.isNumber) {
            val sz = (nn as PdfNumber).intValue()
            put(PdfName.LENGTH, PdfNumber(crypto.calculateStreamSize(sz)))
            superToPdf(writer, os)
            put(PdfName.LENGTH, nn)
        } else
            superToPdf(writer, os)

        os.write(PdfStream.STARTSTREAM)
        if (inputStream != null) {
            rawLength = 0
            var def: DeflaterOutputStream? = null
            val osc = OutputStreamCounter(os)
            var ose: OutputStreamEncryption? = null
            var fout: OutputStream = osc
            if (crypto != null)
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
            if (crypto == null) {
                if (streamBytes != null)
                    streamBytes!!.writeTo(os)
                else
                    os.write(bytes)
            } else {
                val b: ByteArray
                if (streamBytes != null) {
                    b = crypto.encryptByteArray(streamBytes!!.toByteArray())
                } else {
                    b = crypto.encryptByteArray(bytes)
                }
                os.write(b)
            }
        }
        os.write(PdfStream.ENDSTREAM)
    }
}

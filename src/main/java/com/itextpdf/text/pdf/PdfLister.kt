/*
 * $Id: 458e2e43fe197ce5059485aff30d55e827b494cf $
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
import java.io.PrintStream

/**
 * List a PDF file in human-readable form (for debugging reasons mostly)
 * @author Mark Thompson
 */

class PdfLister
/**
 * Create a new lister object.
 * @param out
 */
(
        /** the printStream you want to write the output to.  */
        internal var out: PrintStream) {

    /**
     * Visualizes a PDF object.
     * @param object    a com.itextpdf.text.pdf object
     */
    fun listAnyObject(`object`: PdfObject) {
        when (`object`.type()) {
            PdfObject.ARRAY -> listArray(`object` as PdfArray)
            PdfObject.DICTIONARY -> listDict(`object` as PdfDictionary)
            PdfObject.STRING -> out.println("(" + `object`.toString() + ")")
            else -> out.println(`object`.toString())
        }
    }

    /**
     * Visualizes a PdfDictionary object.
     * @param dictionary    a com.itextpdf.text.pdf.PdfDictionary object
     */
    fun listDict(dictionary: PdfDictionary) {
        out.println("<<")
        var value: PdfObject
        for (key in dictionary.keys) {
            value = dictionary.get(key)
            out.print(key.toString())
            out.print(' ')
            listAnyObject(value)
        }
        out.println(">>")
    }

    /**
     * Visualizes a PdfArray object.
     * @param array    a com.itextpdf.text.pdf.PdfArray object
     */
    fun listArray(array: PdfArray) {
        out.println('[')
        val i = array.listIterator()
        while (i.hasNext()) {
            val item = i.next()
            listAnyObject(item)
        }
        out.println(']')
    }

    /**
     * Visualizes a Stream.
     * @param stream
     * *
     * @param reader
     */
    fun listStream(stream: PRStream, reader: PdfReaderInstance) {
        try {
            listDict(stream)
            out.println("startstream")
            val b = PdfReader.getStreamBytes(stream)
            //                  byte buf[] = new byte[Math.min(stream.getLength(), 4096)];
            //                  int r = 0;
            //                  stream.openStream(reader);
            //                  for (;;) {
            //                      r = stream.readStream(buf, 0, buf.length);
            //                      if (r == 0) break;
            //                      out.write(buf, 0, r);
            //                  }
            //                  stream.closeStream();
            val len = b.size - 1
            for (k in 0..len - 1) {
                if (b[k] == '\r' && b[k + 1] != '\n')
                    b[k] = '\n'.toByte()
            }
            out.println(String(b))
            out.println("endstream")
        } catch (e: IOException) {
            System.err.println("I/O exception: " + e)
            //          } catch (java.util.zip.DataFormatException e) {
            //              System.err.println("Data Format Exception: " + e);
        }

    }

    /**
     * Visualizes an imported page
     * @param iPage
     */
    fun listPage(iPage: PdfImportedPage) {
        val pageNum = iPage.pageNumber
        val readerInst = iPage.pdfReaderInstance
        val reader = readerInst.reader

        val page = reader.getPageN(pageNum)
        listDict(page)
        val obj = PdfReader.getPdfObject(page.get(PdfName.CONTENTS)) ?: return
        when (obj.type) {
            PdfObject.STREAM -> listStream(obj as PRStream?, readerInst)
            PdfObject.ARRAY ->
                val i = (obj as PdfArray).listIterator()
            while (i.hasNext()) {
                val o = PdfReader.getPdfObject(i.next())
                listStream(o as PRStream, readerInst)
                out.println("-----------")
            }
        }
    }
}

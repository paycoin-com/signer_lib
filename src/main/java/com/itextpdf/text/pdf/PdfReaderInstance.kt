/*
 * $Id: 8688f60702f6d723fe8cbd9bebe804dad90dea63 $
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
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

import com.itextpdf.text.error_messages.MessageLocalization

/**
 * Instance of PdfReader in each output document.

 * @author Paulo Soares
 */
internal class PdfReaderInstance(var reader: PdfReader, var writer: PdfWriter) {
    var myXref: IntArray
    var readerFile:

            RandomAccessFileOrArray
    var importedPages = HashMap<Int, PdfImportedPage>()
    var visited = HashSet<Int>()
    var nextRound = ArrayList<Int>()

    init {
        readerFile = reader.safeFile
        myXref = IntArray(reader.xrefSize)
    }

    fun getImportedPage(pageNumber: Int): PdfImportedPage {
        if (!reader.isOpenedWithFullPermissions)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("pdfreader.not.opened.with.owner.password"))
        if (pageNumber < 1 || pageNumber > reader.numberOfPages)
            throw IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.page.number.1", pageNumber))
        val i = Integer.valueOf(pageNumber)
        var pageT: PdfImportedPage? = importedPages[i]
        if (pageT == null) {
            pageT = PdfImportedPage(this, writer, pageNumber)
            importedPages.put(i, pageT)
        }
        return pageT
    }

    fun getNewObjectNumber(number: Int, generation: Int): Int {
        if (myXref[number] == 0) {
            myXref[number] = writer.indirectReferenceNumber
            nextRound.add(Integer.valueOf(number))
        }
        return myXref[number]
    }

    fun getResources(pageNumber: Int): PdfObject {
        val obj = PdfReader.getPdfObjectRelease(reader.getPageNRelease(pageNumber)!!.get(PdfName.RESOURCES))
        return obj
    }

    /**
     * Gets the content stream of a page as a PdfStream object.
     * @param    pageNumber            the page of which you want the stream
     * *
     * @param    compressionLevel    the compression level you want to apply to the stream
     * *
     * @return    a PdfStream object
     * *
     * @since    2.1.3 (the method already existed without param compressionLevel)
     */
    @Throws(IOException::class)
    fun getFormXObject(pageNumber: Int, compressionLevel: Int): PdfStream {
        val page = reader.getPageNRelease(pageNumber)
        val contents = PdfReader.getPdfObjectRelease(page.get(PdfName.CONTENTS))
        val dic = PdfDictionary()
        var bout: ByteArray? = null
        if (contents != null) {
            if (contents.isStream)
                dic.putAll(contents as PRStream?)
            else
                bout = reader.getPageContent(pageNumber, readerFile)
        } else
            bout = ByteArray(0)
        dic.put(PdfName.RESOURCES, PdfReader.getPdfObjectRelease(page.get(PdfName.RESOURCES)))
        dic.put(PdfName.TYPE, PdfName.XOBJECT)
        dic.put(PdfName.SUBTYPE, PdfName.FORM)
        val impPage = importedPages[Integer.valueOf(pageNumber)]
        dic.put(PdfName.BBOX, PdfRectangle(impPage.boundingBox))
        val matrix = impPage.matrix
        if (matrix == null)
            dic.put(PdfName.MATRIX, IDENTITYMATRIX)
        else
            dic.put(PdfName.MATRIX, matrix)
        dic.put(PdfName.FORMTYPE, ONE)
        val stream: PRStream
        if (bout == null) {
            stream = PRStream(contents as PRStream?, dic)
        } else {
            stream = PRStream(reader, bout, compressionLevel)
            stream.putAll(dic)
        }
        return stream
    }

    @Throws(IOException::class)
    fun writeAllVisited() {
        while (!nextRound.isEmpty()) {
            val vec = nextRound
            nextRound = ArrayList<Int>()
            for (k in vec.indices) {
                val i = vec[k]
                if (!visited.contains(i)) {
                    visited.add(i)
                    val n = i.toInt()
                    writer.addToBody(reader.getPdfObjectRelease(n), myXref[n])
                }
            }
        }
    }

    @Throws(IOException::class)
    fun writeAllPages() {
        try {
            readerFile.reOpen()
            for (element in importedPages.values) {
                if (element.isToCopy) {
                    writer.addToBody(element.getFormXObject(writer.compressionLevel), element.indirectReference)
                    element.setCopied()
                }
            }
            writeAllVisited()
        } finally {
            try {
                // TODO: Removed - the user should be responsible for closing all PdfReaders.  But, this could cause a lot of memory leaks in code out there that hasn't been properly closing things - maybe add a finalizer to PdfReader that calls PdfReader#close() ??            	
                //                reader.close();
                readerFile.close()
            } catch (e: Exception) {
                //Empty on purpose
            }

        }
    }

    companion object {
        val IDENTITYMATRIX = PdfLiteral("[1 0 0 1 0 0]")
        val ONE = PdfNumber(1)
    }
}

/*
 * $Id: d9f0faf6ead84117152fdf5675398ca10a231995 $
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

import com.itextpdf.text.DocumentException
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.error_messages.MessageLocalization

/**
 * PdfPages is the PDF Pages-object.
 *
 * The Pages of a document are accessible through a tree of nodes known as the Pages tree.
 * This tree defines the ordering of the pages in the document.
 * This object is described in the 'Portable Document Format Reference Manual version 1.3'
 * section 6.3 (page 71-73)

 * @see PdfPage
 */

class PdfPages// constructors

/**
 * Constructs a PdfPages-object.
 */
internal constructor(private val writer: PdfWriter) {

    private val pages = ArrayList<PdfIndirectReference>()
    private val parents = ArrayList<PdfIndirectReference>()
    private var leafSize = 10
    internal var topParent:

            PdfIndirectReference? = null
        private set

    internal fun addPage(page: PdfDictionary) {
        try {
            if (pages.size % leafSize == 0)
                parents.add(writer.pdfIndirectReference)
            val parent = parents[parents.size - 1]
            page.put(PdfName.PARENT, parent)
            val current = writer.currentPage
            writer.addToBody(page, current)
            pages.add(current)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    internal fun addPageRef(pageRef: PdfIndirectReference): PdfIndirectReference {
        try {
            if (pages.size % leafSize == 0)
                parents.add(writer.pdfIndirectReference)
            pages.add(pageRef)
            return parents[parents.size - 1]
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

    }

    // returns the top parent to include in the catalog
    @Throws(IOException::class)
    internal fun writePageTree(): PdfIndirectReference {
        if (pages.isEmpty())
            throw IOException(MessageLocalization.getComposedMessage("the.document.has.no.pages"))
        var leaf = 1
        var tParents = parents
        var tPages = pages
        var nextParents = ArrayList<PdfIndirectReference>()
        while (true) {
            leaf *= leafSize
            val stdCount = leafSize
            var rightCount = tPages.size % leafSize
            if (rightCount == 0)
                rightCount = leafSize
            for (p in tParents.indices) {
                val count: Int
                var thisLeaf = leaf
                if (p == tParents.size - 1) {
                    count = rightCount
                    thisLeaf = pages.size % leaf
                    if (thisLeaf == 0)
                        thisLeaf = leaf
                } else
                    count = stdCount
                val top = PdfDictionary(PdfName.PAGES)
                top.put(PdfName.COUNT, PdfNumber(thisLeaf))
                val kids = PdfArray()
                val internal = kids.arrayList
                internal.addAll(tPages.subList(p * stdCount, p * stdCount + count))
                top.put(PdfName.KIDS, kids)
                if (tParents.size > 1) {
                    if (p % leafSize == 0)
                        nextParents.add(writer.pdfIndirectReference)
                    top.put(PdfName.PARENT, nextParents[p / leafSize])
                }
                writer.addToBody(top, tParents[p])
            }
            if (tParents.size == 1) {
                topParent = tParents[0]
                return topParent
            }
            tPages = tParents
            tParents = nextParents
            nextParents = ArrayList<PdfIndirectReference>()
        }
    }

    internal fun setLinearMode(topParent: PdfIndirectReference?) {
        if (parents.size > 1)
            throw RuntimeException(MessageLocalization.getComposedMessage("linear.page.mode.can.only.be.called.with.a.single.parent"))
        if (topParent != null) {
            this.topParent = topParent
            parents.clear()
            parents.add(topParent)
        }
        leafSize = 10000000
    }

    internal fun addPage(page: PdfIndirectReference) {
        pages.add(page)
    }

    @Throws(DocumentException::class)
    internal fun reorderPages(order: IntArray?): Int {
        if (order == null)
            return pages.size
        if (parents.size > 1)
            throw DocumentException(MessageLocalization.getComposedMessage("page.reordering.requires.a.single.parent.in.the.page.tree.call.pdfwriter.setlinearmode.after.open"))
        if (order.size != pages.size)
            throw DocumentException(MessageLocalization.getComposedMessage("page.reordering.requires.an.array.with.the.same.size.as.the.number.of.pages"))
        val max = pages.size
        val temp = BooleanArray(max)
        for (k in 0..max - 1) {
            val p = order[k]
            if (p < 1 || p > max)
                throw DocumentException(MessageLocalization.getComposedMessage("page.reordering.requires.pages.between.1.and.1.found.2", max.toString(), p.toString()))
            if (temp[p - 1])
                throw DocumentException(MessageLocalization.getComposedMessage("page.reordering.requires.no.page.repetition.page.1.is.repeated", p))
            temp[p - 1] = true
        }
        val copy = pages.toArray<PdfIndirectReference>(arrayOfNulls<PdfIndirectReference>(pages.size))
        for (k in 0..max - 1) {
            pages[k] = copy[order[k] - 1]
        }
        return max
    }
}

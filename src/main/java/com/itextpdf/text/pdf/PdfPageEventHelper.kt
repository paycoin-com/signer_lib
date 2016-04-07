/*
 * $Id: 3733e61c02910234e42d6b825cbe199156379ae9 $
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

import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Rectangle

/**
 * Helps the use of PdfPageEvent by implementing all the interface methods.
 * A class can extend PdfPageEventHelper and only implement the
 * needed methods.
 *
 *
 * Note: do not use Document.add() inside a page event.
 * @author Paulo Soares
 */

class PdfPageEventHelper : PdfPageEvent {

    /**
     * Called when the document is opened.

     * @param writer the PdfWriter for this document
     * *
     * @param document the document
     */
    override fun onOpenDocument(writer: PdfWriter, document: Document) {
    }

    /**
     * Called when a page is initialized.
     *
     * Note that if even if a page is not written this method is still
     * called. It is preferable to use onEndPage to avoid
     * infinite loops.

     * @param writer the PdfWriter for this document
     * *
     * @param document the document
     */
    override fun onStartPage(writer: PdfWriter, document: Document) {
    }

    /**
     * Called when a page is finished, just before being written to the document.

     * @param writer the PdfWriter for this document
     * *
     * @param document the document
     */
    override fun onEndPage(writer: PdfWriter, document: Document) {
    }

    /**
     * Called when the document is closed.
     *
     * Note that this method is called with the page number equal
     * to the last page plus one.

     * @param writer the PdfWriter for this document
     * *
     * @param document the document
     */
    override fun onCloseDocument(writer: PdfWriter, document: Document) {
    }

    /**
     * Called when a Paragraph is written.
     *
     * paragraphPosition will hold the height at which the
     * paragraph will be written to. This is useful to insert bookmarks with
     * more control.

     * @param writer the PdfWriter for this document
     * *
     * @param document the document
     * *
     * @param paragraphPosition the position the paragraph will be written to
     */
    override fun onParagraph(writer: PdfWriter, document: Document, paragraphPosition: Float) {
    }

    /**
     * Called when a Paragraph is written.
     *
     * paragraphPosition will hold the height of the end of the paragraph.

     * @param writer the PdfWriter for this document
     * *
     * @param document the document
     * *
     * @param paragraphPosition the position of the end of the paragraph
     */
    override fun onParagraphEnd(writer: PdfWriter, document: Document, paragraphPosition: Float) {
    }

    /**
     * Called when a Chapter is written.
     *
     * position will hold the height at which the
     * chapter will be written to.

     * @param writer            the PdfWriter for this document
     * *
     * @param document          the document
     * *
     * @param paragraphPosition the position the chapter will be written to
     * *
     * @param title             the title of the Chapter
     */
    override fun onChapter(writer: PdfWriter, document: Document, paragraphPosition: Float, title: Paragraph) {
    }

    /**
     * Called when the end of a Chapter is reached.
     *
     * position will hold the height of the end of the chapter.

     * @param writer the PdfWriter for this document
     * *
     * @param document the document
     * *
     * @param position the position of the end of the chapter.
     */
    override fun onChapterEnd(writer: PdfWriter, document: Document, position: Float) {
    }

    /**
     * Called when a Section is written.
     *
     * position will hold the height at which the
     * section will be written to.

     * @param writer            the PdfWriter for this document
     * *
     * @param document          the document
     * *
     * @param paragraphPosition the position the section will be written to
     * *
     * @param depth                the number depth of the Section
     * *
     * @param title             the title of the section
     */
    override fun onSection(writer: PdfWriter, document: Document, paragraphPosition: Float, depth: Int, title: Paragraph) {
    }

    /**
     * Called when the end of a Section is reached.
     *
     * position will hold the height of the section end.

     * @param writer the PdfWriter for this document
     * *
     * @param document the document
     * *
     * @param position the position of the end of the section
     */
    override fun onSectionEnd(writer: PdfWriter, document: Document, position: Float) {
    }

    /**
     * Called when a Chunk with a generic tag is written.
     *
     * It is useful to pinpoint the Chunk location to generate
     * bookmarks, for example.

     * @param writer the PdfWriter for this document
     * *
     * @param document the document
     * *
     * @param rect the Rectangle containing the Chunk
     * *
     * @param text the text of the tag
     */
    override fun onGenericTag(writer: PdfWriter, document: Document, rect: Rectangle, text: String) {
    }
}

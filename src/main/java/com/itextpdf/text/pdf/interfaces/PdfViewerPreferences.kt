/*
 * $Id: 36fff9ca2bfe9427638a7f81783fc55131ad9c22 $
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
package com.itextpdf.text.pdf.interfaces

import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfObject

/**
 * Viewer preferences are described in section 3.6.1 and 8.1 of the
 * PDF Reference 1.7 (Table 3.25 on p139-142 and Table 8.1 on p579-581).
 * They are explained in section 13.1 of the book 'iText in Action'.
 * The values of the different  preferences were originally stored
 * in class PdfWriter, but they have been moved to this separate interface
 * for reasons of convenience.
 */

interface PdfViewerPreferences {

    /**
     * Sets the page layout and page mode preferences by ORing one or two of these constants.
     *
     *
     *
     *  * The page layout to be used when the document is opened (choose one).
     *
     *  * **PageLayoutSinglePage** - Display one page at a time. (default)
     *  * **PageLayoutOneColumn** - Display the pages in one column.
     *  * **PageLayoutTwoColumnLeft** - Display the pages in two columns, with
     * odd-numbered pages on the left.
     *  * **PageLayoutTwoColumnRight** - Display the pages in two columns, with
     * odd-numbered pages on the right.
     *  * **PageLayoutTwoPageLeft** - Display the pages two at a time, with
     * odd-numbered pages on the left.
     *  * **PageLayoutTwoPageRight** - Display the pages two at a time, with
     * odd-numbered pages on the right.
     *
     *  * The page mode how the document should be displayed
     * when opened (choose one).
     *
     *  * **PageModeUseNone** - Neither document outline nor thumbnail images visible. (default)
     *  * **PageModeUseOutlines** - Document outline visible.
     *  * **PageModeUseThumbs** - Thumbnail images visible.
     *  * **PageModeFullScreen** - Full-screen mode, with no menu bar, window
     * controls, or any other window visible.
     *  * **PageModeUseOC** - Optional content group panel visible
     *  * **PageModeUseAttachments** - Attachments panel visible
     *
     *
     * For backward compatibility these values are also supported,
     * but it's better to use method `addViewerPreference(key, value)`
     * if you want to change the following preferences:
     *
     *  * **HideToolbar** - A flag specifying whether to hide the viewer application's tool
     * bars when the document is active.
     *  * **HideMenubar** - A flag specifying whether to hide the viewer application's
     * menu bar when the document is active.
     *  * **HideWindowUI** - A flag specifying whether to hide user interface elements in
     * the document's window (such as scroll bars and navigation controls),
     * leaving only the document's contents displayed.
     *  * **FitWindow** - A flag specifying whether to resize the document's window to
     * fit the size of the first displayed page.
     *  * **CenterWindow** - A flag specifying whether to position the document's window
     * in the center of the screen.
     *  * **DisplayDocTitle** - A flag specifying whether to display the document's title
     * in the top bar.
     *  * The predominant reading order for text. This entry has no direct effect on the
     * document's contents or page numbering, but can be used to determine the relative
     * positioning of pages when displayed side by side or printed *n-up* (choose one).
     *
     *  * **DirectionL2R** - Left to right
     *  * **DirectionR2L** - Right to left (including vertical writing systems such as
     * Chinese, Japanese, and Korean)
     *
     *  * The document's page mode, specifying how to display the
     * document on exiting full-screen mode. It is meaningful only
     * if the page mode is **PageModeFullScreen** (choose one).
     *
     *  * **NonFullScreenPageModeUseNone** - Neither document outline nor thumbnail images
     * visible
     *  * **NonFullScreenPageModeUseOutlines** - Document outline visible
     *  * **NNonFullScreenPageModeUseThumbs** - Thumbnail images visible
     *  * **NonFullScreenPageModeUseOC** - Optional content group panel visible
     *
     *  * **PrintScalingNone** - Indicates that the print dialog should reflect no page scaling.
     *
     * @param preferences the viewer preferences
     * *
     * @see PdfViewerPreferences.addViewerPreference
     */
    fun setViewerPreferences(preferences: Int)

    /**
     * Adds a viewer preference.
     *
     *  * In case the key is one of these values:
     *
     *  * PdfName.**HIDETOOLBAR**
     *  * PdfName.**HIDEMENUBAR**
     *  * PdfName.**HIDEWINDOWUI**
     *  * PdfName.**FITWINDOW**
     *  * PdfName.**CENTERWINDOW**
     *  * PdfName.**DISPLAYDOCTITLE**
     *
     * The value must be a of type PdfBoolean (true or false).
     *  * In case the key is PdfName.**NONFULLSCREENPAGEMODE**,
     * the value must be one of these names:
     *
     *  * PdfName.**USENONE**
     *  * PdfName.**USEOUTLINES**
     *  * PdfName.**USETHUMBS**
     *  * PdfName.**USEOC**
     *
     *  * In case the key is PdfName.DIRECTION,
     * the value must be one of these names:
     *
     *  * PdfName.**L2R**
     *  * PdfName.**R2L**
     *
     *  * In case the key is one of these values:
     *
     *  * PdfName.**VIEWAREA**
     *  * PdfName.**VIEWCLIP**
     *  * PdfName.**PRINTAREA**
     *  * PdfName.**PRINTCLIP**
     *
     * The value must be one of these names:
     *
     *  * PdfName.**MEDIABOX**
     *  * PdfName.**CROPBOX**
     *  * PdfName.**BLEEDBOX**
     *  * PdfName.**TRIMBOX**
     *  * PdfName.**ARTBOX**
     *
     *  * In case the key is PdfName.**PRINTSCALING**, the value can be
     *
     *  * PdfName.**APPDEFAULT**
     *  * PdfName.**NONE**
     *
     *  * In case the key is PdfName.**DUPLEX**, the value can be:
     *
     *  * PdfName.**SIMPLEX**
     *  * PdfName.**DUPLEXFLIPSHORTEDGE**
     *  * PdfName.**DUPLEXFLIPLONGEDGE**
     *
     *  * In case the key is PdfName.**PICKTRAYBYPDFSIZE**, the value must be of type PdfBoolean.
     *  * In case the key is PdfName.**PRINTPAGERANGE**, the value must be of type PdfArray.
     *  * In case the key is PdfName.**NUMCOPIES**, the value must be of type PdfNumber.
     *
     *
     * @param key    the name of the viewer preference
     * *
     * @param value    the value of the viewer preference
     * *
     * @see PdfViewerPreferences.setViewerPreferences
     */
    fun addViewerPreference(key: PdfName, value: PdfObject)
}

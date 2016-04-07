/*
 * $Id: 6986e0888b5d5e1f7bdd132da6573d3e93002ca7 $
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

import com.itextpdf.awt.geom.AffineTransform
import com.itextpdf.text.AccessibleElementId
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Rectangle
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.interfaces.IAccessibleElement
import com.itextpdf.text.pdf.internal.PdfIsoKeys

import java.io.IOException
import java.io.OutputStream
import java.util.HashMap
import java.util.HashSet

/**
 * A PdfAnnotation is a note that is associated with a page.

 * @see PdfDictionary
 */

open class PdfAnnotation : PdfDictionary, IAccessibleElement {

    protected var writer: PdfWriter
    /**
     * Reference to this annotation.
     * @since    2.1.6; was removed in 2.1.5, but restored in 2.1.6
     */
    protected var reference: PdfIndirectReference? = null
    var templates: HashSet<PdfTemplate>? = null
        protected set
    /** Getter for property form.
     * @return Value of property form.
     */
    var isForm = false
        protected set
    /** Getter for property annotation.
     * @return Value of property annotation.
     */
    var isAnnotation = true
        protected set

    /** Holds value of property used.  */
    /** Getter for property used.
     * @return Value of property used.
     */
    var isUsed = false
        protected set

    /** Holds value of property placeInPage.  */
    /** Getter for property placeInPage.
     * @return Value of property placeInPage.
     */
    /** Places the annotation in a specified page that must be greater
     * or equal to the current one. With `PdfStamper` the page
     * can be any. The first page is 1.
     * @param placeInPage New value of property placeInPage.
     */
    var placeInPage = -1

    override var role: PdfName? = null
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id: AccessibleElementId? = null
        get() {
            if (id == null)
                id = AccessibleElementId()
            return id
        }

    // constructors
    constructor(writer: PdfWriter, rect: Rectangle?) {
        this.writer = writer
        if (rect != null)
            put(PdfName.RECT, PdfRectangle(rect))
    }

    /**
     * Constructs a new PdfAnnotation of subtype text.
     * @param writer
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @param title
     * *
     * @param content
     */

    constructor(writer: PdfWriter, llx: Float, lly: Float, urx: Float, ury: Float, title: PdfString, content: PdfString) {
        this.writer = writer
        put(PdfName.SUBTYPE, PdfName.TEXT)
        put(PdfName.T, title)
        put(PdfName.RECT, PdfRectangle(llx, lly, urx, ury))
        put(PdfName.CONTENTS, content)
    }

    /**
     * Constructs a new PdfAnnotation of subtype link (Action).
     * @param writer
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @param action
     */

    constructor(writer: PdfWriter, llx: Float, lly: Float, urx: Float, ury: Float, action: PdfAction) {
        this.writer = writer
        put(PdfName.SUBTYPE, PdfName.LINK)
        put(PdfName.RECT, PdfRectangle(llx, lly, urx, ury))
        put(PdfName.A, action)
        put(PdfName.BORDER, PdfBorderArray(0f, 0f, 0f))
        put(PdfName.C, PdfColor(0x00, 0x00, 0xFF))
    }

    /**
     * Returns an indirect reference to the annotation
     * @return the indirect reference
     */
    val indirectReference: PdfIndirectReference
        get() {
            if (reference == null) {
                reference = writer.pdfIndirectReference
            }
            return reference
        }

    fun setDefaultAppearanceString(cb: PdfContentByte) {
        val b = cb.internalBuffer.toByteArray()
        val len = b.size
        for (k in 0..len - 1) {
            if (b[k] == '\n')
                b[k] = 32
        }
        put(PdfName.DA, PdfString(b))
    }

    fun setFlags(flags: Int) {
        if (flags == 0)
            remove(PdfName.F)
        else
            put(PdfName.F, PdfNumber(flags))
    }

    fun setBorder(border: PdfBorderArray) {
        put(PdfName.BORDER, border)
    }

    fun setBorderStyle(border: PdfBorderDictionary) {
        put(PdfName.BS, border)
    }

    /**
     * Sets the annotation's highlighting mode. The values can be
     * HIGHLIGHT_NONE, HIGHLIGHT_INVERT,
     * HIGHLIGHT_OUTLINE and HIGHLIGHT_PUSH;
     * @param highlight the annotation's highlighting mode
     */
    fun setHighlighting(highlight: PdfName) {
        if (highlight == HIGHLIGHT_INVERT)
            remove(PdfName.H)
        else
            put(PdfName.H, highlight)
    }

    fun setAppearance(ap: PdfName, template: PdfTemplate) {
        var dic: PdfDictionary? = get(PdfName.AP) as PdfDictionary?
        if (dic == null)
            dic = PdfDictionary()
        dic.put(ap, template.indirectReference)
        put(PdfName.AP, dic)
        if (!isForm)
            return
        if (templates == null)
            templates = HashSet<PdfTemplate>()
        templates!!.add(template)
    }

    fun setAppearance(ap: PdfName, state: String, template: PdfTemplate) {
        var dicAp: PdfDictionary? = get(PdfName.AP) as PdfDictionary?
        if (dicAp == null)
            dicAp = PdfDictionary()

        val dic: PdfDictionary
        val obj = dicAp.get(ap)
        if (obj != null && obj.isDictionary)
            dic = obj as PdfDictionary?
        else
            dic = PdfDictionary()
        dic.put(PdfName(state), template.indirectReference)
        dicAp.put(ap, dic)
        put(PdfName.AP, dicAp)
        if (!isForm)
            return
        if (templates == null)
            templates = HashSet<PdfTemplate>()
        templates!!.add(template)
    }

    fun setAppearanceState(state: String?) {
        if (state == null) {
            remove(PdfName.AS)
            return
        }
        put(PdfName.AS, PdfName(state))
    }

    fun setColor(color: BaseColor) {
        put(PdfName.C, PdfColor(color))
    }

    fun setTitle(title: String?) {
        if (title == null) {
            remove(PdfName.T)
            return
        }
        put(PdfName.T, PdfString(title, PdfObject.TEXT_UNICODE))
    }

    fun setPopup(popup: PdfAnnotation) {
        put(PdfName.POPUP, popup.indirectReference)
        popup.put(PdfName.PARENT, indirectReference)
    }

    fun setAction(action: PdfAction) {
        put(PdfName.A, action)
    }

    fun setAdditionalActions(key: PdfName, action: PdfAction) {
        val dic: PdfDictionary
        val obj = get(PdfName.AA)
        if (obj != null && obj.isDictionary)
            dic = obj as PdfDictionary?
        else
            dic = PdfDictionary()
        dic.put(key, action)
        put(PdfName.AA, dic)
    }

    /** Setter for property used.
     */
    open fun setUsed() {
        isUsed = true
    }

    fun setPage(page: Int) {
        put(PdfName.P, writer.getPageReference(page))
    }

    fun setPage() {
        put(PdfName.P, writer.currentPage)
    }

    fun setRotate(v: Int) {
        put(PdfName.ROTATE, PdfNumber(v))
    }

    internal val mk: PdfDictionary
        get() {
            var mk: PdfDictionary? = get(PdfName.MK) as PdfDictionary?
            if (mk == null) {
                mk = PdfDictionary()
                put(PdfName.MK, mk)
            }
            return mk
        }

    fun setMKRotation(rotation: Int) {
        mk.put(PdfName.R, PdfNumber(rotation))
    }

    fun setMKBorderColor(color: BaseColor?) {
        if (color == null)
            mk.remove(PdfName.BC)
        else
            mk.put(PdfName.BC, getMKColor(color))
    }

    fun setMKBackgroundColor(color: BaseColor?) {
        if (color == null)
            mk.remove(PdfName.BG)
        else
            mk.put(PdfName.BG, getMKColor(color))
    }

    fun setMKNormalCaption(caption: String) {
        mk.put(PdfName.CA, PdfString(caption, PdfObject.TEXT_UNICODE))
    }

    fun setMKRolloverCaption(caption: String) {
        mk.put(PdfName.RC, PdfString(caption, PdfObject.TEXT_UNICODE))
    }

    fun setMKAlternateCaption(caption: String) {
        mk.put(PdfName.AC, PdfString(caption, PdfObject.TEXT_UNICODE))
    }

    fun setMKNormalIcon(template: PdfTemplate) {
        mk.put(PdfName.I, template.indirectReference)
    }

    fun setMKRolloverIcon(template: PdfTemplate) {
        mk.put(PdfName.RI, template.indirectReference)
    }

    fun setMKAlternateIcon(template: PdfTemplate) {
        mk.put(PdfName.IX, template.indirectReference)
    }

    fun setMKIconFit(scale: PdfName, scalingType: PdfName, leftoverLeft: Float, leftoverBottom: Float, fitInBounds: Boolean) {
        val dic = PdfDictionary()
        if (scale != PdfName.A)
            dic.put(PdfName.SW, scale)
        if (scalingType != PdfName.P)
            dic.put(PdfName.S, scalingType)
        if (leftoverLeft != 0.5f || leftoverBottom != 0.5f) {
            val array = PdfArray(PdfNumber(leftoverLeft))
            array.add(PdfNumber(leftoverBottom))
            dic.put(PdfName.A, array)
        }
        if (fitInBounds)
            dic.put(PdfName.FB, PdfBoolean.PDFTRUE)
        mk.put(PdfName.IF, dic)
    }

    fun setMKTextPosition(tp: Int) {
        mk.put(PdfName.TP, PdfNumber(tp))
    }

    /**
     * Sets the layer this annotation belongs to.
     * @param layer the layer this annotation belongs to
     */
    fun setLayer(layer: PdfOCG) {
        put(PdfName.OC, layer.ref)
    }

    /**
     * Sets the name of the annotation.
     * With this name the annotation can be identified among
     * all the annotations on a page (it has to be unique).
     */
    fun setName(name: String) {
        put(PdfName.NM, PdfString(name))
    }


    fun applyCTM(ctm: AffineTransform) {
        val origRect = getAsArray(PdfName.RECT)
        if (origRect != null) {
            val rect: PdfRectangle
            if (origRect.size() == 4) {
                rect = PdfRectangle(origRect.getAsNumber(0).floatValue(), origRect.getAsNumber(1).floatValue(), origRect.getAsNumber(2).floatValue(), origRect.getAsNumber(3).floatValue())
            } else {
                rect = PdfRectangle(origRect.getAsNumber(0).floatValue(), origRect.getAsNumber(1).floatValue())
            }
            put(PdfName.RECT, rect.transform(ctm))
        }
    }

    /**
     * This class processes links from imported pages so that they may be active. The following example code reads a group
     * of files and places them all on the output PDF, four pages in a single page, keeping the links active.
     *
     * String[] files = new String[] {&quot;input1.pdf&quot;, &quot;input2.pdf&quot;};
     * String outputFile = &quot;output.pdf&quot;;
     * int firstPage=1;
     * Document document = new Document();
     * PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFile));
     * document.setPageSize(PageSize.A4);
     * float W = PageSize.A4.getWidth() / 2;
     * float H = PageSize.A4.getHeight() / 2;
     * document.open();
     * PdfContentByte cb = writer.getDirectContent();
     * for (int i = 0; i &lt; files.length; i++) {
     * PdfReader currentReader = new PdfReader(files[i]);
     * currentReader.consolidateNamedDestinations();
     * for (int page = 1; page &lt;= currentReader.getNumberOfPages(); page++) {
     * PdfImportedPage importedPage = writer.getImportedPage(currentReader, page);
     * float a = 0.5f;
     * float e = (page % 2 == 0) ? W : 0;
     * float f = (page % 4 == 1 || page % 4 == 2) ? H : 0;
     * ArrayList links = currentReader.getLinks(page);
     * cb.addTemplate(importedPage, a, 0, 0, a, e, f);
     * for (int j = 0; j &lt; links.size(); j++) {
     * PdfAnnotation.PdfImportedLink link = (PdfAnnotation.PdfImportedLink)links.get(j);
     * if (link.isInternal()) {
     * int dPage = link.getDestinationPage();
     * int newDestPage = (dPage-1)/4 + firstPage;
     * float ee = (dPage % 2 == 0) ? W : 0;
     * float ff = (dPage % 4 == 1 || dPage % 4 == 2) ? H : 0;
     * link.setDestinationPage(newDestPage);
     * link.transformDestination(a, 0, 0, a, ee, ff);
     * }
     * link.transformRect(a, 0, 0, a, e, f);
     * writer.addAnnotation(link.createAnnotation(writer));
     * }
     * if (page % 4 == 0)
     * document.newPage();
     * }
     * if (i &lt; files.length - 1)
     * document.newPage();
     * firstPage += (currentReader.getNumberOfPages()+3)/4;
     * }
     * document.close();
     *
     */
    class PdfImportedLink internal constructor(annotation: PdfDictionary) {
        internal var llx: Float = 0.toFloat()
        internal var lly: Float = 0.toFloat()
        internal var urx: Float = 0.toFloat()
        internal var ury: Float = 0.toFloat()
        internal var parameters: HashMap<PdfName, PdfObject>? = HashMap()
        internal var destination: PdfArray? = null
        internal var newPage = 0
        internal var rect: PdfArray

        init {
            parameters!!.putAll(annotation.hashMap)
            try {
                destination = parameters!!.remove(PdfName.DEST) as PdfArray
            } catch (ex: ClassCastException) {
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("you.have.to.consolidate.the.named.destinations.of.your.reader"))
            }

            if (destination != null) {
                destination = PdfArray(destination)
            }
            val rc = parameters!!.remove(PdfName.RECT) as PdfArray
            llx = rc.getAsNumber(0).floatValue()
            lly = rc.getAsNumber(1).floatValue()
            urx = rc.getAsNumber(2).floatValue()
            ury = rc.getAsNumber(3).floatValue()

            rect = PdfArray(rc)
        }

        fun getParameters(): Map<PdfName, PdfObject> {
            return HashMap(parameters)
        }

        fun getRect(): PdfArray {
            return PdfArray(rect)
        }

        val isInternal: Boolean
            get() = destination != null

        // here destination is something like
        // [132 0 R, /XYZ, 29.3898, 731.864502, null]
        var destinationPage: Int
            get() {
                if (!isInternal) return 0
                val ref = destination!!.getAsIndirectObject(0)

                val pr = ref as PRIndirectReference
                val r = pr.reader
                for (i in 1..r.numberOfPages) {
                    val pp = r.getPageOrigRef(i)
                    if (pp.generation == pr.generation && pp.number == pr.number) return i
                }
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("page.not.found"))
            }
            set(newPage) {
                if (!isInternal) throw IllegalArgumentException(MessageLocalization.getComposedMessage("cannot.change.destination.of.external.link"))
                this.newPage = newPage
            }

        fun transformDestination(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
            if (!isInternal) throw IllegalArgumentException(MessageLocalization.getComposedMessage("cannot.change.destination.of.external.link"))
            if (destination!!.getAsName(1) == PdfName.XYZ) {
                val x = destination!!.getAsNumber(2).floatValue()
                val y = destination!!.getAsNumber(3).floatValue()
                val xx = x * a + y * c + e
                val yy = x * b + y * d + f
                destination!!.set(2, PdfNumber(xx))
                destination!!.set(3, PdfNumber(yy))
            }
        }

        fun transformRect(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
            var x = llx * a + lly * c + e
            var y = llx * b + lly * d + f
            llx = x
            lly = y
            x = urx * a + ury * c + e
            y = urx * b + ury * d + f
            urx = x
            ury = y
        }

        fun createAnnotation(writer: PdfWriter): PdfAnnotation {
            val annotation = writer.createAnnotation(Rectangle(llx, lly, urx, ury), null)
            if (newPage != 0) {
                val ref = writer.getPageReference(newPage)
                destination!!.set(0, ref)
            }
            if (destination != null) annotation.put(PdfName.DEST, destination)
            annotation.hashMap.putAll(parameters)
            return annotation
        }

        /**
         * Returns a String representation of the link.
         * @return    a String representation of the imported link
         * *
         * @since    2.1.6
         */
        override fun toString(): String {
            val buf = StringBuffer("Imported link: location [")
            buf.append(llx)
            buf.append(' ')
            buf.append(lly)
            buf.append(' ')
            buf.append(urx)
            buf.append(' ')
            buf.append(ury)
            buf.append("] destination ")
            buf.append(destination)
            buf.append(" parameters ")
            buf.append(parameters)
            if (parameters != null) {
                appendDictionary(buf, parameters)
            }

            return buf.toString()
        }

        private fun appendDictionary(buf: StringBuffer, dict: HashMap<PdfName, PdfObject>) {
            buf.append(" <<")
            for (entry in dict.entries) {
                buf.append(entry.key)
                buf.append(":")
                if (entry.value is PdfDictionary)
                    appendDictionary(buf, (entry.value as PdfDictionary).hashMap)
                else
                    buf.append(entry.value)
                buf.append(" ")
            }

            buf.append(">> ")
        }

    }

    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_ANNOTATION, this)
        super.toPdf(writer, os)
    }

    fun getAccessibleAttribute(key: PdfName): PdfObject? {
        if (accessibleAttributes != null)
            return accessibleAttributes!![key]
        else
            return null
    }

    fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        if (accessibleAttributes == null)
            accessibleAttributes = HashMap<PdfName, PdfObject>()
        accessibleAttributes!!.put(key, value)
    }

    override val isInline: Boolean
        get() = false

    companion object {
        /** highlight attributename  */
        val HIGHLIGHT_NONE = PdfName.N
        /** highlight attributename  */
        val HIGHLIGHT_INVERT = PdfName.I
        /** highlight attributename  */
        val HIGHLIGHT_OUTLINE = PdfName.O
        /** highlight attributename  */
        val HIGHLIGHT_PUSH = PdfName.P
        /** highlight attributename  */
        val HIGHLIGHT_TOGGLE = PdfName.T
        /** flagvalue  */
        val FLAGS_INVISIBLE = 1
        /** flagvalue  */
        val FLAGS_HIDDEN = 2
        /** flagvalue  */
        val FLAGS_PRINT = 4
        /** flagvalue  */
        val FLAGS_NOZOOM = 8
        /** flagvalue  */
        val FLAGS_NOROTATE = 16
        /** flagvalue  */
        val FLAGS_NOVIEW = 32
        /** flagvalue  */
        val FLAGS_READONLY = 64
        /** flagvalue  */
        val FLAGS_LOCKED = 128
        /** flagvalue  */
        val FLAGS_TOGGLENOVIEW = 256
        /** flagvalue PDF 1.7 */
        val FLAGS_LOCKEDCONTENTS = 512
        /** appearance attributename  */
        val APPEARANCE_NORMAL = PdfName.N
        /** appearance attributename  */
        val APPEARANCE_ROLLOVER = PdfName.R
        /** appearance attributename  */
        val APPEARANCE_DOWN = PdfName.D
        /** attributevalue  */
        val AA_ENTER = PdfName.E
        /** attributevalue  */
        val AA_EXIT = PdfName.X
        /** attributevalue  */
        val AA_DOWN = PdfName.D
        /** attributevalue  */
        val AA_UP = PdfName.U
        /** attributevalue  */
        val AA_FOCUS = PdfName.FO
        /** attributevalue  */
        val AA_BLUR = PdfName.BL
        /** attributevalue  */
        val AA_JS_KEY = PdfName.K
        /** attributevalue  */
        val AA_JS_FORMAT = PdfName.F
        /** attributevalue  */
        val AA_JS_CHANGE = PdfName.V
        /** attributevalue  */
        val AA_JS_OTHER_CHANGE = PdfName.C
        /** attributevalue  */
        val MARKUP_HIGHLIGHT = 0
        /** attributevalue  */
        val MARKUP_UNDERLINE = 1
        /** attributevalue  */
        val MARKUP_STRIKEOUT = 2
        /**
         * attributevalue
         * @since 2.1.3
         */
        val MARKUP_SQUIGGLY = 3

        /**
         * Creates a screen PdfAnnotation
         * @param writer
         * *
         * @param rect
         * *
         * @param clipTitle
         * *
         * @param fs
         * *
         * @param mimeType
         * *
         * @param playOnDisplay
         * *
         * @return a screen PdfAnnotation
         * *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun createScreen(writer: PdfWriter, rect: Rectangle, clipTitle: String, fs: PdfFileSpecification,
                         mimeType: String, playOnDisplay: Boolean): PdfAnnotation {
            val ann = writer.createAnnotation(rect, PdfName.SCREEN)
            ann.put(PdfName.F, PdfNumber(FLAGS_PRINT))
            ann.put(PdfName.TYPE, PdfName.ANNOT)
            ann.setPage()
            val ref = ann.indirectReference
            val action = PdfAction.rendition(clipTitle, fs, mimeType, ref)
            val actionRef = writer.addToBody(action).indirectReference
            // for play on display add trigger event
            if (playOnDisplay) {
                val aa = PdfDictionary()
                aa.put(PdfName("PV"), actionRef)
                ann.put(PdfName.AA, aa)
            }
            ann.put(PdfName.A, actionRef)
            return ann
        }

        /**
         * @param writer
         * *
         * @param rect
         * *
         * @param title
         * *
         * @param contents
         * *
         * @param open
         * *
         * @param icon
         * *
         * @return a PdfAnnotation
         */
        fun createText(writer: PdfWriter, rect: Rectangle, title: String?, contents: String?, open: Boolean, icon: String?): PdfAnnotation {
            val annot = writer.createAnnotation(rect, PdfName.TEXT)
            if (title != null)
                annot.put(PdfName.T, PdfString(title, PdfObject.TEXT_UNICODE))
            if (contents != null)
                annot.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            if (open)
                annot.put(PdfName.OPEN, PdfBoolean.PDFTRUE)
            if (icon != null) {
                annot.put(PdfName.NAME, PdfName(icon))
            }
            return annot
        }

        /**
         * Creates a link.
         * @param writer
         * *
         * @param rect
         * *
         * @param highlight
         * *
         * @return A PdfAnnotation
         */
        protected fun createLink(writer: PdfWriter, rect: Rectangle, highlight: PdfName): PdfAnnotation {
            val annot = writer.createAnnotation(rect, PdfName.LINK)
            if (highlight != HIGHLIGHT_INVERT)
                annot.put(PdfName.H, highlight)
            return annot
        }

        /**
         * Creates an Annotation with an Action.
         * @param writer
         * *
         * @param rect
         * *
         * @param highlight
         * *
         * @param action
         * *
         * @return A PdfAnnotation
         */
        fun createLink(writer: PdfWriter, rect: Rectangle, highlight: PdfName, action: PdfAction): PdfAnnotation {
            val annot = createLink(writer, rect, highlight)
            annot.putEx(PdfName.A, action)
            return annot
        }

        /**
         * Creates an Annotation with an local destination.
         * @param writer
         * *
         * @param rect
         * *
         * @param highlight
         * *
         * @param namedDestination
         * *
         * @return A PdfAnnotation
         */
        fun createLink(writer: PdfWriter, rect: Rectangle, highlight: PdfName, namedDestination: String): PdfAnnotation {
            val annot = createLink(writer, rect, highlight)
            annot.put(PdfName.DEST, PdfString(namedDestination, PdfObject.TEXT_UNICODE))
            return annot
        }

        /**
         * Creates an Annotation with a PdfDestination.
         * @param writer
         * *
         * @param rect
         * *
         * @param highlight
         * *
         * @param page
         * *
         * @param dest
         * *
         * @return A PdfAnnotation
         */
        fun createLink(writer: PdfWriter, rect: Rectangle, highlight: PdfName, page: Int, dest: PdfDestination): PdfAnnotation {
            val annot = createLink(writer, rect, highlight)
            val ref = writer.getPageReference(page)
            val d = PdfDestination(dest)
            d.addPage(ref)
            annot.put(PdfName.DEST, d)
            return annot
        }

        /**
         * Add some free text to the document.
         * @param writer
         * *
         * @param rect
         * *
         * @param contents
         * *
         * @param defaultAppearance
         * *
         * @return A PdfAnnotation
         */
        fun createFreeText(writer: PdfWriter, rect: Rectangle, contents: String, defaultAppearance: PdfContentByte): PdfAnnotation {
            val annot = writer.createAnnotation(rect, PdfName.FREETEXT)
            annot.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            annot.setDefaultAppearanceString(defaultAppearance)
            return annot
        }

        /**
         * Adds a line to the document. Move over the line and a tooltip is shown.
         * @param writer
         * *
         * @param rect
         * *
         * @param contents
         * *
         * @param x1
         * *
         * @param y1
         * *
         * @param x2
         * *
         * @param y2
         * *
         * @return A PdfAnnotation
         */
        fun createLine(writer: PdfWriter, rect: Rectangle, contents: String, x1: Float, y1: Float, x2: Float, y2: Float): PdfAnnotation {
            val annot = writer.createAnnotation(rect, PdfName.LINE)
            annot.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            val array = PdfArray(PdfNumber(x1))
            array.add(PdfNumber(y1))
            array.add(PdfNumber(x2))
            array.add(PdfNumber(y2))
            annot.put(PdfName.L, array)
            return annot
        }

        /**
         * Adds a circle or a square that shows a tooltip when you pass over it.
         * @param writer
         * *
         * @param rect
         * *
         * @param contents The tooltip
         * *
         * @param square true if you want a square, false if you want a circle
         * *
         * @return A PdfAnnotation
         */
        fun createSquareCircle(writer: PdfWriter, rect: Rectangle, contents: String, square: Boolean): PdfAnnotation {
            val annot: PdfAnnotation
            if (square)
                annot = writer.createAnnotation(rect, PdfName.SQUARE)
            else
                annot = writer.createAnnotation(rect, PdfName.CIRCLE)
            annot.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            return annot
        }

        fun createMarkup(writer: PdfWriter, rect: Rectangle, contents: String, type: Int, quadPoints: FloatArray): PdfAnnotation {
            var name = PdfName.HIGHLIGHT
            when (type) {
                MARKUP_UNDERLINE -> name = PdfName.UNDERLINE
                MARKUP_STRIKEOUT -> name = PdfName.STRIKEOUT
                MARKUP_SQUIGGLY -> name = PdfName.SQUIGGLY
            }
            val annot = writer.createAnnotation(rect, name)
            annot.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            val array = PdfArray()
            for (k in quadPoints.indices)
                array.add(PdfNumber(quadPoints[k]))
            annot.put(PdfName.QUADPOINTS, array)
            return annot
        }

        /**
         * Adds a Stamp to your document. Move over the stamp and a tooltip is shown
         * @param writer
         * *
         * @param rect
         * *
         * @param contents
         * *
         * @param name
         * *
         * @return A PdfAnnotation
         */
        fun createStamp(writer: PdfWriter, rect: Rectangle, contents: String, name: String): PdfAnnotation {
            val annot = writer.createAnnotation(rect, PdfName.STAMP)
            annot.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            annot.put(PdfName.NAME, PdfName(name))
            return annot
        }

        fun createInk(writer: PdfWriter, rect: Rectangle, contents: String, inkList: Array<FloatArray>): PdfAnnotation {
            val annot = writer.createAnnotation(rect, PdfName.INK)
            annot.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            val outer = PdfArray()
            for (k in inkList.indices) {
                val inner = PdfArray()
                val deep = inkList[k]
                for (j in deep.indices)
                    inner.add(PdfNumber(deep[j]))
                outer.add(inner)
            }
            annot.put(PdfName.INKLIST, outer)
            return annot
        }

        /** Creates a file attachment annotation.
         * @param writer the PdfWriter
         * *
         * @param rect the dimensions in the page of the annotation
         * *
         * @param contents the file description
         * *
         * @param fileStore an array with the file. If it's null
         * * the file will be read from the disk
         * *
         * @param file the path to the file. It will only be used if
         * * fileStore is not null
         * *
         * @param fileDisplay the actual file name stored in the pdf
         * *
         * @throws IOException on error
         * *
         * @return the annotation
         */
        @Throws(IOException::class)
        fun createFileAttachment(writer: PdfWriter, rect: Rectangle, contents: String, fileStore: ByteArray, file: String, fileDisplay: String): PdfAnnotation {
            return createFileAttachment(writer, rect, contents, PdfFileSpecification.fileEmbedded(writer, file, fileDisplay, fileStore))
        }

        /** Creates a file attachment annotation
         * @param writer
         * *
         * @param rect
         * *
         * @param contents
         * *
         * @param fs
         * *
         * @return the annotation
         * *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun createFileAttachment(writer: PdfWriter, rect: Rectangle, contents: String?, fs: PdfFileSpecification): PdfAnnotation {
            val annot = writer.createAnnotation(rect, PdfName.FILEATTACHMENT)
            if (contents != null)
                annot.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            annot.put(PdfName.FS, fs.reference)
            return annot
        }

        /**
         * Adds a popup to your document.
         * @param writer
         * *
         * @param rect
         * *
         * @param contents
         * *
         * @param open
         * *
         * @return A PdfAnnotation
         */
        fun createPopup(writer: PdfWriter, rect: Rectangle, contents: String?, open: Boolean): PdfAnnotation {
            val annot = writer.createAnnotation(rect, PdfName.POPUP)
            if (contents != null)
                annot.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            if (open)
                annot.put(PdfName.OPEN, PdfBoolean.PDFTRUE)
            return annot
        }

        /**
         * Creates a polygon or -line annotation
         * @param writer the PdfWriter
         * *
         * @param rect the annotation position
         * *
         * @param contents the textual content of the annotation
         * *
         * @param polygon if true, the we're creating a polygon annotation, if false, a polyline
         * *
         * @param vertices an array with the vertices of the polygon or -line
         * *
         * @since 5.0.2
         */
        fun createPolygonPolyline(
                writer: PdfWriter, rect: Rectangle, contents: String, polygon: Boolean, vertices: PdfArray): PdfAnnotation {
            var annot: PdfAnnotation? = null
            if (polygon)
                annot = writer.createAnnotation(rect, PdfName.POLYGON)
            else
                annot = writer.createAnnotation(rect, PdfName.POLYLINE)
            annot!!.put(PdfName.CONTENTS, PdfString(contents, PdfObject.TEXT_UNICODE))
            annot.put(PdfName.VERTICES, PdfArray(vertices))
            return annot
        }

        fun getMKColor(color: BaseColor): PdfArray {
            val array = PdfArray()
            val type = ExtendedColor.getType(color)
            when (type) {
                ExtendedColor.TYPE_GRAY -> {
                    array.add(PdfNumber((color as GrayColor).gray))
                }
                ExtendedColor.TYPE_CMYK -> {
                    val cmyk = color as CMYKColor
                    array.add(PdfNumber(cmyk.cyan))
                    array.add(PdfNumber(cmyk.magenta))
                    array.add(PdfNumber(cmyk.yellow))
                    array.add(PdfNumber(cmyk.black))
                }
                ExtendedColor.TYPE_SEPARATION, ExtendedColor.TYPE_PATTERN, ExtendedColor.TYPE_SHADING -> throw RuntimeException(MessageLocalization.getComposedMessage("separations.patterns.and.shadings.are.not.allowed.in.mk.dictionary"))
                else -> {
                    array.add(PdfNumber(color.red / 255f))
                    array.add(PdfNumber(color.green / 255f))
                    array.add(PdfNumber(color.blue / 255f))
                }
            }
            return array
        }
    }
}

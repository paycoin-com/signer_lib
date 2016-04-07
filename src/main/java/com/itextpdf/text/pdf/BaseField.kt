/*
 * $Id: 4e10c00e8a5d191286011baf0ed929e041c66f8d $
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

import com.itextpdf.text.BaseColor
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.Rectangle
import com.itextpdf.text.error_messages.MessageLocalization

/** Common field variables.
 * @author Paulo Soares
 */
abstract class BaseField
/** Creates a new TextField.
 * @param writer the document PdfWriter
 * *
 * @param box the field location and dimensions
 * *
 * @param fieldName the field name. If null only the widget keys
 * * will be included in the field allowing it to be used as a kid field.
 */
(
        /**
         * Getter for property writer.
         * @return Value of property writer.
         */
        /**
         * Setter for property writer.
         * @param writer New value of property writer.
         */
        var writer: PdfWriter, box: Rectangle,
        /** Holds value of property fieldName.  */
        /** Gets the field name.
         * @return the field name
         */
        /** Sets the field name.
         * @param fieldName the field name. If null only the widget keys
         * * will be included in the field allowing it to be used as a kid field.
         */
        var fieldName: String) {

    /** Gets the border width in points.
     * @return the border width in points
     */
    /** Sets the border width in points. To eliminate the border
     * set the border color to null.
     * @param borderWidth the border width in points
     */
    var borderWidth = BORDER_WIDTH_THIN
    /** Gets the border style.
     * @return the border style
     */
    /** Sets the border style. The styles are found in PdfBorderDictionary
     * and can be STYLE_SOLID, STYLE_DASHED,
     * STYLE_BEVELED, STYLE_INSET and
     * STYLE_UNDERLINE.
     * @param borderStyle the border style
     */
    var borderStyle = PdfBorderDictionary.STYLE_SOLID
    /** Gets the border color.
     * @return the border color
     */
    /** Sets the border color. Set to null to remove
     * the border.
     * @param borderColor the border color
     */
    var borderColor: BaseColor? = null
    /** Gets the background color.
     * @return the background color
     */
    /** Sets the background color. Set to null for
     * transparent background.
     * @param backgroundColor the background color
     */
    var backgroundColor: BaseColor? = null
    /** Gets the text color.
     * @return the text color
     */
    /** Sets the text color. If null the color used
     * will be black.
     * @param textColor the text color
     */
    var textColor: BaseColor
    /** Gets the text font.
     * @return the text font
     */
    /** Sets the text font. If null then Helvetica
     * will be used.
     * @param font the text font
     */
    var font: BaseFont? = null
    /** Gets the font size.
     * @return the font size
     */
    /** Sets the font size. If 0 then auto-sizing will be used but
     * only for text fields.
     * @param fontSize the font size
     */
    var fontSize = 0f
    /** Gets the text horizontal alignment.
     * @return the text horizontal alignment
     */
    /** Sets the text horizontal alignment. It can be Element.ALIGN_LEFT,
     * Element.ALIGN_CENTER and Element.ALIGN_RIGHT.
     * @param alignment the text horizontal alignment
     */
    var alignment = Element.ALIGN_LEFT
    /** Gets the text.
     * @return the text
     */
    /** Sets the text for text fields.
     * @param text the text
     */
    var text: String
    /** Gets the field dimension and position.
     * @return the field dimension and position
     */
    /** Sets the field dimension and position.
     * @param box the field dimension and position
     */
    var box: Rectangle? = null
        set(box) = if (box == null) {
            this.box = null
        } else {
            this.box = Rectangle(box)
            this.box!!.normalize()
        }

    /** Holds value of property rotation.  */
    /** Gets the field rotation.
     * @return the field rotation
     */
    /** Sets the field rotation. This value should be the same as
     * the page rotation where the field will be shown.
     * @param rotation the field rotation
     */
    var rotation = 0
        set(rotation) {
            var rotation = rotation
            if (rotation % 90 != 0)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("rotation.must.be.a.multiple.of.90"))
            rotation %= 360
            if (rotation < 0)
                rotation += 360
            this.rotation = rotation
        }

    /** Holds value of property visibility.  */
    /** Gets the field visibility flag.
     * @return the field visibility flag
     */
    /** Sets the field visibility flag. This flags can be one of
     * VISIBLE, HIDDEN, VISIBLE_BUT_DOES_NOT_PRINT
     * and HIDDEN_BUT_PRINTABLE.
     * @param visibility field visibility flag
     */
    var visibility: Int = 0

    /** Holds value of property options.  */
    /** Gets the option flags.
     * @return the option flags
     */
    /** Sets the option flags. The option flags can be a combination by oring of
     * READ_ONLY, REQUIRED,
     * MULTILINE, DO_NOT_SCROLL,
     * PASSWORD, FILE_SELECTION,
     * DO_NOT_SPELL_CHECK and EDIT.
     * @param options the option flags
     */
    var options: Int = 0

    /** Holds value of property maxCharacterLength.  */
    /** Gets the maximum length of the field's text, in characters.
     * @return the maximum length of the field's text, in characters.
     */
    /** Sets the maximum length of the field's text, in characters.
     * It is only meaningful for text fields.
     * @param maxCharacterLength the maximum length of the field's text, in characters
     */
    var maxCharacterLength: Int = 0

    init {
        box = box
    }

    protected val realFont: BaseFont
        @Throws(IOException::class, DocumentException::class)
        get() {
            if (font == null)
                return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false)
            else
                return font
        }

    protected // background
            // border
            // beveled
            // inset
    val borderAppearance: PdfAppearance
        get() {
            val app = PdfAppearance.createAppearance(writer, box!!.width, box!!.height)
            when (rotation) {
                90 -> app.setMatrix(0f, 1f, -1f, 0f, box!!.height, 0f)
                180 -> app.setMatrix(-1f, 0f, 0f, -1f, box!!.width, box!!.height)
                270 -> app.setMatrix(0f, -1f, 1f, 0f, 0f, box!!.width)
            }
            app.saveState()
            if (backgroundColor != null) {
                app.setColorFill(backgroundColor)
                app.rectangle(0f, 0f, box!!.width, box!!.height)
                app.fill()
            }
            if (borderStyle == PdfBorderDictionary.STYLE_UNDERLINE) {
                if (borderWidth != 0f && borderColor != null) {
                    app.setColorStroke(borderColor)
                    app.setLineWidth(borderWidth)
                    app.moveTo(0f, borderWidth / 2)
                    app.lineTo(box!!.width, borderWidth / 2)
                    app.stroke()
                }
            } else if (borderStyle == PdfBorderDictionary.STYLE_BEVELED) {
                if (borderWidth != 0f && borderColor != null) {
                    app.setColorStroke(borderColor)
                    app.setLineWidth(borderWidth)
                    app.rectangle(borderWidth / 2, borderWidth / 2, box!!.width - borderWidth, box!!.height - borderWidth)
                    app.stroke()
                }
                var actual = backgroundColor
                if (actual == null)
                    actual = BaseColor.WHITE
                app.setGrayFill(1f)
                drawTopFrame(app)
                app.setColorFill(actual!!.darker())
                drawBottomFrame(app)
            } else if (borderStyle == PdfBorderDictionary.STYLE_INSET) {
                if (borderWidth != 0f && borderColor != null) {
                    app.setColorStroke(borderColor)
                    app.setLineWidth(borderWidth)
                    app.rectangle(borderWidth / 2, borderWidth / 2, box!!.width - borderWidth, box!!.height - borderWidth)
                    app.stroke()
                }
                app.setGrayFill(0.5f)
                drawTopFrame(app)
                app.setGrayFill(0.75f)
                drawBottomFrame(app)
            } else {
                if (borderWidth != 0f && borderColor != null) {
                    if (borderStyle == PdfBorderDictionary.STYLE_DASHED)
                        app.setLineDash(3f, 0f)
                    app.setColorStroke(borderColor)
                    app.setLineWidth(borderWidth)
                    app.rectangle(borderWidth / 2, borderWidth / 2, box!!.width - borderWidth, box!!.height - borderWidth)
                    app.stroke()
                    if (options and COMB != 0 && maxCharacterLength > 1) {
                        val step = box!!.width / maxCharacterLength
                        val yb = borderWidth / 2
                        val yt = box!!.height - borderWidth / 2
                        for (k in 1..maxCharacterLength - 1) {
                            val x = step * k
                            app.moveTo(x, yb)
                            app.lineTo(x, yt)
                        }
                        app.stroke()
                    }
                }
            }
            app.restoreState()
            return app
        }

    private fun drawTopFrame(app: PdfAppearance) {
        app.moveTo(borderWidth, borderWidth)
        app.lineTo(borderWidth, box!!.height - borderWidth)
        app.lineTo(box!!.width - borderWidth, box!!.height - borderWidth)
        app.lineTo(box!!.width - 2 * borderWidth, box!!.height - 2 * borderWidth)
        app.lineTo(2 * borderWidth, box!!.height - 2 * borderWidth)
        app.lineTo(2 * borderWidth, 2 * borderWidth)
        app.lineTo(borderWidth, borderWidth)
        app.fill()
    }

    private fun drawBottomFrame(app: PdfAppearance) {
        app.moveTo(borderWidth, borderWidth)
        app.lineTo(box!!.width - borderWidth, borderWidth)
        app.lineTo(box!!.width - borderWidth, box!!.height - borderWidth)
        app.lineTo(box!!.width - 2 * borderWidth, box!!.height - 2 * borderWidth)
        app.lineTo(box!!.width - 2 * borderWidth, 2 * borderWidth)
        app.lineTo(2 * borderWidth, 2 * borderWidth)
        app.lineTo(borderWidth, borderWidth)
        app.fill()
    }

    /** Convenience method to set the field rotation the same as the
     * page rotation.
     * @param page the page
     */
    fun setRotationFromPage(page: Rectangle) {
        rotation = page.rotation
    }

    companion object {

        /** A thin border with 1 point width.  */
        val BORDER_WIDTH_THIN = 1f
        /** A medium border with 2 point width.  */
        val BORDER_WIDTH_MEDIUM = 2f
        /** A thick border with 3 point width.  */
        val BORDER_WIDTH_THICK = 3f
        /** The field is visible.  */
        val VISIBLE = 0
        /** The field is hidden.  */
        val HIDDEN = 1
        /** The field is visible but does not print.  */
        val VISIBLE_BUT_DOES_NOT_PRINT = 2
        /** The field is hidden but is printable.  */
        val HIDDEN_BUT_PRINTABLE = 3
        /** The user may not change the value of the field.  */
        val READ_ONLY = PdfFormField.FF_READ_ONLY
        /** The field must have a value at the time it is exported by a submit-form
         * action.
         */
        val REQUIRED = PdfFormField.FF_REQUIRED
        /** The field may contain multiple lines of text.
         * This flag is only meaningful with text fields.
         */
        val MULTILINE = PdfFormField.FF_MULTILINE
        /** The field will not scroll (horizontally for single-line
         * fields, vertically for multiple-line fields) to accommodate more text
         * than will fit within its annotation rectangle. Once the field is full, no
         * further text will be accepted.
         */
        val DO_NOT_SCROLL = PdfFormField.FF_DONOTSCROLL
        /** The field is intended for entering a secure password that should
         * not be echoed visibly to the screen.
         */
        val PASSWORD = PdfFormField.FF_PASSWORD
        /** The text entered in the field represents the pathname of
         * a file whose contents are to be submitted as the value of the field.
         */
        val FILE_SELECTION = PdfFormField.FF_FILESELECT
        /** The text entered in the field will not be spell-checked.
         * This flag is meaningful only in text fields and in combo
         * fields with the EDIT flag set.
         */
        val DO_NOT_SPELL_CHECK = PdfFormField.FF_DONOTSPELLCHECK
        /** If set the combo box includes an editable text box as well as a drop list; if
         * clear, it includes only a drop list.
         * This flag is only meaningful with combo fields.
         */
        val EDIT = PdfFormField.FF_EDIT

        /** whether or not a list may have multiple selections.  Only applies to /CH LIST
         * fields, not combo boxes.
         */
        val MULTISELECT = PdfFormField.FF_MULTISELECT

        /**
         * combo box flag.
         */
        val COMB = PdfFormField.FF_COMB

        private val fieldKeys = HashMap<PdfName, Int>()

        init {
            fieldKeys.putAll(PdfCopyFieldsImp.fieldKeys)
            fieldKeys.put(PdfName.T, Integer.valueOf(1))
        }

        protected fun getHardBreaks(text: String): ArrayList<String> {
            val arr = ArrayList<String>()
            val cs = text.toCharArray()
            val len = cs.size
            var buf = StringBuffer()
            var k = 0
            while (k < len) {
                val c = cs[k]
                if (c == '\r') {
                    if (k + 1 < len && cs[k + 1] == '\n')
                        ++k
                    arr.add(buf.toString())
                    buf = StringBuffer()
                } else if (c == '\n') {
                    arr.add(buf.toString())
                    buf = StringBuffer()
                } else
                    buf.append(c)
                ++k
            }
            arr.add(buf.toString())
            return arr
        }

        protected fun trimRight(buf: StringBuffer) {
            var len = buf.length
            while (true) {
                if (len == 0)
                    return
                if (buf[--len] != ' ')
                    return
                buf.setLength(len)
            }
        }

        protected fun breakLines(breaks: ArrayList<String>, font: BaseFont, fontSize: Float, width: Float): ArrayList<String> {
            val lines = ArrayList<String>()
            val buf = StringBuffer()
            for (ck in breaks.indices) {
                buf.setLength(0)
                var w = 0f
                val cs = breaks[ck].toCharArray()
                val len = cs.size
                // 0 inline first, 1 inline, 2 spaces
                var state = 0
                var lastspace = -1
                var c: Char = 0.toChar()
                var refk = 0
                var k = 0
                while (k < len) {
                    c = cs[k]
                    when (state) {
                        0 -> {
                            w += font.getWidthPoint(c.toInt(), fontSize)
                            buf.append(c)
                            if (w > width) {
                                w = 0f
                                if (buf.length > 1) {
                                    --k
                                    buf.setLength(buf.length - 1)
                                }
                                lines.add(buf.toString())
                                buf.setLength(0)
                                refk = k
                                if (c == ' ')
                                    state = 2
                                else
                                    state = 1
                            } else {
                                if (c != ' ')
                                    state = 1
                            }
                        }
                        1 -> {
                            w += font.getWidthPoint(c.toInt(), fontSize)
                            buf.append(c)
                            if (c == ' ')
                                lastspace = k
                            if (w > width) {
                                w = 0f
                                if (lastspace >= 0) {
                                    k = lastspace
                                    buf.setLength(lastspace - refk)
                                    trimRight(buf)
                                    lines.add(buf.toString())
                                    buf.setLength(0)
                                    refk = k
                                    lastspace = -1
                                    state = 2
                                } else {
                                    if (buf.length > 1) {
                                        --k
                                        buf.setLength(buf.length - 1)
                                    }
                                    lines.add(buf.toString())
                                    buf.setLength(0)
                                    refk = k
                                    if (c == ' ')
                                        state = 2
                                }
                            }
                        }
                        2 -> if (c != ' ') {
                            w = 0f
                            --k
                            state = 1
                        }
                    }
                    ++k
                }
                trimRight(buf)
                lines.add(buf.toString())
            }
            return lines
        }

        /**
         * Moves the field keys from from to to. The moved keys
         * are removed from from.
         * @param from the source
         * *
         * @param to the destination. It may be null
         */
        fun moveFields(from: PdfDictionary, to: PdfDictionary?) {
            val i = from.keys.iterator()
            while (i.hasNext()) {
                val key = i.next()
                if (fieldKeys.containsKey(key)) {
                    to?.put(key, from.get(key))
                    i.remove()
                }
            }
        }
    }
}

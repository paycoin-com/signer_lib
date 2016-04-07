/*
 * $Id: e7f4ee7f1e608858c44535ffe29dcc7091501611 $
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

import com.itextpdf.text.DocumentException
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Rectangle
import com.itextpdf.text.error_messages.MessageLocalization

import java.io.IOException

/**
 * Creates a radio or a check field.
 *
 *
 * Example usage:
 *
 *
 *
 * Document document = new Document(PageSize.A4, 50, 50, 50, 50);
 * PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("output.pdf"));
 * document.open();
 * PdfContentByte cb = writer.getDirectContent();
 * RadioCheckField bt = new RadioCheckField(writer, new Rectangle(100, 100, 200, 200), "radio", "v1");
 * bt.setCheckType(RadioCheckField.TYPE_CIRCLE);
 * bt.setBackgroundColor(Color.cyan);
 * bt.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
 * bt.setBorderColor(Color.red);
 * bt.setTextColor(Color.yellow);
 * bt.setBorderWidth(BaseField.BORDER_WIDTH_THICK);
 * bt.setChecked(false);
 * PdfFormField f1 = bt.getRadioField();
 * bt.setOnValue("v2");
 * bt.setChecked(true);
 * bt.setBox(new Rectangle(100, 300, 200, 400));
 * PdfFormField f2 = bt.getRadioField();
 * bt.setChecked(false);
 * PdfFormField top = bt.getRadioGroup(true, false);
 * bt.setOnValue("v3");
 * bt.setBox(new Rectangle(100, 500, 200, 600));
 * PdfFormField f3 = bt.getRadioField();
 * top.addKid(f1);
 * top.addKid(f2);
 * top.addKid(f3);
 * writer.addAnnotation(top);
 * bt = new RadioCheckField(writer, new Rectangle(300, 300, 400, 400), "check1", "Yes");
 * bt.setCheckType(RadioCheckField.TYPE_CHECK);
 * bt.setBorderWidth(BaseField.BORDER_WIDTH_THIN);
 * bt.setBorderColor(Color.black);
 * bt.setBackgroundColor(Color.white);
 * PdfFormField ck = bt.getCheckField();
 * writer.addAnnotation(ck);
 * document.close();
 *
 * @author Paulo Soares
 */
class RadioCheckField
/**
 * Creates a new instance of RadioCheckField
 * @param writer the document PdfWriter
 * *
 * @param box the field location and dimensions
 * *
 * @param fieldName the field name. It must not be null
 * *
 * @param onValue the value when the field is checked
 */
(writer: PdfWriter, box: Rectangle, fieldName: String, onValue: String) : BaseField(writer, box, fieldName) {

    /**
     * Holds value of property checkType.
     */
    /**
     * Getter for property checkType.
     * @return Value of property checkType.
     */
    /**
     * Sets the checked symbol. It can be
     * TYPE_CHECK,
     * TYPE_CIRCLE,
     * TYPE_CROSS,
     * TYPE_DIAMOND,
     * TYPE_SQUARE and
     * TYPE_STAR.
     * @param checkType the checked symbol
     */
    var checkType: Int = 0
        set(checkType) {
            var checkType = checkType
            if (checkType < TYPE_CHECK || checkType > TYPE_STAR)
                checkType = TYPE_CIRCLE
            this.checkType = checkType
            text = typeChars[checkType - 1]
            try {
                font = BaseFont.createFont(BaseFont.ZAPFDINGBATS, BaseFont.WINANSI, false)
            } catch (e: Exception) {
                throw ExceptionConverter(e)
            }

        }

    /**
     * Holds value of property onValue.
     */
    /**
     * Getter for property onValue.
     * @return Value of property onValue.
     */
    /**
     * Sets the value when the field is checked.
     * @param onValue the value when the field is checked
     */
    var onValue: String? = null

    /**
     * Holds value of property checked.
     */
    /**
     * Getter for property checked.
     * @return Value of property checked.
     */
    /**
     * Sets the state of the field to checked or unchecked.
     * @param checked the state of the field, true for checked
     * * and false for unchecked
     */
    var isChecked: Boolean = false

    init {
        onValue = onValue
        checkType = TYPE_CIRCLE
    }

    /**
     * Gets the field appearance.
     * @param isRadio true for a radio field and false
     * * for a check field
     * *
     * @param on true for the checked state, false
     * * otherwise
     * *
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return the appearance
     */
    @Throws(IOException::class, DocumentException::class)
    fun getAppearance(isRadio: Boolean, on: Boolean): PdfAppearance {
        if (isRadio && checkType == TYPE_CIRCLE)
            return getAppearanceRadioCircle(on)
        val app = borderAppearance
        if (!on)
            return app
        val ufont = realFont
        val borderExtra = borderStyle === PdfBorderDictionary.STYLE_BEVELED || borderStyle === PdfBorderDictionary.STYLE_INSET
        var h = box!!.height - borderWidth * 2
        var bw2 = borderWidth
        if (borderExtra) {
            h -= borderWidth * 2
            bw2 *= 2f
        }
        var offsetX = if (borderExtra) 2 * borderWidth else borderWidth
        offsetX = Math.max(offsetX, 1f)
        val offX = Math.min(bw2, offsetX)
        val wt = box!!.width - 2 * offX
        val ht = box!!.height - 2 * offX
        var fsize = fontSize
        if (fsize == 0f) {
            val bw = ufont.getWidthPoint(text, 1f)
            if (bw == 0f)
                fsize = 12f
            else
                fsize = wt / bw
            val nfsize = h / ufont.getFontDescriptor(BaseFont.ASCENT, 1f)
            fsize = Math.min(fsize, nfsize)
        }
        app.saveState()
        app.rectangle(offX, offX, wt, ht)
        app.clip()
        app.newPath()
        if (textColor == null)
            app.resetGrayFill()
        else
            app.setColorFill(textColor)
        app.beginText()
        app.setFontAndSize(ufont, fsize)
        app.setTextMatrix((box!!.width - ufont.getWidthPoint(text, fsize)) / 2,
                (box!!.height - ufont.getAscentPoint(text, fsize)) / 2)
        app.showText(text)
        app.endText()
        app.restoreState()
        return app
    }

    /**
     * Gets the special field appearance for the radio circle.
     * @param on true for the checked state, false
     * * otherwise
     * *
     * @return the appearance
     */
    fun getAppearanceRadioCircle(on: Boolean): PdfAppearance {
        val app = PdfAppearance.createAppearance(writer, box!!.width, box!!.height)
        when (rotation) {
            90 -> app.setMatrix(0f, 1f, -1f, 0f, box!!.height, 0f)
            180 -> app.setMatrix(-1f, 0f, 0f, -1f, box!!.width, box!!.height)
            270 -> app.setMatrix(0f, -1f, 1f, 0f, 0f, box!!.width)
        }
        val box = Rectangle(app.boundingBox)
        val cx = box.width / 2
        val cy = box.height / 2
        val r = (Math.min(box.width, box.height) - borderWidth) / 2
        if (r <= 0)
            return app
        if (backgroundColor != null) {
            app.setColorFill(backgroundColor)
            app.circle(cx, cy, r + borderWidth / 2)
            app.fill()
        }
        if (borderWidth > 0 && borderColor != null) {
            app.setLineWidth(borderWidth)
            app.setColorStroke(borderColor)
            app.circle(cx, cy, r)
            app.stroke()
        }
        if (on) {
            if (textColor == null)
                app.resetGrayFill()
            else
                app.setColorFill(textColor)
            app.circle(cx, cy, r / 2)
            app.fill()
        }
        return app
    }

    /**
     * Gets a radio group. It's composed of the field specific keys, without the widget
     * ones. This field is to be used as a field aggregator with [addKid()][PdfFormField.addKid].
     * @param noToggleToOff if true, exactly one radio button must be selected at all
     * * times; clicking the currently selected button has no effect.
     * * If false, clicking
     * * the selected button deselects it, leaving no button selected.
     * *
     * @param radiosInUnison if true, a group of radio buttons within a radio button field that
     * * use the same value for the on state will turn on and off in unison; that is if
     * * one is checked, they are all checked. If false, the buttons are mutually exclusive
     * * (the same behavior as HTML radio buttons)
     * *
     * @return the radio group
     */
    fun getRadioGroup(noToggleToOff: Boolean, radiosInUnison: Boolean): PdfFormField {
        val field = PdfFormField.createRadioButton(writer, noToggleToOff)
        if (radiosInUnison)
            field.setFieldFlags(PdfFormField.FF_RADIOSINUNISON)
        field.setFieldName(fieldName)
        if (options and BaseField.READ_ONLY !== 0)
            field.setFieldFlags(PdfFormField.FF_READ_ONLY)
        if (options and BaseField.REQUIRED !== 0)
            field.setFieldFlags(PdfFormField.FF_REQUIRED)
        field.setValueAsName(if (isChecked) onValue else "Off")
        return field
    }

    /**
     * Gets the radio field. It's only composed of the widget keys and must be used
     * with [.getRadioGroup].
     * @return the radio field
     * *
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     */
    val radioField: PdfFormField
        @Throws(IOException::class, DocumentException::class)
        get() = getField(true)

    /**
     * Gets the check field.
     * @return the check field
     * *
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     */
    val checkField: PdfFormField
        @Throws(IOException::class, DocumentException::class)
        get() = getField(false)

    /**
     * Gets a radio or check field.
     * @param isRadio true to get a radio field, false to get
     * * a check field
     * *
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return the field
     */
    @Throws(IOException::class, DocumentException::class)
    protected fun getField(isRadio: Boolean): PdfFormField {
        var field: PdfFormField? = null
        if (isRadio)
            field = PdfFormField.createEmpty(writer)
        else
            field = PdfFormField.createCheckBox(writer)
        field!!.setWidget(box, PdfAnnotation.HIGHLIGHT_INVERT)
        if (!isRadio) {
            field.setFieldName(fieldName)
            if (options and BaseField.READ_ONLY !== 0)
                field.setFieldFlags(PdfFormField.FF_READ_ONLY)
            if (options and BaseField.REQUIRED !== 0)
                field.setFieldFlags(PdfFormField.FF_REQUIRED)
            field.setValueAsName(if (isChecked) onValue else "Off")
            checkType = checkType
        }
        if (text != null)
            field.setMKNormalCaption(text)
        if (rotation != 0)
            field.setMKRotation(rotation)
        field.setBorderStyle(PdfBorderDictionary(borderWidth, borderStyle.toInt(), PdfDashPattern(3f)))
        val tpon = getAppearance(isRadio, true)
        val tpoff = getAppearance(isRadio, false)
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, onValue, tpon)
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, "Off", tpoff)
        field.setAppearanceState(if (isChecked) onValue else "Off")
        val da = tpon.duplicate as PdfAppearance
        val realFont = realFont
        if (realFont != null)
            da.setFontAndSize(realFont, fontSize)
        if (textColor == null)
            da.setGrayFill(0f)
        else
            da.setColorFill(textColor)
        field.setDefaultAppearanceString(da)
        if (borderColor != null)
            field.setMKBorderColor(borderColor)
        if (backgroundColor != null)
            field.setMKBackgroundColor(backgroundColor)
        when (visibility) {
            BaseField.HIDDEN -> field.setFlags(PdfAnnotation.FLAGS_PRINT or PdfAnnotation.FLAGS_HIDDEN)
            BaseField.VISIBLE_BUT_DOES_NOT_PRINT -> {
            }
            BaseField.HIDDEN_BUT_PRINTABLE -> field.setFlags(PdfAnnotation.FLAGS_PRINT or PdfAnnotation.FLAGS_NOVIEW)
            else -> field.setFlags(PdfAnnotation.FLAGS_PRINT)
        }
        return field
    }

    companion object {

        /** A field with the symbol check  */
        val TYPE_CHECK = 1
        /** A field with the symbol circle  */
        val TYPE_CIRCLE = 2
        /** A field with the symbol cross  */
        val TYPE_CROSS = 3
        /** A field with the symbol diamond  */
        val TYPE_DIAMOND = 4
        /** A field with the symbol square  */
        val TYPE_SQUARE = 5
        /** A field with the symbol star  */
        val TYPE_STAR = 6

        protected var typeChars = arrayOf("4", "l", "8", "u", "n", "H")
    }
}

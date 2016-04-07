/*
 * $Id: a50bde9726eb6e24d06ec57ed797fa2777d2d0df $
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
import java.io.OutputStream
import java.util.HashSet

import com.itextpdf.text.BaseColor
import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.internal.PdfIsoKeys

/**
 * Each PDF document can contain maximum 1 AcroForm.
 */

class PdfAcroForm
/** Creates new PdfAcroForm
 * @param writer
 */
(private val writer: PdfWriter) : PdfDictionary() {


    /** This is a map containing FieldTemplates.  */
    private val fieldTemplates = HashSet<PdfTemplate>()

    /** This is an array containing DocumentFields.  */
    private val documentFields = PdfArray()

    /** This is an array containing the calculationorder of the fields.  */
    private val calculationOrder = PdfArray()

    /** Contains the signature flags.  */
    private var sigFlags = 0

    fun setNeedAppearances(value: Boolean) {
        put(PdfName.NEEDAPPEARANCES, PdfBoolean(value))
    }

    /**
     * Adds fieldTemplates.
     * @param ft
     */

    fun addFieldTemplates(ft: HashSet<PdfTemplate>) {
        fieldTemplates.addAll(ft)
    }

    /**
     * Adds documentFields.
     * @param ref
     */

    fun addDocumentField(ref: PdfIndirectReference) {
        documentFields.add(ref)
    }

    /**
     * Checks if the Acroform is valid
     * @return true if the Acroform is valid
     */

    val isValid: Boolean
        get() {
            if (documentFields.size() == 0) return false
            put(PdfName.FIELDS, documentFields)
            if (sigFlags != 0)
                put(PdfName.SIGFLAGS, PdfNumber(sigFlags))
            if (calculationOrder.size() > 0)
                put(PdfName.CO, calculationOrder)
            if (fieldTemplates.isEmpty()) return true
            val dic = PdfDictionary()
            for (template in fieldTemplates) {
                PdfFormField.mergeResources(dic, template.resources as PdfDictionary)
            }
            put(PdfName.DR, dic)
            put(PdfName.DA, PdfString("/Helv 0 Tf 0 g "))
            val fonts = dic.get(PdfName.FONT) as PdfDictionary?
            if (fonts != null) {
                writer.eliminateFontSubset(fonts)
            }
            return true
        }

    /**
     * Adds an object to the calculationOrder.
     * @param formField
     */

    fun addCalculationOrder(formField: PdfFormField) {
        calculationOrder.add(formField.indirectReference)
    }

    /**
     * Sets the signature flags.
     * @param f
     */

    fun setSigFlags(f: Int) {
        sigFlags = sigFlags or f
    }

    /**
     * Adds a formfield to the AcroForm.
     * @param formField
     */

    fun addFormField(formField: PdfFormField) {
        writer.addAnnotation(formField)
    }

    /**
     * @param name
     * *
     * @param caption
     * *
     * @param value
     * *
     * @param url
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addHtmlPostButton(name: String, caption: String, value: String, url: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val action = PdfAction.createSubmitForm(url, null, PdfAction.SUBMIT_HTML_FORMAT)
        val button = PdfFormField(writer, llx, lly, urx, ury, action)
        setButtonParams(button, PdfFormField.FF_PUSHBUTTON, name, value)
        drawButton(button, caption, font, fontSize, llx, lly, urx, ury)
        addFormField(button)
        return button
    }

    /**
     * @param name
     * *
     * @param caption
     * *
     * @param value
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addResetButton(name: String, caption: String, value: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val action = PdfAction.createResetForm(null, 0)
        val button = PdfFormField(writer, llx, lly, urx, ury, action)
        setButtonParams(button, PdfFormField.FF_PUSHBUTTON, name, value)
        drawButton(button, caption, font, fontSize, llx, lly, urx, ury)
        addFormField(button)
        return button
    }

    /**
     * @param name
     * *
     * @param value
     * *
     * @param url
     * *
     * @param appearance
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addMap(name: String, value: String, url: String, appearance: PdfContentByte, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val action = PdfAction.createSubmitForm(url, null, PdfAction.SUBMIT_HTML_FORMAT or PdfAction.SUBMIT_COORDINATES)
        val button = PdfFormField(writer, llx, lly, urx, ury, action)
        setButtonParams(button, PdfFormField.FF_PUSHBUTTON, name, null)
        val pa = PdfAppearance.createAppearance(writer, urx - llx, ury - lly)
        pa.add(appearance)
        button.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, pa)
        addFormField(button)
        return button
    }

    /**
     * @param button
     * *
     * @param characteristics
     * *
     * @param name
     * *
     * @param value
     */
    fun setButtonParams(button: PdfFormField, characteristics: Int, name: String, value: String?) {
        button.setButton(characteristics)
        button.setFlags(PdfAnnotation.FLAGS_PRINT)
        button.setPage()
        button.setFieldName(name)
        if (value != null) button.setValueAsString(value)
    }

    /**
     * @param button
     * *
     * @param caption
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun drawButton(button: PdfFormField, caption: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float) {
        val pa = PdfAppearance.createAppearance(writer, urx - llx, ury - lly)
        pa.drawButton(0f, 0f, urx - llx, ury - lly, caption, font, fontSize)
        button.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, pa)
    }

    /**
     * @param name
     * *
     * @param value
     * *
     * @return a PdfFormField
     */
    fun addHiddenField(name: String, value: String): PdfFormField {
        val hidden = PdfFormField.createEmpty(writer)
        hidden.setFieldName(name)
        hidden.setValueAsName(value)
        addFormField(hidden)
        return hidden
    }

    /**
     * @param name
     * *
     * @param text
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addSingleLineTextField(name: String, text: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val field = PdfFormField.createTextField(writer, PdfFormField.SINGLELINE, PdfFormField.PLAINTEXT, 0)
        setTextFieldParams(field, text, name, llx, lly, urx, ury)
        drawSingleLineOfText(field, text, font, fontSize, llx, lly, urx, ury)
        addFormField(field)
        return field
    }

    /**
     * @param name
     * *
     * @param text
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addMultiLineTextField(name: String, text: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val field = PdfFormField.createTextField(writer, PdfFormField.MULTILINE, PdfFormField.PLAINTEXT, 0)
        setTextFieldParams(field, text, name, llx, lly, urx, ury)
        drawMultiLineOfText(field, text, font, fontSize, llx, lly, urx, ury)
        addFormField(field)
        return field
    }

    /**
     * @param name
     * *
     * @param text
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return PdfFormField
     */
    fun addSingleLinePasswordField(name: String, text: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val field = PdfFormField.createTextField(writer, PdfFormField.SINGLELINE, PdfFormField.PASSWORD, 0)
        setTextFieldParams(field, text, name, llx, lly, urx, ury)
        drawSingleLineOfText(field, text, font, fontSize, llx, lly, urx, ury)
        addFormField(field)
        return field
    }

    /**
     * @param field
     * *
     * @param text
     * *
     * @param name
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun setTextFieldParams(field: PdfFormField, text: String, name: String, llx: Float, lly: Float, urx: Float, ury: Float) {
        field.setWidget(Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_INVERT)
        field.setValueAsString(text)
        field.setDefaultValueAsString(text)
        field.setFieldName(name)
        field.setFlags(PdfAnnotation.FLAGS_PRINT)
        field.setPage()
    }

    /**
     * @param field
     * *
     * @param text
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun drawSingleLineOfText(field: PdfFormField, text: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float) {
        val tp = PdfAppearance.createAppearance(writer, urx - llx, ury - lly)
        val tp2 = tp.duplicate as PdfAppearance
        tp2.setFontAndSize(font, fontSize)
        tp2.resetRGBColorFill()
        field.setDefaultAppearanceString(tp2)
        tp.drawTextField(0f, 0f, urx - llx, ury - lly)
        tp.beginVariableText()
        tp.saveState()
        tp.rectangle(3f, 3f, urx - llx - 6f, ury - lly - 6f)
        tp.clip()
        tp.newPath()
        tp.beginText()
        tp.setFontAndSize(font, fontSize)
        tp.resetRGBColorFill()
        tp.setTextMatrix(4f, (ury - lly) / 2 - fontSize * 0.3f)
        tp.showText(text)
        tp.endText()
        tp.restoreState()
        tp.endVariableText()
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp)
    }

    /**
     * @param field
     * *
     * @param text
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun drawMultiLineOfText(field: PdfFormField, text: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float) {
        val tp = PdfAppearance.createAppearance(writer, urx - llx, ury - lly)
        val tp2 = tp.duplicate as PdfAppearance
        tp2.setFontAndSize(font, fontSize)
        tp2.resetRGBColorFill()
        field.setDefaultAppearanceString(tp2)
        tp.drawTextField(0f, 0f, urx - llx, ury - lly)
        tp.beginVariableText()
        tp.saveState()
        tp.rectangle(3f, 3f, urx - llx - 6f, ury - lly - 6f)
        tp.clip()
        tp.newPath()
        tp.beginText()
        tp.setFontAndSize(font, fontSize)
        tp.resetRGBColorFill()
        tp.setTextMatrix(4f, 5f)
        val tokenizer = java.util.StringTokenizer(text, "\n")
        var yPos = ury - lly
        while (tokenizer.hasMoreTokens()) {
            yPos -= fontSize * 1.2f
            tp.showTextAligned(PdfContentByte.ALIGN_LEFT, tokenizer.nextToken(), 3f, yPos, 0f)
        }
        tp.endText()
        tp.restoreState()
        tp.endVariableText()
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp)
    }

    /**
     * @param name
     * *
     * @param value
     * *
     * @param status
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addCheckBox(name: String, value: String, status: Boolean, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val field = PdfFormField.createCheckBox(writer)
        setCheckBoxParams(field, name, value, status, llx, lly, urx, ury)
        drawCheckBoxAppearences(field, value, llx, lly, urx, ury)
        addFormField(field)
        return field
    }

    /**
     * @param field
     * *
     * @param name
     * *
     * @param value
     * *
     * @param status
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun setCheckBoxParams(field: PdfFormField, name: String, value: String, status: Boolean, llx: Float, lly: Float, urx: Float, ury: Float) {
        field.setWidget(Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_TOGGLE)
        field.setFieldName(name)
        if (status) {
            field.setValueAsName(value)
            field.setAppearanceState(value)
        } else {
            field.setValueAsName("Off")
            field.setAppearanceState("Off")
        }
        field.setFlags(PdfAnnotation.FLAGS_PRINT)
        field.setPage()
        field.setBorderStyle(PdfBorderDictionary(1f, PdfBorderDictionary.STYLE_SOLID))
    }

    /**
     * @param field
     * *
     * @param value
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun drawCheckBoxAppearences(field: PdfFormField, value: String, llx: Float, lly: Float, urx: Float, ury: Float) {
        var font: BaseFont? = null
        try {
            font = BaseFont.createFont(BaseFont.ZAPFDINGBATS, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED)
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

        val size = ury - lly
        val tpOn = PdfAppearance.createAppearance(writer, urx - llx, ury - lly)
        val tp2 = tpOn.duplicate as PdfAppearance
        tp2.setFontAndSize(font, size)
        tp2.resetRGBColorFill()
        field.setDefaultAppearanceString(tp2)
        tpOn.drawTextField(0f, 0f, urx - llx, ury - lly)
        tpOn.saveState()
        tpOn.resetRGBColorFill()
        tpOn.beginText()
        tpOn.setFontAndSize(font, size)
        tpOn.showTextAligned(PdfContentByte.ALIGN_CENTER, "4", (urx - llx) / 2, (ury - lly) / 2 - size * 0.3f, 0f)
        tpOn.endText()
        tpOn.restoreState()
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, value, tpOn)
        val tpOff = PdfAppearance.createAppearance(writer, urx - llx, ury - lly)
        tpOff.drawTextField(0f, 0f, urx - llx, ury - lly)
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, "Off", tpOff)
    }

    /**
     * @param name
     * *
     * @param defaultValue
     * *
     * @param noToggleToOff
     * *
     * @return a PdfFormField
     */
    fun getRadioGroup(name: String, defaultValue: String, noToggleToOff: Boolean): PdfFormField {
        val radio = PdfFormField.createRadioButton(writer, noToggleToOff)
        radio.setFieldName(name)
        radio.setValueAsName(defaultValue)
        return radio
    }

    /**
     * @param radiogroup
     */
    fun addRadioGroup(radiogroup: PdfFormField) {
        addFormField(radiogroup)
    }

    /**
     * @param radiogroup
     * *
     * @param value
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addRadioButton(radiogroup: PdfFormField, value: String, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val radio = PdfFormField.createEmpty(writer)
        radio.setWidget(Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_TOGGLE)
        val name = (radiogroup.get(PdfName.V) as PdfName).toString().substring(1)
        if (name == value) {
            radio.setAppearanceState(value)
        } else {
            radio.setAppearanceState("Off")
        }
        drawRadioAppearences(radio, value, llx, lly, urx, ury)
        radiogroup.addKid(radio)
        return radio
    }

    /**
     * @param field
     * *
     * @param value
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun drawRadioAppearences(field: PdfFormField, value: String, llx: Float, lly: Float, urx: Float, ury: Float) {
        val tpOn = PdfAppearance.createAppearance(writer, urx - llx, ury - lly)
        tpOn.drawRadioField(0f, 0f, urx - llx, ury - lly, true)
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, value, tpOn)
        val tpOff = PdfAppearance.createAppearance(writer, urx - llx, ury - lly)
        tpOff.drawRadioField(0f, 0f, urx - llx, ury - lly, false)
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, "Off", tpOff)
    }

    /**
     * @param name
     * *
     * @param options
     * *
     * @param defaultValue
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addSelectList(name: String, options: Array<String>, defaultValue: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val choice = PdfFormField.createList(writer, options, 0)
        setChoiceParams(choice, name, defaultValue, llx, lly, urx, ury)
        val text = StringBuffer()
        for (option in options) {
            text.append(option).append('\n')
        }
        drawMultiLineOfText(choice, text.toString(), font, fontSize, llx, lly, urx, ury)
        addFormField(choice)
        return choice
    }

    /**
     * @param name
     * *
     * @param options
     * *
     * @param defaultValue
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addSelectList(name: String, options: Array<Array<String>>, defaultValue: String, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val choice = PdfFormField.createList(writer, options, 0)
        setChoiceParams(choice, name, defaultValue, llx, lly, urx, ury)
        val text = StringBuffer()
        for (option in options) {
            text.append(option[1]).append('\n')
        }
        drawMultiLineOfText(choice, text.toString(), font, fontSize, llx, lly, urx, ury)
        addFormField(choice)
        return choice
    }

    /**
     * @param name
     * *
     * @param options
     * *
     * @param defaultValue
     * *
     * @param editable
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addComboBox(name: String, options: Array<String>, defaultValue: String?, editable: Boolean, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        var defaultValue = defaultValue
        val choice = PdfFormField.createCombo(writer, editable, options, 0)
        setChoiceParams(choice, name, defaultValue, llx, lly, urx, ury)
        if (defaultValue == null) {
            defaultValue = options[0]
        }
        drawSingleLineOfText(choice, defaultValue, font, fontSize, llx, lly, urx, ury)
        addFormField(choice)
        return choice
    }

    /**
     * @param name
     * *
     * @param options
     * *
     * @param defaultValue
     * *
     * @param editable
     * *
     * @param font
     * *
     * @param fontSize
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addComboBox(name: String, options: Array<Array<String>>, defaultValue: String, editable: Boolean, font: BaseFont, fontSize: Float, llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val choice = PdfFormField.createCombo(writer, editable, options, 0)
        setChoiceParams(choice, name, defaultValue, llx, lly, urx, ury)
        var value: String? = null
        for (option in options) {
            if (option[0] == defaultValue) {
                value = option[1]
                break
            }
        }
        if (value == null) {
            value = options[0][1]
        }
        drawSingleLineOfText(choice, value, font, fontSize, llx, lly, urx, ury)
        addFormField(choice)
        return choice
    }

    /**
     * @param field
     * *
     * @param name
     * *
     * @param defaultValue
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun setChoiceParams(field: PdfFormField, name: String, defaultValue: String?, llx: Float, lly: Float, urx: Float, ury: Float) {
        field.setWidget(Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_INVERT)
        if (defaultValue != null) {
            field.setValueAsString(defaultValue)
            field.setDefaultValueAsString(defaultValue)
        }
        field.setFieldName(name)
        field.setFlags(PdfAnnotation.FLAGS_PRINT)
        field.setPage()
        field.setBorderStyle(PdfBorderDictionary(2f, PdfBorderDictionary.STYLE_SOLID))
    }

    /**
     * @param name
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     * *
     * @return a PdfFormField
     */
    fun addSignature(name: String,
                     llx: Float, lly: Float, urx: Float, ury: Float): PdfFormField {
        val signature = PdfFormField.createSignature(writer)
        setSignatureParams(signature, name, llx, lly, urx, ury)
        drawSignatureAppearences(signature, llx, lly, urx, ury)
        addFormField(signature)
        return signature
    }

    /**
     * @param field
     * *
     * @param name
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun setSignatureParams(field: PdfFormField, name: String,
                           llx: Float, lly: Float, urx: Float, ury: Float) {
        field.setWidget(Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_INVERT)
        field.setFieldName(name)
        field.setFlags(PdfAnnotation.FLAGS_PRINT)
        field.setPage()
        field.setMKBorderColor(BaseColor.BLACK)
        field.setMKBackgroundColor(BaseColor.WHITE)
    }

    /**
     * @param field
     * *
     * @param llx
     * *
     * @param lly
     * *
     * @param urx
     * *
     * @param ury
     */
    fun drawSignatureAppearences(field: PdfFormField,
                                 llx: Float, lly: Float, urx: Float, ury: Float) {
        val tp = PdfAppearance.createAppearance(writer, urx - llx, ury - lly)
        tp.setGrayFill(1.0f)
        tp.rectangle(0f, 0f, urx - llx, ury - lly)
        tp.fill()
        tp.setGrayStroke(0f)
        tp.setLineWidth(1f)
        tp.rectangle(0.5f, 0.5f, urx - llx - 0.5f, ury - lly - 0.5f)
        tp.closePathStroke()
        tp.saveState()
        tp.rectangle(1f, 1f, urx - llx - 2f, ury - lly - 2f)
        tp.clip()
        tp.newPath()
        tp.restoreState()
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp)
    }

    @Throws(IOException::class)
    override fun toPdf(writer: PdfWriter, os: OutputStream) {
        PdfWriter.checkPdfIsoConformance(writer, PdfIsoKeys.PDFISOKEY_FORM, this)
        super.toPdf(writer, os)
    }

}

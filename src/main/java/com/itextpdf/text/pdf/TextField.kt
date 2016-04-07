/*
 * $Id: 32bf793eb429470f1ce5b941df90d5f9eba59e18 $
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

import com.itextpdf.text.*

import java.io.IOException
import java.util.ArrayList

/**
 * Supports text, combo and list fields generating the correct appearances.
 * All the option in the Acrobat GUI are supported in an easy to use API.
 * @author Paulo Soares
 */
class TextField
/**
 * Creates a new TextField.
 * @param writer the document PdfWriter
 * *
 * @param box the field location and dimensions
 * *
 * @param fieldName the field name. If null only the widget keys
 * * will be included in the field allowing it to be used as a kid field.
 */
(writer: PdfWriter, box: Rectangle, fieldName: String) : BaseField(writer, box, fieldName) {

    /** Holds value of property defaultText.  */
    /**
     * Gets the default text.
     * @return the default text
     */
    /**
     * Sets the default text. It is only meaningful for text fields.
     * @param defaultText the default text
     */
    var defaultText: String? = null

    /** Holds value of property choices.  */
    /**
     * Gets the choices to be presented to the user in list/combo fields.
     * @return the choices to be presented to the user
     */
    /**
     * Sets the choices to be presented to the user in list/combo fields.
     * @param choices the choices to be presented to the user
     */
    var choices: Array<String>? = null

    /** Holds value of property choiceExports.  */
    /**
     * Gets the export values in list/combo fields.
     * @return the export values in list/combo fields
     */
    /**
     * Sets the export values in list/combo fields. If this array
     * is null then the choice values will also be used
     * as the export values.
     * @param choiceExports the export values in list/combo fields
     */
    var choiceExports: Array<String>? = null

    /** Holds value of property choiceSelection.  */
    /**
     * Gets the selected items.
     * @return the selected items
     * *
     * *
     * @since 5.0.1
     */
    /**
     * Replaces the existing selections with the param. If this field isn't a MULTISELECT
     * list, all but the first element will be removed.
     * @param selections new selections.  If null, it clear()s the underlying ArrayList.
     */
    // can't have multiple selections in a single-select field
    var choiceSelections: ArrayList<Int>? = ArrayList()
        set(selections) = if (selections != null) {
            choiceSelections = ArrayList(selections)
            if (choiceSelections!!.size > 1 && options and BaseField.MULTISELECT === 0) {
                while (choiceSelections!!.size > 1) {
                    choiceSelections!!.removeAt(1)
                }
            }

        } else {
            choiceSelections!!.clear()
        }

    internal var topFirst: Int = 0
        private set
    /** Represents the /TI value  */
    /**
     * Returns the index of the top visible choice of a list. Default is -1.
     * @return the index of the top visible choice
     */
    /**
     * Sets the top visible choice for lists;

     * @since 5.5.3
     * *
     * @param visibleTopChoice index of the first visible item (zero-based array)
     */
    var visibleTopChoice = -1
        set(visibleTopChoice) {
            if (visibleTopChoice < 0) {
                return
            }

            if (choices != null) {
                if (visibleTopChoice < choices!!.size) {
                    this.visibleTopChoice = visibleTopChoice
                }
            }
        }

    private var extraMarginLeft: Float = 0.toFloat()
    private var extraMarginTop: Float = 0.toFloat()

    private fun composePhrase(text: String, ufont: BaseFont, color: BaseColor, fontSize: Float): Phrase {
        var phrase: Phrase? = null
        if (extensionFont == null && (substitutionFonts == null || substitutionFonts!!.isEmpty()))
            phrase = Phrase(Chunk(text, Font(ufont, fontSize, 0, color)))
        else {
            val fs = FontSelector()
            fs.addFont(Font(ufont, fontSize, 0, color))
            if (extensionFont != null)
                fs.addFont(Font(extensionFont, fontSize, 0, color))
            if (substitutionFonts != null) {
                for (k in substitutionFonts!!.indices)
                    fs.addFont(Font(substitutionFonts!![k], fontSize, 0, color))
            }
            phrase = fs.process(text)
        }
        return phrase
    }

    /**
     * Get the `PdfAppearance` of a text or combo field
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return A `PdfAppearance`
     */
    //fixed by Kazuya Ujihara (ujihara.jp)
    val appearance: PdfAppearance
        @Throws(IOException::class, DocumentException::class)
        get() {
            val app = borderAppearance
            app.beginVariableText()
            if (text == null || text.length == 0) {
                app.endVariableText()
                return app
            }

            val borderExtra = borderStyle === PdfBorderDictionary.STYLE_BEVELED || borderStyle === PdfBorderDictionary.STYLE_INSET
            var h = box!!.height - borderWidth * 2 - extraMarginTop
            var bw2 = borderWidth
            if (borderExtra) {
                h -= borderWidth * 2
                bw2 *= 2f
            }
            val offsetX = Math.max(bw2, 1f)
            val offX = Math.min(bw2, offsetX)
            app.saveState()
            app.rectangle(offX, offX, box!!.width - 2 * offX, box!!.height - 2 * offX)
            app.clip()
            app.newPath()
            val ptext: String
            if (options and BaseField.PASSWORD !== 0)
                ptext = obfuscatePassword(text)
            else if (options and BaseField.MULTILINE === 0)
                ptext = removeCRLF(text)
            else
                ptext = text
            val ufont = realFont
            val fcolor = if (textColor == null) GrayColor.GRAYBLACK else textColor
            val rtl = if (checkRTL(ptext)) PdfWriter.RUN_DIRECTION_LTR else PdfWriter.RUN_DIRECTION_NO_BIDI
            var usize = fontSize
            val phrase = composePhrase(ptext, ufont, fcolor, usize)
            if (options and BaseField.MULTILINE !== 0) {
                val width = box!!.width - 4 * offsetX - extraMarginLeft
                val factor = ufont.getFontDescriptor(BaseFont.BBOXURY, 1f) - ufont.getFontDescriptor(BaseFont.BBOXLLY, 1f)
                val ct = ColumnText(null)
                if (usize == 0f) {
                    usize = h / factor
                    if (usize > 4) {
                        if (usize > 12)
                            usize = 12f
                        val step = Math.max((usize - 4) / 10, 0.2f)
                        ct.setSimpleColumn(0f, -h, width, 0f)
                        ct.alignment = alignment
                        ct.runDirection = rtl
                        while (usize > 4) {
                            ct.yLine = 0
                            changeFontSize(phrase, usize)
                            ct.setText(phrase)
                            ct.leading = factor * usize
                            val status = ct.go(true)
                            if (status and ColumnText.NO_MORE_COLUMN == 0)
                                break
                            usize -= step
                        }
                    }
                    if (usize < 4)
                        usize = 4f
                }
                changeFontSize(phrase, usize)
                ct.canvas = app
                val leading = usize * factor
                val offsetY = offsetX + h - ufont.getFontDescriptor(BaseFont.BBOXURY, usize)
                ct.setSimpleColumn(extraMarginLeft + 2 * offsetX, -20000f, box!!.width - 2 * offsetX, offsetY + leading)
                ct.leading = leading
                ct.alignment = alignment
                ct.runDirection = rtl
                ct.setText(phrase)
                ct.go()
            } else {
                if (usize == 0f) {
                    val maxCalculatedSize = h / (ufont.getFontDescriptor(BaseFont.BBOXURX, 1f) - ufont.getFontDescriptor(BaseFont.BBOXLLY, 1f))
                    changeFontSize(phrase, 1f)
                    val wd = ColumnText.getWidth(phrase, rtl, 0)
                    if (wd == 0f)
                        usize = maxCalculatedSize
                    else
                        usize = Math.min(maxCalculatedSize, (box!!.width - extraMarginLeft - 4 * offsetX) / wd)
                    if (usize < 4)
                        usize = 4f
                }
                changeFontSize(phrase, usize)
                var offsetY = offX + (box!!.height - 2 * offX - ufont.getFontDescriptor(BaseFont.ASCENT, usize)) / 2
                if (offsetY < offX)
                    offsetY = offX
                if (offsetY - offX < -ufont.getFontDescriptor(BaseFont.DESCENT, usize)) {
                    val ny = -ufont.getFontDescriptor(BaseFont.DESCENT, usize) + offX
                    val dy = box!!.height - offX - ufont.getFontDescriptor(BaseFont.ASCENT, usize)
                    offsetY = Math.min(ny, Math.max(offsetY, dy))
                }
                if (options and BaseField.COMB !== 0 && maxCharacterLength > 0) {
                    val textLen = Math.min(maxCharacterLength, ptext.length)
                    var position = 0
                    if (alignment === Element.ALIGN_RIGHT)
                        position = maxCharacterLength - textLen
                    else if (alignment === Element.ALIGN_CENTER)
                        position = (maxCharacterLength - textLen) / 2
                    val step = (box!!.width - extraMarginLeft) / maxCharacterLength
                    var start = step / 2 + position * step
                    if (textColor == null)
                        app.setGrayFill(0f)
                    else
                        app.setColorFill(textColor)
                    app.beginText()
                    for (k in phrase.indices) {
                        val ck = phrase[k] as Chunk
                        val bf = ck.font.baseFont
                        app.setFontAndSize(bf, usize)
                        val sb = ck.append("")
                        for (j in 0..sb.length - 1) {
                            val c = sb.substring(j, j + 1)
                            val wd = bf.getWidthPoint(c, usize)
                            app.setTextMatrix(extraMarginLeft + start - wd / 2, offsetY - extraMarginTop)
                            app.showText(c)
                            start += step
                        }
                    }
                    app.endText()
                } else {
                    val x: Float
                    when (alignment) {
                        Element.ALIGN_RIGHT -> x = extraMarginLeft + box!!.width - 2 * offsetX
                        Element.ALIGN_CENTER -> x = extraMarginLeft + box!!.width / 2
                        else -> x = extraMarginLeft + 2 * offsetX
                    }
                    ColumnText.showTextAligned(app, alignment.toInt(), phrase, x, offsetY - extraMarginTop, 0f, rtl, 0)
                }
            }
            app.restoreState()
            app.endVariableText()
            return app
        }

    /**
     * Get the `PdfAppearance` of a list field
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return A `PdfAppearance`
     */
    internal // background boxes for selected value[s]
            // only draw selections within our display range... not strictly necessary with
            // that clipping rect from above, but it certainly doesn't hurt either
            // highlight selected values against their (presumably) darker background
    val listAppearance: PdfAppearance
        @Throws(IOException::class, DocumentException::class)
        get() {
            val app = borderAppearance
            if (choices == null || choices!!.size == 0) {
                return app
            }
            app.beginVariableText()

            val topChoice = topChoice

            val ufont = realFont
            var usize = fontSize
            if (usize == 0f)
                usize = 12f

            val borderExtra = borderStyle === PdfBorderDictionary.STYLE_BEVELED || borderStyle === PdfBorderDictionary.STYLE_INSET
            var h = box!!.height - borderWidth * 2
            var offsetX = borderWidth
            if (borderExtra) {
                h -= borderWidth * 2
                offsetX *= 2f
            }

            val leading = ufont.getFontDescriptor(BaseFont.BBOXURY, usize) - ufont.getFontDescriptor(BaseFont.BBOXLLY, usize)
            val maxFit = (h / leading).toInt() + 1
            var first = 0
            var last = 0
            first = topChoice
            last = first + maxFit
            if (last > choices!!.size)
                last = choices!!.size
            topFirst = first
            app.saveState()
            app.rectangle(offsetX, offsetX, box!!.width - 2 * offsetX, box!!.height - 2 * offsetX)
            app.clip()
            app.newPath()
            val fcolor = if (textColor == null) GrayColor.GRAYBLACK else textColor
            app.setColorFill(BaseColor(10, 36, 106))
            for (curVal in choiceSelections!!.indices) {
                val curChoice = choiceSelections!![curVal].toInt()
                if (curChoice >= first && curChoice <= last) {
                    app.rectangle(offsetX, offsetX + h - (curChoice - first + 1) * leading, box!!.width - 2 * offsetX, leading)
                    app.fill()
                }
            }
            val xp = offsetX * 2
            var yp = offsetX + h - ufont.getFontDescriptor(BaseFont.BBOXURY, usize)
            var idx = first
            while (idx < last) {
                var ptext = choices!![idx]
                val rtl = if (checkRTL(ptext)) PdfWriter.RUN_DIRECTION_LTR else PdfWriter.RUN_DIRECTION_NO_BIDI
                ptext = removeCRLF(ptext)
                val textCol = if (choiceSelections!!.contains(Integer.valueOf(idx))) GrayColor.GRAYWHITE else fcolor
                val phrase = composePhrase(ptext, ufont, textCol, usize)
                ColumnText.showTextAligned(app, Element.ALIGN_LEFT, phrase, xp, yp, 0f, rtl, 0)
                ++idx
                yp -= leading
            }
            app.restoreState()
            app.endVariableText()
            return app
        }

    /**
     * Gets a new text field.
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return a new text field
     */
    val textField: PdfFormField
        @Throws(IOException::class, DocumentException::class)
        get() {
            if (maxCharacterLength <= 0)
                options = options and BaseField.COMB.inv()
            if (options and BaseField.COMB !== 0)
                options = options and BaseField.MULTILINE.inv()
            val field = PdfFormField.createTextField(writer, false, false, maxCharacterLength)
            field.setWidget(box, PdfAnnotation.HIGHLIGHT_INVERT)
            when (alignment) {
                Element.ALIGN_CENTER -> field.setQuadding(PdfFormField.Q_CENTER)
                Element.ALIGN_RIGHT -> field.setQuadding(PdfFormField.Q_RIGHT)
            }
            if (rotation != 0)
                field.setMKRotation(rotation)
            if (fieldName != null) {
                field.setFieldName(fieldName)
                if ("" != text)
                    field.setValueAsString(text)
                if (defaultText != null)
                    field.setDefaultValueAsString(defaultText)
                if (options and BaseField.READ_ONLY !== 0)
                    field.setFieldFlags(PdfFormField.FF_READ_ONLY)
                if (options and BaseField.REQUIRED !== 0)
                    field.setFieldFlags(PdfFormField.FF_REQUIRED)
                if (options and BaseField.MULTILINE !== 0)
                    field.setFieldFlags(PdfFormField.FF_MULTILINE)
                if (options and BaseField.DO_NOT_SCROLL !== 0)
                    field.setFieldFlags(PdfFormField.FF_DONOTSCROLL)
                if (options and BaseField.PASSWORD !== 0)
                    field.setFieldFlags(PdfFormField.FF_PASSWORD)
                if (options and BaseField.FILE_SELECTION !== 0)
                    field.setFieldFlags(PdfFormField.FF_FILESELECT)
                if (options and BaseField.DO_NOT_SPELL_CHECK !== 0)
                    field.setFieldFlags(PdfFormField.FF_DONOTSPELLCHECK)
                if (options and BaseField.COMB !== 0)
                    field.setFieldFlags(PdfFormField.FF_COMB)
            }
            field.setBorderStyle(PdfBorderDictionary(borderWidth, borderStyle.toInt(), PdfDashPattern(3f)))
            val tp = appearance
            field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp)
            val da = tp.duplicate as PdfAppearance
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

    /**
     * Gets a new combo field.
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return a new combo field
     */
    val comboField: PdfFormField
        @Throws(IOException::class, DocumentException::class)
        get() = getChoiceField(false)

    /**
     * Gets a new list field.
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return a new list field
     */
    val listField: PdfFormField
        @Throws(IOException::class, DocumentException::class)
        get() = getChoiceField(true)

    private // else topChoice still 0
    val topChoice: Int
        get() {
            if (choiceSelections == null || choiceSelections!!.size == 0) {
                return 0
            }

            val firstValue = choiceSelections!![0] ?: return 0

            var topChoice = 0
            if (choices != null) {
                if (visibleTopChoice != -1) {
                    return visibleTopChoice
                }

                topChoice = firstValue.toInt()
                topChoice = Math.min(topChoice, choices!!.size)
                topChoice = Math.max(0, topChoice)
            }
            return topChoice
        }

    @Throws(IOException::class, DocumentException::class)
    protected fun getChoiceField(isList: Boolean): PdfFormField {
        options = options and (BaseField.MULTILINE.inv() and BaseField.COMB.inv())
        var uchoices = choices
        if (uchoices == null)
            uchoices = arrayOfNulls<String>(0)

        val topChoice = topChoice

        if (uchoices.size > 0 && topChoice >= 0)
            text = uchoices[topChoice]

        if (text == null)
            text = ""

        var field: PdfFormField? = null
        var mix: Array<Array<String>>? = null

        if (choiceExports == null) {
            if (isList)
                field = PdfFormField.createList(writer, uchoices, topChoice)
            else
                field = PdfFormField.createCombo(writer, options and BaseField.EDIT !== 0, uchoices, topChoice)
        } else {
            mix = Array<Array<String>>(uchoices.size) { arrayOfNulls<String>(2) }
            for (k in mix.indices)
                mix[k][0] = mix[k][1] = uchoices[k]
            val top = Math.min(uchoices.size, choiceExports!!.size)
            for (k in 0..top - 1) {
                if (choiceExports!![k] != null)
                    mix[k][0] = choiceExports!![k]
            }
            if (isList)
                field = PdfFormField.createList(writer, mix, topChoice)
            else
                field = PdfFormField.createCombo(writer, options and BaseField.EDIT !== 0, mix, topChoice)
        }
        field!!.setWidget(box, PdfAnnotation.HIGHLIGHT_INVERT)
        if (rotation != 0)
            field.setMKRotation(rotation)
        if (fieldName != null) {
            field.setFieldName(fieldName)
            if (uchoices.size > 0) {
                if (mix != null) {
                    if (choiceSelections!!.size < 2) {
                        field.setValueAsString(mix[topChoice][0])
                        field.setDefaultValueAsString(mix[topChoice][0])
                    } else {
                        writeMultipleValues(field, mix)
                    }
                } else {
                    if (choiceSelections!!.size < 2) {
                        field.setValueAsString(text)
                        field.setDefaultValueAsString(text)
                    } else {
                        writeMultipleValues(field, null)
                    }
                }
            }
            if (options and BaseField.READ_ONLY !== 0)
                field.setFieldFlags(PdfFormField.FF_READ_ONLY)
            if (options and BaseField.REQUIRED !== 0)
                field.setFieldFlags(PdfFormField.FF_REQUIRED)
            if (options and BaseField.DO_NOT_SPELL_CHECK !== 0)
                field.setFieldFlags(PdfFormField.FF_DONOTSPELLCHECK)
            if (options and BaseField.MULTISELECT !== 0) {
                field.setFieldFlags(PdfFormField.FF_MULTISELECT)
            }
        }
        field.setBorderStyle(PdfBorderDictionary(borderWidth, borderStyle.toInt(), PdfDashPattern(3f)))
        val tp: PdfAppearance
        if (isList) {
            tp = listAppearance
            if (topFirst > 0)
                field.put(PdfName.TI, PdfNumber(topFirst))
        } else
            tp = appearance
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp)
        val da = tp.duplicate as PdfAppearance
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

    private fun writeMultipleValues(field: PdfFormField, mix: Array<Array<String>>?) {
        val indexes = PdfArray()
        val values = PdfArray()
        for (i in choiceSelections!!.indices) {
            val idx = choiceSelections!![i].toInt()
            indexes.add(PdfNumber(idx))

            if (mix != null)
                values.add(PdfString(mix[idx][0]))
            else if (choices != null)
                values.add(PdfString(choices!![idx]))
        }

        field.put(PdfName.V, values)
        field.put(PdfName.I, indexes)

    }

    /**
     * Gets the zero based index of the selected item.
     * @return the zero based index of the selected item
     */
    /**
     * Sets the zero based index of the selected item.
     * @param choiceSelection the zero based index of the selected item
     */
    var choiceSelection: Int
        get() = topChoice
        set(choiceSelection) {
            choiceSelections = ArrayList<Int>()
            choiceSelections!!.add(Integer.valueOf(choiceSelection))
        }

    /**
     * Adds another (or a first I suppose) selection to a MULTISELECT list.
     * This doesn't do anything unless this.options & MUTLISELECT != 0
     * @param selection new selection
     */
    fun addChoiceSelection(selection: Int) {
        if (this.options and BaseField.MULTISELECT !== 0) {
            choiceSelections!!.add(Integer.valueOf(selection))
        }
    }

    /**
     * Sets extra margins in text fields to better mimic the Acrobat layout.
     * @param extraMarginLeft the extra margin left
     * *
     * @param extraMarginTop the extra margin top
     */
    fun setExtraMargin(extraMarginLeft: Float, extraMarginTop: Float) {
        this.extraMarginLeft = extraMarginLeft
        this.extraMarginTop = extraMarginTop
    }

    /**
     * Holds value of property substitutionFonts.
     */
    /**
     * Gets the list of substitution fonts. The list is composed of BaseFont and can be null. The fonts in this list will be used if the original
     * font doesn't contain the needed glyphs.
     * @return the list
     */
    /**
     * Sets a list of substitution fonts. The list is composed of BaseFont and can also be null. The fonts in this list will be used if the original
     * font doesn't contain the needed glyphs.
     * @param substitutionFonts the list
     */
    var substitutionFonts: ArrayList<BaseFont>? = null

    /**
     * Holds value of property extensionFont.
     */
    /**
     * Gets the extensionFont. This font will be searched before the
     * substitution fonts. It may be `null`.
     * @return the extensionFont
     */
    /**
     * Sets the extensionFont. This font will be searched before the
     * substitution fonts. It may be `null`.
     * @param extensionFont New value of property extensionFont.
     */
    var extensionFont: BaseFont? = null

    companion object {

        private fun checkRTL(text: String?): Boolean {
            if (text == null || text.length == 0)
                return false
            val cc = text.toCharArray()
            for (k in cc.indices) {
                val c = cc[k].toInt()
                if (c >= 0x590 && c < 0x0780)
                    return true
            }
            return false
        }

        private fun changeFontSize(p: Phrase, size: Float) {
            for (k in p.indices)
                (p[k] as Chunk).font.size = size
        }

        /**
         * Removes CRLF from a `String`.

         * @param text
         * *
         * @return String
         * *
         * @since    2.1.5
         */
        fun removeCRLF(text: String): String {
            if (text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
                val p = text.toCharArray()
                val sb = StringBuffer(p.size)
                var k = 0
                while (k < p.size) {
                    val c = p[k]
                    if (c == '\n')
                        sb.append(' ')
                    else if (c == '\r') {
                        sb.append(' ')
                        if (k < p.size - 1 && p[k + 1] == '\n')
                            ++k
                    } else
                        sb.append(c)
                    ++k
                }
                return sb.toString()
            }
            return text
        }

        /**
         * Obfuscates a password `String`.
         * Every character is replaced by an asterisk (*).

         * @param text
         * *
         * @return String
         * *
         * @since    2.1.5
         */
        fun obfuscatePassword(text: String): String {
            val pchar = CharArray(text.length)
            for (i in 0..text.length - 1)
                pchar[i] = '*'
            return String(pchar)
        }
    }
}

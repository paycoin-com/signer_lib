/*
 * $Id: 9b292dc199b45529c24e88218a555bcc162760ca $
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
import com.itextpdf.text.error_messages.MessageLocalization

import com.itextpdf.text.DocumentException
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle

/**
 * Creates a pushbutton field. It supports all the text and icon alignments.
 * The icon may be an image or a template.
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
 * Image img = Image.getInstance("image.png");
 * PushbuttonField bt = new PushbuttonField(writer, new Rectangle(100, 100, 200, 200), "Button1");
 * bt.setText("My Caption");
 * bt.setFontSize(0);
 * bt.setImage(img);
 * bt.setLayout(PushbuttonField.LAYOUT_ICON_TOP_LABEL_BOTTOM);
 * bt.setBackgroundColor(Color.cyan);
 * bt.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
 * bt.setBorderColor(Color.red);
 * bt.setBorderWidth(3);
 * PdfFormField ff = bt.getField();
 * PdfAction ac = PdfAction.createSubmitForm("http://www.submit-site.com", null, 0);
 * ff.setAction(ac);
 * writer.addAnnotation(ff);
 * document.close();
 *
 * @author Paulo Soares
 */
class PushbuttonField
/**
 * Creates a new instance of PushbuttonField
 * @param writer the document PdfWriter
 * *
 * @param box the field location and dimensions
 * *
 * @param fieldName the field name. If null only the widget keys
 * * will be included in the field allowing it to be used as a kid field.
 */
(writer: PdfWriter, box: Rectangle, fieldName: String) : BaseField(writer, box, fieldName) {

    /**
     * Holds value of property layout.
     */
    /**
     * Getter for property layout.
     * @return Value of property layout.
     */
    /**
     * Sets the icon and label layout. Possible values are LAYOUT_LABEL_ONLY,
     * LAYOUT_ICON_ONLY, LAYOUT_ICON_TOP_LABEL_BOTTOM,
     * LAYOUT_LABEL_TOP_ICON_BOTTOM, LAYOUT_ICON_LEFT_LABEL_RIGHT,
     * LAYOUT_LABEL_LEFT_ICON_RIGHT and LAYOUT_LABEL_OVER_ICON.
     * The default is LAYOUT_LABEL_ONLY.
     * @param layout New value of property layout.
     */
    var layout = LAYOUT_LABEL_ONLY
        set(layout) {
            if (layout < LAYOUT_LABEL_ONLY || layout > LAYOUT_LABEL_OVER_ICON)
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("layout.out.of.bounds"))
            this.layout = layout
        }

    /**
     * Holds value of property image.
     */
    /**
     * Getter for property image.
     * @return Value of property image.
     */
    /**
     * Sets the icon as an image.
     * @param image the image
     */
    var image: Image? = null
        set(image) {
            this.image = image
            template = null
        }

    /**
     * Holds value of property template.
     */
    /**
     * Getter for property template.
     * @return Value of property template.
     */
    /**
     * Sets the icon as a template.
     * @param template the template
     */
    var template: PdfTemplate? = null
        set(template) {
            this.template = template
            image = null
        }

    /**
     * Holds value of property scaleIcon.
     */
    /**
     * Getter for property scaleIcon.
     * @return Value of property scaleIcon.
     */
    /**
     * Sets the way the icon will be scaled. Possible values are
     * SCALE_ICON_ALWAYS, SCALE_ICON_NEVER,
     * SCALE_ICON_IS_TOO_BIG and SCALE_ICON_IS_TOO_SMALL.
     * The default is SCALE_ICON_ALWAYS.
     * @param scaleIcon the way the icon will be scaled
     */
    var scaleIcon = SCALE_ICON_ALWAYS
        set(scaleIcon) {
            var scaleIcon = scaleIcon
            if (scaleIcon < SCALE_ICON_ALWAYS || scaleIcon > SCALE_ICON_IS_TOO_SMALL)
                scaleIcon = SCALE_ICON_ALWAYS
            this.scaleIcon = scaleIcon
        }

    /**
     * Holds value of property proportionalIcon.
     */
    /**
     * Getter for property proportionalIcon.
     * @return Value of property proportionalIcon.
     */
    /**
     * Sets the way the icon is scaled. If true the icon is scaled proportionally,
     * if false the scaling is done anamorphicaly.
     * @param proportionalIcon the way the icon is scaled
     */
    var isProportionalIcon = true

    /**
     * Holds value of property iconVerticalAdjustment.
     */
    /**
     * Getter for property iconVerticalAdjustment.
     * @return Value of property iconVerticalAdjustment.
     */
    /**
     * A number between 0 and 1 indicating the fraction of leftover space to allocate at the bottom of the icon.
     * A value of 0 positions the icon at the bottom of the annotation rectangle.
     * A value of 0.5 centers it within the rectangle. The default is 0.5.
     * @param iconVerticalAdjustment a number between 0 and 1 indicating the fraction of leftover space to allocate at the bottom of the icon
     */
    var iconVerticalAdjustment = 0.5f
        set(iconVerticalAdjustment) {
            var iconVerticalAdjustment = iconVerticalAdjustment
            if (iconVerticalAdjustment < 0)
                iconVerticalAdjustment = 0f
            else if (iconVerticalAdjustment > 1)
                iconVerticalAdjustment = 1f
            this.iconVerticalAdjustment = iconVerticalAdjustment
        }

    /**
     * Holds value of property iconHorizontalAdjustment.
     */
    /**
     * Getter for property iconHorizontalAdjustment.
     * @return Value of property iconHorizontalAdjustment.
     */
    /**
     * A number between 0 and 1 indicating the fraction of leftover space to allocate at the left of the icon.
     * A value of 0 positions the icon at the left of the annotation rectangle.
     * A value of 0.5 centers it within the rectangle. The default is 0.5.
     * @param iconHorizontalAdjustment a number between 0 and 1 indicating the fraction of leftover space to allocate at the left of the icon
     */
    var iconHorizontalAdjustment = 0.5f
        set(iconHorizontalAdjustment) {
            var iconHorizontalAdjustment = iconHorizontalAdjustment
            if (iconHorizontalAdjustment < 0)
                iconHorizontalAdjustment = 0f
            else if (iconHorizontalAdjustment > 1)
                iconHorizontalAdjustment = 1f
            this.iconHorizontalAdjustment = iconHorizontalAdjustment
        }

    /**
     * Holds value of property iconFitToBounds.
     */
    /**
     * Getter for property iconFitToBounds.
     * @return Value of property iconFitToBounds.
     */
    /**
     * If true the icon will be scaled to fit fully within the bounds of the annotation,
     * if false the border width will be taken into account. The default
     * is false.
     * @param iconFitToBounds if true the icon will be scaled to fit fully within the bounds of the annotation,
     * * if false the border width will be taken into account
     */
    var isIconFitToBounds: Boolean = false

    private var tp: PdfTemplate? = null

    @Throws(IOException::class, DocumentException::class)
    private fun calculateFontSize(w: Float, h: Float): Float {
        val ufont = realFont
        var fsize = fontSize
        if (fsize == 0f) {
            val bw = ufont.getWidthPoint(text, 1f)
            if (bw == 0f)
                fsize = 12f
            else
                fsize = w / bw
            val nfsize = h / (1 - ufont.getFontDescriptor(BaseFont.DESCENT, 1f))
            fsize = Math.min(fsize, nfsize)
            if (fsize < 4)
                fsize = 4f
        }
        return fsize
    }

    /**
     * Gets the button appearance.
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return the button appearance
     */
    val appearance: PdfAppearance
        @Throws(IOException::class, DocumentException::class)
        get() {
            val app = borderAppearance
            val box = Rectangle(app.boundingBox)
            if ((text == null || text.length == 0) && (layout == LAYOUT_LABEL_ONLY || image == null && template == null && iconReference == null)) {
                return app
            }
            if (layout == LAYOUT_ICON_ONLY && image == null && template == null && iconReference == null)
                return app
            val ufont = realFont
            val borderExtra = borderStyle === PdfBorderDictionary.STYLE_BEVELED || borderStyle === PdfBorderDictionary.STYLE_INSET
            var h = box.height - borderWidth * 2
            var bw2 = borderWidth
            if (borderExtra) {
                h -= borderWidth * 2
                bw2 *= 2f
            }
            var offsetX = if (borderExtra) 2 * borderWidth else borderWidth
            offsetX = Math.max(offsetX, 1f)
            val offX = Math.min(bw2, offsetX)
            tp = null
            var textX = java.lang.Float.NaN
            var textY = 0f
            var fsize = fontSize
            val wt = box.width - 2 * offX - 2f
            val ht = box.height - 2 * offX
            val adj = if (isIconFitToBounds) 0 else offX + 1
            var nlayout = layout
            if (image == null && template == null && iconReference == null)
                nlayout = LAYOUT_LABEL_ONLY
            var iconBox: Rectangle? = null
            while (true) {
                when (nlayout) {
                    LAYOUT_LABEL_ONLY, LAYOUT_LABEL_OVER_ICON -> {
                        if (text != null && text.length > 0 && wt > 0 && ht > 0) {
                            fsize = calculateFontSize(wt, ht)
                            textX = (box.width - ufont.getWidthPoint(text, fsize)) / 2
                            textY = (box.height - ufont.getFontDescriptor(BaseFont.ASCENT, fsize)) / 2
                        }
                        if (nlayout == LAYOUT_LABEL_OVER_ICON || nlayout == LAYOUT_ICON_ONLY)
                            iconBox = Rectangle(box.left + adj, box.bottom + adj, box.right - adj, box.top - adj)
                    }
                    LAYOUT_ICON_ONLY -> if (nlayout == LAYOUT_LABEL_OVER_ICON || nlayout == LAYOUT_ICON_ONLY)
                        iconBox = Rectangle(box.left + adj, box.bottom + adj, box.right - adj, box.top - adj)
                    LAYOUT_ICON_TOP_LABEL_BOTTOM -> {
                        if (text == null || text.length == 0 || wt <= 0 || ht <= 0) {
                            nlayout = LAYOUT_ICON_ONLY
                            continue
                        }
                        var nht = box.height * 0.35f - offX
                        if (nht > 0)
                            fsize = calculateFontSize(wt, nht)
                        else
                            fsize = 4f
                        textX = (box.width - ufont.getWidthPoint(text, fsize)) / 2
                        textY = offX - ufont.getFontDescriptor(BaseFont.DESCENT, fsize)
                        iconBox = Rectangle(box.left + adj, textY + fsize, box.right - adj, box.top - adj)
                    }
                    LAYOUT_LABEL_TOP_ICON_BOTTOM -> {
                        if (text == null || text.length == 0 || wt <= 0 || ht <= 0) {
                            nlayout = LAYOUT_ICON_ONLY
                            continue
                        }
                        nht = box.height * 0.35f - offX
                        if (nht > 0)
                            fsize = calculateFontSize(wt, nht)
                        else
                            fsize = 4f
                        textX = (box.width - ufont.getWidthPoint(text, fsize)) / 2
                        textY = box.height - offX - fsize
                        if (textY < offX)
                            textY = offX
                        iconBox = Rectangle(box.left + adj, box.bottom + adj, box.right - adj, textY + ufont.getFontDescriptor(BaseFont.DESCENT, fsize))
                    }
                    LAYOUT_LABEL_LEFT_ICON_RIGHT -> {
                        if (text == null || text.length == 0 || wt <= 0 || ht <= 0) {
                            nlayout = LAYOUT_ICON_ONLY
                            continue
                        }
                        var nw = box.width * 0.35f - offX
                        if (nw > 0)
                            fsize = calculateFontSize(wt, nw)
                        else
                            fsize = 4f
                        if (ufont.getWidthPoint(text, fsize) >= wt) {
                            nlayout = LAYOUT_LABEL_ONLY
                            fsize = fontSize
                            continue
                        }
                        textX = offX + 1
                        textY = (box.height - ufont.getFontDescriptor(BaseFont.ASCENT, fsize)) / 2
                        iconBox = Rectangle(textX + ufont.getWidthPoint(text, fsize), box.bottom + adj, box.right - adj, box.top - adj)
                    }
                    LAYOUT_ICON_LEFT_LABEL_RIGHT -> {
                        if (text == null || text.length == 0 || wt <= 0 || ht <= 0) {
                            nlayout = LAYOUT_ICON_ONLY
                            continue
                        }
                        nw = box.width * 0.35f - offX
                        if (nw > 0)
                            fsize = calculateFontSize(wt, nw)
                        else
                            fsize = 4f
                        if (ufont.getWidthPoint(text, fsize) >= wt) {
                            nlayout = LAYOUT_LABEL_ONLY
                            fsize = fontSize
                            continue
                        }
                        textX = box.width - ufont.getWidthPoint(text, fsize) - offX - 1f
                        textY = (box.height - ufont.getFontDescriptor(BaseFont.ASCENT, fsize)) / 2
                        iconBox = Rectangle(box.left + adj, box.bottom + adj, textX - 1, box.top - adj)
                    }
                }
                break
            }
            if (textY < box.bottom + offX)
                textY = box.bottom + offX
            if (iconBox != null && (iconBox.width <= 0 || iconBox.height <= 0))
                iconBox = null
            var haveIcon = false
            var boundingBoxWidth = 0f
            var boundingBoxHeight = 0f
            var matrix: PdfArray? = null
            if (iconBox != null) {
                if (image != null) {
                    tp = PdfTemplate(writer)
                    tp!!.boundingBox = Rectangle(image)
                    writer.addDirectTemplateSimple(tp, PdfName.FRM)
                    tp!!.addImage(image, image!!.width, 0f, 0f, image!!.height, 0f, 0f)
                    haveIcon = true
                    boundingBoxWidth = tp!!.boundingBox.width
                    boundingBoxHeight = tp!!.boundingBox.height
                } else if (template != null) {
                    tp = PdfTemplate(writer)
                    tp!!.boundingBox = Rectangle(template!!.width, template!!.height)
                    writer.addDirectTemplateSimple(tp, PdfName.FRM)
                    tp!!.addTemplate(template, template!!.boundingBox.left, template!!.boundingBox.bottom)
                    haveIcon = true
                    boundingBoxWidth = tp!!.boundingBox.width
                    boundingBoxHeight = tp!!.boundingBox.height
                } else if (iconReference != null) {
                    val dic = PdfReader.getPdfObject(iconReference) as PdfDictionary?
                    if (dic != null) {
                        val r2 = PdfReader.getNormalizedRectangle(dic.getAsArray(PdfName.BBOX))
                        matrix = dic.getAsArray(PdfName.MATRIX)
                        haveIcon = true
                        boundingBoxWidth = r2.width
                        boundingBoxHeight = r2.height
                    }
                }
            }
            if (haveIcon) {
                var icx = iconBox!!.width / boundingBoxWidth
                var icy = iconBox.height / boundingBoxHeight
                if (isProportionalIcon) {
                    when (scaleIcon) {
                        SCALE_ICON_IS_TOO_BIG -> {
                            icx = Math.min(icx, icy)
                            icx = Math.min(icx, 1f)
                        }
                        SCALE_ICON_IS_TOO_SMALL -> {
                            icx = Math.min(icx, icy)
                            icx = Math.max(icx, 1f)
                        }
                        SCALE_ICON_NEVER -> icx = 1f
                        else -> icx = Math.min(icx, icy)
                    }
                    icy = icx
                } else {
                    when (scaleIcon) {
                        SCALE_ICON_IS_TOO_BIG -> {
                            icx = Math.min(icx, 1f)
                            icy = Math.min(icy, 1f)
                        }
                        SCALE_ICON_IS_TOO_SMALL -> {
                            icx = Math.max(icx, 1f)
                            icy = Math.max(icy, 1f)
                        }
                        SCALE_ICON_NEVER -> icx = icy = 1f
                        else -> {
                        }
                    }
                }
                val xpos = iconBox.left + (iconBox.width - boundingBoxWidth * icx) * iconHorizontalAdjustment
                val ypos = iconBox.bottom + (iconBox.height - boundingBoxHeight * icy) * iconVerticalAdjustment
                app.saveState()
                app.rectangle(iconBox.left, iconBox.bottom, iconBox.width, iconBox.height)
                app.clip()
                app.newPath()
                if (tp != null)
                    app.addTemplate(tp, icx, 0f, 0f, icy, xpos, ypos)
                else {
                    var cox = 0f
                    var coy = 0f
                    if (matrix != null && matrix.size() == 6) {
                        var nm: PdfNumber? = matrix.getAsNumber(4)
                        if (nm != null)
                            cox = nm.floatValue()
                        nm = matrix.getAsNumber(5)
                        if (nm != null)
                            coy = nm.floatValue()
                    }
                    app.addTemplateReference(iconReference, PdfName.FRM, icx, 0f, 0f, icy, xpos - cox * icx, ypos - coy * icy)
                }
                app.restoreState()
            }
            if (!java.lang.Float.isNaN(textX)) {
                app.saveState()
                app.rectangle(offX, offX, box.width - 2 * offX, box.height - 2 * offX)
                app.clip()
                app.newPath()
                if (textColor == null)
                    app.resetGrayFill()
                else
                    app.setColorFill(textColor)
                app.beginText()
                app.setFontAndSize(ufont, fsize)
                app.setTextMatrix(textX, textY)
                app.showText(text)
                app.endText()
                app.restoreState()
            }
            return app
        }

    /**
     * Gets the pushbutton field.
     * @throws IOException on error
     * *
     * @throws DocumentException on error
     * *
     * @return the pushbutton field
     */
    val field: PdfFormField
        @Throws(IOException::class, DocumentException::class)
        get() {
            val field = PdfFormField.createPushButton(writer)
            field.setWidget(box, PdfAnnotation.HIGHLIGHT_INVERT)
            if (fieldName != null) {
                field.setFieldName(fieldName)
                if (options and BaseField.READ_ONLY !== 0)
                    field.setFieldFlags(PdfFormField.FF_READ_ONLY)
                if (options and BaseField.REQUIRED !== 0)
                    field.setFieldFlags(PdfFormField.FF_REQUIRED)
            }
            if (text != null)
                field.setMKNormalCaption(text)
            if (rotation != 0)
                field.setMKRotation(rotation)
            field.setBorderStyle(PdfBorderDictionary(borderWidth, borderStyle.toInt(), PdfDashPattern(3f)))
            val tpa = appearance
            field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tpa)
            val da = tpa.duplicate as PdfAppearance
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
            if (tp != null)
                field.setMKNormalIcon(tp)
            field.setMKTextPosition(layout - 1)
            var scale = PdfName.A
            if (scaleIcon == SCALE_ICON_IS_TOO_BIG)
                scale = PdfName.B
            else if (scaleIcon == SCALE_ICON_IS_TOO_SMALL)
                scale = PdfName.S
            else if (scaleIcon == SCALE_ICON_NEVER)
                scale = PdfName.N
            field.setMKIconFit(scale, if (isProportionalIcon) PdfName.P else PdfName.A, iconHorizontalAdjustment,
                    iconVerticalAdjustment, isIconFitToBounds)
            return field
        }

    /**
     * Holds value of property iconReference.
     */
    /**
     * Gets the reference to an existing icon.
     * @return the reference to an existing icon.
     */
    /**
     * Sets the reference to an existing icon.
     * @param iconReference the reference to an existing icon
     */
    var iconReference: PRIndirectReference? = null

    companion object {

        /** A layout option  */
        val LAYOUT_LABEL_ONLY = 1
        /** A layout option  */
        val LAYOUT_ICON_ONLY = 2
        /** A layout option  */
        val LAYOUT_ICON_TOP_LABEL_BOTTOM = 3
        /** A layout option  */
        val LAYOUT_LABEL_TOP_ICON_BOTTOM = 4
        /** A layout option  */
        val LAYOUT_ICON_LEFT_LABEL_RIGHT = 5
        /** A layout option  */
        val LAYOUT_LABEL_LEFT_ICON_RIGHT = 6
        /** A layout option  */
        val LAYOUT_LABEL_OVER_ICON = 7
        /** An icon scaling option  */
        val SCALE_ICON_ALWAYS = 1
        /** An icon scaling option  */
        val SCALE_ICON_NEVER = 2
        /** An icon scaling option  */
        val SCALE_ICON_IS_TOO_BIG = 3
        /** An icon scaling option  */
        val SCALE_ICON_IS_TOO_SMALL = 4
    }

}

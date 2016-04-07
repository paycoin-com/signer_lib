/*
 * $Id: 7efcd9f1ac9ae777019e62d0dafdac8abc436ac7 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Kevin Day, Bruno Lowagie, Paulo Soares, et al.
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
package com.itextpdf.text.pdf.parser

import com.itextpdf.text.BaseColor
import com.itextpdf.text.pdf.CMapAwareDocumentFont
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfName

/**
 * Keeps all the parameters of the graphics state.
 * @since    2.1.4
 */
class GraphicsState {
    /** The current transformation matrix.  */
    /**
     * Getter for the current transformation matrix
     * @return the ctm
     * *
     * @since iText 5.0.1
     */
    var ctm: Matrix
        internal set
    /** The current character spacing.  */
    /**
     * Getter for the character spacing.
     * @return the character spacing
     * *
     * @since iText 5.0.1
     */
    var characterSpacing: Float = 0.toFloat()
        internal set
    /** The current word spacing.  */
    /**
     * Getter for the word spacing
     * @return the word spacing
     * *
     * @since iText 5.0.1
     */
    var wordSpacing: Float = 0.toFloat()
        internal set
    /** The current horizontal scaling  */
    /**
     * Getter for the horizontal scaling
     * @return the horizontal scaling
     * *
     * @since iText 5.0.1
     */
    var horizontalScaling: Float = 0.toFloat()
        internal set
    /** The current leading.  */
    /**
     * Getter for the leading
     * @return the leading
     * *
     * @since iText 5.0.1
     */
    var leading: Float = 0.toFloat()
        internal set
    /** The active font.  */
    /**
     * Getter for the font
     * @return the font
     * *
     * @since iText 5.0.1
     */
    var font: CMapAwareDocumentFont? = null
        internal set
    /** The current font size.  */
    /**
     * Getter for the font size
     * @return the font size
     * *
     * @since iText 5.0.1
     */
    var fontSize: Float = 0.toFloat()
        internal set
    /** The current render mode.  */
    /**
     * Getter for the render mode
     * @return the renderMode
     * *
     * @since iText 5.0.1
     */
    var renderMode: Int = 0
        internal set
    /** The current text rise  */
    /**
     * Getter for text rise
     * @return the text rise
     * *
     * @since iText 5.0.1
     */
    var rise: Float = 0.toFloat()
        internal set
    /** The current knockout value.  */
    /**
     * Getter for knockout
     * @return the knockout
     * *
     * @since iText 5.0.1
     */
    var isKnockout: Boolean = false
        internal set
    /** The current color space for stroke.  */
    /**
     * Gets the current color space for fill operations
     */
    var colorSpaceFill: PdfName? = null
        internal set
    /** The current color space for stroke.  */
    /**
     * Gets the current color space for stroke operations
     */
    var colorSpaceStroke: PdfName? = null
        internal set
    /** The current fill color.  */
    /**
     * Gets the current fill color
     * @return a BaseColor
     */
    var fillColor: BaseColor? = BaseColor.BLACK
        internal set
    /** The current stroke color.  */
    /**
     * Gets the current stroke color
     * @return a BaseColor
     */
    var strokeColor: BaseColor? = BaseColor.BLACK
        internal set

    /** The line width for stroking operations  */
    /**
     * Getter for the line width.
     * @return The line width
     * *
     * @since 5.5.6
     */
    /**
     * Setter for the line width.
     * @param lineWidth New line width.
     * *
     * @since 5.5.6
     */
    var lineWidth: Float = 0.toFloat()

    /**
     * The line cap style. For possible values
     * see [PdfContentByte]
     */
    /**
     * Getter for the line cap style.
     * For possible values see [PdfContentByte]
     * @return The line cap style.
     * *
     * @since 5.5.6
     */
    /**
     * Setter for the line cap style.
     * For possible values see [PdfContentByte]
     * @param lineCapStyle New line cap style.
     * *
     * @since 5.5.6
     */
    var lineCapStyle: Int = 0

    /**
     * The line join style. For possible values
     * see [PdfContentByte]
     */
    /**
     * Getter for the line join style.
     * For possible values see [PdfContentByte]
     * @return The line join style.
     * *
     * @since 5.5.6
     */
    /**
     * Setter for the line join style.
     * For possible values see [PdfContentByte]
     * @param lineJoinStyle New line join style.
     * *
     * @since 5.5.6
     */
    var lineJoinStyle: Int = 0

    /** The mitir limit value  */
    /**
     * Getter for the miter limit value.
     * @return The miter limit.
     * *
     * @since 5.5.6
     */
    /**
     * Setter for the miter limit value.
     * @param miterLimit New miter limit.
     * *
     * @since 5.5.6
     */
    var miterLimit: Float = 0.toFloat()

    /** The line dash pattern  */
    /**
     * Getter for the line dash pattern.
     * @return The line dash pattern.
     * *
     * @since 5.5.6
     */
    /**
     * Setter for the line dash pattern.
     * @param lineDashPattern New line dash pattern.
     * *
     * @since 5.5.6
     */
    var lineDashPattern: LineDashPattern? = null
        set(lineDashPattern) {
            this.lineDashPattern = LineDashPattern(lineDashPattern.dashArray, lineDashPattern.dashPhase)
        }

    /**
     * Constructs a new Graphics State object with the default values.
     */
    constructor() {
        ctm = Matrix()
        characterSpacing = 0f
        wordSpacing = 0f
        horizontalScaling = 1.0f
        leading = 0f
        font = null
        fontSize = 0f
        renderMode = 0
        rise = 0f
        isKnockout = true
        colorSpaceFill = null
        colorSpaceStroke = null
        fillColor = null
        strokeColor = null
        lineWidth = 1.0f
        lineCapStyle = PdfContentByte.LINE_CAP_BUTT
        lineJoinStyle = PdfContentByte.LINE_JOIN_MITER
        miterLimit = 10.0f
    }

    /**
     * Copy constructor.
     * @param source    another GraphicsState object
     */
    constructor(source: GraphicsState) {
        // note: all of the following are immutable, with the possible exception of font
        // so it is safe to copy them as-is
        ctm = source.ctm
        characterSpacing = source.characterSpacing
        wordSpacing = source.wordSpacing
        horizontalScaling = source.horizontalScaling
        leading = source.leading
        font = source.font
        fontSize = source.fontSize
        renderMode = source.renderMode
        rise = source.rise
        isKnockout = source.isKnockout
        colorSpaceFill = source.colorSpaceFill
        colorSpaceStroke = source.colorSpaceStroke
        fillColor = source.fillColor
        strokeColor = source.strokeColor
        lineWidth = source.lineWidth
        lineCapStyle = source.lineCapStyle
        lineJoinStyle = source.lineJoinStyle
        miterLimit = source.miterLimit

        if (source.lineDashPattern != null) {
            lineDashPattern = LineDashPattern(source.lineDashPattern!!.dashArray, source.lineDashPattern!!.dashPhase)
        }
    }
}

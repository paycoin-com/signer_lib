/*
 * $Id: e4e094f4b50e7bd8713a2c153454869a33375366 $
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
package com.itextpdf.text.html

/**
 * Static final values of supported HTML tags and attributes.
 * @since 5.0.6
 * *
 */
@Deprecated("")
@Deprecated("since 5.5.2")
object HtmlTags {

    // tag names

    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val A = "a"
    /** name of a tag  */
    val B = "b"
    /** name of a tag  */
    val BODY = "body"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val BLOCKQUOTE = "blockquote"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val BR = "br"
    /** name of a tag  */
    val DIV = "div"
    /** name of a tag  */
    val EM = "em"
    /** name of a tag  */
    val FONT = "font"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val H1 = "h1"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val H2 = "h2"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val H3 = "h3"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val H4 = "h4"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val H5 = "h5"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val H6 = "h6"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val HR = "hr"
    /** name of a tag  */
    val I = "i"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val IMG = "img"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val LI = "li"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val OL = "ol"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val P = "p"
    /** name of a tag  */
    val PRE = "pre"
    /** name of a tag  */
    val S = "s"
    /** name of a tag  */
    val SPAN = "span"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val STRIKE = "strike"
    /** name of a tag  */
    val STRONG = "strong"
    /** name of a tag  */
    val SUB = "sub"
    /** name of a tag  */
    val SUP = "sup"
    /** name of a tag  */
    val TABLE = "table"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val TD = "td"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val TH = "th"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val TR = "tr"
    /** name of a tag  */
    val U = "u"
    /**
     * name of a tag.
     * @since 5.0.6 (reorganized all constants)
     */
    val UL = "ul"

    // attributes (some are not real HTML attributes!)

    /** name of an attribute  */
    val ALIGN = "align"
    /**
     * name of an attribute
     * @since 5.0.6
     */
    val BGCOLOR = "bgcolor"
    /**
     * name of an attribute
     * @since 5.0.6
     */
    val BORDER = "border"
    /** name of an attribute  */
    val CELLPADDING = "cellpadding"
    /** name of an attribute  */
    val COLSPAN = "colspan"
    /**
     * name of an attribute
     * @since 5.0.6
     */
    val EXTRAPARASPACE = "extraparaspace"
    /**
     * name of an attribute
     * @since 5.0.6
     */
    val ENCODING = "encoding"
    /**
     * name of an attribute
     * @since 5.0.6
     */
    val FACE = "face"
    /**
     * Name of an attribute.
     * @since 5.0.6
     */
    val HEIGHT = "height"
    /**
     * Name of an attribute.
     * @since 5.0.6
     */
    val HREF = "href"
    /**
     * Name of an attribute.
     * @since 5.0.6
     */
    val HYPHENATION = "hyphenation"
    /**
     * Name of an attribute.
     * @since 5.0.6
     */
    val IMAGEPATH = "image_path"
    /**
     * Name of an attribute.
     * @since 5.0.6
     */
    val INDENT = "indent"
    /**
     * Name of an attribute.
     * @since 5.0.6
     */
    val LEADING = "leading"
    /** name of an attribute  */
    val ROWSPAN = "rowspan"
    /** name of an attribute  */
    val SIZE = "size"
    /**
     * Name of an attribute.
     * @since 5.0.6
     */
    val SRC = "src"
    /**
     * Name of an attribute.
     * @since 5.0.6
     */
    val VALIGN = "valign"
    /** name of an attribute  */
    val WIDTH = "width"

    // attribute values

    /** the possible value of an alignment attribute  */
    val ALIGN_LEFT = "left"
    /** the possible value of an alignment attribute  */
    val ALIGN_CENTER = "center"
    /** the possible value of an alignment attribute  */
    val ALIGN_RIGHT = "right"
    /**
     * The possible value of an alignment attribute.
     * @since 5.0.6
     */
    val ALIGN_JUSTIFY = "justify"
    /**
     * The possible value of an alignment attribute.
     * @since 5.0.6
     */
    val ALIGN_JUSTIFIED_ALL = "JustifyAll"
    /** the possible value of an alignment attribute  */
    val ALIGN_TOP = "top"
    /** the possible value of an alignment attribute  */
    val ALIGN_MIDDLE = "middle"
    /** the possible value of an alignment attribute  */
    val ALIGN_BOTTOM = "bottom"
    /** the possible value of an alignment attribute  */
    val ALIGN_BASELINE = "baseline"

    // CSS

    /** This is used for inline css style information  */
    val STYLE = "style"
    /**
     * Attribute for specifying externally defined CSS class.
     * @since 5.0.6
     */
    val CLASS = "class"
    /** the CSS tag for text color  */
    val COLOR = "color"
    /**
     * The CSS tag for the font size.
     * @since 5.0.6
     */
    val FONTFAMILY = "font-family"
    /**
     * The CSS tag for the font size.
     * @since 5.0.6
     */
    val FONTSIZE = "font-size"
    /**
     * The CSS tag for the font size.
     * @since 5.0.6
     */
    val FONTSTYLE = "font-style"
    /**
     * The CSS tag for the font size.
     * @since 5.0.6
     */
    val FONTWEIGHT = "font-weight"
    /**
     * The CSS tag for the font size.
     * @since 5.0.6
     */
    val LINEHEIGHT = "line-height"
    /**
     * The CSS tag for the font size.
     * @since 5.0.6
     */
    val PADDINGLEFT = "padding-left"
    /**
     * The CSS tag for the font size.
     * @since 5.0.6
     */
    val TEXTALIGN = "text-align"
    /**
     * The CSS tag for the font size.
     * @since 5.0.6
     */
    val TEXTDECORATION = "text-decoration"
    /** the CSS tag for text decorations  */
    val VERTICALALIGN = "vertical-align"
    /**
     * a CSS value for text decoration
     * @since 5.0.6
     */
    val BOLD = "bold"
    /**
     * a CSS value for text decoration
     * @since 5.0.6
     */
    val ITALIC = "italic"
    /**
     * a CSS value for text decoration
     * @since 5.0.6
     */
    val LINETHROUGH = "line-through"
    /**
     * a CSS value for text decoration
     * @since 5.0.6
     */
    val NORMAL = "normal"
    /**
     * a CSS value for text decoration
     * @since 5.0.6
     */
    val OBLIQUE = "oblique"
    /**
     * a CSS value for text decoration
     * @since 5.0.6
     */
    val UNDERLINE = "underline"

    /**
     * A possible attribute.
     * @since 5.0.6
     */
    val AFTER = "after"
    /**
     * A possible attribute.
     * @since 5.0.6
     */
    val BEFORE = "before"
}

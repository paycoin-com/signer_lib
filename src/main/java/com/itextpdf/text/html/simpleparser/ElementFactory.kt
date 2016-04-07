/*
 * $Id: 3cfd72870fc70ea9027bf0a7ec8d31ed3f202b0f $
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
package com.itextpdf.text.html.simpleparser

import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.StringTokenizer

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.DocListener
import com.itextpdf.text.DocumentException
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.FontProvider
import com.itextpdf.text.Image
import com.itextpdf.text.List
import com.itextpdf.text.ListItem
import com.itextpdf.text.Paragraph
import com.itextpdf.text.html.HtmlTags
import com.itextpdf.text.html.HtmlUtilities
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.HyphenationAuto
import com.itextpdf.text.pdf.HyphenationEvent
import com.itextpdf.text.pdf.draw.LineSeparator

/**
 * Factory that produces iText Element objects,
 * based on tags and their properties.
 * @author blowagie
 * *
 * @author psoares
 * *
 * @since 5.0.6 (renamed)
 * *
 */
@Deprecated("")
@Deprecated("since 5.5.2")
class ElementFactory {


    /**
     * The font provider that will be used to fetch fonts.
     * @since    iText 5.0	This used to be a FontFactoryImp
     */
    /**
     * Getter for the font provider
     * @return provider
     * *
     * @since 5.0.6 renamed from getFontImp
     */
    /**
     * Setter for the font provider
     * @param provider
     * *
     * @since    5.0.6 renamed from setFontImp
     */
    var fontProvider: FontProvider = FontFactory.getFontImp()

    /**
     * Creates a Font object based on a chain of properties.
     * @param    chain    chain of properties
     * *
     * @return    an iText Font object
     */
    fun getFont(chain: ChainedProperties): Font {

        // [1] font name

        var face = chain.getProperty(HtmlTags.FACE)
        // try again, under the CSS key.
        //ISSUE: If both are present, we always go with face, even if font-family was
        //  defined more recently in our ChainedProperties.  One solution would go like this:
        //    Map all our supported style attributes to the 'normal' tag name, so we could
        //    look everything up under that one tag, retrieving the most current value.
        if (face == null || face.trim { it <= ' ' }.length == 0) {
            face = chain.getProperty(HtmlTags.FONTFAMILY)
        }
        // if the font consists of a comma separated list,
        // take the first font that is registered
        if (face != null) {
            val tok = StringTokenizer(face, ",")
            while (tok.hasMoreTokens()) {
                face = tok.nextToken().trim { it <= ' ' }
                if (face.startsWith("\""))
                    face = face.substring(1)
                if (face.endsWith("\""))
                    face = face.substring(0, face.length - 1)
                if (fontProvider.isRegistered(face))
                    break
            }
        }

        // [2] encoding
        var encoding = chain.getProperty(HtmlTags.ENCODING)
        if (encoding == null)
            encoding = BaseFont.WINANSI

        // [3] embedded

        // [4] font size
        val value = chain.getProperty(HtmlTags.SIZE)
        var size = 12f
        if (value != null)
            size = java.lang.Float.parseFloat(value)

        // [5] font style
        var style = 0

        // text-decoration
        val decoration = chain.getProperty(HtmlTags.TEXTDECORATION)
        if (decoration != null && decoration.trim { it <= ' ' }.length != 0) {
            if (HtmlTags.UNDERLINE == decoration) {
                style = style or Font.UNDERLINE
            } else if (HtmlTags.LINETHROUGH == decoration) {
                style = style or Font.STRIKETHRU
            }
        }
        // italic
        if (chain.hasProperty(HtmlTags.I))
            style = style or Font.ITALIC
        // bold
        if (chain.hasProperty(HtmlTags.B))
            style = style or Font.BOLD
        // underline
        if (chain.hasProperty(HtmlTags.U))
            style = style or Font.UNDERLINE
        // strikethru
        if (chain.hasProperty(HtmlTags.S))
            style = style or Font.STRIKETHRU

        // [6] Color
        val color = HtmlUtilities.decodeColor(chain.getProperty(HtmlTags.COLOR))

        // Get the font object from the provider
        return fontProvider.getFont(face, encoding, true, size, style, color)
    }


    /**
     * Creates an iText Chunk
     * @param content the content of the Chunk
     * *
     * @param chain the hierarchy chain
     * *
     * @return a Chunk
     */
    fun createChunk(content: String, chain: ChainedProperties): Chunk {
        val font = getFont(chain)
        val ck = Chunk(content, font)
        if (chain.hasProperty(HtmlTags.SUB))
            ck.textRise = -font.size / 2
        else if (chain.hasProperty(HtmlTags.SUP))
            ck.textRise = font.size / 2
        ck.hyphenation = getHyphenation(chain)
        return ck
    }

    /**
     * Creates an iText Paragraph object using the properties
     * of the different tags and properties in the hierarchy chain.
     * @param    chain    the hierarchy chain
     * *
     * @return    a Paragraph without any content
     */
    fun createParagraph(chain: ChainedProperties): Paragraph {
        val paragraph = Paragraph()
        updateElement(paragraph, chain)
        return paragraph
    }

    /**
     * Creates an iText Paragraph object using the properties
     * of the different tags and properties in the hierarchy chain.
     * @param    chain    the hierarchy chain
     * *
     * @return    a ListItem without any content
     */
    fun createListItem(chain: ChainedProperties): ListItem {
        val item = ListItem()
        updateElement(item, chain)
        return item
    }

    /**
     * Method that does the actual Element creating for
     * the createParagraph and createListItem method.
     * @param paragraph
     * *
     * @param chain
     */
    protected fun updateElement(paragraph: Paragraph, chain: ChainedProperties) {
        // Alignment
        var value = chain.getProperty(HtmlTags.ALIGN)
        paragraph.alignment = HtmlUtilities.alignmentValue(value)
        // hyphenation
        paragraph.hyphenation = getHyphenation(chain)
        // leading
        setParagraphLeading(paragraph, chain.getProperty(HtmlTags.LEADING))
        // spacing before
        value = chain.getProperty(HtmlTags.AFTER)
        if (value != null) {
            try {
                paragraph.setSpacingBefore(java.lang.Float.parseFloat(value))
            } catch (e: Exception) {
            }

        }
        // spacing after
        value = chain.getProperty(HtmlTags.AFTER)
        if (value != null) {
            try {
                paragraph.setSpacingAfter(java.lang.Float.parseFloat(value))
            } catch (e: Exception) {
            }

        }
        // extra paragraph space
        value = chain.getProperty(HtmlTags.EXTRAPARASPACE)
        if (value != null) {
            try {
                paragraph.extraParagraphSpace = java.lang.Float.parseFloat(value)
            } catch (e: Exception) {
            }

        }
        // indentation
        value = chain.getProperty(HtmlTags.INDENT)
        if (value != null) {
            try {
                paragraph.setIndentationLeft(java.lang.Float.parseFloat(value))
            } catch (e: Exception) {
            }

        }
    }


    /**
     * Gets a HyphenationEvent based on the hyphenation entry in
     * the hierarchy chain.
     * @param    chain    the hierarchy chain
     * *
     * @return    a HyphenationEvent
     * *
     * @since    2.1.2
     */
    fun getHyphenation(chain: ChainedProperties): HyphenationEvent? {
        var value = chain.getProperty(HtmlTags.HYPHENATION)
        // no hyphenation defined
        if (value == null || value.length == 0) {
            return null
        }
        // language code only
        var pos = value.indexOf('_')
        if (pos == -1) {
            return HyphenationAuto(value, null, 2, 2)
        }
        // language and country code
        val lang = value.substring(0, pos)
        var country = value.substring(pos + 1)
        // no leftMin or rightMin
        pos = country.indexOf(',')
        if (pos == -1) {
            return HyphenationAuto(lang, country, 2, 2)
        }
        // leftMin and rightMin value
        val leftMin: Int
        var rightMin = 2
        value = country.substring(pos + 1)
        country = country.substring(0, pos)
        pos = value.indexOf(',')
        if (pos == -1) {
            leftMin = Integer.parseInt(value)
        } else {
            leftMin = Integer.parseInt(value.substring(0, pos))
            rightMin = Integer.parseInt(value.substring(pos + 1))
        }
        return HyphenationAuto(lang, country, leftMin, rightMin)
    }

    /**
     * Creates a LineSeparator.
     * @param attrs the attributes
     * *
     * @param offset
     * *
     * @return a LineSeparator
     * *
     * @since 5.0.6
     */
    fun createLineSeparator(attrs: Map<String, String>, offset: Float): LineSeparator {
        // line thickness
        var lineWidth = 1f
        val size = attrs[HtmlTags.SIZE]
        if (size != null) {
            val tmpSize = HtmlUtilities.parseLength(size, HtmlUtilities.DEFAULT_FONT_SIZE)
            if (tmpSize > 0)
                lineWidth = tmpSize
        }
        // width percentage
        val width = attrs[HtmlTags.WIDTH]
        var percentage = 100f
        if (width != null) {
            val tmpWidth = HtmlUtilities.parseLength(width, HtmlUtilities.DEFAULT_FONT_SIZE)
            if (tmpWidth > 0) percentage = tmpWidth
            if (!width.endsWith("%"))
                percentage = 100f // Treat a pixel width as 100% for now.
        }
        // line color
        val lineColor: BaseColor? = null
        // alignment
        val align = HtmlUtilities.alignmentValue(attrs[HtmlTags.ALIGN])
        return LineSeparator(lineWidth, percentage, lineColor, align, offset)
    }

    /**
     * @param src
     * *
     * @param attrs
     * *
     * @param chain
     * *
     * @param document
     * *
     * @param img_provider
     * *
     * @param img_store
     * *
     * @param img_baseurl
     * *
     * @return the Image
     * *
     * @throws DocumentException
     * *
     * @throws IOException
     */
    @Throws(DocumentException::class, IOException::class)
    fun createImage(
            src: String,
            attrs: Map<String, String>,
            chain: ChainedProperties,
            document: DocListener,
            img_provider: ImageProvider?,
            img_store: HashMap<String, Image>?,
            img_baseurl: String?): Image? {
        var src = src
        var img: Image? = null
        // getting the image using an image provider
        if (img_provider != null)
            img = img_provider.getImage(src, attrs, chain, document)
        // getting the image from an image store
        if (img == null && img_store != null) {
            val tim = img_store[src]
            if (tim != null)
                img = Image.getInstance(tim)
        }
        if (img != null)
            return img
        // introducing a base url
        // relative src references only
        if (!src.startsWith("http") && img_baseurl != null) {
            src = img_baseurl + src
        } else if (img == null && !src.startsWith("http")) {
            var path = chain.getProperty(HtmlTags.IMAGEPATH)
            if (path == null)
                path = ""
            src = File(path, src).path
        }
        img = Image.getInstance(src)
        if (img == null)
            return null

        var actualFontSize = HtmlUtilities.parseLength(
                chain.getProperty(HtmlTags.SIZE),
                HtmlUtilities.DEFAULT_FONT_SIZE)
        if (actualFontSize <= 0f)
            actualFontSize = HtmlUtilities.DEFAULT_FONT_SIZE
        val width = attrs[HtmlTags.WIDTH]
        var widthInPoints = HtmlUtilities.parseLength(width, actualFontSize)
        val height = attrs[HtmlTags.HEIGHT]
        var heightInPoints = HtmlUtilities.parseLength(height, actualFontSize)
        if (widthInPoints > 0 && heightInPoints > 0) {
            img.scaleAbsolute(widthInPoints, heightInPoints)
        } else if (widthInPoints > 0) {
            heightInPoints = img.height * widthInPoints / img.width
            img.scaleAbsolute(widthInPoints, heightInPoints)
        } else if (heightInPoints > 0) {
            widthInPoints = img.width * heightInPoints / img.height
            img.scaleAbsolute(widthInPoints, heightInPoints)
        }

        val before = chain.getProperty(HtmlTags.BEFORE)
        if (before != null)
            img.setSpacingBefore(java.lang.Float.parseFloat(before))
        val after = chain.getProperty(HtmlTags.AFTER)
        if (after != null)
            img.setSpacingAfter(java.lang.Float.parseFloat(after))
        img.widthPercentage = 0f
        return img
    }

    /**
     * @param tag
     * *
     * @param chain
     * *
     * @return the List
     */
    fun createList(tag: String, chain: ChainedProperties): List {
        val list: List
        if (HtmlTags.UL.equals(tag, ignoreCase = true)) {
            list = List(List.UNORDERED)
            list.setListSymbol("\u2022 ")
        } else {
            list = List(List.ORDERED)
        }
        try {
            list.setIndentationLeft(Float(chain.getProperty(HtmlTags.INDENT)).toFloat())
        } catch (e: Exception) {
            list.isAutoindent = true
        }

        return list
    }

    companion object {

        /**
         * Sets the leading of a Paragraph object.
         * @param    paragraph    the Paragraph for which we set the leading
         * *
         * @param    leading        the String value of the leading
         */
        protected fun setParagraphLeading(paragraph: Paragraph, leading: String?) {
            // default leading
            if (leading == null) {
                paragraph.setLeading(0f, 1.5f)
                return
            }
            try {
                val tk = StringTokenizer(leading, " ,")
                // absolute leading
                var v = tk.nextToken()
                val v1 = java.lang.Float.parseFloat(v)
                if (!tk.hasMoreTokens()) {
                    paragraph.setLeading(v1, 0f)
                    return
                }
                // relative leading
                v = tk.nextToken()
                val v2 = java.lang.Float.parseFloat(v)
                paragraph.setLeading(v1, v2)
            } catch (e: Exception) {
                // default leading
                paragraph.setLeading(0f, 1.5f)
            }

        }
    }
}
/**
 * Creates a new instance of FactoryProperties.
 */

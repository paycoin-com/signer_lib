/*
 * $Id: af9343d9f0f2940aaa146262e0ff296beec5de9b $
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

import java.util.HashMap
import java.util.Properties

import com.itextpdf.text.BaseColor
import com.itextpdf.text.html.HtmlTags
import com.itextpdf.text.html.HtmlUtilities

/**
 * Old class to define styles for HTMLWorker.
 * We've completely rewritten HTML to PDF functionality; see project XML Worker.
 * XML Worker is able to parse CSS files and "style" attribute values.
 */
@Deprecated("")
@Deprecated("since 5.5.2")
class StyleSheet {

    /**
     * Map storing tags and their corresponding styles.
     * @since 5.0.6 (changed HashMap => Map)
     */
    protected var tagMap: MutableMap<String, Map<String, String>> = HashMap()

    /**
     * Map storing possible names of the "class" attribute
     * and their corresponding styles.
     * @since 5.0.6 (changed HashMap => Map)
     */
    protected var classMap: MutableMap<String, Map<String, String>> = HashMap()

    /**
     * Associates a Map containing styles with a tag.
     * @param    tag        the name of the HTML/XML tag
     * *
     * @param    attrs    a map containing styles
     */
    fun loadTagStyle(tag: String, attrs: Map<String, String>) {
        tagMap.put(tag.toLowerCase(), attrs)
    }

    /**
     * Adds an extra style key-value pair to the styles Map
     * of a specific tag
     * @param    tag        the name of the HTML/XML tag
     * *
     * @param    key        the key specifying a specific style
     * *
     * @param    value    the value defining the style
     */
    fun loadTagStyle(tag: String, key: String, value: String) {
        var tag = tag
        tag = tag.toLowerCase()
        var styles: MutableMap<String, String>? = tagMap[tag]
        if (styles == null) {
            styles = HashMap<String, String>()
            tagMap.put(tag, styles)
        }
        styles.put(key, value)
    }

    /**
     * Associates a Map containing styles with a class name.
     * @param    className    the value of the class attribute
     * *
     * @param    attrs        a map containing styles
     */
    fun loadStyle(className: String, attrs: HashMap<String, String>) {
        classMap.put(className.toLowerCase(), attrs)
    }

    /**
     * Adds an extra style key-value pair to the styles Map
     * of a specific tag
     * @param    className    the name of the HTML/XML tag
     * *
     * @param    key            the key specifying a specific style
     * *
     * @param    value        the value defining the style
     */
    fun loadStyle(className: String, key: String, value: String) {
        var className = className
        className = className.toLowerCase()
        var styles: MutableMap<String, String>? = classMap[className]
        if (styles == null) {
            styles = HashMap<String, String>()
            classMap.put(className, styles)
        }
        styles.put(key, value)
    }

    /**
     * Resolves the styles based on the tag name and the value
     * of the class attribute.
     * @param    tag        the tag that needs to be resolved
     * *
     * @param    attrs    existing style map that will be updated
     */
    fun applyStyle(tag: String, attrs: MutableMap<String, String>) {
        // first fetch the styles corresponding with the tag name
        var map: Map<String, String>? = tagMap[tag.toLowerCase()]
        if (map != null) {
            // create a new map with properties
            val temp = HashMap(map)
            // override with the existing properties
            temp.putAll(attrs)
            // update the existing properties
            attrs.putAll(temp)
        }
        // look for the class attribute
        val cm = attrs[HtmlTags.CLASS] ?: return
        // fetch the styles corresponding with the class attribute
        map = classMap[cm.toLowerCase()]
        if (map == null)
            return
        // remove the class attribute from the properties
        attrs.remove(HtmlTags.CLASS)
        // create a map with the styles corresponding with the class value
        val temp = HashMap(map)
        // override with the existing properties
        temp.putAll(attrs)
        // update the properties
        attrs.putAll(temp)
    }

    companion object {

        /**
         * Method contributed by Lubos Strapko
         * @param h
         * *
         * @param chain
         * *
         * @since 2.1.3
         */
        fun resolveStyleAttribute(h: MutableMap<String, String>, chain: ChainedProperties) {
            val style = h[HtmlTags.STYLE] ?: return
            val prop = HtmlUtilities.parseAttributes(style)
            for (element in prop.keys) {
                val key = element as String
                if (key == HtmlTags.FONTFAMILY) {
                    h.put(HtmlTags.FACE, prop.getProperty(key))
                } else if (key == HtmlTags.FONTSIZE) {
                    var actualFontSize = HtmlUtilities.parseLength(chain.getProperty(HtmlTags.SIZE),
                            HtmlUtilities.DEFAULT_FONT_SIZE)
                    if (actualFontSize <= 0f)
                        actualFontSize = HtmlUtilities.DEFAULT_FONT_SIZE
                    h.put(HtmlTags.SIZE, java.lang.Float.toString(HtmlUtilities.parseLength(prop.getProperty(key), actualFontSize)) + "pt")
                } else if (key == HtmlTags.FONTSTYLE) {
                    val ss = prop.getProperty(key).trim { it <= ' ' }.toLowerCase()
                    if (ss == HtmlTags.ITALIC || ss == HtmlTags.OBLIQUE)
                        h.put(HtmlTags.I, null)
                } else if (key == HtmlTags.FONTWEIGHT) {
                    val ss = prop.getProperty(key).trim { it <= ' ' }.toLowerCase()
                    if (ss == HtmlTags.BOLD || ss == "700" || ss == "800"
                            || ss == "900")
                        h.put(HtmlTags.B, null)
                } else if (key == HtmlTags.TEXTDECORATION) {
                    val ss = prop.getProperty(key).trim { it <= ' ' }.toLowerCase()
                    if (ss == HtmlTags.UNDERLINE)
                        h.put(HtmlTags.U, null)
                } else if (key == HtmlTags.COLOR) {
                    val c = HtmlUtilities.decodeColor(prop.getProperty(key))
                    if (c != null) {
                        val hh = c.rgb
                        var hs = Integer.toHexString(hh)
                        hs = "000000" + hs
                        hs = "#" + hs.substring(hs.length - 6)
                        h.put(HtmlTags.COLOR, hs)
                    }
                } else if (key == HtmlTags.LINEHEIGHT) {
                    val ss = prop.getProperty(key).trim { it <= ' ' }
                    var actualFontSize = HtmlUtilities.parseLength(chain.getProperty(HtmlTags.SIZE),
                            HtmlUtilities.DEFAULT_FONT_SIZE)
                    if (actualFontSize <= 0f)
                        actualFontSize = HtmlUtilities.DEFAULT_FONT_SIZE
                    val v = HtmlUtilities.parseLength(prop.getProperty(key),
                            actualFontSize)
                    if (ss.endsWith("%")) {
                        h.put(HtmlTags.LEADING, "0," + v / 100)
                        return
                    }
                    if (HtmlTags.NORMAL.equals(ss, ignoreCase = true)) {
                        h.put(HtmlTags.LEADING, "0,1.5")
                        return
                    }
                    h.put(HtmlTags.LEADING, v + ",0")
                } else if (key == HtmlTags.TEXTALIGN) {
                    val ss = prop.getProperty(key).trim { it <= ' ' }.toLowerCase()
                    h.put(HtmlTags.ALIGN, ss)
                } else if (key == HtmlTags.PADDINGLEFT) {
                    val ss = prop.getProperty(key).trim { it <= ' ' }.toLowerCase()
                    h.put(HtmlTags.INDENT, java.lang.Float.toString(HtmlUtilities.parseLength(ss)))
                }
            }
        }
    }
}
/**
 * Creates a new instance of StyleSheet
 */

/*
 * $Id: c7a33add4f1fc8dc4caa96843c6ee2fd9f47b553 $
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
import com.itextpdf.text.Utilities
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.io.StreamUtil
import com.itextpdf.text.pdf.fonts.cmaps.CMapCache
import com.itextpdf.text.pdf.fonts.cmaps.CMapCidByte
import com.itextpdf.text.pdf.fonts.cmaps.CMapCidUni
import com.itextpdf.text.pdf.fonts.cmaps.CMapUniCid

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Creates a CJK font compatible with the fonts in the Adobe Asian font Pack.

 * @author  Paulo Soares
 */

internal class CJKFont
/** Creates a CJK font.
 * @param fontName the name of the font
 * *
 * @param enc the encoding of the font
 * *
 * @param emb always false. CJK font and not embedded
 * *
 * @throws DocumentException on error
 */
@Throws(DocumentException::class)
constructor(fontName: String,
            /** The CMap name associated with this font  */
            private val CMap: String, emb: Boolean) : BaseFont() {
    private var cidByte: CMapCidByte? = null
    private var uniCid: CMapUniCid? = null
    private var cidUni: CMapCidUni? = null
    var uniMap:

            String? = null
        private set

    /** The font name  */
    /**
     * Sets the font name that will appear in the pdf font dictionary.
     * Use with care as it can easily make a font unreadable if not embedded.
     * @param name the new font name
     */
    override var postscriptFontName: String? = null
    /** The style modifier  */
    private var style = ""

    var isIdentity = false
        private set

    //private char[] translationMap;
    private var vMetrics: IntHashtable? = null
    private var hMetrics: IntHashtable? = null
    private var fontDesc: HashMap<String, Any>? = null

    init {
        var fontName = fontName
        loadProperties()
        fontType = BaseFont.FONT_TYPE_CJK
        val nameBase = getBaseName(fontName)
        if (!isCJKFont(nameBase, CMap))
            throw DocumentException(MessageLocalization.getComposedMessage("font.1.with.2.encoding.is.not.a.cjk.font", fontName, CMap))
        if (nameBase.length < fontName.length) {
            style = fontName.substring(nameBase.length)
            fontName = nameBase
        }
        this.postscriptFontName = fontName
        encoding = CJK_ENCODING
        vertical = CMap.endsWith("V")
        if (CMap == BaseFont.IDENTITY_H || CMap == BaseFont.IDENTITY_V)
            isIdentity = true
        loadCMaps()
    }

    @Throws(DocumentException::class)
    private fun loadCMaps() {
        try {
            fontDesc = allFonts[postscriptFontName]
            hMetrics = fontDesc!!["W"] as IntHashtable
            vMetrics = fontDesc!!["W2"] as IntHashtable
            val registry = fontDesc!!["Registry"] as String
            uniMap = ""
            for (name in registryNames[registry + "_Uni"]) {
                uniMap = name
                if (name.endsWith("V") && vertical)
                    break
                if (!name.endsWith("V") && !vertical)
                    break
            }
            if (isIdentity) {
                cidUni = CMapCache.getCachedCMapCidUni(uniMap)
            } else {
                uniCid = CMapCache.getCachedCMapUniCid(uniMap)
                cidByte = CMapCache.getCachedCMapCidByte(CMap)
            }
        } catch (ex: Exception) {
            throw DocumentException(ex)
        }

    }

    /**
     * Gets the width of a char in normalized 1000 units.
     * @param char1 the unicode char to get the width of
     * *
     * @return the width in normalized 1000 units
     */
    override fun getWidth(char1: Int): Int {
        var c = char1
        if (!isIdentity)
            c = uniCid!!.lookup(char1)
        val v: Int
        if (vertical)
            v = vMetrics!!.get(c)
        else
            v = hMetrics!!.get(c)
        if (v > 0)
            return v
        else
            return 1000
    }

    override fun getWidth(text: String): Int {
        var total = 0
        if (isIdentity) {
            for (k in 0..text.length - 1) {
                total += getWidth(text[k].toInt())
            }
        } else {
            var k = 0
            while (k < text.length) {
                val `val`: Int
                if (Utilities.isSurrogatePair(text, k)) {
                    `val` = Utilities.convertToUtf32(text, k)
                    k++
                } else {
                    `val` = text[k].toInt()
                }
                total += getWidth(`val`)
                ++k
            }
        }
        return total
    }

    fun getRawWidth(c: Int, name: String): Int {
        return 0
    }

    override fun getKerning(char1: Int, char2: Int): Int {
        return 0
    }

    private val fontDescriptor: PdfDictionary
        get() {
            val dic = PdfDictionary(PdfName.FONTDESCRIPTOR)
            dic.put(PdfName.ASCENT, PdfLiteral(fontDesc!!["Ascent"] as String))
            dic.put(PdfName.CAPHEIGHT, PdfLiteral(fontDesc!!["CapHeight"] as String))
            dic.put(PdfName.DESCENT, PdfLiteral(fontDesc!!["Descent"] as String))
            dic.put(PdfName.FLAGS, PdfLiteral(fontDesc!!["Flags"] as String))
            dic.put(PdfName.FONTBBOX, PdfLiteral(fontDesc!!["FontBBox"] as String))
            dic.put(PdfName.FONTNAME, PdfName(postscriptFontName!! + style))
            dic.put(PdfName.ITALICANGLE, PdfLiteral(fontDesc!!["ItalicAngle"] as String))
            dic.put(PdfName.STEMV, PdfLiteral(fontDesc!!["StemV"] as String))
            val pdic = PdfDictionary()
            pdic.put(PdfName.PANOSE, PdfString(fontDesc!!["Panose"] as String, null))
            dic.put(PdfName.STYLE, pdic)
            return dic
        }

    private fun getCIDFont(fontDescriptor: PdfIndirectReference, cjkTag: IntHashtable): PdfDictionary {
        val dic = PdfDictionary(PdfName.FONT)
        dic.put(PdfName.SUBTYPE, PdfName.CIDFONTTYPE0)
        dic.put(PdfName.BASEFONT, PdfName(postscriptFontName!! + style))
        dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor)
        val keys = cjkTag.toOrderedKeys()
        var w = convertToHCIDMetrics(keys, hMetrics)
        if (w != null)
            dic.put(PdfName.W, PdfLiteral(w))
        if (vertical) {
            w = convertToVCIDMetrics(keys, vMetrics, hMetrics)
            if (w != null)
                dic.put(PdfName.W2, PdfLiteral(w))
        } else
            dic.put(PdfName.DW, PdfNumber(1000))
        val cdic = PdfDictionary()
        if (isIdentity) {
            cdic.put(PdfName.REGISTRY, PdfString(cidUni!!.registry, null))
            cdic.put(PdfName.ORDERING, PdfString(cidUni!!.ordering, null))
            cdic.put(PdfName.SUPPLEMENT, PdfNumber(cidUni!!.supplement))
        } else {
            cdic.put(PdfName.REGISTRY, PdfString(cidByte!!.registry, null))
            cdic.put(PdfName.ORDERING, PdfString(cidByte!!.ordering, null))
            cdic.put(PdfName.SUPPLEMENT, PdfNumber(cidByte!!.supplement))
        }
        dic.put(PdfName.CIDSYSTEMINFO, cdic)
        return dic
    }

    private fun getFontBaseType(CIDFont: PdfIndirectReference): PdfDictionary {
        val dic = PdfDictionary(PdfName.FONT)
        dic.put(PdfName.SUBTYPE, PdfName.TYPE0)
        var name: String = postscriptFontName
        if (style.length > 0)
            name += "-" + style.substring(1)
        name += "-" + CMap
        dic.put(PdfName.BASEFONT, PdfName(name))
        dic.put(PdfName.ENCODING, PdfName(CMap))
        dic.put(PdfName.DESCENDANTFONTS, PdfArray(CIDFont))
        return dic
    }

    @Throws(DocumentException::class, IOException::class)
    fun writeFont(writer: PdfWriter, ref: PdfIndirectReference, params: Array<Any>) {
        val cjkTag = params[0] as IntHashtable
        var ind_font: PdfIndirectReference? = null
        var pobj: PdfObject? = null
        var obj: PdfIndirectObject? = null
        pobj = fontDescriptor
        if (pobj != null) {
            obj = writer.addToBody(pobj)
            ind_font = obj!!.indirectReference
        }
        pobj = getCIDFont(ind_font, cjkTag)
        if (pobj != null) {
            obj = writer.addToBody(pobj)
            ind_font = obj!!.indirectReference
        }
        pobj = getFontBaseType(ind_font)
        writer.addToBody(pobj, ref)
    }

    /**
     * You can't get the FontStream of a CJK font (CJK fonts are never embedded),
     * so this method always returns null.
     * @return    null
     * *
     * @since    2.1.3
     */
    val fullFontStream: PdfStream?
        get() = null

    private fun getDescNumber(name: String): Float {
        return Integer.parseInt(fontDesc!![name] as String).toFloat()
    }

    private fun getBBox(idx: Int): Float {
        val s = fontDesc!!["FontBBox"] as String
        val tk = StringTokenizer(s, " []\r\n\t\f")
        var ret = tk.nextToken()
        for (k in 0..idx - 1)
            ret = tk.nextToken()
        return Integer.parseInt(ret).toFloat()
    }

    /** Gets the font parameter identified by key. Valid values
     * for key are ASCENT, CAPHEIGHT, DESCENT
     * and ITALICANGLE.
     * @param key the parameter to be extracted
     * *
     * @param fontSize the font size in points
     * *
     * @return the parameter in points
     */
    override fun getFontDescriptor(key: Int, fontSize: Float): Float {
        when (key) {
            BaseFont.AWT_ASCENT, BaseFont.ASCENT -> return getDescNumber("Ascent") * fontSize / 1000
            BaseFont.CAPHEIGHT -> return getDescNumber("CapHeight") * fontSize / 1000
            BaseFont.AWT_DESCENT, BaseFont.DESCENT -> return getDescNumber("Descent") * fontSize / 1000
            BaseFont.ITALICANGLE -> return getDescNumber("ItalicAngle")
            BaseFont.BBOXLLX -> return fontSize * getBBox(0) / 1000
            BaseFont.BBOXLLY -> return fontSize * getBBox(1) / 1000
            BaseFont.BBOXURX -> return fontSize * getBBox(2) / 1000
            BaseFont.BBOXURY -> return fontSize * getBBox(3) / 1000
            BaseFont.AWT_LEADING -> return 0f
            BaseFont.AWT_MAXADVANCE -> return fontSize * (getBBox(2) - getBBox(0)) / 1000
        }
        return 0f
    }

    /** Gets the full name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.
     * @return the full name of the font
     */
    override val fullFontName: Array<Array<String>>
        get() = arrayOf(arrayOf<String>("", "", "", postscriptFontName))

    /** Gets all the entries of the names-table. If it is a True Type font
     * each array element will have {Name ID, Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"4", "", "", "",
     * font name}.
     * @return the full name of the font
     */
    override val allNameEntries: Array<Array<String>>
        get() = arrayOf(arrayOf<String>("4", "", "", "", postscriptFontName))

    /** Gets the family name of the font. If it is a True Type font
     * each array element will have {Platform ID, Platform Encoding ID,
     * Language ID, font name}. The interpretation of this values can be
     * found in the Open Type specification, chapter 2, in the 'name' table.
     * For the other fonts the array has a single element with {"", "", "",
     * font name}.
     * @return the family name of the font
     */
    override val familyFontName: Array<Array<String>>
        get() = fullFontName

    override fun getUnicodeEquivalent(c: Int): Int {
        if (isIdentity) {
            if (c == BaseFont.CID_NEWLINE.toInt())
                return '\n'
            return cidUni!!.lookup(c)
        }
        return c
    }

    override fun getCidCode(c: Int): Int {
        if (isIdentity)
            return c
        return uniCid!!.lookup(c)
    }

    /** Checks if the font has any kerning pairs.
     * @return always false
     */
    override fun hasKernPairs(): Boolean {
        return false
    }

    /**
     * Checks if a character exists in this font.
     * @param c the character to check
     * *
     * @return true if the character has a glyph,
     * * false otherwise
     */
    override fun charExists(c: Int): Boolean {
        if (isIdentity)
            return true
        return cidByte!!.lookup(uniCid!!.lookup(c)).size > 0
    }

    /**
     * Sets the character advance.
     * @param c the character
     * *
     * @param advance the character advance normalized to 1000 units
     * *
     * @return true if the advance was set,
     * * false otherwise. Will always return false
     */
    override fun setCharAdvance(c: Int, advance: Int): Boolean {
        return false
    }

    override fun setKerning(char1: Int, char2: Int, kern: Int): Boolean {
        return false
    }

    override fun getCharBBox(c: Int): IntArray? {
        return null
    }

    override fun getRawCharBBox(c: Int, name: String?): IntArray {
        return null
    }

    /**
     * Converts a String to a byte array according
     * to the font's encoding.
     * @param text the String to be converted
     * *
     * @return an array of byte representing the conversion according to the font's encoding
     */
    override fun convertToBytes(text: String): ByteArray {
        if (isIdentity)
            return super.convertToBytes(text)
        try {
            if (text.length == 1)
                return convertToBytes(text[0].toInt())
            val bout = ByteArrayOutputStream()
            var k = 0
            while (k < text.length) {
                val `val`: Int
                if (Utilities.isSurrogatePair(text, k)) {
                    `val` = Utilities.convertToUtf32(text, k)
                    k++
                } else {
                    `val` = text[k].toInt()
                }
                bout.write(convertToBytes(`val`))
                ++k
            }
            return bout.toByteArray()
        } catch (ex: Exception) {
            throw ExceptionConverter(ex)
        }

    }

    /**
     * Converts a char to a byte array according
     * to the font's encoding.
     * @param char1 the char to be converted
     * *
     * @return an array of byte representing the conversion according to the font's encoding
     */
    fun convertToBytes(char1: Int): ByteArray {
        if (isIdentity)
            return super.convertToBytes(char1)
        return cidByte!!.lookup(uniCid!!.lookup(char1))
    }

    companion object {
        /** The encoding used in the PDF document for CJK fonts
         */
        val CJK_ENCODING = "UnicodeBigUnmarked"
        private val FIRST = 0
        private val BRACKET = 1
        private val SERIAL = 2
        private val V1Y = 880

        var cjkFonts = Properties()
        var cjkEncodings = Properties()
        private val allFonts = HashMap<String, HashMap<String, Any>>()
        private var propertiesLoaded = false

        /** The path to the font resources.  */
        val RESOURCE_PATH_CMAP = BaseFont.RESOURCE_PATH + "cmaps/"
        private val registryNames = HashMap<String, Set<String>>()

        private fun loadProperties() {
            if (propertiesLoaded)
                return
            synchronized (allFonts) {
                if (propertiesLoaded)
                    return
                try {
                    loadRegistry()
                    for (font in registryNames["fonts"]) {
                        allFonts.put(font, readFontProperties(font))
                    }
                } catch (e: Exception) {
                }

                propertiesLoaded = true
            }
        }

        @Throws(IOException::class)
        private fun loadRegistry() {
            val `is` = StreamUtil.getResourceStream(RESOURCE_PATH_CMAP + "cjk_registry.properties")
            val p = Properties()
            p.load(`is`)
            `is`.close()
            for (key in p.keys) {
                val value = p.getProperty(key as String)
                val sp = value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val hs = HashSet<String>()
                for (s in sp) {
                    if (s.length > 0)
                        hs.add(s)
                }
                registryNames.put(key, hs)
            }

        }

        /**
         * Returns a font compatible with a CJK encoding or null if not found.
         * @param enc
         * *
         * @return
         */
        fun GetCompatibleFont(enc: String): String? {
            loadProperties()
            var registry: String? = null
            for (e in registryNames.entries) {
                if (e.value.contains(enc)) {
                    registry = e.key
                    for (e1 in allFonts.entries) {
                        if (registry == e1.value["Registry"])
                            return e1.key
                    }
                }
            }
            return null
        }

        /** Checks if its a valid CJK font.
         * @param fontName the font name
         * *
         * @param enc the encoding
         * *
         * @return true if it is CJK font
         */
        fun isCJKFont(fontName: String, enc: String): Boolean {
            loadProperties()
            if (!registryNames.containsKey("fonts"))
                return false
            if (!registryNames["fonts"].contains(fontName))
                return false
            if (enc == BaseFont.IDENTITY_H || enc == BaseFont.IDENTITY_V)
                return true
            val registry = allFonts[fontName]["Registry"] as String
            val encodings = registryNames[registry]
            return encodings != null && encodings.contains(enc)
        }

        //    static char[] readCMap(String name) {
        //        try {
        //            name = name + ".cmap";
        //            InputStream is = getResourceStream(RESOURCE_PATH + name);
        //            char c[] = new char[0x10000];
        //            for (int k = 0; k < 0x10000; ++k)
        //                c[k] = (char)((is.read() << 8) + is.read());
        //            is.close();
        //            return c;
        //        }
        //        catch (Exception e) {
        //            // empty on purpose
        //        }
        //        return null;
        //    }

        fun createMetric(s: String): IntHashtable {
            val h = IntHashtable()
            val tk = StringTokenizer(s)
            while (tk.hasMoreTokens()) {
                val n1 = Integer.parseInt(tk.nextToken())
                h.put(n1, Integer.parseInt(tk.nextToken()))
            }
            return h
        }

        fun convertToHCIDMetrics(keys: IntArray, h: IntHashtable): String? {
            if (keys.size == 0)
                return null
            var lastCid = 0
            var lastValue = 0
            var start: Int
            start = 0
            while (start < keys.size) {
                lastCid = keys[start]
                lastValue = h.get(lastCid)
                if (lastValue != 0) {
                    ++start
                    break
                }
                ++start
            }
            if (lastValue == 0)
                return null
            val buf = StringBuilder()
            buf.append('[')
            buf.append(lastCid)
            var state = FIRST
            for (k in start..keys.size - 1) {
                val cid = keys[k]
                val value = h.get(cid)
                if (value == 0)
                    continue
                when (state) {
                    FIRST -> {
                        if (cid == lastCid + 1 && value == lastValue) {
                            state = SERIAL
                        } else if (cid == lastCid + 1) {
                            state = BRACKET
                            buf.append('[').append(lastValue)
                        } else {
                            buf.append('[').append(lastValue).append(']').append(cid)
                        }
                    }
                    BRACKET -> {
                        if (cid == lastCid + 1 && value == lastValue) {
                            state = SERIAL
                            buf.append(']').append(lastCid)
                        } else if (cid == lastCid + 1) {
                            buf.append(' ').append(lastValue)
                        } else {
                            state = FIRST
                            buf.append(' ').append(lastValue).append(']').append(cid)
                        }
                    }
                    SERIAL -> {
                        if (cid != lastCid + 1 || value != lastValue) {
                            buf.append(' ').append(lastCid).append(' ').append(lastValue).append(' ').append(cid)
                            state = FIRST
                        }
                    }
                }
                lastValue = value
                lastCid = cid
            }
            when (state) {
                FIRST -> {
                    buf.append('[').append(lastValue).append("]]")
                }
                BRACKET -> {
                    buf.append(' ').append(lastValue).append("]]")
                }
                SERIAL -> {
                    buf.append(' ').append(lastCid).append(' ').append(lastValue).append(']')
                }
            }
            return buf.toString()
        }

        fun convertToVCIDMetrics(keys: IntArray, v: IntHashtable, h: IntHashtable): String? {
            if (keys.size == 0)
                return null
            var lastCid = 0
            var lastValue = 0
            var lastHValue = 0
            var start: Int
            start = 0
            while (start < keys.size) {
                lastCid = keys[start]
                lastValue = v.get(lastCid)
                if (lastValue != 0) {
                    ++start
                    break
                } else
                    lastHValue = h.get(lastCid)
                ++start
            }
            if (lastValue == 0)
                return null
            if (lastHValue == 0)
                lastHValue = 1000
            val buf = StringBuilder()
            buf.append('[')
            buf.append(lastCid)
            var state = FIRST
            for (k in start..keys.size - 1) {
                val cid = keys[k]
                val value = v.get(cid)
                if (value == 0)
                    continue
                var hValue = h.get(lastCid)
                if (hValue == 0)
                    hValue = 1000
                when (state) {
                    FIRST -> {
                        if (cid == lastCid + 1 && value == lastValue && hValue == lastHValue) {
                            state = SERIAL
                        } else {
                            buf.append(' ').append(lastCid).append(' ').append(-lastValue).append(' ').append(lastHValue / 2).append(' ').append(V1Y).append(' ').append(cid)
                        }
                    }
                    SERIAL -> {
                        if (cid != lastCid + 1 || value != lastValue || hValue != lastHValue) {
                            buf.append(' ').append(lastCid).append(' ').append(-lastValue).append(' ').append(lastHValue / 2).append(' ').append(V1Y).append(' ').append(cid)
                            state = FIRST
                        }
                    }
                }
                lastValue = value
                lastCid = cid
                lastHValue = hValue
            }
            buf.append(' ').append(lastCid).append(' ').append(-lastValue).append(' ').append(lastHValue / 2).append(' ').append(V1Y).append(" ]")
            return buf.toString()
        }

        @Throws(IOException::class)
        private fun readFontProperties(name: String): HashMap<String, Any> {
            var name = name
            name += ".properties"
            val `is` = StreamUtil.getResourceStream(RESOURCE_PATH_CMAP + name)
            val p = Properties()
            p.load(`is`)
            `is`.close()
            val W = createMetric(p.getProperty("W"))
            p.remove("W")
            val W2 = createMetric(p.getProperty("W2"))
            p.remove("W2")
            val map = HashMap<String, Any>()
            val e = p.keys()
            while (e.hasMoreElements()) {
                val obj = e.nextElement()
                map.put(obj as String, p.getProperty(obj))
            }
            map.put("W", W)
            map.put("W2", W2)
            return map
        }
    }
}

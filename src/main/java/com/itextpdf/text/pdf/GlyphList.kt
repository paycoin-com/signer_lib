/*
 * $Id: 8ee0775d64aca2f57f50d35b0c0e1cb18278c238 $
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

import com.itextpdf.text.io.StreamUtil
import com.itextpdf.text.pdf.fonts.FontsResourceAnchor

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.HashMap
import java.util.StringTokenizer

object GlyphList {
    private val unicode2names = HashMap<Int, String>()
    private val names2unicode = HashMap<String, IntArray>()

    init {
        var `is`: InputStream? = null
        try {
            `is` = StreamUtil.getResourceStream(BaseFont.RESOURCE_PATH + "glyphlist.txt", FontsResourceAnchor().javaClass.classLoader)
            if (`is` == null) {
                val msg = "glyphlist.txt not found as resource. (It must exist as resource in the package com.itextpdf.text.pdf.fonts)"
                throw Exception(msg)
            }
            val buf = ByteArray(1024)
            val out = ByteArrayOutputStream()
            while (true) {
                val size = `is`.read(buf)
                if (size < 0)
                    break
                out.write(buf, 0, size)
            }
            `is`.close()
            `is` = null
            val s = PdfEncodings.convertToString(out.toByteArray(), null)
            val tk = StringTokenizer(s, "\r\n")
            while (tk.hasMoreTokens()) {
                val line = tk.nextToken()
                if (line.startsWith("#"))
                    continue
                val t2 = StringTokenizer(line, " ;\r\n\t\f")
                var name: String? = null
                var hex: String? = null
                if (!t2.hasMoreTokens())
                    continue
                name = t2.nextToken()
                if (!t2.hasMoreTokens())
                    continue
                hex = t2.nextToken()
                val num = Integer.valueOf(hex, 16)
                unicode2names.put(num, name)
                names2unicode.put(name, intArrayOf(num!!.toInt()))
            }
        } catch (e: Exception) {
            System.err.println("glyphlist.txt loading error: " + e.message)
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: Exception) {
                    // empty on purpose
                }

            }
        }
    }

    fun nameToUnicode(name: String): IntArray {
        val v = names2unicode[name]
        if (v == null && name.length == 7 && name.toLowerCase().startsWith("uni")) {
            try {
                return intArrayOf(Integer.parseInt(name.substring(3), 16))
            } catch (ex: Exception) {
            }

        }
        return v
    }

    fun unicodeToName(num: Int): String {
        return unicode2names[Integer.valueOf(num)]
    }
}

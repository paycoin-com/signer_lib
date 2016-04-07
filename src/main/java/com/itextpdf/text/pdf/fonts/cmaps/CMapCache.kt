/*
 * $Id: 8c979c9a26cc491bba8d5c24c4a8223ef13711bc $
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
package com.itextpdf.text.pdf.fonts.cmaps

import java.io.IOException
import java.util.HashMap

/**

 * @author psoares
 */
object CMapCache {
    private val cacheUniCid = HashMap<String, CMapUniCid>()
    private val cacheCidUni = HashMap<String, CMapCidUni>()
    private val cacheCidByte = HashMap<String, CMapCidByte>()
    private val cacheByteCid = HashMap<String, CMapByteCid>()

    @Throws(IOException::class)
    fun getCachedCMapUniCid(name: String): CMapUniCid {
        var cmap: CMapUniCid? = null
        synchronized (cacheUniCid) {
            cmap = cacheUniCid[name]
        }
        if (cmap == null) {
            cmap = CMapUniCid()
            CMapParserEx.parseCid(name, cmap, CidResource())
            synchronized (cacheUniCid) {
                cacheUniCid.put(name, cmap)
            }
        }
        return cmap
    }

    @Throws(IOException::class)
    fun getCachedCMapCidUni(name: String): CMapCidUni {
        var cmap: CMapCidUni? = null
        synchronized (cacheCidUni) {
            cmap = cacheCidUni[name]
        }
        if (cmap == null) {
            cmap = CMapCidUni()
            CMapParserEx.parseCid(name, cmap, CidResource())
            synchronized (cacheCidUni) {
                cacheCidUni.put(name, cmap)
            }
        }
        return cmap
    }

    @Throws(IOException::class)
    fun getCachedCMapCidByte(name: String): CMapCidByte {
        var cmap: CMapCidByte? = null
        synchronized (cacheCidByte) {
            cmap = cacheCidByte[name]
        }
        if (cmap == null) {
            cmap = CMapCidByte()
            CMapParserEx.parseCid(name, cmap, CidResource())
            synchronized (cacheCidByte) {
                cacheCidByte.put(name, cmap)
            }
        }
        return cmap
    }

    @Throws(IOException::class)
    fun getCachedCMapByteCid(name: String): CMapByteCid {
        var cmap: CMapByteCid? = null
        synchronized (cacheByteCid) {
            cmap = cacheByteCid[name]
        }
        if (cmap == null) {
            cmap = CMapByteCid()
            CMapParserEx.parseCid(name, cmap, CidResource())
            synchronized (cacheByteCid) {
                cacheByteCid.put(name, cmap)
            }
        }
        return cmap
    }
}

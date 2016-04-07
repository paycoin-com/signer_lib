/*
 * $Id: 1a61efbff8d00d09b57c12ad063862871a04b02a $
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

import java.util.HashMap
import java.util.HashSet

internal class PageResources {

    protected var fontDictionary = PdfDictionary()
    protected var xObjectDictionary = PdfDictionary()
    protected var colorDictionary = PdfDictionary()
    protected var patternDictionary = PdfDictionary()
    protected var shadingDictionary = PdfDictionary()
    protected var extGStateDictionary = PdfDictionary()
    protected var propertyDictionary = PdfDictionary()
    protected var forbiddenNames: HashSet<PdfName>? = null
    protected var originalResources: PdfDictionary? = null
    protected var namePtr = intArrayOf(0)
    protected var usedNames: HashMap<PdfName, PdfName>

    fun setOriginalResources(resources: PdfDictionary?, newNamePtr: IntArray?) {
        if (newNamePtr != null)
            namePtr = newNamePtr
        forbiddenNames = HashSet<PdfName>()
        usedNames = HashMap<PdfName, PdfName>()
        if (resources == null)
            return
        originalResources = PdfDictionary()
        originalResources!!.merge(resources)
        for (element in resources.keys) {
            val sub = PdfReader.getPdfObject(resources.get(element))
            if (sub != null && sub.isDictionary) {
                val dic = sub as PdfDictionary?
                for (element2 in dic.keys) {
                    forbiddenNames!!.add(element2)
                }
                val dic2 = PdfDictionary()
                dic2.merge(dic)
                originalResources!!.put(element, dic2)
            }
        }
    }

    fun translateName(name: PdfName): PdfName {
        var translated: PdfName? = name
        if (forbiddenNames != null) {
            translated = usedNames[name]
            if (translated == null) {
                while (true) {
                    translated = PdfName("Xi" + namePtr[0]++)
                    if (!forbiddenNames!!.contains(translated))
                        break
                }
                usedNames.put(name, translated)
            }
        }
        return translated
    }

    fun addFont(name: PdfName, reference: PdfIndirectReference): PdfName {
        var name = name
        name = translateName(name)
        fontDictionary.put(name, reference)
        return name
    }

    fun addXObject(name: PdfName, reference: PdfIndirectReference): PdfName {
        var name = name
        name = translateName(name)
        xObjectDictionary.put(name, reference)
        return name
    }

    fun addColor(name: PdfName, reference: PdfIndirectReference): PdfName {
        var name = name
        name = translateName(name)
        colorDictionary.put(name, reference)
        return name
    }

    fun addDefaultColor(name: PdfName, obj: PdfObject?) {
        if (obj == null || obj.isNull)
            colorDictionary.remove(name)
        else
            colorDictionary.put(name, obj)
    }

    fun addDefaultColor(dic: PdfDictionary) {
        colorDictionary.merge(dic)
    }

    fun addDefaultColorDiff(dic: PdfDictionary) {
        colorDictionary.mergeDifferent(dic)
    }

    fun addShading(name: PdfName, reference: PdfIndirectReference): PdfName {
        var name = name
        name = translateName(name)
        shadingDictionary.put(name, reference)
        return name
    }

    fun addPattern(name: PdfName, reference: PdfIndirectReference): PdfName {
        var name = name
        name = translateName(name)
        patternDictionary.put(name, reference)
        return name
    }

    fun addExtGState(name: PdfName, reference: PdfIndirectReference): PdfName {
        var name = name
        name = translateName(name)
        extGStateDictionary.put(name, reference)
        return name
    }

    fun addProperty(name: PdfName, reference: PdfIndirectReference): PdfName {
        var name = name
        name = translateName(name)
        propertyDictionary.put(name, reference)
        return name
    }

    val resources: PdfDictionary
        get() {
            val resources = PdfResources()
            if (originalResources != null)
                resources.putAll(originalResources)
            resources.add(PdfName.FONT, fontDictionary)
            resources.add(PdfName.XOBJECT, xObjectDictionary)
            resources.add(PdfName.COLORSPACE, colorDictionary)
            resources.add(PdfName.PATTERN, patternDictionary)
            resources.add(PdfName.SHADING, shadingDictionary)
            resources.add(PdfName.EXTGSTATE, extGStateDictionary)
            resources.add(PdfName.PROPERTIES, propertyDictionary)
            return resources
        }

    fun hasResources(): Boolean {
        return fontDictionary.size() > 0
                || xObjectDictionary.size() > 0
                || colorDictionary.size() > 0
                || patternDictionary.size() > 0
                || shadingDictionary.size() > 0
                || extGStateDictionary.size() > 0
                || propertyDictionary.size() > 0
    }
}

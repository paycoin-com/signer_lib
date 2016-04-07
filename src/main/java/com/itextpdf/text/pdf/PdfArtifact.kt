/*
 * $Id: c5c47570d977aabcf992a341894d943fce4477fa $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Alexander Chingarev, Bruno Lowagie, et al.
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

import com.itextpdf.text.AccessibleElementId
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.interfaces.IAccessibleElement

import java.util.*

class PdfArtifact : IAccessibleElement {

    override var role = PdfName.ARTIFACT
        set(role) {
        }
    override var accessibleAttributes: HashMap<PdfName, PdfObject>? = null
        protected set(value: HashMap<PdfName, PdfObject>?) {
            super.accessibleAttributes = value
        }
    override var id = AccessibleElementId()

    fun getAccessibleAttribute(key: PdfName): PdfObject? {
        if (accessibleAttributes != null)
            return accessibleAttributes!![key]
        else
            return null
    }

    fun setAccessibleAttribute(key: PdfName, value: PdfObject) {
        if (accessibleAttributes == null)
            accessibleAttributes = HashMap<PdfName, PdfObject>()
        accessibleAttributes!!.put(key, value)
    }

    override val isInline: Boolean
        get() = true

    var type: PdfString?
        get() = if (accessibleAttributes == null) null else accessibleAttributes!![PdfName.TYPE] as PdfString
        set(type) {
            if (!allowedArtifactTypes.contains(type.toString()))
                throw IllegalArgumentException(MessageLocalization.getComposedMessage("the.artifact.type.1.is.invalid", type))
            setAccessibleAttribute(PdfName.TYPE, type)
        }

    fun setType(type: PdfArtifact.ArtifactType) {
        var artifactType: PdfString? = null
        when (type) {
            PdfArtifact.ArtifactType.BACKGROUND -> artifactType = PdfString("Background")
            PdfArtifact.ArtifactType.LAYOUT -> artifactType = PdfString("Layout")
            PdfArtifact.ArtifactType.PAGE -> artifactType = PdfString("Page")
            PdfArtifact.ArtifactType.PAGINATION -> artifactType = PdfString("Pagination")
        }
        setAccessibleAttribute(PdfName.TYPE, artifactType)
    }

    var bBox: PdfArray?
        get() = if (accessibleAttributes == null) null else accessibleAttributes!![PdfName.BBOX] as PdfArray
        set(bbox) = setAccessibleAttribute(PdfName.BBOX, bbox)

    var attached: PdfArray?
        get() = if (accessibleAttributes == null) null else accessibleAttributes!![PdfName.ATTACHED] as PdfArray
        set(attached) = setAccessibleAttribute(PdfName.ATTACHED, attached)

    enum class ArtifactType {
        PAGINATION,
        LAYOUT,
        PAGE,
        BACKGROUND
    }

    companion object {

        private val allowedArtifactTypes = HashSet(Arrays.asList("Pagination", "Layout", "Page", "Background"))
    }
}

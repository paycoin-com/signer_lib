/*
 * $Id: 2d45b3cfa5be57c6ad19fbdf6827410a045118ca $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Pavel Alay, Bruno Lowagie, et al.
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

import com.itextpdf.text.pdf.security.XpathConstructor

/**
 * Constructor for xpath expression for signing XfaForm
 */
class XfaXpathConstructor : XpathConstructor {

    /**
     * Possible xdp packages to sign
     */
    enum class XdpPackage {
        Config,
        ConnectionSet,
        Datasets,
        LocaleSet,
        Pdf,
        SourceSet,
        Stylesheet,
        Template,
        Xdc,
        Xfdf,
        Xmpmeta
    }

    private val CONFIG = "config"
    private val CONNECTIONSET = "connectionSet"
    private val DATASETS = "datasets"
    private val LOCALESET = "localeSet"
    private val PDF = "pdf"
    private val SOURCESET = "sourceSet"
    private val STYLESHEET = "stylesheet"
    private val TEMPLATE = "template"
    private val XDC = "xdc"
    private val XFDF = "xfdf"
    private val XMPMETA = "xmpmeta"

    /**
     * Empty constructor, no transform.
     */
    constructor() {
        this.xpathExpression = ""
    }

    /**
     * Construct for XPath expression. Depends from selected xdp package.
     * @param xdpPackage
     */
    constructor(xdpPackage: XdpPackage) {
        val strPackage: String
        when (xdpPackage) {
            XfaXpathConstructor.XdpPackage.Config -> strPackage = CONFIG
            XfaXpathConstructor.XdpPackage.ConnectionSet -> strPackage = CONNECTIONSET
            XfaXpathConstructor.XdpPackage.Datasets -> strPackage = DATASETS
            XfaXpathConstructor.XdpPackage.LocaleSet -> strPackage = LOCALESET
            XfaXpathConstructor.XdpPackage.Pdf -> strPackage = PDF
            XfaXpathConstructor.XdpPackage.SourceSet -> strPackage = SOURCESET
            XfaXpathConstructor.XdpPackage.Stylesheet -> strPackage = STYLESHEET
            XfaXpathConstructor.XdpPackage.Template -> strPackage = TEMPLATE
            XfaXpathConstructor.XdpPackage.Xdc -> strPackage = XDC
            XfaXpathConstructor.XdpPackage.Xfdf -> strPackage = XFDF
            XfaXpathConstructor.XdpPackage.Xmpmeta -> strPackage = XMPMETA
            else -> {
                xpathExpression = ""
                return
            }
        }

        val builder = StringBuilder("/xdp:xdp/*[local-name()='")
        builder.append(strPackage)
        builder.append("']")
        xpathExpression = builder.toString()
    }

    /**
     * Get XPath expression
     */
    override var xpathExpression: String? = null
        private set(value: String?) {
            super.xpathExpression = value
        }
}

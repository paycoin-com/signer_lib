/*
 * $Id: 48e7f35eb846ee8b5e880834d865134c7811bab6 $
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
package com.itextpdf.text.pdf.security

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.security.cert.Certificate
import java.security.cert.CertificateParsingException
import java.security.cert.X509Certificate
import java.util.ArrayList

import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.log.Logger
import com.itextpdf.text.log.LoggerFactory

/**
 * An implementation of the CrlClient that fetches the CRL bytes
 * from an URL.
 * @author Paulo Soares
 */
class CrlClientOnline : CrlClient {

    /** The URLs of the CRLs.  */
    protected var urls: MutableList<URL> = ArrayList()

    /**
     * Creates a CrlClientOnline instance that will try to find
     * a single CRL by walking through the certificate chain.
     */
    constructor() {
    }

    /**
     * Creates a CrlClientOnline instance using one or more URLs.
     */
    constructor(vararg crls: String) {
        for (url in crls) {
            addUrl(url)
        }
    }

    /**
     * Creates a CrlClientOnline instance using one or more URLs.
     */
    constructor(vararg crls: URL) {
        for (url in urls) {
            addUrl(url)
        }
    }

    /**
     * Creates a CrlClientOnline instance using a certificate chain.
     */
    constructor(chain: Array<Certificate>) {
        for (i in chain.indices) {
            val cert = chain[i] as X509Certificate
            LOGGER.info("Checking certificate: " + cert.subjectDN)
            try {
                addUrl(CertificateUtil.getCRLURL(cert))
            } catch (e: CertificateParsingException) {
                LOGGER.info("Skipped CRL url (certificate could not be parsed)")
            }

        }
    }

    /**
     * Adds an URL to the list of CRL URLs
     * @param url    an URL in the form of a String
     */
    protected fun addUrl(url: String) {
        try {
            addUrl(URL(url))
        } catch (e: MalformedURLException) {
            LOGGER.info("Skipped CRL url (malformed): " + url)
        }

    }

    /**
     * Adds an URL to the list of CRL URLs
     * @param url    an URL object
     */
    protected fun addUrl(url: URL) {
        if (urls.contains(url)) {
            LOGGER.info("Skipped CRL url (duplicate): " + url)
            return
        }
        urls.add(url)
        LOGGER.info("Added CRL url: " + url)
    }

    /**
     * Fetches the CRL bytes from an URL.
     * If no url is passed as parameter, the url will be obtained from the certificate.
     * If you want to load a CRL from a local file, subclass this method and pass an
     * URL with the path to the local file to this method. An other option is to use
     * the CrlClientOffline class.
     * @see com.itextpdf.text.pdf.security.CrlClient.getEncoded
     */
    override fun getEncoded(checkCert: X509Certificate?, url: String?): Collection<ByteArray>? {
        var url = url
        if (checkCert == null)
            return null
        val urllist = ArrayList(urls)
        if (urllist.size == 0) {
            LOGGER.info("Looking for CRL for certificate " + checkCert.subjectDN)
            try {
                if (url == null)
                    url = CertificateUtil.getCRLURL(checkCert)
                if (url == null)
                    throw NullPointerException()
                urllist.add(URL(url))
                LOGGER.info("Found CRL url: " + url)
            } catch (e: Exception) {
                LOGGER.info("Skipped CRL url: " + e.message)
            }

        }
        val ar = ArrayList<ByteArray>()
        for (urlt in urllist) {
            try {
                LOGGER.info("Checking CRL: " + urlt)
                val con = urlt.openConnection() as HttpURLConnection
                if (con.responseCode / 100 != 2) {
                    throw IOException(MessageLocalization.getComposedMessage("invalid.http.response.1", con.responseCode))
                }
                //Get Response
                val inp = con.content as InputStream
                val buf = ByteArray(1024)
                val bout = ByteArrayOutputStream()
                while (true) {
                    val n = inp.read(buf, 0, buf.size)
                    if (n <= 0)
                        break
                    bout.write(buf, 0, n)
                }
                inp.close()
                ar.add(bout.toByteArray())
                LOGGER.info("Added CRL found at: " + urlt)
            } catch (e: Exception) {
                LOGGER.info("Skipped CRL: " + e.message + " for " + urlt)
            }

        }
        return ar
    }

    companion object {

        /** The Logger instance.  */
        private val LOGGER = LoggerFactory.getLogger(CrlClientOnline::class.java)
    }
}

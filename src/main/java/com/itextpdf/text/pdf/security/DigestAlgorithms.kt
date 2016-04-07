/*
 * $Id: 840a6099de27181740b790bd110baa7b94de8d12 $
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

import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.util.HashMap

/**
 * Class that contains a map with the different message digest algorithms.
 */
object DigestAlgorithms {

    /** Algorithm available for signatures since PDF 1.3  */
    val SHA1 = "SHA-1"

    /** Algorithm available for signatures since PDF 1.6  */
    val SHA256 = "SHA-256"

    /** Algorithm available for signatures since PDF 1.7  */
    val SHA384 = "SHA-384"

    /** Algorithm available for signatures since PDF 1.7  */
    val SHA512 = "SHA-512"

    /** Algorithm available for signatures since PDF 1.7  */
    val RIPEMD160 = "RIPEMD160"

    /** Maps the digest IDs with the human-readable name of the digest algorithm.  */
    private val digestNames = HashMap<String, String>()

    /** Maps digest algorithm that are unknown by the JDKs MessageDigest object to a known one.  */
    private val fixNames = HashMap<String, String>()

    /** Maps the name of a digest algorithm with its ID.  */
    private val allowedDigests = HashMap<String, String>()

    init {
        digestNames.put("1.2.840.113549.2.5", "MD5")
        digestNames.put("1.2.840.113549.2.2", "MD2")
        digestNames.put("1.3.14.3.2.26", "SHA1")
        digestNames.put("2.16.840.1.101.3.4.2.4", "SHA224")
        digestNames.put("2.16.840.1.101.3.4.2.1", "SHA256")
        digestNames.put("2.16.840.1.101.3.4.2.2", "SHA384")
        digestNames.put("2.16.840.1.101.3.4.2.3", "SHA512")
        digestNames.put("1.3.36.3.2.2", "RIPEMD128")
        digestNames.put("1.3.36.3.2.1", "RIPEMD160")
        digestNames.put("1.3.36.3.2.3", "RIPEMD256")
        digestNames.put("1.2.840.113549.1.1.4", "MD5")
        digestNames.put("1.2.840.113549.1.1.2", "MD2")
        digestNames.put("1.2.840.113549.1.1.5", "SHA1")
        digestNames.put("1.2.840.113549.1.1.14", "SHA224")
        digestNames.put("1.2.840.113549.1.1.11", "SHA256")
        digestNames.put("1.2.840.113549.1.1.12", "SHA384")
        digestNames.put("1.2.840.113549.1.1.13", "SHA512")
        digestNames.put("1.2.840.113549.2.5", "MD5")
        digestNames.put("1.2.840.113549.2.2", "MD2")
        digestNames.put("1.2.840.10040.4.3", "SHA1")
        digestNames.put("2.16.840.1.101.3.4.3.1", "SHA224")
        digestNames.put("2.16.840.1.101.3.4.3.2", "SHA256")
        digestNames.put("2.16.840.1.101.3.4.3.3", "SHA384")
        digestNames.put("2.16.840.1.101.3.4.3.4", "SHA512")
        digestNames.put("1.3.36.3.3.1.3", "RIPEMD128")
        digestNames.put("1.3.36.3.3.1.2", "RIPEMD160")
        digestNames.put("1.3.36.3.3.1.4", "RIPEMD256")
        digestNames.put("1.2.643.2.2.9", "GOST3411")

        fixNames.put("SHA256", SHA256)
        fixNames.put("SHA384", SHA384)
        fixNames.put("SHA512", SHA512)

        allowedDigests.put("MD2", "1.2.840.113549.2.2")
        allowedDigests.put("MD-2", "1.2.840.113549.2.2")
        allowedDigests.put("MD5", "1.2.840.113549.2.5")
        allowedDigests.put("MD-5", "1.2.840.113549.2.5")
        allowedDigests.put("SHA1", "1.3.14.3.2.26")
        allowedDigests.put("SHA-1", "1.3.14.3.2.26")
        allowedDigests.put("SHA224", "2.16.840.1.101.3.4.2.4")
        allowedDigests.put("SHA-224", "2.16.840.1.101.3.4.2.4")
        allowedDigests.put("SHA256", "2.16.840.1.101.3.4.2.1")
        allowedDigests.put("SHA-256", "2.16.840.1.101.3.4.2.1")
        allowedDigests.put("SHA384", "2.16.840.1.101.3.4.2.2")
        allowedDigests.put("SHA-384", "2.16.840.1.101.3.4.2.2")
        allowedDigests.put("SHA512", "2.16.840.1.101.3.4.2.3")
        allowedDigests.put("SHA-512", "2.16.840.1.101.3.4.2.3")
        allowedDigests.put("RIPEMD128", "1.3.36.3.2.2")
        allowedDigests.put("RIPEMD-128", "1.3.36.3.2.2")
        allowedDigests.put("RIPEMD160", "1.3.36.3.2.1")
        allowedDigests.put("RIPEMD-160", "1.3.36.3.2.1")
        allowedDigests.put("RIPEMD256", "1.3.36.3.2.3")
        allowedDigests.put("RIPEMD-256", "1.3.36.3.2.3")
        allowedDigests.put("GOST3411", "1.2.643.2.2.9")
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    fun getMessageDigestFromOid(digestOid: String, provider: String): MessageDigest {
        return getMessageDigest(getDigest(digestOid), provider)
    }

    /**
     * Creates a MessageDigest object that can be used to create a hash.
     * @param hashAlgorithm    the algorithm you want to use to create a hash
     * *
     * @param provider    the provider you want to use to create the hash
     * *
     * @return    a MessageDigest object
     * *
     * @throws NoSuchAlgorithmException
     * *
     * @throws NoSuchProviderException
     * *
     * @throws GeneralSecurityException
     */
    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class)
    fun getMessageDigest(hashAlgorithm: String, provider: String?): MessageDigest {
        if (provider == null || provider.startsWith("SunPKCS11") || provider.startsWith("SunMSCAPI"))
            return MessageDigest.getInstance(DigestAlgorithms.normalizeDigestName(hashAlgorithm))
        else
            return MessageDigest.getInstance(hashAlgorithm, provider)
    }


    /**
     * Creates a hash using a specific digest algorithm and a provider.
     * @param data    the message of which you want to create a hash
     * *
     * @param hashAlgorithm    the algorithm used to create the hash
     * *
     * @param provider    the provider used to create the hash
     * *
     * @return    the hash
     * *
     * @throws GeneralSecurityException
     * *
     * @throws IOException
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun digest(data: InputStream, hashAlgorithm: String, provider: String): ByteArray {
        val messageDigest = getMessageDigest(hashAlgorithm, provider)
        return digest(data, messageDigest)
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    fun digest(data: InputStream, messageDigest: MessageDigest): ByteArray {
        val buf = ByteArray(8192)
        var n: Int
        while ((n = data.read(buf)) > 0) {
            messageDigest.update(buf, 0, n)
        }
        return messageDigest.digest()
    }

    /**
     * Gets the digest name for a certain id
     * @param oid    an id (for instance "1.2.840.113549.2.5")
     * *
     * @return    a digest name (for instance "MD5")
     */
    fun getDigest(oid: String): String {
        val ret = digestNames[oid]
        if (ret == null)
            return oid
        else
            return ret
    }

    fun normalizeDigestName(algo: String): String {
        if (fixNames.containsKey(algo))
            return fixNames[algo]
        return algo
    }

    /**
     * Returns the id of a digest algorithms that is allowed in PDF,
     * or null if it isn't allowed.
     * @param name    the name of the digest algorithm
     * *
     * @return    an oid
     */
    fun getAllowedDigests(name: String): String {
        return allowedDigests[name.toUpperCase()]
    }
}

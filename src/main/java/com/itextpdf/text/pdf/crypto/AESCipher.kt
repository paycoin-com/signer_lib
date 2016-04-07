/*
 * $Id: 069d8bf5eded088f5a8626c72519b20054dda280 $
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
package com.itextpdf.text.pdf.crypto

import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.engines.AESFastEngine
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV

/**
 * Creates an AES Cipher with CBC and padding PKCS5/7.
 * @author Paulo Soares
 */
class AESCipher
/** Creates a new instance of AESCipher  */
(forEncryption: Boolean, key: ByteArray, iv: ByteArray) {
    private val bp: PaddedBufferedBlockCipher

    init {
        val aes = AESFastEngine()
        val cbc = CBCBlockCipher(aes)
        bp = PaddedBufferedBlockCipher(cbc)
        val kp = KeyParameter(key)
        val piv = ParametersWithIV(kp, iv)
        bp.init(forEncryption, piv)
    }

    fun update(inp: ByteArray, inpOff: Int, inpLen: Int): ByteArray {
        var neededLen = bp.getUpdateOutputSize(inpLen)
        var outp: ByteArray? = null
        if (neededLen > 0)
            outp = ByteArray(neededLen)
        else
            neededLen = 0
        bp.processBytes(inp, inpOff, inpLen, outp, 0)
        return outp
    }

    fun doFinal(): ByteArray {
        val neededLen = bp.getOutputSize(0)
        val outp = ByteArray(neededLen)
        var n = 0
        try {
            n = bp.doFinal(outp, 0)
        } catch (ex: Exception) {
            return outp
        }

        if (n != outp.size) {
            val outp2 = ByteArray(n)
            System.arraycopy(outp, 0, outp2, 0, n)
            return outp2
        } else
            return outp
    }

}

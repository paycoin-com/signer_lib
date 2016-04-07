/*
 * $Id: d0b12bc68733303f0e45bb234582c055a07cb6c2 $
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
package com.itextpdf.text.pdf.codec

import com.itextpdf.text.ExceptionConverter
import com.itextpdf.text.Image
import com.itextpdf.text.ImgRaw
import com.itextpdf.text.Utilities
import com.itextpdf.text.error_messages.MessageLocalization
import com.itextpdf.text.pdf.*

import java.io.*
import java.net.URL
import java.util.ArrayList

/** Reads gif images of all types. All the images in a gif are read in the constructors
 * and can be retrieved with other methods.
 * @author Paulo Soares
 */
class GifImage {

    protected var `in`: DataInputStream
    protected var width: Int = 0            // full image width
    protected var height: Int = 0           // full image height
    protected var gctFlag: Boolean = false      // global color table used

    protected var bgIndex: Int = 0          // background color index
    protected var bgColor: Int = 0          // background color
    protected var pixelAspect: Int = 0      // pixel aspect ratio

    protected var lctFlag: Boolean = false      // local color table flag
    protected var interlace: Boolean = false    // interlace flag
    protected var lctSize: Int = 0          // local color table size

    protected var ix: Int = 0
    protected var iy: Int = 0
    protected var iw: Int = 0
    protected var ih: Int = 0   // current image rectangle

    protected var block = ByteArray(256)  // current data block
    protected var blockSize = 0    // block size

    // last graphic control extension info
    protected var dispose = 0   // 0=no action; 1=leave in place; 2=restore to bg; 3=restore to prev
    protected var transparency = false   // use transparent color
    protected var delay = 0        // delay in milliseconds
    protected var transIndex: Int = 0       // transparent color index

    // LZW decoder working arrays
    protected var prefix: ShortArray? = null
    protected var suffix: ByteArray? = null
    protected var pixelStack: ByteArray? = null
    protected var pixels: ByteArray

    protected var m_out: ByteArray
    protected var m_bpc: Int = 0
    protected var m_gbpc: Int = 0
    protected var m_global_table: ByteArray
    protected var m_local_table: ByteArray
    protected var m_curr_table: ByteArray
    protected var m_line_stride: Int = 0
    protected var fromData: ByteArray
    protected var fromUrl: URL


    protected var frames = ArrayList<GifFrame>()     // frames read from current file

    /** Reads gif images from an URL.
     * @param url the URL
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(url: URL) {
        fromUrl = url
        var `is`: InputStream? = null
        try {
            `is` = url.openStream()

            val baos = ByteArrayOutputStream()
            var read = 0
            val bytes = ByteArray(1024)

            while ((read = `is`!!.read(bytes)) != -1) {
                baos.write(bytes, 0, read)
            }
            `is`!!.close()

            `is` = ByteArrayInputStream(baos.toByteArray())
            baos.flush()
            baos.close()

            process(`is`)
        } finally {
            if (`is` != null) {
                `is`.close()
            }
        }
    }

    /** Reads gif images from a file.
     * @param file the file
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(file: String) : this(Utilities.toURL(file)) {
    }

    /** Reads gif images from a byte array.
     * @param data the byte array
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(data: ByteArray) {
        fromData = data
        var `is`: InputStream? = null
        try {
            `is` = ByteArrayInputStream(data)
            process(`is`)
        } finally {
            if (`is` != null) {
                `is`.close()
            }
        }
    }

    /** Reads gif images from a stream. The stream is not closed.
     * @param is the stream
     * *
     * @throws IOException on error
     */
    @Throws(IOException::class)
    constructor(`is`: InputStream) {
        process(`is`)
    }

    /** Gets the number of frames the gif has.
     * @return the number of frames the gif has
     */
    val frameCount: Int
        get() = frames.size

    /** Gets the image from a frame. The first frame is 1.
     * @param frame the frame to get the image from
     * *
     * @return the image
     */
    fun getImage(frame: Int): Image {
        val gf = frames[frame - 1]
        return gf.image
    }

    /** Gets the [x,y] position of the frame in reference to the
     * logical screen.
     * @param frame the frame
     * *
     * @return the [x,y] position of the frame
     */
    fun getFramePosition(frame: Int): IntArray {
        val gf = frames[frame - 1]
        return intArrayOf(gf.ix, gf.iy)

    }

    /** Gets the logical screen. The images may be smaller and placed
     * in some position in this screen to playback some animation.
     * No image will be be bigger that this.
     * @return the logical screen dimensions as [x,y]
     */
    val logicalScreen: IntArray
        get() = intArrayOf(width, height)

    @Throws(IOException::class)
    internal fun process(`is`: InputStream) {
        `in` = DataInputStream(BufferedInputStream(`is`))
        readHeader()
        readContents()
        if (frames.isEmpty())
            throw IOException(MessageLocalization.getComposedMessage("the.file.does.not.contain.any.valid.image"))
    }

    /**
     * Reads GIF file header information.
     */
    @Throws(IOException::class)
    protected fun readHeader() {
        val id = StringBuilder("")
        for (i in 0..5)
            id.append(`in`.read().toChar())
        if (!id.toString().startsWith("GIF8")) {
            throw IOException(MessageLocalization.getComposedMessage("gif.signature.nor.found"))
        }

        readLSD()
        if (gctFlag) {
            m_global_table = readColorTable(m_gbpc)
        }
    }

    /**
     * Reads Logical Screen Descriptor
     */
    @Throws(IOException::class)
    protected fun readLSD() {

        // logical screen size
        width = readShort()
        height = readShort()

        // packed fields
        val packed = `in`.read()
        gctFlag = packed and 0x80 != 0      // 1   : global color table flag
        m_gbpc = (packed and 7) + 1
        bgIndex = `in`.read()        // background color index
        pixelAspect = `in`.read()    // pixel aspect ratio
    }

    /**
     * Reads next 16-bit value, LSB first
     */
    @Throws(IOException::class)
    protected fun readShort(): Int {
        // read 16-bit value, LSB first
        return `in`.read() or (`in`.read() shl 8)
    }

    /**
     * Reads next variable length block from input.

     * @return number of bytes stored in "buffer"
     */
    @Throws(IOException::class)
    protected fun readBlock(): Int {
        blockSize = `in`.read()
        if (blockSize <= 0)
            return blockSize = 0

        blockSize = `in`.read(block, 0, blockSize)

        return blockSize
    }

    @Throws(IOException::class)
    protected fun readColorTable(bpc: Int): ByteArray {
        var bpc = bpc
        val ncolors = 1 shl bpc
        val nbytes = 3 * ncolors
        bpc = newBpc(bpc)
        val table = ByteArray((1 shl bpc) * 3)
        `in`.readFully(table, 0, nbytes)
        return table
    }

    @Throws(IOException::class)
    protected fun readContents() {
        // read GIF file content blocks
        var done = false
        while (!done) {
            var code = `in`.read()
            when (code) {

                0x2C    // image separator
                -> readImage()

                0x21    // extension
                -> {
                    code = `in`.read()
                    when (code) {

                        0xf9    // graphics control extension
                        -> readGraphicControlExt()

                        0xff    // application extension
                        -> {
                            readBlock()
                            skip()        // don't care
                        }

                        else    // uninteresting extension
                        -> skip()
                    }
                }

                else -> done = true
            }
        }
    }

    /**
     * Reads next frame image
     */
    @Throws(IOException::class)
    protected fun readImage() {
        ix = readShort()    // (sub)image position & size
        iy = readShort()
        iw = readShort()
        ih = readShort()

        val packed = `in`.read()
        lctFlag = packed and 0x80 != 0     // 1 - local color table flag
        interlace = packed and 0x40 != 0   // 2 - interlace flag
        // 3 - sort flag
        // 4-5 - reserved
        lctSize = 2 shl (packed and 7)        // 6-8 - local color table size
        m_bpc = newBpc(m_gbpc)
        if (lctFlag) {
            m_curr_table = readColorTable((packed and 7) + 1)   // read table
            m_bpc = newBpc((packed and 7) + 1)
        } else {
            m_curr_table = m_global_table
        }
        if (transparency && transIndex >= m_curr_table.size / 3)
            transparency = false
        if (transparency && m_bpc == 1) {
            // Acrobat 5.05 doesn't like this combination
            val tp = ByteArray(12)
            System.arraycopy(m_curr_table, 0, tp, 0, 6)
            m_curr_table = tp
            m_bpc = 2
        }
        val skipZero = decodeImageData()   // decode pixel data
        if (!skipZero)
            skip()

        var img: Image? = null
        try {
            img = ImgRaw(iw, ih, 1, m_bpc, m_out)
            val colorspace = PdfArray()
            colorspace.add(PdfName.INDEXED)
            colorspace.add(PdfName.DEVICERGB)
            val len = m_curr_table.size
            colorspace.add(PdfNumber(len / 3 - 1))
            colorspace.add(PdfString(m_curr_table))
            val ad = PdfDictionary()
            ad.put(PdfName.COLORSPACE, colorspace)
            img.additional = ad
            if (transparency) {
                img.transparency = intArrayOf(transIndex, transIndex)
            }
        } catch (e: Exception) {
            throw ExceptionConverter(e)
        }

        img.originalType = Image.ORIGINAL_GIF
        img.originalData = fromData
        img.url = fromUrl
        val gf = GifFrame()
        gf.image = img
        gf.ix = ix
        gf.iy = iy
        frames.add(gf)   // add image to frame list

        //resetFrame();

    }

    @Throws(IOException::class)
    protected fun decodeImageData(): Boolean {
        val NullCode = -1
        val npix = iw * ih
        var available: Int
        val clear: Int
        var code_mask: Int
        var code_size: Int
        val end_of_information: Int
        var in_code: Int
        var old_code: Int
        var bits: Int
        var code: Int
        var count: Int
        var i: Int
        var datum: Int
        val data_size: Int
        var first: Int
        var top: Int
        var bi: Int
        var skipZero = false

        if (prefix == null)
            prefix = ShortArray(MaxStackSize)
        if (suffix == null)
            suffix = ByteArray(MaxStackSize)
        if (pixelStack == null)
            pixelStack = ByteArray(MaxStackSize + 1)

        m_line_stride = (iw * m_bpc + 7) / 8
        m_out = ByteArray(m_line_stride * ih)
        var pass = 1
        var inc = if (interlace) 8 else 1
        var line = 0
        var xpos = 0

        //  Initialize GIF data stream decoder.

        data_size = `in`.read()
        clear = 1 shl data_size
        end_of_information = clear + 1
        available = clear + 2
        old_code = NullCode
        code_size = data_size + 1
        code_mask = (1 shl code_size) - 1
        code = 0
        while (code < clear) {
            prefix[code] = 0
            suffix[code] = code.toByte()
            code++
        }

        //  Decode GIF pixel stream.

        datum = bits = count = first = top = bi = 0

        i = 0
        while (i < npix) {
            if (top == 0) {
                if (bits < code_size) {
                    //  Load bytes until there are enough bits for a code.
                    if (count == 0) {
                        // Read a new data block.
                        count = readBlock()
                        if (count <= 0) {
                            skipZero = true
                            break
                        }
                        bi = 0
                    }
                    datum += block[bi] and 0xff shl bits
                    bits += 8
                    bi++
                    count--
                    continue
                }

                //  Get the next code.

                code = datum and code_mask
                datum = datum shr code_size
                bits -= code_size

                //  Interpret the code

                if (code > available || code == end_of_information)
                    break
                if (code == clear) {
                    //  Reset decoder.
                    code_size = data_size + 1
                    code_mask = (1 shl code_size) - 1
                    available = clear + 2
                    old_code = NullCode
                    continue
                }
                if (old_code == NullCode) {
                    pixelStack[top++] = suffix!![code]
                    old_code = code
                    first = code
                    continue
                }
                in_code = code
                if (code == available) {
                    pixelStack[top++] = first.toByte()
                    code = old_code
                }
                while (code > clear) {
                    pixelStack[top++] = suffix!![code]
                    code = prefix!![code].toInt()
                }
                first = suffix!![code] and 0xff

                //  Add a new string to the string table,

                if (available >= MaxStackSize)
                    break
                pixelStack[top++] = first.toByte()
                prefix[available] = old_code.toShort()
                suffix[available] = first.toByte()
                available++
                if (available and code_mask == 0 && available < MaxStackSize) {
                    code_size++
                    code_mask += available
                }
                old_code = in_code
            }

            //  Pop a pixel off the pixel stack.

            top--
            i++

            setPixel(xpos, line, pixelStack!![top].toInt())
            ++xpos
            if (xpos >= iw) {
                xpos = 0
                line += inc
                if (line >= ih) {
                    if (interlace) {
                        do {
                            pass++
                            when (pass) {
                                2 -> line = 4
                                3 -> {
                                    line = 2
                                    inc = 4
                                }
                                4 -> {
                                    line = 1
                                    inc = 2
                                }
                                else // this shouldn't happen
                                -> {
                                    line = ih - 1
                                    inc = 0
                                }
                            }
                        } while (line >= ih)
                    } else {
                        line = ih - 1 // this shouldn't happen
                        inc = 0
                    }
                }
            }
        }
        return skipZero
    }


    protected fun setPixel(x: Int, y: Int, v: Int) {
        if (m_bpc == 8) {
            val pos = x + iw * y
            m_out[pos] = v.toByte()
        } else {
            val pos = m_line_stride * y + x / (8 / m_bpc)
            val vout = v shl 8 - m_bpc * (x % (8 / m_bpc)) - m_bpc
            m_out[pos] = m_out[pos] or vout.toByte()
        }
    }

    /**
     * Resets frame state for reading next image.
     */
    protected fun resetFrame() {
        // it does nothing in the pdf context
        //boolean transparency = false;
        //int delay = 0;
    }

    /**
     * Reads Graphics Control Extension values
     */
    @Throws(IOException::class)
    protected fun readGraphicControlExt() {
        `in`.read()    // block size
        val packed = `in`.read()   // packed fields
        dispose = packed and 0x1c shr 2   // disposal method
        if (dispose == 0)
            dispose = 1   // elect to keep old image if discretionary
        transparency = packed and 1 != 0
        delay = readShort() * 10   // delay in milliseconds
        transIndex = `in`.read()        // transparent color index
        `in`.read()                     // block terminator
    }

    /**
     * Skips variable length blocks up to and including
     * next zero length block.
     */
    @Throws(IOException::class)
    protected fun skip() {
        do {
            readBlock()
        } while (blockSize > 0)
    }

    internal class GifFrame {
        var image: Image
        var ix: Int = 0
        var iy: Int = 0
    }

    companion object {

        protected val MaxStackSize = 4096   // max decoder pixel stack size


        protected fun newBpc(bpc: Int): Int {
            when (bpc) {
                1, 2, 4 -> {
                }
                3 -> return 4
                else -> return 8
            }
            return bpc
        }
    }
}

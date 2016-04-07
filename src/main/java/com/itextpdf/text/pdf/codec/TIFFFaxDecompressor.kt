/*
 * Copyright 2003-2012 by Paulo Soares.
 *
 * This code was originally released in 2001 by SUN (see class
 * com.sun.media.imageioimpl.plugins.tiff.TIFFFaxDecompressor.java)
 * using the BSD license in a specific wording. In a mail dating from
 * January 23, 2008, Brian Burkhalter (@sun.com) gave us permission
 * to use the code under the following version of the BSD license:
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision$
 * $Date$
 * $State: Exp $
 */
package com.itextpdf.text.pdf.codec

//import com.itextpdf.text.error_messages.MessageLocalization;

/**
 * Class that can decompress TIFF files.
 * @since 5.0.3
 */
class TIFFFaxDecompressor {

    /**
     * The logical order of bits within a byte.
     *
     * 1 = MSB-to-LSB
     * 2 = LSB-to-MSB (flipped)
     *
     */
    protected var fillOrder: Int = 0
    protected var compression: Int = 0
    private var t4Options: Int = 0
    private var t6Options: Int = 0
    var fails: Int = 0
    // Variables set by T4Options
    /**
     * Uncompressed mode flag: 1 if uncompressed, 0 if not.
     */
    protected var uncompressedMode = 0
    /**
     * EOL padding flag: 1 if fill bits have been added before an EOL such
     * that the EOL ends on a byte boundary, 0 otherwise.
     */
    protected var fillBits = 0
    /**
     * Coding dimensionality: 1 for 2-dimensional, 0 for 1-dimensional.
     */
    protected var oneD: Int = 0
    private var data: ByteArray? = null
    private var bitPointer: Int = 0
    private var bytePointer: Int = 0
    // Output image buffer
    private var buffer: ByteArray? = null
    private var w: Int = 0
    private var h: Int = 0
    private var bitsPerScanline: Int = 0
    private var lineBitNum: Int = 0
    // Data structures needed to store changing elements for the previous
    // and the current scanline
    private var changingElemSize = 0
    private var prevChangingElems: IntArray? = null
    private var currChangingElems: IntArray? = null
    // Element at which to start search in getNextChangingElement
    private var lastChangingElement = 0

    /**
     * Invokes the superclass method and then sets instance variables on
     * the basis of the metadata set on this decompressor.
     */
    fun SetOptions(fillOrder: Int, compression: Int, t4Options: Int, t6Options: Int) {
        this.fillOrder = fillOrder
        this.compression = compression
        this.t4Options = t4Options
        this.t6Options = t6Options
        this.oneD = (t4Options and 0x01).toInt()
        this.uncompressedMode = (t4Options and 0x02 shr 1).toInt()
        this.fillBits = (t4Options and 0x04 shr 2).toInt()
    }

    fun decodeRaw(buffer: ByteArray, compData: ByteArray, w: Int, h: Int) {

        this.buffer = buffer
        this.data = compData
        this.w = w
        this.h = h
        this.bitsPerScanline = w
        this.lineBitNum = 0

        this.bitPointer = 0
        this.bytePointer = 0
        this.prevChangingElems = IntArray(w + 1)
        this.currChangingElems = IntArray(w + 1)

        fails = 0

        try {
            if (compression == TIFFConstants.COMPRESSION_CCITTRLE) {
                decodeRLE()
            } else if (compression == TIFFConstants.COMPRESSION_CCITTFAX3) {
                decodeT4()
            } else if (compression == TIFFConstants.COMPRESSION_CCITTFAX4) {
                this.uncompressedMode = (t6Options and 0x02 shr 1).toInt()
                decodeT6()
            } else {
                throw RuntimeException("Unknown compression type " + compression)
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            //ignore
        }

    }

    fun decodeRLE() {
        for (i in 0..h - 1) {
            // Decode the line.
            decodeNextScanline()

            // Advance to the next byte boundary if not already there.
            if (bitPointer != 0) {
                bytePointer++
                bitPointer = 0
            }

            // Update the total number of bits.
            lineBitNum += bitsPerScanline
        }
    }

    fun decodeNextScanline() {
        var bits = 0
        var code = 0
        var isT = 0
        var current: Int
        var entry: Int
        var twoBits: Int
        var isWhite = true

        var bitOffset = 0

        // Initialize starting of the changing elements array
        changingElemSize = 0

        // While scanline not complete
        while (bitOffset < w) {

            // Mark start of white run.
            var runOffset = bitOffset

            while (isWhite && bitOffset < w) {
                // White run
                current = nextNBits(10)
                entry = white[current].toInt()

                // Get the 3 fields from the entry
                isT = entry and 0x0001
                bits = entry.ushr(1) and 0x0f

                if (bits == 12) {
                    // Additional Make up code
                    // Get the next 2 bits
                    twoBits = nextLesserThan8Bits(2)
                    // Consolidate the 2 new bits and last 2 bits into 4 bits
                    current = current shl 2 and 0x000c or twoBits
                    entry = additionalMakeup[current].toInt()
                    bits = entry.ushr(1) and 0x07     // 3 bits 0000 0111
                    code = entry.ushr(4) and 0x0fff  // 12 bits
                    bitOffset += code // Skip white run

                    updatePointer(4 - bits)
                } else if (bits == 0) {
                    // ERROR
                    ++fails
                    // XXX return?
                } else if (bits == 15) {
                    // EOL
                    //
                    // Instead of throwing an exception, assume that the
                    // EOL was premature; emit a warning and return.
                    //
                    ++fails
                    return
                } else {
                    // 11 bits - 0000 0111 1111 1111 = 0x07ff
                    code = entry.ushr(5) and 0x07ff
                    bitOffset += code

                    updatePointer(10 - bits)
                    if (isT == 0) {
                        isWhite = false
                        currChangingElems[changingElemSize++] = bitOffset
                    }
                }
            }

            // Check whether this run completed one width
            if (bitOffset == w) {
                // If the white run has not been terminated then ensure that
                // the next code word is a terminating code for a white run
                // of length zero.
                val runLength = bitOffset - runOffset
                if (isWhite
                        && runLength != 0 && runLength % 64 == 0
                        && nextNBits(8) != 0x35) {
                    ++fails
                    updatePointer(8)
                }
                break
            }

            // Mark start of black run.
            runOffset = bitOffset

            while (isWhite == false && bitOffset < w) {
                // Black run
                current = nextLesserThan8Bits(4)
                entry = initBlack[current].toInt()

                // Get the 3 fields from the entry
                isT = entry and 0x0001
                bits = entry.ushr(1) and 0x000f
                code = entry.ushr(5) and 0x07ff

                if (code == 100) {
                    current = nextNBits(9)
                    entry = black[current].toInt()

                    // Get the 3 fields from the entry
                    isT = entry and 0x0001
                    bits = entry.ushr(1) and 0x000f
                    code = entry.ushr(5) and 0x07ff

                    if (bits == 12) {
                        // Additional makeup codes
                        updatePointer(5)
                        current = nextLesserThan8Bits(4)
                        entry = additionalMakeup[current].toInt()
                        bits = entry.ushr(1) and 0x07     // 3 bits 0000 0111
                        code = entry.ushr(4) and 0x0fff  // 12 bits

                        setToBlack(bitOffset, code)
                        bitOffset += code

                        updatePointer(4 - bits)
                    } else if (bits == 15) {
                        //
                        // Instead of throwing an exception, assume that the
                        // EOL was premature; emit a warning and return.
                        //
                        ++fails
                        return
                    } else {
                        setToBlack(bitOffset, code)
                        bitOffset += code

                        updatePointer(9 - bits)
                        if (isT == 0) {
                            isWhite = true
                            currChangingElems[changingElemSize++] = bitOffset
                        }
                    }
                } else if (code == 200) {
                    // Is a Terminating code
                    current = nextLesserThan8Bits(2)
                    entry = twoBitBlack[current].toInt()
                    code = entry.ushr(5) and 0x07ff
                    bits = entry.ushr(1) and 0x0f

                    setToBlack(bitOffset, code)
                    bitOffset += code

                    updatePointer(2 - bits)
                    isWhite = true
                    currChangingElems[changingElemSize++] = bitOffset
                } else {
                    // Is a Terminating code
                    setToBlack(bitOffset, code)
                    bitOffset += code

                    updatePointer(4 - bits)
                    isWhite = true
                    currChangingElems[changingElemSize++] = bitOffset
                }
            }

            // Check whether this run completed one width
            if (bitOffset == w) {
                // If the black run has not been terminated then ensure that
                // the next code word is a terminating code for a black run
                // of length zero.
                val runLength = bitOffset - runOffset
                if (!isWhite
                        && runLength != 0 && runLength % 64 == 0
                        && nextNBits(10) != 0x37) {
                    ++fails
                    updatePointer(10)
                }
                break
            }
        }

        currChangingElems[changingElemSize++] = bitOffset
    }

    fun decodeT4() {
        val height = h

        var a0: Int
        var a1: Int
        var b1: Int
        var b2: Int
        val b = IntArray(2)
        var entry: Int
        var code: Int
        var bits: Int
        val color: Int
        var isWhite: Boolean
        var currIndex = 0
        var temp: IntArray

        if (data!!.size < 2) {
            throw RuntimeException("Insufficient data to read initial EOL.")
        }

        // The data should start with an EOL code
        val next12 = nextNBits(12)
        if (next12 != 1) {
            ++fails
        }
        updatePointer(12)

        // Find the first one-dimensionally encoded line.
        var modeFlag = 0
        var lines = -1 // indicates imaginary line before first actual line.
        while (modeFlag != 1) {
            try {
                modeFlag = findNextLine()
                lines++ // Normally 'lines' will be 0 on exiting loop.
            } catch (eofe: Exception) {
                throw RuntimeException("No reference line present.")
            }

        }

        var bitOffset: Int

        // Then the 1D encoded scanline data will occur, changing elements
        // array gets set.
        decodeNextScanline()
        lines++
        lineBitNum += bitsPerScanline

        while (lines < height) {

            // Every line must begin with an EOL followed by a bit which
            // indicates whether the following scanline is 1D or 2D encoded.
            try {
                modeFlag = findNextLine()
            } catch (eofe: Exception) {
                ++fails
                break
            }

            if (modeFlag == 0) {
                // 2D encoded scanline follows

                // Initialize previous scanlines changing elements, and
                // initialize current scanline's changing elements array
                temp = prevChangingElems
                prevChangingElems = currChangingElems
                currChangingElems = temp
                currIndex = 0

                // a0 has to be set just before the start of this scanline.
                a0 = -1
                isWhite = true
                bitOffset = 0

                lastChangingElement = 0

                while (bitOffset < w) {
                    // Get the next changing element
                    getNextChangingElement(a0, isWhite, b)

                    b1 = b[0]
                    b2 = b[1]

                    // Get the next seven bits
                    entry = nextLesserThan8Bits(7)

                    // Run these through the 2DCodes table
                    entry = (twoDCodes[entry] and 0xff).toInt()

                    // Get the code and the number of bits used up
                    code = (entry and 0x78).ushr(3)
                    bits = entry and 0x07

                    if (code == 0) {
                        if (!isWhite) {
                            setToBlack(bitOffset, b2 - bitOffset)
                        }
                        bitOffset = a0 = b2

                        // Set pointer to consume the correct number of bits.
                        updatePointer(7 - bits)
                    } else if (code == 1) {
                        // Horizontal
                        updatePointer(7 - bits)

                        // identify the next 2 codes.
                        var number: Int
                        if (isWhite) {
                            number = decodeWhiteCodeWord()
                            bitOffset += number
                            currChangingElems[currIndex++] = bitOffset

                            number = decodeBlackCodeWord()
                            setToBlack(bitOffset, number)
                            bitOffset += number
                            currChangingElems[currIndex++] = bitOffset
                        } else {
                            number = decodeBlackCodeWord()
                            setToBlack(bitOffset, number)
                            bitOffset += number
                            currChangingElems[currIndex++] = bitOffset

                            number = decodeWhiteCodeWord()
                            bitOffset += number
                            currChangingElems[currIndex++] = bitOffset
                        }

                        a0 = bitOffset
                    } else if (code <= 8) {
                        // Vertical
                        a1 = b1 + (code - 5)

                        currChangingElems[currIndex++] = a1

                        // We write the current color till a1 - 1 pos,
                        // since a1 is where the next color starts
                        if (!isWhite) {
                            setToBlack(bitOffset, a1 - bitOffset)
                        }
                        bitOffset = a0 = a1
                        isWhite = !isWhite

                        updatePointer(7 - bits)
                    } else {
                        ++fails
                        // Find the next one-dimensionally encoded line.
                        var numLinesTested = 0
                        while (modeFlag != 1) {
                            try {
                                modeFlag = findNextLine()
                                numLinesTested++
                            } catch (eofe: Exception) {
                                return
                            }

                        }
                        lines += numLinesTested - 1
                        updatePointer(13)
                        break
                    }
                }

                // Add the changing element beyond the current scanline for the
                // other color too
                currChangingElems[currIndex++] = bitOffset
                changingElemSize = currIndex
            } else {
                // modeFlag == 1
                // 1D encoded scanline follows
                decodeNextScanline()
            }

            lineBitNum += bitsPerScanline
            lines++
        } // while(lines < height)
    }

    @Synchronized fun decodeT6() {
        val height = h


        var a0: Int
        var a1: Int
        var b1: Int
        var b2: Int
        var entry: Int
        var code: Int
        var bits: Int
        var isWhite: Boolean
        var currIndex: Int
        var temp: IntArray

        // Return values from getNextChangingElement
        val b = IntArray(2)

        // uncompressedMode - have written some code for this, but this
        // has not been tested due to lack of test images using this optional
        // extension. This code is when code == 11. aastha 03/03/1999

        // Local cached reference
        var cce: IntArray = currChangingElems

        // Assume invisible preceding row of all white pixels and insert
        // both black and white changing elements beyond the end of this
        // imaginary scanline.
        changingElemSize = 0
        cce[changingElemSize++] = w
        cce[changingElemSize++] = w

        var bitOffset: Int

        for (lines in 0..height - 1) {
            // a0 has to be set just before the start of the scanline.
            a0 = -1
            isWhite = true

            // Assign the changing elements of the previous scanline to
            // prevChangingElems and start putting this new scanline's
            // changing elements into the currChangingElems.
            temp = prevChangingElems
            prevChangingElems = currChangingElems
            cce = currChangingElems = temp
            currIndex = 0

            // Start decoding the scanline
            bitOffset = 0

            // Reset search start position for getNextChangingElement
            lastChangingElement = 0

            // Till one whole scanline is decoded
            while (bitOffset < w) {
                // Get the next changing element
                getNextChangingElement(a0, isWhite, b)
                b1 = b[0]
                b2 = b[1]

                // Get the next seven bits
                entry = nextLesserThan8Bits(7)
                // Run these through the 2DCodes table
                entry = (twoDCodes[entry] and 0xff).toInt()

                // Get the code and the number of bits used up
                code = (entry and 0x78).ushr(3)
                bits = entry and 0x07

                if (code == 0) {
                    // Pass
                    // We always assume WhiteIsZero format for fax.
                    if (!isWhite) {
                        if (b2 > w) {
                            b2 = w
                        }
                        setToBlack(bitOffset, b2 - bitOffset)
                    }
                    bitOffset = a0 = b2

                    // Set pointer to only consume the correct number of bits.
                    updatePointer(7 - bits)
                } else if (code == 1) {
                    // Horizontal
                    // Set pointer to only consume the correct number of bits.
                    updatePointer(7 - bits)

                    // identify the next 2 alternating color codes.
                    var number: Int
                    if (isWhite) {
                        // Following are white and black runs
                        number = decodeWhiteCodeWord()
                        bitOffset += number
                        cce[currIndex++] = bitOffset

                        number = decodeBlackCodeWord()
                        if (number > w - bitOffset) {
                            number = w - bitOffset
                        }
                        setToBlack(bitOffset, number)
                        bitOffset += number
                        cce[currIndex++] = bitOffset
                    } else {
                        // First a black run and then a white run follows
                        number = decodeBlackCodeWord()
                        if (number > w - bitOffset) {
                            number = w - bitOffset
                        }
                        setToBlack(bitOffset, number)
                        bitOffset += number
                        cce[currIndex++] = bitOffset

                        number = decodeWhiteCodeWord()
                        bitOffset += number
                        cce[currIndex++] = bitOffset
                    }

                    a0 = bitOffset
                } else if (code <= 8) {
                    // Vertical
                    a1 = b1 + (code - 5)
                    cce[currIndex++] = a1

                    // We write the current color till a1 - 1 pos,
                    // since a1 is where the next color starts
                    if (!isWhite) {
                        if (a1 > w) {
                            a1 = w
                        }
                        setToBlack(bitOffset, a1 - bitOffset)
                    }
                    bitOffset = a0 = a1
                    isWhite = !isWhite

                    updatePointer(7 - bits)
                } else if (code == 11) {
                    val entranceCode = nextLesserThan8Bits(3)

                    var zeros = 0
                    var exit = false

                    while (!exit) {
                        while (nextLesserThan8Bits(1) != 1) {
                            zeros++
                        }

                        if (zeros > 5) {
                            // Exit code

                            // Zeros before exit code
                            zeros = zeros - 6

                            if (!isWhite && zeros > 0) {
                                cce[currIndex++] = bitOffset
                            }

                            // Zeros before the exit code
                            bitOffset += zeros
                            if (zeros > 0) {
                                // Some zeros have been written
                                isWhite = true
                            }

                            // Read in the bit which specifies the color of
                            // the following run
                            if (nextLesserThan8Bits(1) == 0) {
                                if (!isWhite) {
                                    cce[currIndex++] = bitOffset
                                }
                                isWhite = true
                            } else {
                                if (isWhite) {
                                    cce[currIndex++] = bitOffset
                                }
                                isWhite = false
                            }

                            exit = true
                        }

                        if (zeros == 5) {
                            if (!isWhite) {
                                cce[currIndex++] = bitOffset
                            }
                            bitOffset += zeros

                            // Last thing written was white
                            isWhite = true
                        } else {
                            bitOffset += zeros

                            cce[currIndex++] = bitOffset
                            setToBlack(bitOffset, 1)
                            ++bitOffset

                            // Last thing written was black
                            isWhite = false
                        }

                    }
                }
            } // while bitOffset < w

            // Add the changing element beyond the current scanline for the
            // other color too, if not already added previously
            if (currIndex <= w)
                cce[currIndex++] = bitOffset

            // Number of changing elements in this scanline.
            changingElemSize = currIndex

            lineBitNum += bitsPerScanline
        } // for lines < height
    }

    private fun setToBlack(bitNum: Int, numBits: Int) {
        var bitNum = bitNum
        // bitNum is relative to current scanline so bump it by lineBitNum
        bitNum += lineBitNum

        val lastBit = bitNum + numBits
        var byteNum = bitNum shr 3

        // Handle bits in first byte
        val shift = bitNum and 0x7
        if (shift > 0) {
            var maskVal = 1 shl 7 - shift
            var `val` = buffer!![byteNum]
            while (maskVal > 0 && bitNum < lastBit) {
                `val` = `val` or maskVal.toByte()
                maskVal = maskVal shr 1
                ++bitNum
            }
            buffer[byteNum] = `val`
        }

        // Fill in 8 bits at a time
        byteNum = bitNum shr 3
        while (bitNum < lastBit - 7) {
            buffer[byteNum++] = 255.toByte()
            bitNum += 8
        }

        // Fill in remaining bits
        while (bitNum < lastBit) {
            byteNum = bitNum shr 3
            buffer[byteNum] = buffer[byteNum] or (1 shl 7 - (bitNum and 0x7)).toByte()
            ++bitNum
        }
    }

    // Returns run length
    private fun decodeWhiteCodeWord(): Int {
        var current: Int
        var entry: Int
        var bits: Int
        var isT: Int
        var twoBits: Int
        var code = -1
        var runLength = 0
        var isWhite = true

        while (isWhite) {
            current = nextNBits(10)
            entry = white[current].toInt()

            // Get the 3 fields from the entry
            isT = entry and 0x0001
            bits = entry.ushr(1) and 0x0f

            if (bits == 12) {
                // Additional Make up code
                // Get the next 2 bits
                twoBits = nextLesserThan8Bits(2)
                // Consolidate the 2 new bits and last 2 bits into 4 bits
                current = current shl 2 and 0x000c or twoBits
                entry = additionalMakeup[current].toInt()
                bits = entry.ushr(1) and 0x07     // 3 bits 0000 0111
                code = entry.ushr(4) and 0x0fff   // 12 bits
                runLength += code
                updatePointer(4 - bits)
            } else if (bits == 0) {
                // ERROR
                throw RuntimeException("Error 0")
            } else if (bits == 15) {
                // EOL
                throw RuntimeException("Error 1")
            } else {
                // 11 bits - 0000 0111 1111 1111 = 0x07ff
                code = entry.ushr(5) and 0x07ff
                runLength += code
                updatePointer(10 - bits)
                if (isT == 0) {
                    isWhite = false
                }
            }
        }

        return runLength
    }

    // Returns run length
    private fun decodeBlackCodeWord(): Int {
        var current: Int
        var entry: Int
        var bits: Int
        var isT: Int
        val twoBits: Int
        var code = -1
        var runLength = 0
        var isWhite = false

        while (!isWhite) {
            current = nextLesserThan8Bits(4)
            entry = initBlack[current].toInt()

            // Get the 3 fields from the entry
            isT = entry and 0x0001
            bits = entry.ushr(1) and 0x000f
            code = entry.ushr(5) and 0x07ff

            if (code == 100) {
                current = nextNBits(9)
                entry = black[current].toInt()

                // Get the 3 fields from the entry
                isT = entry and 0x0001
                bits = entry.ushr(1) and 0x000f
                code = entry.ushr(5) and 0x07ff

                if (bits == 12) {
                    // Additional makeup codes
                    updatePointer(5)
                    current = nextLesserThan8Bits(4)
                    entry = additionalMakeup[current].toInt()
                    bits = entry.ushr(1) and 0x07     // 3 bits 0000 0111
                    code = entry.ushr(4) and 0x0fff  // 12 bits
                    runLength += code

                    updatePointer(4 - bits)
                } else if (bits == 15) {
                    // EOL code
                    throw RuntimeException("Error 2")
                } else {
                    runLength += code
                    updatePointer(9 - bits)
                    if (isT == 0) {
                        isWhite = true
                    }
                }
            } else if (code == 200) {
                // Is a Terminating code
                current = nextLesserThan8Bits(2)
                entry = twoBitBlack[current].toInt()
                code = entry.ushr(5) and 0x07ff
                runLength += code
                bits = entry.ushr(1) and 0x0f
                updatePointer(2 - bits)
                isWhite = true
            } else {
                // Is a Terminating code
                runLength += code
                updatePointer(4 - bits)
                isWhite = true
            }
        }

        return runLength
    }

    private fun findNextLine(): Int {
        // Set maximum and current bit index into the compressed data.
        val bitIndexMax = data!!.size * 8 - 1
        val bitIndexMax12 = bitIndexMax - 12
        var bitIndex = bytePointer * 8 + bitPointer

        // Loop while at least 12 bits are available.
        while (bitIndex <= bitIndexMax12) {
            // Get the next 12 bits.
            var next12Bits = nextNBits(12)
            bitIndex += 12

            // Loop while the 12 bits are not unity, i.e., while the EOL
            // has not been reached, and there is at least one bit left.
            while (next12Bits != 1 && bitIndex < bitIndexMax) {
                next12Bits = next12Bits and 0x000007ff shl 1 or (nextLesserThan8Bits(1) and 0x00000001)
                bitIndex++
            }

            if (next12Bits == 1) {
                // now positioned just after EOL
                if (oneD == 1) {
                    // two-dimensional coding
                    if (bitIndex < bitIndexMax) {
                        // check next bit against type of line being sought
                        return nextLesserThan8Bits(1)
                    }
                } else {
                    return 1
                }
            }
        }

        // EOL not found.
        throw RuntimeException()
    }

    private fun getNextChangingElement(a0: Int, isWhite: Boolean, ret: IntArray) {
        // Local copies of instance variables
        val pce = this.prevChangingElems
        val ces = this.changingElemSize

        // If the previous match was at an odd element, we still
        // have to search the preceeding element.
        // int start = lastChangingElement & ~0x1;
        var start = if (lastChangingElement > 0) lastChangingElement - 1 else 0
        if (isWhite) {
            start = start and 0x1.inv() // Search even numbered elements
        } else {
            start = start or 0x1 // Search odd numbered elements
        }

        var i = start
        while (i < ces) {
            val temp = pce[i]
            if (temp > a0) {
                lastChangingElement = i
                ret[0] = temp
                break
            }
            i += 2
        }

        if (i + 1 < ces) {
            ret[1] = pce[i + 1]
        }
    }

    private fun nextNBits(bitsToGet: Int): Int {
        val b: Byte
        val next: Byte
        val next2next: Byte
        val l = data!!.size - 1
        val bp = this.bytePointer

        if (fillOrder == 1) {
            b = data!![bp]

            if (bp == l) {
                next = 0x00
                next2next = 0x00
            } else if (bp + 1 == l) {
                next = data!![bp + 1]
                next2next = 0x00
            } else {
                next = data!![bp + 1]
                next2next = data!![bp + 2]
            }
        } else if (fillOrder == 2) {
            b = flipTable[data!![bp] and 0xff]

            if (bp == l) {
                next = 0x00
                next2next = 0x00
            } else if (bp + 1 == l) {
                next = flipTable[data!![bp + 1] and 0xff]
                next2next = 0x00
            } else {
                next = flipTable[data!![bp + 1] and 0xff]
                next2next = flipTable[data!![bp + 2] and 0xff]
            }
        } else {
            throw RuntimeException("Invalid FillOrder")
        }

        val bitsLeft = 8 - bitPointer
        var bitsFromNextByte = bitsToGet - bitsLeft
        var bitsFromNext2NextByte = 0
        if (bitsFromNextByte > 8) {
            bitsFromNext2NextByte = bitsFromNextByte - 8
            bitsFromNextByte = 8
        }

        bytePointer++

        val i1 = b and table1[bitsLeft] shl bitsToGet - bitsLeft
        var i2 = (next and table2[bitsFromNextByte]).ushr(8 - bitsFromNextByte)

        var i3 = 0
        if (bitsFromNext2NextByte != 0) {
            i2 = i2 shl bitsFromNext2NextByte
            i3 = (next2next and table2[bitsFromNext2NextByte]).ushr(8 - bitsFromNext2NextByte)
            i2 = i2 or i3
            bytePointer++
            bitPointer = bitsFromNext2NextByte
        } else {
            if (bitsFromNextByte == 8) {
                bitPointer = 0
                bytePointer++
            } else {
                bitPointer = bitsFromNextByte
            }
        }

        val i = i1 or i2
        return i
    }

    private fun nextLesserThan8Bits(bitsToGet: Int): Int {
        val b: Byte
        val next: Byte
        val l = data!!.size - 1
        val bp = this.bytePointer

        if (fillOrder == 1) {
            b = data!![bp]
            if (bp == l) {
                next = 0x00
            } else {
                next = data!![bp + 1]
            }
        } else if (fillOrder == 2) {
            b = flipTable[data!![bp] and 0xff]
            if (bp == l) {
                next = 0x00
            } else {
                next = flipTable[data!![bp + 1] and 0xff]
            }
        } else {
            throw RuntimeException("Invalid FillOrder")
        }

        val bitsLeft = 8 - bitPointer
        val bitsFromNextByte = bitsToGet - bitsLeft

        val shift = bitsLeft - bitsToGet
        var i1: Int
        val i2: Int
        if (shift >= 0) {
            i1 = (b and table1[bitsLeft]).ushr(shift)
            bitPointer += bitsToGet
            if (bitPointer == 8) {
                bitPointer = 0
                bytePointer++
            }
        } else {
            i1 = b and table1[bitsLeft] shl -shift
            i2 = (next and table2[bitsFromNextByte]).ushr(8 - bitsFromNextByte)

            i1 = i1 or i2
            bytePointer++
            bitPointer = bitsFromNextByte
        }

        return i1
    }

    // Move pointer backwards by given amount of bits
    private fun updatePointer(bitsToMoveBack: Int) {
        var bitsToMoveBack = bitsToMoveBack
        if (bitsToMoveBack > 8) {
            bytePointer -= bitsToMoveBack / 8
            bitsToMoveBack %= 8
        }

        val i = bitPointer - bitsToMoveBack
        if (i < 0) {
            bytePointer--
            bitPointer = 8 + i
        } else {
            bitPointer = i
        }
    }

    companion object {
        internal var table1 = intArrayOf(0x00, // 0 bits are left in first byte - SHOULD NOT HAPPEN
                0x01, // 1 bits are left in first byte
                0x03, // 2 bits are left in first byte
                0x07, // 3 bits are left in first byte
                0x0f, // 4 bits are left in first byte
                0x1f, // 5 bits are left in first byte
                0x3f, // 6 bits are left in first byte
                0x7f, // 7 bits are left in first byte
                0xff // 8 bits are left in first byte
        )
        internal var table2 = intArrayOf(0x00, // 0
                0x80, // 1
                0xc0, // 2
                0xe0, // 3
                0xf0, // 4
                0xf8, // 5
                0xfc, // 6
                0xfe, // 7
                0xff // 8
        )
        // Table to be used when fillOrder = 2, for flipping bytes.
        internal var flipTable = byteArrayOf(0, -128, 64, -64, 32, -96, 96, -32, 16, -112, 80, -48, 48, -80, 112, -16, 8, -120, 72, -56, 40, -88, 104, -24, 24, -104, 88, -40, 56, -72, 120, -8, 4, -124, 68, -60, 36, -92, 100, -28, 20, -108, 84, -44, 52, -76, 116, -12, 12, -116, 76, -52, 44, -84, 108, -20, 28, -100, 92, -36, 60, -68, 124, -4, 2, -126, 66, -62, 34, -94, 98, -30, 18, -110, 82, -46, 50, -78, 114, -14, 10, -118, 74, -54, 42, -86, 106, -22, 26, -102, 90, -38, 58, -70, 122, -6, 6, -122, 70, -58, 38, -90, 102, -26, 22, -106, 86, -42, 54, -74, 118, -10, 14, -114, 78, -50, 46, -82, 110, -18, 30, -98, 94, -34, 62, -66, 126, -2, 1, -127, 65, -63, 33, -95, 97, -31, 17, -111, 81, -47, 49, -79, 113, -15, 9, -119, 73, -55, 41, -87, 105, -23, 25, -103, 89, -39, 57, -71, 121, -7, 5, -123, 69, -59, 37, -91, 101, -27, 21, -107, 85, -43, 53, -75, 117, -11, 13, -115, 77, -51, 45, -83, 109, -19, 29, -99, 93, -35, 61, -67, 125, -3, 3, -125, 67, -61, 35, -93, 99, -29, 19, -109, 83, -45, 51, -77, 115, -13, 11, -117, 75, -53, 43, -85, 107, -21, 27, -101, 91, -37, 59, -69, 123, -5, 7, -121, 71, -57, 39, -89, 103, -25, 23, -105, 87, -41, 55, -73, 119, -9, 15, -113, 79, -49, 47, -81, 111, -17, 31, -97, 95, -33, 63, -65, 127, -1)
        // The main 10 bit white runs lookup table
        internal var white = shortArrayOf(// 0 - 7
                6430, 6400, 6400, 6400, 3225, 3225, 3225, 3225, // 8 - 15
                944, 944, 944, 944, 976, 976, 976, 976, // 16 - 23
                1456, 1456, 1456, 1456, 1488, 1488, 1488, 1488, // 24 - 31
                718, 718, 718, 718, 718, 718, 718, 718, // 32 - 39
                750, 750, 750, 750, 750, 750, 750, 750, // 40 - 47
                1520, 1520, 1520, 1520, 1552, 1552, 1552, 1552, // 48 - 55
                428, 428, 428, 428, 428, 428, 428, 428, // 56 - 63
                428, 428, 428, 428, 428, 428, 428, 428, // 64 - 71
                654, 654, 654, 654, 654, 654, 654, 654, // 72 - 79
                1072, 1072, 1072, 1072, 1104, 1104, 1104, 1104, // 80 - 87
                1136, 1136, 1136, 1136, 1168, 1168, 1168, 1168, // 88 - 95
                1200, 1200, 1200, 1200, 1232, 1232, 1232, 1232, // 96 - 103
                622, 622, 622, 622, 622, 622, 622, 622, // 104 - 111
                1008, 1008, 1008, 1008, 1040, 1040, 1040, 1040, // 112 - 119
                44, 44, 44, 44, 44, 44, 44, 44, // 120 - 127
                44, 44, 44, 44, 44, 44, 44, 44, // 128 - 135
                396, 396, 396, 396, 396, 396, 396, 396, // 136 - 143
                396, 396, 396, 396, 396, 396, 396, 396, // 144 - 151
                1712, 1712, 1712, 1712, 1744, 1744, 1744, 1744, // 152 - 159
                846, 846, 846, 846, 846, 846, 846, 846, // 160 - 167
                1264, 1264, 1264, 1264, 1296, 1296, 1296, 1296, // 168 - 175
                1328, 1328, 1328, 1328, 1360, 1360, 1360, 1360, // 176 - 183
                1392, 1392, 1392, 1392, 1424, 1424, 1424, 1424, // 184 - 191
                686, 686, 686, 686, 686, 686, 686, 686, // 192 - 199
                910, 910, 910, 910, 910, 910, 910, 910, // 200 - 207
                1968, 1968, 1968, 1968, 2000, 2000, 2000, 2000, // 208 - 215
                2032, 2032, 2032, 2032, 16, 16, 16, 16, // 216 - 223
                10257, 10257, 10257, 10257, 12305, 12305, 12305, 12305, // 224 - 231
                330, 330, 330, 330, 330, 330, 330, 330, // 232 - 239
                330, 330, 330, 330, 330, 330, 330, 330, // 240 - 247
                330, 330, 330, 330, 330, 330, 330, 330, // 248 - 255
                330, 330, 330, 330, 330, 330, 330, 330, // 256 - 263
                362, 362, 362, 362, 362, 362, 362, 362, // 264 - 271
                362, 362, 362, 362, 362, 362, 362, 362, // 272 - 279
                362, 362, 362, 362, 362, 362, 362, 362, // 280 - 287
                362, 362, 362, 362, 362, 362, 362, 362, // 288 - 295
                878, 878, 878, 878, 878, 878, 878, 878, // 296 - 303
                1904, 1904, 1904, 1904, 1936, 1936, 1936, 1936, // 304 - 311
                -18413, -18413, -16365, -16365, -14317, -14317, -10221, -10221, // 312 - 319
                590, 590, 590, 590, 590, 590, 590, 590, // 320 - 327
                782, 782, 782, 782, 782, 782, 782, 782, // 328 - 335
                1584, 1584, 1584, 1584, 1616, 1616, 1616, 1616, // 336 - 343
                1648, 1648, 1648, 1648, 1680, 1680, 1680, 1680, // 344 - 351
                814, 814, 814, 814, 814, 814, 814, 814, // 352 - 359
                1776, 1776, 1776, 1776, 1808, 1808, 1808, 1808, // 360 - 367
                1840, 1840, 1840, 1840, 1872, 1872, 1872, 1872, // 368 - 375
                6157, 6157, 6157, 6157, 6157, 6157, 6157, 6157, // 376 - 383
                6157, 6157, 6157, 6157, 6157, 6157, 6157, 6157, // 384 - 391
                -12275, -12275, -12275, -12275, -12275, -12275, -12275, -12275, // 392 - 399
                -12275, -12275, -12275, -12275, -12275, -12275, -12275, -12275, // 400 - 407
                14353, 14353, 14353, 14353, 16401, 16401, 16401, 16401, // 408 - 415
                22547, 22547, 24595, 24595, 20497, 20497, 20497, 20497, // 416 - 423
                18449, 18449, 18449, 18449, 26643, 26643, 28691, 28691, // 424 - 431
                30739, 30739, -32749, -32749, -30701, -30701, -28653, -28653, // 432 - 439
                -26605, -26605, -24557, -24557, -22509, -22509, -20461, -20461, // 440 - 447
                8207, 8207, 8207, 8207, 8207, 8207, 8207, 8207, // 448 - 455
                72, 72, 72, 72, 72, 72, 72, 72, // 456 - 463
                72, 72, 72, 72, 72, 72, 72, 72, // 464 - 471
                72, 72, 72, 72, 72, 72, 72, 72, // 472 - 479
                72, 72, 72, 72, 72, 72, 72, 72, // 480 - 487
                72, 72, 72, 72, 72, 72, 72, 72, // 488 - 495
                72, 72, 72, 72, 72, 72, 72, 72, // 496 - 503
                72, 72, 72, 72, 72, 72, 72, 72, // 504 - 511
                72, 72, 72, 72, 72, 72, 72, 72, // 512 - 519
                104, 104, 104, 104, 104, 104, 104, 104, // 520 - 527
                104, 104, 104, 104, 104, 104, 104, 104, // 528 - 535
                104, 104, 104, 104, 104, 104, 104, 104, // 536 - 543
                104, 104, 104, 104, 104, 104, 104, 104, // 544 - 551
                104, 104, 104, 104, 104, 104, 104, 104, // 552 - 559
                104, 104, 104, 104, 104, 104, 104, 104, // 560 - 567
                104, 104, 104, 104, 104, 104, 104, 104, // 568 - 575
                104, 104, 104, 104, 104, 104, 104, 104, // 576 - 583
                4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, // 584 - 591
                4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, // 592 - 599
                4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, // 600 - 607
                4107, 4107, 4107, 4107, 4107, 4107, 4107, 4107, // 608 - 615
                266, 266, 266, 266, 266, 266, 266, 266, // 616 - 623
                266, 266, 266, 266, 266, 266, 266, 266, // 624 - 631
                266, 266, 266, 266, 266, 266, 266, 266, // 632 - 639
                266, 266, 266, 266, 266, 266, 266, 266, // 640 - 647
                298, 298, 298, 298, 298, 298, 298, 298, // 648 - 655
                298, 298, 298, 298, 298, 298, 298, 298, // 656 - 663
                298, 298, 298, 298, 298, 298, 298, 298, // 664 - 671
                298, 298, 298, 298, 298, 298, 298, 298, // 672 - 679
                524, 524, 524, 524, 524, 524, 524, 524, // 680 - 687
                524, 524, 524, 524, 524, 524, 524, 524, // 688 - 695
                556, 556, 556, 556, 556, 556, 556, 556, // 696 - 703
                556, 556, 556, 556, 556, 556, 556, 556, // 704 - 711
                136, 136, 136, 136, 136, 136, 136, 136, // 712 - 719
                136, 136, 136, 136, 136, 136, 136, 136, // 720 - 727
                136, 136, 136, 136, 136, 136, 136, 136, // 728 - 735
                136, 136, 136, 136, 136, 136, 136, 136, // 736 - 743
                136, 136, 136, 136, 136, 136, 136, 136, // 744 - 751
                136, 136, 136, 136, 136, 136, 136, 136, // 752 - 759
                136, 136, 136, 136, 136, 136, 136, 136, // 760 - 767
                136, 136, 136, 136, 136, 136, 136, 136, // 768 - 775
                168, 168, 168, 168, 168, 168, 168, 168, // 776 - 783
                168, 168, 168, 168, 168, 168, 168, 168, // 784 - 791
                168, 168, 168, 168, 168, 168, 168, 168, // 792 - 799
                168, 168, 168, 168, 168, 168, 168, 168, // 800 - 807
                168, 168, 168, 168, 168, 168, 168, 168, // 808 - 815
                168, 168, 168, 168, 168, 168, 168, 168, // 816 - 823
                168, 168, 168, 168, 168, 168, 168, 168, // 824 - 831
                168, 168, 168, 168, 168, 168, 168, 168, // 832 - 839
                460, 460, 460, 460, 460, 460, 460, 460, // 840 - 847
                460, 460, 460, 460, 460, 460, 460, 460, // 848 - 855
                492, 492, 492, 492, 492, 492, 492, 492, // 856 - 863
                492, 492, 492, 492, 492, 492, 492, 492, // 864 - 871
                2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, // 872 - 879
                2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, // 880 - 887
                2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, // 888 - 895
                2059, 2059, 2059, 2059, 2059, 2059, 2059, 2059, // 896 - 903
                200, 200, 200, 200, 200, 200, 200, 200, // 904 - 911
                200, 200, 200, 200, 200, 200, 200, 200, // 912 - 919
                200, 200, 200, 200, 200, 200, 200, 200, // 920 - 927
                200, 200, 200, 200, 200, 200, 200, 200, // 928 - 935
                200, 200, 200, 200, 200, 200, 200, 200, // 936 - 943
                200, 200, 200, 200, 200, 200, 200, 200, // 944 - 951
                200, 200, 200, 200, 200, 200, 200, 200, // 952 - 959
                200, 200, 200, 200, 200, 200, 200, 200, // 960 - 967
                232, 232, 232, 232, 232, 232, 232, 232, // 968 - 975
                232, 232, 232, 232, 232, 232, 232, 232, // 976 - 983
                232, 232, 232, 232, 232, 232, 232, 232, // 984 - 991
                232, 232, 232, 232, 232, 232, 232, 232, // 992 - 999
                232, 232, 232, 232, 232, 232, 232, 232, // 1000 - 1007
                232, 232, 232, 232, 232, 232, 232, 232, // 1008 - 1015
                232, 232, 232, 232, 232, 232, 232, 232, // 1016 - 1023
                232, 232, 232, 232, 232, 232, 232, 232)
        // Additional make up codes for both White and Black runs
        internal var additionalMakeup = shortArrayOf(28679, 28679, 31752, 32777.toShort(), 33801.toShort(), 34825.toShort(), 35849.toShort(), 36873.toShort(), 29703.toShort(), 29703.toShort(), 30727.toShort(), 30727.toShort(), 37897.toShort(), 38921.toShort(), 39945.toShort(), 40969.toShort())
        // Initial black run look up table, uses the first 4 bits of a code
        internal var initBlack = shortArrayOf(// 0 - 7
                3226, 6412, 200, 168, 38, 38, 134, 134, // 8 - 15
                100, 100, 100, 100, 68, 68, 68, 68)
        // 
        internal var twoBitBlack = shortArrayOf(292, 260, 226, 226)   // 0 - 3
        // Main black run table, using the last 9 bits of possible 13 bit code
        internal var black = shortArrayOf(// 0 - 7
                62, 62, 30, 30, 0, 0, 0, 0, // 8 - 15
                0, 0, 0, 0, 0, 0, 0, 0, // 16 - 23
                0, 0, 0, 0, 0, 0, 0, 0, // 24 - 31
                0, 0, 0, 0, 0, 0, 0, 0, // 32 - 39
                3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, // 40 - 47
                3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, // 48 - 55
                3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, // 56 - 63
                3225, 3225, 3225, 3225, 3225, 3225, 3225, 3225, // 64 - 71
                588, 588, 588, 588, 588, 588, 588, 588, // 72 - 79
                1680, 1680, 20499, 22547, 24595, 26643, 1776, 1776, // 80 - 87
                1808, 1808, -24557, -22509, -20461, -18413, 1904, 1904, // 88 - 95
                1936, 1936, -16365, -14317, 782, 782, 782, 782, // 96 - 103
                814, 814, 814, 814, -12269, -10221, 10257, 10257, // 104 - 111
                12305, 12305, 14353, 14353, 16403, 18451, 1712, 1712, // 112 - 119
                1744, 1744, 28691, 30739, -32749, -30701, -28653, -26605, // 120 - 127
                2061, 2061, 2061, 2061, 2061, 2061, 2061, 2061, // 128 - 135
                424, 424, 424, 424, 424, 424, 424, 424, // 136 - 143
                424, 424, 424, 424, 424, 424, 424, 424, // 144 - 151
                424, 424, 424, 424, 424, 424, 424, 424, // 152 - 159
                424, 424, 424, 424, 424, 424, 424, 424, // 160 - 167
                750, 750, 750, 750, 1616, 1616, 1648, 1648, // 168 - 175
                1424, 1424, 1456, 1456, 1488, 1488, 1520, 1520, // 176 - 183
                1840, 1840, 1872, 1872, 1968, 1968, 8209, 8209, // 184 - 191
                524, 524, 524, 524, 524, 524, 524, 524, // 192 - 199
                556, 556, 556, 556, 556, 556, 556, 556, // 200 - 207
                1552, 1552, 1584, 1584, 2000, 2000, 2032, 2032, // 208 - 215
                976, 976, 1008, 1008, 1040, 1040, 1072, 1072, // 216 - 223
                1296, 1296, 1328, 1328, 718, 718, 718, 718, // 224 - 231
                456, 456, 456, 456, 456, 456, 456, 456, // 232 - 239
                456, 456, 456, 456, 456, 456, 456, 456, // 240 - 247
                456, 456, 456, 456, 456, 456, 456, 456, // 248 - 255
                456, 456, 456, 456, 456, 456, 456, 456, // 256 - 263
                326, 326, 326, 326, 326, 326, 326, 326, // 264 - 271
                326, 326, 326, 326, 326, 326, 326, 326, // 272 - 279
                326, 326, 326, 326, 326, 326, 326, 326, // 280 - 287
                326, 326, 326, 326, 326, 326, 326, 326, // 288 - 295
                326, 326, 326, 326, 326, 326, 326, 326, // 296 - 303
                326, 326, 326, 326, 326, 326, 326, 326, // 304 - 311
                326, 326, 326, 326, 326, 326, 326, 326, // 312 - 319
                326, 326, 326, 326, 326, 326, 326, 326, // 320 - 327
                358, 358, 358, 358, 358, 358, 358, 358, // 328 - 335
                358, 358, 358, 358, 358, 358, 358, 358, // 336 - 343
                358, 358, 358, 358, 358, 358, 358, 358, // 344 - 351
                358, 358, 358, 358, 358, 358, 358, 358, // 352 - 359
                358, 358, 358, 358, 358, 358, 358, 358, // 360 - 367
                358, 358, 358, 358, 358, 358, 358, 358, // 368 - 375
                358, 358, 358, 358, 358, 358, 358, 358, // 376 - 383
                358, 358, 358, 358, 358, 358, 358, 358, // 384 - 391
                490, 490, 490, 490, 490, 490, 490, 490, // 392 - 399
                490, 490, 490, 490, 490, 490, 490, 490, // 400 - 407
                4113, 4113, 6161, 6161, 848, 848, 880, 880, // 408 - 415
                912, 912, 944, 944, 622, 622, 622, 622, // 416 - 423
                654, 654, 654, 654, 1104, 1104, 1136, 1136, // 424 - 431
                1168, 1168, 1200, 1200, 1232, 1232, 1264, 1264, // 432 - 439
                686, 686, 686, 686, 1360, 1360, 1392, 1392, // 440 - 447
                12, 12, 12, 12, 12, 12, 12, 12, // 448 - 455
                390, 390, 390, 390, 390, 390, 390, 390, // 456 - 463
                390, 390, 390, 390, 390, 390, 390, 390, // 464 - 471
                390, 390, 390, 390, 390, 390, 390, 390, // 472 - 479
                390, 390, 390, 390, 390, 390, 390, 390, // 480 - 487
                390, 390, 390, 390, 390, 390, 390, 390, // 488 - 495
                390, 390, 390, 390, 390, 390, 390, 390, // 496 - 503
                390, 390, 390, 390, 390, 390, 390, 390, // 504 - 511
                390, 390, 390, 390, 390, 390, 390, 390)
        internal var twoDCodes = byteArrayOf(// 0 - 7
                80, 88, 23, 71, 30, 30, 62, 62, // 8 - 15
                4, 4, 4, 4, 4, 4, 4, 4, // 16 - 23
                11, 11, 11, 11, 11, 11, 11, 11, // 24 - 31
                11, 11, 11, 11, 11, 11, 11, 11, // 32 - 39
                35, 35, 35, 35, 35, 35, 35, 35, // 40 - 47
                35, 35, 35, 35, 35, 35, 35, 35, // 48 - 55
                51, 51, 51, 51, 51, 51, 51, 51, // 56 - 63
                51, 51, 51, 51, 51, 51, 51, 51, // 64 - 71
                41, 41, 41, 41, 41, 41, 41, 41, // 72 - 79
                41, 41, 41, 41, 41, 41, 41, 41, // 80 - 87
                41, 41, 41, 41, 41, 41, 41, 41, // 88 - 95
                41, 41, 41, 41, 41, 41, 41, 41, // 96 - 103
                41, 41, 41, 41, 41, 41, 41, 41, // 104 - 111
                41, 41, 41, 41, 41, 41, 41, 41, // 112 - 119
                41, 41, 41, 41, 41, 41, 41, 41, // 120 - 127
                41, 41, 41, 41, 41, 41, 41, 41)
    }
}

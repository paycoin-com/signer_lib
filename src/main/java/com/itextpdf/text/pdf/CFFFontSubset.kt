/*
 * $Id: c836a5e48fa74e74d8fe83d56b5c2f17927d4c47 $
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

import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList

/**
 * This Class subsets a CFF Type Font. The subset is preformed for CID fonts and NON CID fonts.
 * The Charstring is subsetted for both types. For CID fonts only the FDArray which are used are embedded.
 * The Lsubroutines of the FDArrays used are subsetted as well. The Subroutine subset supports both Type1 and Type2
 * formatting although only tested on Type2 Format.
 * For Non CID the Lsubroutines are subsetted. On both types the Gsubroutines is subsetted.
 * A font which was not of CID type is transformed into CID as a part of the subset process.
 * The CID synthetic creation was written by Sivan Toledo (sivan@math.tau.ac.il)
 * @author Oren Manor (manorore@post.tau.ac.il) and Ygal Blum (blumygal@post.tau.ac.il)
 */
class CFFFontSubset
/**
 * C'tor for CFFFontSubset
 * @param rf - The font file
 * *
 * @param GlyphsUsed - a HashMap that contains the glyph used in the subset
 */
(rf: RandomAccessFileOrArray,
 /**
  * A HashMap containing the glyphs used in the text after being converted
  * to glyph number by the CMap
  */
 internal var GlyphsUsed: HashMap<Int, IntArray>) : CFFFont(rf) {
    /**
     * The GlyphsUsed keys as an ArrayList
     */
    internal var glyphsInList: ArrayList<Int>
    /**
     * A HashSet for keeping the FDArrays being used by the font
     */
    internal var FDArrayUsed = HashSet<Int>()
    /**
     * A HashMaps array for keeping the subroutines used in each FontDict
     */
    internal var hSubrsUsed: Array<HashMap<Int, IntArray>>
    /**
     * The SubroutinesUsed HashMaps as ArrayLists
     */
    internal var lSubrsUsed: Array<ArrayList<Int>>
    /**
     * A HashMap for keeping the Global subroutines used in the font
     */
    internal var hGSubrsUsed = HashMap<Int, IntArray>()
    /**
     * The Global SubroutinesUsed HashMaps as ArrayLists
     */
    internal var lGSubrsUsed = ArrayList<Int>()
    /**
     * A HashMap for keeping the subroutines used in a non-cid font
     */
    internal var hSubrsUsedNonCID = HashMap<Int, IntArray>()
    /**
     * The SubroutinesUsed HashMap as ArrayList
     */
    internal var lSubrsUsedNonCID = ArrayList<Int>()
    /**
     * An array of the new Indexes for the local Subr. One index for each FontDict
     */
    internal var NewLSubrsIndex: Array<ByteArray>
    /**
     * The new subroutines index for a non-cid font
     */
    internal var NewSubrsIndexNonCID: ByteArray? = null
    /**
     * The new global subroutines index of the font
     */
    internal var NewGSubrsIndex: ByteArray
    /**
     * The new CharString of the font
     */
    internal var NewCharStringsIndex: ByteArray

    /**
     * The bias for the global subroutines
     */
    internal var GBias = 0

    /**
     * The linked list for generating the new font stream
     */
    internal var OutputList: LinkedList<CFFFont.Item>

    /**
     * Number of arguments to the stem operators in a subroutine calculated recursively
     */
    internal var NumOfHints = 0


    init {
        //Put the glyphs into a list
        glyphsInList = ArrayList(GlyphsUsed.keys)


        for (i in fonts.indices) {
            // Read the number of glyphs in the font
            seek(fonts[i].charstringsOffset)
            fonts[i].nglyphs = card16.toInt()

            // Jump to the count field of the String Index
            seek(stringIndexOffset)
            fonts[i].nstrings = card16.toInt() + CFFFont.standardStrings.size

            // For each font save the offset array of the charstring
            fonts[i].charstringsOffsets = getIndex(fonts[i].charstringsOffset)

            // Process the FDSelect if exist
            if (fonts[i].fdselectOffset >= 0) {
                // Process the FDSelect
                readFDSelect(i)
                // Build the FDArrayUsed hashmap
                BuildFDArrayUsed(i)
            }
            if (fonts[i].isCID)
            // Build the FD Array used Hash Map
                ReadFDArray(i)
            // compute the charset length
            fonts[i].CharsetLength = CountCharset(fonts[i].charsetOffset, fonts[i].nglyphs)
        }
    }// Use CFFFont c'tor in order to parse the font file.

    /**
     * Calculates the length of the charset according to its format
     * @param Offset The Charset Offset
     * *
     * @param NumofGlyphs Number of glyphs in the font
     * *
     * @return the length of the Charset
     */
    internal fun CountCharset(Offset: Int, NumofGlyphs: Int): Int {
        val format: Int
        var Length = 0
        seek(Offset)
        // Read the format
        format = card8.toInt()
        // Calc according to format
        when (format) {
            0 -> Length = 1 + 2 * NumofGlyphs
            1 -> Length = 1 + 3 * CountRange(NumofGlyphs, 1)
            2 -> Length = 1 + 4 * CountRange(NumofGlyphs, 2)
            else -> {
            }
        }
        return Length
    }

    /**
     * Function calculates the number of ranges in the Charset
     * @param NumofGlyphs The number of glyphs in the font
     * *
     * @param Type The format of the Charset
     * *
     * @return The number of ranges in the Charset data structure
     */
    internal fun CountRange(NumofGlyphs: Int, Type: Int): Int {
        var num = 0
        var Sid: Char
        var i = 1
        var nLeft: Int
        while (i < NumofGlyphs) {
            num++
            Sid = card16
            if (Type == 1)
                nLeft = card8.toInt()
            else
                nLeft = card16.toInt()
            i += nLeft + 1
        }
        return num
    }


    /**
     * Read the FDSelect of the font and compute the array and its length
     * @param Font The index of the font being processed
     */
    protected fun readFDSelect(Font: Int) {
        // Restore the number of glyphs
        val NumOfGlyphs = fonts[Font].nglyphs
        val FDSelect = IntArray(NumOfGlyphs)
        // Go to the beginning of the FDSelect
        seek(fonts[Font].fdselectOffset)
        // Read the FDSelect's format
        fonts[Font].FDSelectFormat = card8.toInt()

        when (fonts[Font].FDSelectFormat) {
        // Format==0 means each glyph has an entry that indicated
        // its FD.
            0 -> {
                for (i in 0..NumOfGlyphs - 1) {
                    FDSelect[i] = card8.toInt()
                }
                // The FDSelect's Length is one for each glyph + the format
                // for later use
                fonts[Font].FDSelectLength = fonts[Font].nglyphs + 1
            }
            3 -> {
                // Format==3 means the ranges version
                // The number of ranges
                val nRanges = card16.toInt()
                var l = 0
                // Read the first in the first range
                var first = card16.toInt()
                for (i in 0..nRanges - 1) {
                    // Read the FD index
                    val fd = card8.toInt()
                    // Read the first of the next range
                    val last = card16.toInt()
                    // Calc the steps and write to the array
                    val steps = last - first
                    for (k in 0..steps - 1) {
                        FDSelect[l] = fd
                        l++
                    }
                    // The last from this iteration is the first of the next
                    first = last
                }
                // Store the length for later use
                fonts[Font].FDSelectLength = 1 + 2 + nRanges * 3 + 2
            }
            else -> {
            }
        }
        // Save the FDSelect of the font
        fonts[Font].FDSelect = FDSelect
    }

    /**
     * Function reads the FDSelect and builds the FDArrayUsed HashMap According to the glyphs used
     * @param Font the Number of font being processed
     */
    protected fun BuildFDArrayUsed(Font: Int) {
        val FDSelect = fonts[Font].FDSelect
        // For each glyph used
        for (i in glyphsInList.indices) {
            // Pop the glyphs index
            val glyph = glyphsInList[i].toInt()
            // Pop the glyph's FD
            val FD = FDSelect[glyph]
            // Put the FD index into the FDArrayUsed HashMap
            FDArrayUsed.add(Integer.valueOf(FD))
        }
    }

    /**
     * Read the FDArray count, offsize and Offset array
     * @param Font
     */
    protected fun ReadFDArray(Font: Int) {
        seek(fonts[Font].fdarrayOffset)
        fonts[Font].FDArrayCount = card16.toInt()
        fonts[Font].FDArrayOffsize = card8.toInt()
        // Since we will change values inside the FDArray objects
        // We increase its offsize to prevent errors
        if (fonts[Font].FDArrayOffsize < 4)
            fonts[Font].FDArrayOffsize++
        fonts[Font].FDArrayOffsets = getIndex(fonts[Font].fdarrayOffset)
    }


    /**
     * The Process function extracts one font out of the CFF file and returns a
     * subset version of the original.
     * @param fontName - The name of the font to be taken out of the CFF
     * *
     * @return The new font stream
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun Process(fontName: String): ByteArray? {
        try {
            // Verify that the file is open
            buf.reOpen()
            // Find the Font that we will be dealing with
            var j: Int
            j = 0
            while (j < fonts.size) {
                if (fontName == fonts[j].name) break
                j++
            }
            if (j == fonts.size) return null

            // Calc the bias for the global subrs
            if (gsubrIndexOffset >= 0)
                GBias = CalcBias(gsubrIndexOffset, j)

            // Prepare the new CharStrings Index
            BuildNewCharString(j)
            // Prepare the new Global and Local Subrs Indices
            BuildNewLGSubrs(j)
            // Build the new file
            val Ret = BuildNewFile(j)
            return Ret
        } finally {
            try {
                buf.close()
            } catch (e: Exception) {
                // empty on purpose
            }

        }
    }

    /**
     * Function calcs bias according to the CharString type and the count
     * of the subrs
     * @param Offset The offset to the relevant subrs index
     * *
     * @param Font the font
     * *
     * @return The calculated Bias
     */
    protected fun CalcBias(Offset: Int, Font: Int): Int {
        seek(Offset)
        val nSubrs = card16.toInt()
        // If type==1 -> bias=0
        if (fonts[Font].CharstringType == 1)
            return 0
        else if (nSubrs < 1240)
            return 107
        else if (nSubrs < 33900)
            return 1131
        else
            return 32768// else calc according to the count
    }

    /**
     * Function uses BuildNewIndex to create the new index of the subset charstrings
     * @param FontIndex the font
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    protected fun BuildNewCharString(FontIndex: Int) {
        NewCharStringsIndex = BuildNewIndex(fonts[FontIndex].charstringsOffsets, GlyphsUsed, ENDCHAR_OP)
    }

    /**
     * Function builds the new local & global subsrs indices. IF CID then All of
     * the FD Array lsubrs will be subsetted.
     * @param Font the font
     * *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Throws(IOException::class)
    protected fun BuildNewLGSubrs(Font: Int) {
        // If the font is CID then the lsubrs are divided into FontDicts.
        // for each FD array the lsubrs will be subsetted.
        if (fonts[Font].isCID) {
            // Init the hashmap-array and the arraylist-array to hold the subrs used
            // in each private dict.
            hSubrsUsed = arrayOfNulls<HashMap<Any, Any>>(fonts[Font].fdprivateOffsets.size)
            lSubrsUsed = arrayOfNulls<ArrayList<Any>>(fonts[Font].fdprivateOffsets.size)
            // A [][] which will store the byte array for each new FD Array lsubs index
            NewLSubrsIndex = arrayOfNulls<ByteArray>(fonts[Font].fdprivateOffsets.size)
            // An array to hold the offset for each Lsubr index
            fonts[Font].PrivateSubrsOffset = IntArray(fonts[Font].fdprivateOffsets.size)
            // A [][] which will store the offset array for each lsubr index
            fonts[Font].PrivateSubrsOffsetsArray = arrayOfNulls<IntArray>(fonts[Font].fdprivateOffsets.size)

            // Put the FDarrayUsed into a list
            val FDInList = ArrayList(FDArrayUsed)
            // For each FD array which is used subset the lsubr
            for (j in FDInList.indices) {
                // The FDArray index, Hash Map, Array List to work on
                val FD = FDInList[j].toInt()
                hSubrsUsed[FD] = HashMap<Int, IntArray>()
                lSubrsUsed[FD] = ArrayList<Int>()
                //Reads the private dicts looking for the subr operator and
                // store both the offset for the index and its offset array
                BuildFDSubrsOffsets(Font, FD)
                // Verify that FDPrivate has a LSubrs index
                if (fonts[Font].PrivateSubrsOffset[FD] >= 0) {
                    //Scans the Charstring data storing the used Local and Global subroutines
                    // by the glyphs. Scans the Subrs recursively.
                    BuildSubrUsed(Font, FD, fonts[Font].PrivateSubrsOffset[FD], fonts[Font].PrivateSubrsOffsetsArray[FD], hSubrsUsed[FD], lSubrsUsed[FD])
                    // Builds the New Local Subrs index
                    NewLSubrsIndex[FD] = BuildNewIndex(fonts[Font].PrivateSubrsOffsetsArray[FD], hSubrsUsed[FD], RETURN_OP)
                }
            }
        } else if (fonts[Font].privateSubrs >= 0) {
            // Build the subrs offsets;
            fonts[Font].SubrsOffsets = getIndex(fonts[Font].privateSubrs)
            //Scans the Charstring data storing the used Local and Global subroutines
            // by the glyphs. Scans the Subrs recursively.
            BuildSubrUsed(Font, -1, fonts[Font].privateSubrs, fonts[Font].SubrsOffsets, hSubrsUsedNonCID, lSubrsUsedNonCID)
        }// If the font is not CID && the Private Subr exists then subset:
        // For all fonts subset the Global Subroutines
        // Scan the Global Subr Hashmap recursively on the Gsubrs
        BuildGSubrsUsed(Font)
        if (fonts[Font].privateSubrs >= 0)
        // Builds the New Local Subrs index
            NewSubrsIndexNonCID = BuildNewIndex(fonts[Font].SubrsOffsets, hSubrsUsedNonCID, RETURN_OP)
        //Builds the New Global Subrs index
        NewGSubrsIndex = BuildNewIndex(gsubrOffsets, hGSubrsUsed, RETURN_OP)
    }

    /**
     * The function finds for the FD array processed the local subr offset and its
     * offset array.
     * @param Font the font
     * *
     * @param FD The FDARRAY processed
     */
    protected fun BuildFDSubrsOffsets(Font: Int, FD: Int) {
        // Initiate to -1 to indicate lsubr operator present
        fonts[Font].PrivateSubrsOffset[FD] = -1
        // Goto beginning of objects
        seek(fonts[Font].fdprivateOffsets[FD])
        // While in the same object:
        while (position < fonts[Font].fdprivateOffsets[FD] + fonts[Font].fdprivateLengths[FD]) {
            getDictItem()
            // If the dictItem is the "Subrs" then find and store offset,
            if (key === "Subrs")
                fonts[Font].PrivateSubrsOffset[FD] = (args[0] as Int).toInt() + fonts[Font].fdprivateOffsets[FD]
        }
        //Read the lsubr index if the lsubr was found
        if (fonts[Font].PrivateSubrsOffset[FD] >= 0)
            fonts[Font].PrivateSubrsOffsetsArray[FD] = getIndex(fonts[Font].PrivateSubrsOffset[FD])
    }

    /**
     * Function uses ReadAsubr on the glyph used to build the LSubr & Gsubr HashMap.
     * The HashMap (of the lsubr only) is then scanned recursively for Lsubr & Gsubrs
     * calls.
     * @param Font the font
     * *
     * @param FD FD array processed. 0 indicates function was called by non CID font
     * *
     * @param SubrOffset the offset to the subr index to calc the bias
     * *
     * @param SubrsOffsets the offset array of the subr index
     * *
     * @param hSubr HashMap of the subrs used
     * *
     * @param lSubr ArrayList of the subrs used
     */
    protected fun BuildSubrUsed(Font: Int, FD: Int, SubrOffset: Int, SubrsOffsets: IntArray, hSubr: HashMap<Int, IntArray>, lSubr: ArrayList<Int>) {

        // Calc the Bias for the subr index
        val LBias = CalcBias(SubrOffset, Font)

        // For each glyph used find its GID, start & end pos
        for (i in glyphsInList.indices) {
            val glyph = glyphsInList[i].toInt()
            val Start = fonts[Font].charstringsOffsets[glyph]
            val End = fonts[Font].charstringsOffsets[glyph + 1]

            // IF CID:
            if (FD >= 0) {
                EmptyStack()
                NumOfHints = 0
                // Using FDSELECT find the FD Array the glyph belongs to.
                val GlyphFD = fonts[Font].FDSelect[glyph]
                // If the Glyph is part of the FD being processed
                if (GlyphFD == FD)
                // Find the Subrs called by the glyph and insert to hash:
                    ReadASubr(Start, End, GBias, LBias, hSubr, lSubr, SubrsOffsets)
            } else
            // If the font is not CID
            //Find the Subrs called by the glyph and insert to hash:
                ReadASubr(Start, End, GBias, LBias, hSubr, lSubr, SubrsOffsets)
        }
        // For all Lsubrs used, check recursively for Lsubr & Gsubr used
        for (i in lSubr.indices) {
            // Pop the subr value from the hash
            val Subr = lSubr[i].toInt()
            // Ensure the Lsubr call is valid
            if (Subr < SubrsOffsets.size - 1 && Subr >= 0) {
                // Read and process the subr
                val Start = SubrsOffsets[Subr]
                val End = SubrsOffsets[Subr + 1]
                ReadASubr(Start, End, GBias, LBias, hSubr, lSubr, SubrsOffsets)
            }
        }
    }

    /**
     * Function scans the Glsubr used ArrayList to find recursive calls
     * to Gsubrs and adds to Hashmap & ArrayList
     * @param Font the font
     */
    protected fun BuildGSubrsUsed(Font: Int) {
        var LBias = 0
        var SizeOfNonCIDSubrsUsed = 0
        if (fonts[Font].privateSubrs >= 0) {
            LBias = CalcBias(fonts[Font].privateSubrs, Font)
            SizeOfNonCIDSubrsUsed = lSubrsUsedNonCID.size
        }

        // For each global subr used
        for (i in lGSubrsUsed.indices) {
            //Pop the value + check valid
            val Subr = lGSubrsUsed[i].toInt()
            if (Subr < gsubrOffsets.size - 1 && Subr >= 0) {
                // Read the subr and process
                val Start = gsubrOffsets[Subr]
                val End = gsubrOffsets[Subr + 1]

                if (fonts[Font].isCID)
                    ReadASubr(Start, End, GBias, 0, hGSubrsUsed, lGSubrsUsed, null)
                else {
                    ReadASubr(Start, End, GBias, LBias, hSubrsUsedNonCID, lSubrsUsedNonCID, fonts[Font].SubrsOffsets)
                    if (SizeOfNonCIDSubrsUsed < lSubrsUsedNonCID.size) {
                        for (j in SizeOfNonCIDSubrsUsed..lSubrsUsedNonCID.size - 1) {
                            //Pop the value + check valid
                            val LSubr = lSubrsUsedNonCID[j].toInt()
                            if (LSubr < fonts[Font].SubrsOffsets.size - 1 && LSubr >= 0) {
                                // Read the subr and process
                                val LStart = fonts[Font].SubrsOffsets[LSubr]
                                val LEnd = fonts[Font].SubrsOffsets[LSubr + 1]
                                ReadASubr(LStart, LEnd, GBias, LBias, hSubrsUsedNonCID, lSubrsUsedNonCID, fonts[Font].SubrsOffsets)
                            }
                        }
                        SizeOfNonCIDSubrsUsed = lSubrsUsedNonCID.size
                    }
                }
            }
        }
    }

    /**
     * The function reads a subrs (glyph info) between begin and end.
     * Adds calls to a Lsubr to the hSubr and lSubrs.
     * Adds calls to a Gsubr to the hGSubr and lGSubrs.
     * @param begin the start point of the subr
     * *
     * @param end the end point of the subr
     * *
     * @param GBias the bias of the Global Subrs
     * *
     * @param LBias the bias of the Local Subrs
     * *
     * @param hSubr the HashMap for the lSubrs
     * *
     * @param lSubr the ArrayList for the lSubrs
     */
    protected fun ReadASubr(begin: Int, end: Int, GBias: Int, LBias: Int, hSubr: HashMap<Int, IntArray>, lSubr: ArrayList<Int>, LSubrsOffsets: IntArray?) {
        // Clear the stack for the subrs
        EmptyStack()
        NumOfHints = 0
        // Goto beginning of the subr
        seek(begin)
        while (position < end) {
            // Read the next command
            ReadCommand()
            val pos = position
            var TopElement: Any? = null
            if (arg_count > 0)
                TopElement = args[arg_count - 1]
            val NumOfArgs = arg_count
            // Check the modification needed on the Argument Stack according to key;
            HandelStack()
            // a call to a Lsubr
            if (key === "callsubr") {
                // Verify that arguments are passed
                if (NumOfArgs > 0) {
                    // Calc the index of the Subrs
                    val Subr = (TopElement as Int).toInt() + LBias
                    // If the subr isn't in the HashMap -> Put in
                    if (!hSubr.containsKey(Integer.valueOf(Subr))) {
                        hSubr.put(Integer.valueOf(Subr), null)
                        lSubr.add(Integer.valueOf(Subr))
                    }
                    CalcHints(LSubrsOffsets!![Subr], LSubrsOffsets[Subr + 1], LBias, GBias, LSubrsOffsets)
                    seek(pos)
                }
            } else if (key === "callgsubr") {
                // Verify that arguments are passed
                if (NumOfArgs > 0) {
                    // Calc the index of the Subrs
                    val Subr = (TopElement as Int).toInt() + GBias
                    // If the subr isn't in the HashMap -> Put in
                    if (!hGSubrsUsed.containsKey(Integer.valueOf(Subr))) {
                        hGSubrsUsed.put(Integer.valueOf(Subr), null)
                        lGSubrsUsed.add(Integer.valueOf(Subr))
                    }
                    CalcHints(gsubrOffsets[Subr], gsubrOffsets[Subr + 1], LBias, GBias, LSubrsOffsets)
                    seek(pos)
                }
            } else if (key === "hstem" || key === "vstem" || key === "hstemhm" || key === "vstemhm") {
                // Increment the NumOfHints by the number couples of of arguments
                NumOfHints += NumOfArgs / 2
            } else if (key === "hintmask" || key === "cntrmask") {
                // if stack is not empty the reason is vstem implicit definition
                // See Adobe Technical Note #5177, page 25, hintmask usage example.
                NumOfHints += NumOfArgs / 2
                // Compute the size of the mask
                var SizeOfMask = NumOfHints / 8
                if (NumOfHints % 8 != 0 || SizeOfMask == 0)
                    SizeOfMask++
                // Continue the pointer in SizeOfMask steps
                for (i in 0..SizeOfMask - 1)
                    card8
            }// A call to "mask"
            // A call to "stem"
            // a call to a Gsubr
        }
    }

    /**
     * Function Checks how the current operator effects the run time stack after being run
     * An operator may increase or decrease the stack size
     */
    protected fun HandelStack() {
        // Find out what the operator does to the stack
        var StackHandel = StackOpp()
        if (StackHandel < 2) {
            // The operators that enlarge the stack by one
            if (StackHandel == 1)
                PushStack()
            else {
                // Abs value for the for loop
                StackHandel *= -1
                for (i in 0..StackHandel - 1)
                    PopStack()
            }// The operators that pop the stack

        } else
            EmptyStack()// All other flush the stack
    }

    /**
     * Function checks the key and return the change to the stack after the operator
     * @return The change in the stack. 2-> flush the stack
     */
    protected fun StackOpp(): Int {
        if (key === "ifelse")
            return -3
        if (key === "roll" || key === "put")
            return -2
        if (key === "callsubr" || key === "callgsubr" || key === "add" || key === "sub" ||
                key === "div" || key === "mul" || key === "drop" || key === "and" ||
                key === "or" || key === "eq")
            return -1
        if (key === "abs" || key === "neg" || key === "sqrt" || key === "exch" ||
                key === "index" || key === "get" || key === "not" || key === "return")
            return 0
        if (key === "random" || key === "dup")
            return 1
        return 2
    }

    /**
     * Empty the Type2 Stack

     */
    protected fun EmptyStack() {
        // Null the arguments
        for (i in 0..arg_count - 1) args[i] = null
        arg_count = 0
    }

    /**
     * Pop one element from the stack

     */
    protected fun PopStack() {
        if (arg_count > 0) {
            args[arg_count - 1] = null
            arg_count--
        }
    }

    /**
     * Add an item to the stack

     */
    protected fun PushStack() {
        arg_count++
    }

    /**
     * The function reads the next command after the file pointer is set
     */
    protected fun ReadCommand() {
        key = null
        var gotKey = false
        // Until a key is found
        while (!gotKey) {
            // Read the first Char
            val b0 = card8
            // decode according to the type1/type2 format
            if (b0.toInt() == 28)
            // the two next bytes represent a short int;
            {
                val first = card8.toInt()
                val second = card8.toInt()
                args[arg_count] = Integer.valueOf(first shl 8 or second)
                arg_count++
                continue
            }
            if (b0.toInt() >= 32 && b0.toInt() <= 246)
            // The byte read is the byte;
            {
                args[arg_count] = Integer.valueOf(b0.toInt() - 139)
                arg_count++
                continue
            }
            if (b0.toInt() >= 247 && b0.toInt() <= 250)
            // The byte read and the next byte constitute a short int
            {
                val w = card8.toInt()
                args[arg_count] = Integer.valueOf((b0.toInt() - 247) * 256 + w + 108)
                arg_count++
                continue
            }
            if (b0.toInt() >= 251 && b0.toInt() <= 254)
            // Same as above except negative
            {
                val w = card8.toInt()
                args[arg_count] = Integer.valueOf(-(b0.toInt() - 251) * 256 - w - 108)
                arg_count++
                continue
            }
            if (b0.toInt() == 255)
            // The next for bytes represent a double.
            {
                val first = card8.toInt()
                val second = card8.toInt()
                val third = card8.toInt()
                val fourth = card8.toInt()
                args[arg_count] = Integer.valueOf(first shl 24 or second shl 16 or third shl 8 or fourth)
                arg_count++
                continue
            }
            if (b0.toInt() <= 31 && b0.toInt() != 28)
            // An operator was found.. Set Key.
            {
                gotKey = true
                // 12 is an escape command therefore the next byte is a part
                // of this command
                if (b0.toInt() == 12) {
                    var b1 = card8.toInt()
                    if (b1 > SubrsEscapeFuncs.size - 1)
                        b1 = SubrsEscapeFuncs.size - 1
                    key = SubrsEscapeFuncs[b1]
                } else
                    key = SubrsFunctions[b0]
                continue
            }
        }
    }

    /**
     * The function reads the subroutine and returns the number of the hint in it.
     * If a call to another subroutine is found the function calls recursively.
     * @param begin the start point of the subr
     * *
     * @param end the end point of the subr
     * *
     * @param LBias the bias of the Local Subrs
     * *
     * @param GBias the bias of the Global Subrs
     * *
     * @param LSubrsOffsets The Offsets array of the subroutines
     * *
     * @return The number of hints in the subroutine read.
     */
    protected fun CalcHints(begin: Int, end: Int, LBias: Int, GBias: Int, LSubrsOffsets: IntArray): Int {
        // Goto beginning of the subr
        seek(begin)
        while (position < end) {
            // Read the next command
            ReadCommand()
            val pos = position
            var TopElement: Any? = null
            if (arg_count > 0)
                TopElement = args[arg_count - 1]
            val NumOfArgs = arg_count
            //Check the modification needed on the Argument Stack according to key;
            HandelStack()
            // a call to a Lsubr
            if (key === "callsubr") {
                if (NumOfArgs > 0) {
                    val Subr = (TopElement as Int).toInt() + LBias
                    CalcHints(LSubrsOffsets[Subr], LSubrsOffsets[Subr + 1], LBias, GBias, LSubrsOffsets)
                    seek(pos)
                }
            } else if (key === "callgsubr") {
                if (NumOfArgs > 0) {
                    val Subr = (TopElement as Int).toInt() + GBias
                    CalcHints(gsubrOffsets[Subr], gsubrOffsets[Subr + 1], LBias, GBias, LSubrsOffsets)
                    seek(pos)
                }
            } else if (key === "hstem" || key === "vstem" || key === "hstemhm" || key === "vstemhm")
            // Increment the NumOfHints by the number couples of of arguments
                NumOfHints += NumOfArgs / 2
            else if (key === "hintmask" || key === "cntrmask") {
                // Compute the size of the mask
                var SizeOfMask = NumOfHints / 8
                if (NumOfHints % 8 != 0 || SizeOfMask == 0)
                    SizeOfMask++
                // Continue the pointer in SizeOfMask steps
                for (i in 0..SizeOfMask - 1)
                    card8
            }// A call to "mask"
            // A call to "stem"
            // a call to a Gsubr
        }
        return NumOfHints
    }


    /**
     * Function builds the new offset array, object array and assembles the index.
     * used for creating the glyph and subrs subsetted index
     * @param Offsets the offset array of the original index
     * *
     * @param Used the hashmap of the used objects
     * *
     * @param OperatorForUnusedEntries the operator inserted into the data stream for unused entries
     * *
     * @return the new index subset version
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    protected fun BuildNewIndex(Offsets: IntArray, Used: HashMap<Int, IntArray>, OperatorForUnusedEntries: Byte): ByteArray {
        var unusedCount = 0
        var Offset = 0
        val NewOffsets = IntArray(Offsets.size)
        // Build the Offsets Array for the Subset
        for (i in Offsets.indices) {
            NewOffsets[i] = Offset
            // If the object in the offset is also present in the used
            // HashMap then increment the offset var by its size
            if (Used.containsKey(Integer.valueOf(i))) {
                Offset += Offsets[i + 1] - Offsets[i]
            } else {
                // Else the same offset is kept in i+1.
                unusedCount++
            }
        }
        // Offset var determines the size of the object array
        val NewObjects = ByteArray(Offset + unusedCount)
        // Build the new Object array
        var unusedOffset = 0
        for (i in 0..Offsets.size - 1 - 1) {
            val start = NewOffsets[i]
            val end = NewOffsets[i + 1]
            NewOffsets[i] = start + unusedOffset
            // If start != End then the Object is used
            // So, we will copy the object data from the font file
            if (start != end) {
                // All offsets are Global Offsets relative to the beginning of the font file.
                // Jump the file pointer to the start address to read from.
                buf.seek(Offsets[i].toLong())
                // Read from the buffer and write into the array at start.
                buf.readFully(NewObjects, start + unusedOffset, end - start)
            } else {
                NewObjects[start + unusedOffset] = OperatorForUnusedEntries
                unusedOffset++
            }
        }
        NewOffsets[Offsets.size - 1] += unusedOffset
        // Use AssembleIndex to build the index from the offset & object arrays
        return AssembleIndex(NewOffsets, NewObjects)
    }

    /**
     * Function creates the new index, inserting the count,offsetsize,offset array
     * and object array.
     * @param NewOffsets the subsetted offset array
     * *
     * @param NewObjects the subsetted object array
     * *
     * @return the new index created
     */
    protected fun AssembleIndex(NewOffsets: IntArray, NewObjects: ByteArray): ByteArray {
        // Calc the index' count field
        val Count = (NewOffsets.size - 1).toChar()
        // Calc the size of the object array
        val Size = NewOffsets[NewOffsets.size - 1]
        // Calc the Offsize
        val Offsize: Byte
        if (Size <= 0xff)
            Offsize = 1
        else if (Size <= 0xffff)
            Offsize = 2
        else if (Size <= 0xffffff)
            Offsize = 3
        else
            Offsize = 4
        // The byte array for the new index. The size is calc by
        // Count=2, Offsize=1, OffsetArray = Offsize*(Count+1), The object array
        val NewIndex = ByteArray(2 + 1 + Offsize * (Count.toInt() + 1) + NewObjects.size)
        // The counter for writing
        var Place = 0
        // Write the count field
        NewIndex[Place++] = (Count.toInt().ushr(8) and 0xff).toByte()
        NewIndex[Place++] = (Count.toInt().ushr(0) and 0xff).toByte()
        // Write the offsize field
        NewIndex[Place++] = Offsize
        // Write the offset array according to the offsize
        for (newOffset in NewOffsets) {
            // The value to be written
            val Num = newOffset - NewOffsets[0] + 1
            // Write in bytes according to the offsize
            when (Offsize) {
                4 -> {
                    NewIndex[Place++] = (Num.ushr(24) and 0xff).toByte()
                    NewIndex[Place++] = (Num.ushr(16) and 0xff).toByte()
                    NewIndex[Place++] = (Num.ushr(8) and 0xff).toByte()
                    NewIndex[Place++] = (Num.ushr(0) and 0xff).toByte()
                }
                3 -> {
                    NewIndex[Place++] = (Num.ushr(16) and 0xff).toByte()
                    NewIndex[Place++] = (Num.ushr(8) and 0xff).toByte()
                    NewIndex[Place++] = (Num.ushr(0) and 0xff).toByte()
                }
                2 -> {
                    NewIndex[Place++] = (Num.ushr(8) and 0xff).toByte()
                    NewIndex[Place++] = (Num.ushr(0) and 0xff).toByte()
                }
                1 -> NewIndex[Place++] = (Num.ushr(0) and 0xff).toByte()
            }
        }
        // Write the new object array one by one
        for (newObject in NewObjects) {
            NewIndex[Place++] = newObject
        }
        // Return the new index
        return NewIndex
    }

    /**
     * The function builds the new output stream according to the subset process
     * @param Font the font
     * *
     * @return the subsetted font stream
     */
    protected fun BuildNewFile(Font: Int): ByteArray {
        // Prepare linked list for new font components
        OutputList = LinkedList<CFFFont.Item>()

        // copy the header of the font
        CopyHeader()

        // create a name index
        BuildIndexHeader(1, 1, 1)
        OutputList.addLast(CFFFont.UInt8Item((1 + fonts[Font].name.length).toChar()))
        OutputList.addLast(CFFFont.StringItem(fonts[Font].name))

        // create the topdict Index
        BuildIndexHeader(1, 2, 1)
        val topdictIndex1Ref = CFFFont.IndexOffsetItem(2)
        OutputList.addLast(topdictIndex1Ref)
        val topdictBase = CFFFont.IndexBaseItem()
        OutputList.addLast(topdictBase)

        // Initialize the Dict Items for later use
        val charsetRef = CFFFont.DictOffsetItem()
        val charstringsRef = CFFFont.DictOffsetItem()
        val fdarrayRef = CFFFont.DictOffsetItem()
        val fdselectRef = CFFFont.DictOffsetItem()
        val privateRef = CFFFont.DictOffsetItem()

        // If the font is not CID create the following keys
        if (!fonts[Font].isCID) {
            // create a ROS key
            OutputList.addLast(CFFFont.DictNumberItem(fonts[Font].nstrings))
            OutputList.addLast(CFFFont.DictNumberItem(fonts[Font].nstrings + 1))
            OutputList.addLast(CFFFont.DictNumberItem(0))
            OutputList.addLast(CFFFont.UInt8Item(12.toChar()))
            OutputList.addLast(CFFFont.UInt8Item(30.toChar()))
            // create a CIDCount key
            OutputList.addLast(CFFFont.DictNumberItem(fonts[Font].nglyphs))
            OutputList.addLast(CFFFont.UInt8Item(12.toChar()))
            OutputList.addLast(CFFFont.UInt8Item(34.toChar()))
            // Sivan's comments
            // What about UIDBase (12,35)? Don't know what is it.
            // I don't think we need FontName; the font I looked at didn't have it.
        }
        // Go to the TopDict of the font being processed
        seek(topdictOffsets[Font])
        // Run until the end of the TopDict
        while (position < topdictOffsets[Font + 1]) {
            val p1 = position
            getDictItem()
            val p2 = position
            // The encoding key is disregarded since CID has no encoding
            if (key === "Encoding"
                    // These keys will be added manually by the process.
                    || key === "Private"
                    || key === "FDSelect"
                    || key === "FDArray"
                    || key === "charset"
                    || key === "CharStrings") {
            } else {
                //OtherWise copy key "as is" to the output list
                OutputList.add(CFFFont.RangeItem(buf, p1, p2 - p1))
            }
        }
        // Create the FDArray, FDSelect, Charset and CharStrings Keys
        CreateKeys(fdarrayRef, fdselectRef, charsetRef, charstringsRef)

        // Mark the end of the top dict area
        OutputList.addLast(CFFFont.IndexMarkerItem(topdictIndex1Ref, topdictBase))

        // Copy the string index

        if (fonts[Font].isCID)
            OutputList.addLast(getEntireIndexRange(stringIndexOffset))
        else
            CreateNewStringIndex(Font)// If the font is not CID we need to append new strings.
        // We need 3 more strings: Registry, Ordering, and a FontName for one FD.
        // The total length is at most "Adobe"+"Identity"+63 = 76

        // copy the new subsetted global subroutine index
        OutputList.addLast(CFFFont.RangeItem(RandomAccessFileOrArray(NewGSubrsIndex), 0, NewGSubrsIndex.size))

        // deal with fdarray, fdselect, and the font descriptors
        // If the font is CID:
        if (fonts[Font].isCID) {
            // copy the FDArray, FDSelect, charset

            // Copy FDSelect
            // Mark the beginning
            OutputList.addLast(CFFFont.MarkerItem(fdselectRef))
            // If an FDSelect exists copy it
            if (fonts[Font].fdselectOffset >= 0)
                OutputList.addLast(CFFFont.RangeItem(buf, fonts[Font].fdselectOffset, fonts[Font].FDSelectLength))
            else
                CreateFDSelect(fdselectRef, fonts[Font].nglyphs)// Else create a new one

            // Copy the Charset
            // Mark the beginning and copy entirely
            OutputList.addLast(CFFFont.MarkerItem(charsetRef))
            OutputList.addLast(CFFFont.RangeItem(buf, fonts[Font].charsetOffset, fonts[Font].CharsetLength))

            // Copy the FDArray
            // If an FDArray exists
            if (fonts[Font].fdarrayOffset >= 0) {
                // Mark the beginning
                OutputList.addLast(CFFFont.MarkerItem(fdarrayRef))
                // Build a new FDArray with its private dicts and their LSubrs
                Reconstruct(Font)
            } else
            // Else create a new one
                CreateFDArray(fdarrayRef, privateRef, Font)

        } else {
            // create FDSelect
            CreateFDSelect(fdselectRef, fonts[Font].nglyphs)
            // recreate a new charset
            CreateCharset(charsetRef, fonts[Font].nglyphs)
            // create a font dict index (fdarray)
            CreateFDArray(fdarrayRef, privateRef, Font)
        }// If the font is not CID

        // if a private dict exists insert its subsetted version
        if (fonts[Font].privateOffset >= 0) {
            // Mark the beginning of the private dict
            val PrivateBase = CFFFont.IndexBaseItem()
            OutputList.addLast(PrivateBase)
            OutputList.addLast(CFFFont.MarkerItem(privateRef))

            val Subr = CFFFont.DictOffsetItem()
            // Build and copy the new private dict
            CreateNonCIDPrivate(Font, Subr)
            // Copy the new LSubrs index
            CreateNonCIDSubrs(Font, PrivateBase, Subr)
        }

        // copy the charstring index
        OutputList.addLast(CFFFont.MarkerItem(charstringsRef))

        // Add the subsetted charstring
        OutputList.addLast(CFFFont.RangeItem(RandomAccessFileOrArray(NewCharStringsIndex), 0, NewCharStringsIndex.size))

        // now create the new CFF font
        val currentOffset = IntArray(1)
        currentOffset[0] = 0
        // Count and save the offset for each item
        var listIter: Iterator<CFFFont.Item> = OutputList.iterator()
        while (listIter.hasNext()) {
            val item = listIter.next()
            item.increment(currentOffset)
        }
        // Compute the Xref for each of the offset items
        listIter = OutputList.iterator()
        while (listIter.hasNext()) {
            val item = listIter.next()
            item.xref()
        }

        val size = currentOffset[0]
        val b = ByteArray(size)

        // Emit all the items into the new byte array
        listIter = OutputList.iterator()
        while (listIter.hasNext()) {
            val item = listIter.next()
            item.emit(b)
        }
        // Return the new stream
        return b
    }

    /**
     * Function Copies the header from the original fileto the output list
     */
    protected fun CopyHeader() {
        seek(0)
        val major = card8.toInt()
        val minor = card8.toInt()
        val hdrSize = card8.toInt()
        val offSize = card8.toInt()
        nextIndexOffset = hdrSize
        OutputList.addLast(CFFFont.RangeItem(buf, 0, hdrSize))
    }

    /**
     * Function Build the header of an index
     * @param Count the count field of the index
     * *
     * @param Offsize the offsize field of the index
     * *
     * @param First the first offset of the index
     */
    protected fun BuildIndexHeader(Count: Int, Offsize: Int, First: Int) {
        // Add the count field
        OutputList.addLast(CFFFont.UInt16Item(Count.toChar())) // count
        // Add the offsize field
        OutputList.addLast(CFFFont.UInt8Item(Offsize.toChar())) // offSize
        // Add the first offset according to the offsize
        when (Offsize) {
            1 -> OutputList.addLast(CFFFont.UInt8Item(First.toChar())) // first offset
            2 -> OutputList.addLast(CFFFont.UInt16Item(First.toChar())) // first offset
            3 -> OutputList.addLast(CFFFont.UInt24Item(First.toChar().toInt())) // first offset
            4 -> OutputList.addLast(CFFFont.UInt32Item(First.toChar().toInt())) // first offset
            else -> {
            }
        }
    }

    /**
     * Function adds the keys into the TopDict
     * @param fdarrayRef OffsetItem for the FDArray
     * *
     * @param fdselectRef OffsetItem for the FDSelect
     * *
     * @param charsetRef OffsetItem for the CharSet
     * *
     * @param charstringsRef OffsetItem for the CharString
     */
    protected fun CreateKeys(fdarrayRef: CFFFont.OffsetItem, fdselectRef: CFFFont.OffsetItem, charsetRef: CFFFont.OffsetItem, charstringsRef: CFFFont.OffsetItem) {
        // create an FDArray key
        OutputList.addLast(fdarrayRef)
        OutputList.addLast(CFFFont.UInt8Item(12.toChar()))
        OutputList.addLast(CFFFont.UInt8Item(36.toChar()))
        // create an FDSelect key
        OutputList.addLast(fdselectRef)
        OutputList.addLast(CFFFont.UInt8Item(12.toChar()))
        OutputList.addLast(CFFFont.UInt8Item(37.toChar()))
        // create an charset key
        OutputList.addLast(charsetRef)
        OutputList.addLast(CFFFont.UInt8Item(15.toChar()))
        // create a CharStrings key
        OutputList.addLast(charstringsRef)
        OutputList.addLast(CFFFont.UInt8Item(17.toChar()))
    }

    /**
     * Function takes the original string item and adds the new strings
     * to accommodate the CID rules
     * @param Font the font
     */
    protected fun CreateNewStringIndex(Font: Int) {
        var fdFontName = fonts[Font].name + "-OneRange"
        if (fdFontName.length > 127)
            fdFontName = fdFontName.substring(0, 127)
        val extraStrings = "Adobe" + "Identity" + fdFontName

        val origStringsLen = stringOffsets[stringOffsets.size - 1] - stringOffsets[0]
        val stringsBaseOffset = stringOffsets[0] - 1

        val stringsIndexOffSize: Byte
        if (origStringsLen + extraStrings.length <= 0xff)
            stringsIndexOffSize = 1
        else if (origStringsLen + extraStrings.length <= 0xffff)
            stringsIndexOffSize = 2
        else if (origStringsLen + extraStrings.length <= 0xffffff)
            stringsIndexOffSize = 3
        else
            stringsIndexOffSize = 4

        OutputList.addLast(CFFFont.UInt16Item((stringOffsets.size - 1 + 3).toChar())) // count
        OutputList.addLast(CFFFont.UInt8Item(stringsIndexOffSize.toChar())) // offSize
        for (stringOffset in stringOffsets)
            OutputList.addLast(CFFFont.IndexOffsetItem(stringsIndexOffSize.toInt(),
                    stringOffset - stringsBaseOffset))
        var currentStringsOffset = stringOffsets[stringOffsets.size - 1] - stringsBaseOffset
        //l.addLast(new IndexOffsetItem(stringsIndexOffSize,currentStringsOffset));
        currentStringsOffset += "Adobe".length
        OutputList.addLast(CFFFont.IndexOffsetItem(stringsIndexOffSize.toInt(), currentStringsOffset))
        currentStringsOffset += "Identity".length
        OutputList.addLast(CFFFont.IndexOffsetItem(stringsIndexOffSize.toInt(), currentStringsOffset))
        currentStringsOffset += fdFontName.length
        OutputList.addLast(CFFFont.IndexOffsetItem(stringsIndexOffSize.toInt(), currentStringsOffset))

        OutputList.addLast(CFFFont.RangeItem(buf, stringOffsets[0], origStringsLen))
        OutputList.addLast(CFFFont.StringItem(extraStrings))
    }

    /**
     * Function creates new FDSelect for non-CID fonts.
     * The FDSelect built uses a single range for all glyphs
     * @param fdselectRef OffsetItem for the FDSelect
     * *
     * @param nglyphs the number of glyphs in the font
     */
    protected fun CreateFDSelect(fdselectRef: CFFFont.OffsetItem, nglyphs: Int) {
        OutputList.addLast(CFFFont.MarkerItem(fdselectRef))
        OutputList.addLast(CFFFont.UInt8Item(3.toChar())) // format identifier
        OutputList.addLast(CFFFont.UInt16Item(1.toChar())) // nRanges

        OutputList.addLast(CFFFont.UInt16Item(0.toChar())) // Range[0].firstGlyph
        OutputList.addLast(CFFFont.UInt8Item(0.toChar())) // Range[0].fd

        OutputList.addLast(CFFFont.UInt16Item(nglyphs.toChar())) // sentinel
    }

    /**
     * Function creates new CharSet for non-CID fonts.
     * The CharSet built uses a single range for all glyphs
     * @param charsetRef OffsetItem for the CharSet
     * *
     * @param nglyphs the number of glyphs in the font
     */
    protected fun CreateCharset(charsetRef: CFFFont.OffsetItem, nglyphs: Int) {
        OutputList.addLast(CFFFont.MarkerItem(charsetRef))
        OutputList.addLast(CFFFont.UInt8Item(2.toChar())) // format identifier
        OutputList.addLast(CFFFont.UInt16Item(1.toChar())) // first glyph in range (ignore .notdef)
        OutputList.addLast(CFFFont.UInt16Item((nglyphs - 1).toChar())) // nLeft
    }

    /**
     * Function creates new FDArray for non-CID fonts.
     * The FDArray built has only the "Private" operator that points to the font's
     * original private dict
     * @param fdarrayRef OffsetItem for the FDArray
     * *
     * @param privateRef OffsetItem for the Private Dict
     * *
     * @param Font the font
     */
    protected fun CreateFDArray(fdarrayRef: CFFFont.OffsetItem, privateRef: CFFFont.OffsetItem, Font: Int) {
        OutputList.addLast(CFFFont.MarkerItem(fdarrayRef))
        // Build the header (count=offsize=first=1)
        BuildIndexHeader(1, 1, 1)

        // Mark
        val privateIndex1Ref = CFFFont.IndexOffsetItem(1)
        OutputList.addLast(privateIndex1Ref)
        val privateBase = CFFFont.IndexBaseItem()
        // Insert the private operands and operator
        OutputList.addLast(privateBase)
        // Calc the new size of the private after subsetting
        // Origianl size
        var NewSize = fonts[Font].privateLength
        // Calc the original size of the Subr offset in the private
        val OrgSubrsOffsetSize = CalcSubrOffsetSize(fonts[Font].privateOffset, fonts[Font].privateLength)
        // Increase the ptivate's size
        if (OrgSubrsOffsetSize != 0)
            NewSize += 5 - OrgSubrsOffsetSize
        OutputList.addLast(CFFFont.DictNumberItem(NewSize))
        OutputList.addLast(privateRef)
        OutputList.addLast(CFFFont.UInt8Item(18.toChar())) // Private

        OutputList.addLast(CFFFont.IndexMarkerItem(privateIndex1Ref, privateBase))
    }

    /**
     * Function reconstructs the FDArray, PrivateDict and LSubr for CID fonts
     * @param Font the font
     */
    internal fun Reconstruct(Font: Int) {
        // Init for later use
        val fdPrivate = arrayOfNulls<CFFFont.DictOffsetItem>(fonts[Font].FDArrayOffsets.size - 1)
        val fdPrivateBase = arrayOfNulls<CFFFont.IndexBaseItem>(fonts[Font].fdprivateOffsets.size)
        val fdSubrs = arrayOfNulls<CFFFont.DictOffsetItem>(fonts[Font].fdprivateOffsets.size)
        // Reconstruct each type
        ReconstructFDArray(Font, fdPrivate)
        ReconstructPrivateDict(Font, fdPrivate, fdPrivateBase, fdSubrs)
        ReconstructPrivateSubrs(Font, fdPrivateBase, fdSubrs)
    }

    /**
     * Function subsets the FDArray and builds the new one with new offsets
     * @param Font The font
     * *
     * @param fdPrivate OffsetItem Array (one for each FDArray)
     */
    internal fun ReconstructFDArray(Font: Int, fdPrivate: Array<CFFFont.OffsetItem>) {
        // Build the header of the index
        BuildIndexHeader(fonts[Font].FDArrayCount, fonts[Font].FDArrayOffsize, 1)

        // For each offset create an Offset Item
        val fdOffsets = arrayOfNulls<CFFFont.IndexOffsetItem>(fonts[Font].FDArrayOffsets.size - 1)
        for (i in 0..fonts[Font].FDArrayOffsets.size - 1 - 1) {
            fdOffsets[i] = CFFFont.IndexOffsetItem(fonts[Font].FDArrayOffsize)
            OutputList.addLast(fdOffsets[i])
        }

        // Declare beginning of the object array
        val fdArrayBase = CFFFont.IndexBaseItem()
        OutputList.addLast(fdArrayBase)

        // For each object check if that FD is used.
        // if is used build a new one by changing the private object
        // Else do nothing
        // At the end of each object mark its ending (Even if wasn't written)
        for (k in 0..fonts[Font].FDArrayOffsets.size - 1 - 1) {
            //			if (FDArrayUsed.contains(Integer.valueOf(k)))
            //			{
            // Goto beginning of objects
            seek(fonts[Font].FDArrayOffsets[k])
            while (position < fonts[Font].FDArrayOffsets[k + 1]) {
                val p1 = position
                getDictItem()
                val p2 = position
                // If the dictItem is the "Private" then compute and copy length,
                // use marker for offset and write operator number
                if (key === "Private") {
                    // Save the original length of the private dict
                    var NewSize = (args[0] as Int).toInt()
                    // Save the size of the offset to the subrs in that private
                    val OrgSubrsOffsetSize = CalcSubrOffsetSize(fonts[Font].fdprivateOffsets[k], fonts[Font].fdprivateLengths[k])
                    // Increase the private's length accordingly
                    if (OrgSubrsOffsetSize != 0)
                        NewSize += 5 - OrgSubrsOffsetSize
                    // Insert the new size, OffsetItem and operator key number
                    OutputList.addLast(CFFFont.DictNumberItem(NewSize))
                    fdPrivate[k] = CFFFont.DictOffsetItem()
                    OutputList.addLast(fdPrivate[k])
                    OutputList.addLast(CFFFont.UInt8Item(18.toChar())) // Private
                    // Go back to place
                    seek(p2)
                } else
                // other than private
                    OutputList.addLast(CFFFont.RangeItem(buf, p1, p2 - p1))// Else copy the entire range
            }
            //			}
            // Mark the ending of the object (even if wasn't written)
            OutputList.addLast(CFFFont.IndexMarkerItem(fdOffsets[k], fdArrayBase))
        }
    }

    /**
     * Function Adds the new private dicts (only for the FDs used) to the list
     * @param Font the font
     * *
     * @param fdPrivate OffsetItem array one element for each private
     * *
     * @param fdPrivateBase IndexBaseItem array one element for each private
     * *
     * @param fdSubrs OffsetItem array one element for each private
     */
    internal fun ReconstructPrivateDict(Font: Int, fdPrivate: Array<CFFFont.OffsetItem>, fdPrivateBase: Array<CFFFont.IndexBaseItem>,
                                        fdSubrs: Array<CFFFont.OffsetItem>) {

        // For each fdarray private dict check if that FD is used.
        // if is used build a new one by changing the subrs offset
        // Else do nothing
        for (i in fonts[Font].fdprivateOffsets.indices) {
            //			if (FDArrayUsed.contains(Integer.valueOf(i)))
            //			{
            // Mark beginning
            OutputList.addLast(CFFFont.MarkerItem(fdPrivate[i]))
            fdPrivateBase[i] = CFFFont.IndexBaseItem()
            OutputList.addLast(fdPrivateBase[i])
            // Goto beginning of objects
            seek(fonts[Font].fdprivateOffsets[i])
            while (position < fonts[Font].fdprivateOffsets[i] + fonts[Font].fdprivateLengths[i]) {
                val p1 = position
                getDictItem()
                val p2 = position
                // If the dictItem is the "Subrs" then,
                // use marker for offset and write operator number
                if (key === "Subrs") {
                    fdSubrs[i] = CFFFont.DictOffsetItem()
                    OutputList.addLast(fdSubrs[i])
                    OutputList.addLast(CFFFont.UInt8Item(19.toChar())) // Subrs
                } else
                    OutputList.addLast(CFFFont.RangeItem(buf, p1, p2 - p1))// Else copy the entire range
            }
            //			}
        }
    }

    /**
     * Function Adds the new LSubrs dicts (only for the FDs used) to the list
     * @param Font  The index of the font
     * *
     * @param fdPrivateBase The IndexBaseItem array for the linked list
     * *
     * @param fdSubrs OffsetItem array for the linked list
     */

    internal fun ReconstructPrivateSubrs(Font: Int, fdPrivateBase: Array<CFFFont.IndexBaseItem>,
                                         fdSubrs: Array<CFFFont.OffsetItem>) {
        // For each private dict
        for (i in fonts[Font].fdprivateLengths.indices) {
            // If that private dict's Subrs are used insert the new LSubrs
            // computed earlier
            if (fdSubrs[i] != null && fonts[Font].PrivateSubrsOffset[i] >= 0) {
                OutputList.addLast(CFFFont.SubrMarkerItem(fdSubrs[i], fdPrivateBase[i]))
                if (NewLSubrsIndex[i] != null)
                    OutputList.addLast(CFFFont.RangeItem(RandomAccessFileOrArray(NewLSubrsIndex[i]), 0, NewLSubrsIndex[i].size))
            }
        }
    }

    /**
     * Calculates how many byte it took to write the offset for the subrs in a specific
     * private dict.
     * @param Offset The Offset for the private dict
     * *
     * @param Size The size of the private dict
     * *
     * @return The size of the offset of the subrs in the private dict
     */
    internal fun CalcSubrOffsetSize(Offset: Int, Size: Int): Int {
        // Set the size to 0
        var OffsetSize = 0
        // Go to the beginning of the private dict
        seek(Offset)
        // Go until the end of the private dict
        while (position < Offset + Size) {
            val p1 = position
            getDictItem()
            val p2 = position
            // When reached to the subrs offset
            if (key === "Subrs") {
                // The Offsize (minus the subrs key)
                OffsetSize = p2 - p1 - 1
            }
            // All other keys are ignored
        }
        // return the size
        return OffsetSize
    }

    /**
     * Function computes the size of an index
     * @param indexOffset The offset for the computed index
     * *
     * @return The size of the index
     */
    protected fun countEntireIndexRange(indexOffset: Int): Int {
        // Go to the beginning of the index
        seek(indexOffset)
        // Read the count field
        val count = card16.toInt()
        // If count==0 -> size=2
        if (count == 0)
            return 2
        else {
            // Read the offsize field
            val indexOffSize = card8.toInt()
            // Go to the last element of the offset array
            seek(indexOffset + 2 + 1 + count * indexOffSize)
            // The size of the object array is the value of the last element-1
            val size = getOffset(indexOffSize) - 1
            // Return the size of the entire index
            return 2 + 1 + (count + 1) * indexOffSize + size
        }
    }

    /**
     * The function creates a private dict for a font that was not CID
     * All the keys are copied as is except for the subrs key
     * @param Font the font
     * *
     * @param Subr The OffsetItem for the subrs of the private
     */
    internal fun CreateNonCIDPrivate(Font: Int, Subr: CFFFont.OffsetItem) {
        // Go to the beginning of the private dict and read until the end
        seek(fonts[Font].privateOffset)
        while (position < fonts[Font].privateOffset + fonts[Font].privateLength) {
            val p1 = position
            getDictItem()
            val p2 = position
            // If the dictItem is the "Subrs" then,
            // use marker for offset and write operator number
            if (key === "Subrs") {
                OutputList.addLast(Subr)
                OutputList.addLast(CFFFont.UInt8Item(19.toChar())) // Subrs
            } else
                OutputList.addLast(CFFFont.RangeItem(buf, p1, p2 - p1))// Else copy the entire range
        }
    }

    /**
     * the function marks the beginning of the subrs index and adds the subsetted subrs
     * index to the output list.
     * @param Font the font
     * *
     * @param PrivateBase IndexBaseItem for the private that's referencing to the subrs
     * *
     * @param Subrs OffsetItem for the subrs
     */
    internal fun CreateNonCIDSubrs(Font: Int, PrivateBase: CFFFont.IndexBaseItem, Subrs: CFFFont.OffsetItem) {
        // Mark the beginning of the Subrs index
        OutputList.addLast(CFFFont.SubrMarkerItem(Subrs, PrivateBase))
        // Put the subsetted new subrs index
        if (NewSubrsIndexNonCID != null) {
            OutputList.addLast(CFFFont.RangeItem(RandomAccessFileOrArray(NewSubrsIndexNonCID), 0, NewSubrsIndexNonCID!!.size))
        }
    }

    companion object {

        /**
         * The Strings in this array represent Type1/Type2 operator names
         */
        internal val SubrsFunctions = arrayOf("RESERVED_0", "hstem", "RESERVED_2", "vstem", "vmoveto", "rlineto", "hlineto", "vlineto", "rrcurveto", "RESERVED_9", "callsubr", "return", "escape", "RESERVED_13", "endchar", "RESERVED_15", "RESERVED_16", "RESERVED_17", "hstemhm", "hintmask", "cntrmask", "rmoveto", "hmoveto", "vstemhm", "rcurveline", "rlinecurve", "vvcurveto", "hhcurveto", "shortint", "callgsubr", "vhcurveto", "hvcurveto")
        /**
         * The Strings in this array represent Type1/Type2 escape operator names
         */
        internal val SubrsEscapeFuncs = arrayOf("RESERVED_0", "RESERVED_1", "RESERVED_2", "and", "or", "not", "RESERVED_6", "RESERVED_7", "RESERVED_8", "abs", "add", "sub", "div", "RESERVED_13", "neg", "eq", "RESERVED_16", "RESERVED_17", "drop", "RESERVED_19", "put", "get", "ifelse", "random", "mul", "RESERVED_25", "sqrt", "dup", "exch", "index", "roll", "RESERVED_31", "RESERVED_32", "RESERVED_33", "hflex", "flex", "hflex1", "flex1", "RESERVED_REST")

        /**
         * Operator codes for unused  CharStrings and unused local and global Subrs
         */
        internal val ENDCHAR_OP: Byte = 14
        internal val RETURN_OP: Byte = 11
    }
}

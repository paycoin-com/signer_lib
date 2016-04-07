/*
 * Copyright 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itextpdf.text.pdf.qrcode


/**
 * @author satorux@google.com (Satoru Takabayashi) - creator
 * *
 * @author dswitkin@google.com (Daniel Switkin) - ported from C++
 * *
 * @since 5.0.2
 */
class QRCode {

    // Mode of the QR Code.
    var mode: Mode? = null
    // Error correction level of the QR Code.
    var ecLevel: ErrorCorrectionLevel? = null
    // Version of the QR Code.  The bigger size, the bigger version.
    var version: Int = 0
    // ByteMatrix width of the QR Code.
    var matrixWidth: Int = 0
    // Mask pattern of the QR Code.
    var maskPattern: Int = 0
    // Number of total bytes in the QR Code.
    var numTotalBytes: Int = 0
    // Number of data bytes in the QR Code.
    var numDataBytes: Int = 0
    // Number of error correction bytes in the QR Code.
    var numECBytes: Int = 0
    // Number of Reedsolomon blocks in the QR Code.
    var numRSBlocks: Int = 0
    // ByteMatrix data of the QR Code.
    // This takes ownership of the 2D array.
    var matrix: ByteMatrix? = null

    init {
        mode = null
        ecLevel = null
        version = -1
        matrixWidth = -1
        maskPattern = -1
        numTotalBytes = -1
        numDataBytes = -1
        numECBytes = -1
        numRSBlocks = -1
        matrix = null
    }


    // Return the value of the module (cell) pointed by "x" and "y" in the matrix of the QR Code. They
    // call cells in the matrix "modules". 1 represents a black cell, and 0 represents a white cell.
    fun at(x: Int, y: Int): Int {
        // The value must be zero or one.
        val value = matrix!!.get(x, y).toInt()
        if (!(value == 0 || value == 1)) {
            // this is really like an assert... not sure what better exception to use?
            throw RuntimeException("Bad value")
        }
        return value
    }

    // Checks all the member variables are set properly. Returns true on success. Otherwise, returns
    // false.
    // First check if all version are not uninitialized.
    // Then check them in other ways..
    // ByteMatrix stuff.
    // See 7.3.1 of JISX0510:2004 (p.5).
    // Must be square.
    val isValid: Boolean
        get() = mode != null &&
                ecLevel != null &&
                version != -1 &&
                matrixWidth != -1 &&
                maskPattern != -1 &&
                numTotalBytes != -1 &&
                numDataBytes != -1 &&
                numECBytes != -1 &&
                numRSBlocks != -1 &&
                isValidMaskPattern(maskPattern) &&
                numTotalBytes == numDataBytes + numECBytes &&
                matrix != null &&
                matrixWidth == matrix!!.width &&
                matrix!!.width == matrix!!.height

    // Return debug String.
    override fun toString(): String {
        val result = StringBuffer(200)
        result.append("<<\n")
        result.append(" mode: ")
        result.append(mode)
        result.append("\n ecLevel: ")
        result.append(ecLevel)
        result.append("\n version: ")
        result.append(version)
        result.append("\n matrixWidth: ")
        result.append(matrixWidth)
        result.append("\n maskPattern: ")
        result.append(maskPattern)
        result.append("\n numTotalBytes: ")
        result.append(numTotalBytes)
        result.append("\n numDataBytes: ")
        result.append(numDataBytes)
        result.append("\n numECBytes: ")
        result.append(numECBytes)
        result.append("\n numRSBlocks: ")
        result.append(numRSBlocks)
        if (matrix == null) {
            result.append("\n matrix: null\n")
        } else {
            result.append("\n matrix:\n")
            result.append(matrix!!.toString())
        }
        result.append(">>\n")
        return result.toString()
    }

    companion object {

        val NUM_MASK_PATTERNS = 8

        // Check if "mask_pattern" is valid.
        fun isValidMaskPattern(maskPattern: Int): Boolean {
            return maskPattern >= 0 && maskPattern < NUM_MASK_PATTERNS
        }
    }

    // Return true if the all values in the matrix are binary numbers.
    //
    // JAVAPORT: This is going to be super expensive and unnecessary, we should not call this in
    // production. I'm leaving it because it may be useful for testing. It should be removed entirely
    // if ByteMatrix is changed never to contain a -1.
    /*
  private static boolean EverythingIsBinary(final ByteMatrix matrix) {
    for (int y = 0; y < matrix.height(); ++y) {
      for (int x = 0; x < matrix.width(); ++x) {
        int value = matrix.get(y, x);
        if (!(value == 0 || value == 1)) {
          // Found non zero/one value.
          return false;
        }
      }
    }
    return true;
  }
   */

}

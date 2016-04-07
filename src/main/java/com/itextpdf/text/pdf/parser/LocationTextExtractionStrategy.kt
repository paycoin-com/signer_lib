/*
 * $Id: 41a7bbb314dcdbd184ee7d4f9773fb6340684fd1 $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2016 iText Group NV
 * Authors: Kevin Day, Bruno Lowagie, Paulo Soares, et al.
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
package com.itextpdf.text.pdf.parser

import java.util.ArrayList
import java.util.Collections


/**
 * **Development preview** - this class (and all of the parser classes) are still experiencing
 * heavy development, and are subject to change both behavior and interface.
 *
 * A text extraction renderer that keeps track of relative position of text on page
 * The resultant text will be relatively consistent with the physical layout that most
 * PDF files have on screen.
 *
 * This renderer keeps track of the orientation and distance (both perpendicular
 * and parallel) to the unit vector of the orientation.  Text is ordered by
 * orientation, then perpendicular, then parallel distance.  Text with the same
 * perpendicular distance, but different parallel distance is treated as being on
 * the same line.
 *
 * This renderer also uses a simple strategy based on the font metrics to determine if
 * a blank space should be inserted into the output.

 * @since   5.0.2
 */
class LocationTextExtractionStrategy
/**
 * Creates a new text extraction renderer, with a custom strategy for
 * creating new TextChunkLocation objects based on the input of the
 * TextRenderInfo.
 * @param strat the custom strategy
 */
@JvmOverloads constructor(private val tclStrat: LocationTextExtractionStrategy.TextChunkLocationStrategy = object : LocationTextExtractionStrategy.TextChunkLocationStrategy {
    override fun createLocation(renderInfo: TextRenderInfo, baseline: LineSegment): LocationTextExtractionStrategy.TextChunkLocation {
        return LocationTextExtractionStrategy.TextChunkLocationDefaultImp(baseline.startPoint, baseline.endPoint, renderInfo.singleSpaceWidth)
    }
}) : TextExtractionStrategy {

    /** a summary of all found text  */
    private val locationalResult = ArrayList<TextChunk>()

    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener.beginTextBlock
     */
    override fun beginTextBlock() {
    }

    /**
     * @see com.itextpdf.text.pdf.parser.RenderListener.endTextBlock
     */
    override fun endTextBlock() {
    }

    /**
     * @param str
     * *
     * @return true if the string starts with a space character, false if the string is empty or starts with a non-space character
     */
    private fun startsWithSpace(str: String): Boolean {
        if (str.length == 0) return false
        return str[0] == ' '
    }

    /**
     * @param str
     * *
     * @return true if the string ends with a space character, false if the string is empty or ends with a non-space character
     */
    private fun endsWithSpace(str: String): Boolean {
        if (str.length == 0) return false
        return str[str.length - 1] == ' '
    }

    /**
     * Filters the provided list with the provided filter
     * @param textChunks a list of all TextChunks that this strategy found during processing
     * *
     * @param filter the filter to apply.  If null, filtering will be skipped.
     * *
     * @return the filtered list
     * *
     * @since 5.3.3
     */
    private fun filterTextChunks(textChunks: List<TextChunk>, filter: TextChunkFilter?): List<TextChunk> {
        if (filter == null)
            return textChunks

        val filtered = ArrayList<TextChunk>()
        for (textChunk in textChunks) {
            if (filter.accept(textChunk))
                filtered.add(textChunk)
        }
        return filtered
    }

    /**
     * Determines if a space character should be inserted between a previous chunk and the current chunk.
     * This method is exposed as a callback so subclasses can fine time the algorithm for determining whether a space should be inserted or not.
     * By default, this method will insert a space if the there is a gap of more than half the font space character width between the end of the
     * previous chunk and the beginning of the current chunk.  It will also indicate that a space is needed if the starting point of the new chunk
     * appears *before* the end of the previous chunk (i.e. overlapping text).
     * @param chunk the new chunk being evaluated
     * *
     * @param previousChunk the chunk that appeared immediately before the current chunk
     * *
     * @return true if the two chunks represent different words (i.e. should have a space between them).  False otherwise.
     */
    protected fun isChunkAtWordBoundary(chunk: TextChunk, previousChunk: TextChunk): Boolean {
        return chunk.location.isAtWordBoundary(previousChunk.location)
    }

    /**
     * Gets text that meets the specified filter
     * If multiple text extractions will be performed for the same page (i.e. for different physical regions of the page),
     * filtering at this level is more efficient than filtering using [FilteredRenderListener] - but not nearly as powerful
     * because most of the RenderInfo state is not captured in [TextChunk]
     * @param chunkFilter the filter to to apply
     * *
     * @return the text results so far, filtered using the specified filter
     */
    fun getResultantText(chunkFilter: TextChunkFilter?): String {
        if (DUMP_STATE) dumpState()

        val filteredTextChunks = filterTextChunks(locationalResult, chunkFilter)
        Collections.sort(filteredTextChunks)

        val sb = StringBuilder()
        var lastChunk: TextChunk? = null
        for (chunk in filteredTextChunks) {

            if (lastChunk == null) {
                sb.append(chunk.text)
            } else {
                if (chunk.sameLine(lastChunk)) {
                    // we only insert a blank space if the trailing character of the previous string wasn't a space, and the leading character of the current string isn't a space
                    if (isChunkAtWordBoundary(chunk, lastChunk) && !startsWithSpace(chunk.text) && !endsWithSpace(lastChunk.text))
                        sb.append(' ')

                    sb.append(chunk.text)
                } else {
                    sb.append('\n')
                    sb.append(chunk.text)
                }
            }
            lastChunk = chunk
        }

        return sb.toString()
    }

    /**
     * Returns the result so far.
     * @return  a String with the resulting text.
     */
    override val resultantText: String
        get() = getResultantText(null)

    /** Used for debugging only  */
    private fun dumpState() {
        for (location in locationalResult) {
            location.printDiagnostics()

            println()
        }

    }

    /**

     * @see com.itextpdf.text.pdf.parser.RenderListener.renderText
     */
    override fun renderText(renderInfo: TextRenderInfo) {
        var segment = renderInfo.baseline
        if (renderInfo.rise != 0f) {
            // remove the rise from the baseline - we do this because the text from a super/subscript render operations should probably be considered as part of the baseline of the text the super/sub is relative to 
            val riseOffsetTransform = Matrix(0f, -renderInfo.rise)
            segment = segment.transformBy(riseOffsetTransform)
        }
        val tc = TextChunk(renderInfo.text, tclStrat.createLocation(renderInfo, segment))
        locationalResult.add(tc)
    }

    interface TextChunkLocationStrategy {
        fun createLocation(renderInfo: TextRenderInfo, baseline: LineSegment): TextChunkLocation
    }

    interface TextChunkLocation : Comparable<TextChunkLocation> {

        fun distParallelEnd(): Float

        fun distParallelStart(): Float

        fun distPerpendicular(): Int

        val charSpaceWidth: Float

        val endLocation: Vector

        val startLocation: Vector

        fun orientationMagnitude(): Int

        fun sameLine(`as`: TextChunkLocation): Boolean

        fun distanceFromEndOf(other: TextChunkLocation): Float

        fun isAtWordBoundary(previous: TextChunkLocation): Boolean
    }

    private class TextChunkLocationDefaultImp(
            /** the starting location of the chunk  */
            /**
             * @return the start location of the text
             */
            override val startLocation: Vector,
            /** the ending location of the chunk  */
            /**
             * @return the end location of the text
             */
            override val endLocation: Vector,
            /** the width of a single space character in the font of the chunk  */
            /**
             * @return the width of a single space character as rendered by this chunk
             */
            override val charSpaceWidth: Float) : TextChunkLocation {
        /** unit vector in the orientation of the chunk  */
        private val orientationVector: Vector
        /** the orientation as a scalar for quick sorting  */
        private val orientationMagnitude: Int
        /** perpendicular distance to the orientation unit vector (i.e. the Y position in an unrotated coordinate system)
         * we round to the nearest integer to handle the fuzziness of comparing floats  */
        private val distPerpendicular: Int
        /** distance of the start of the chunk parallel to the orientation unit vector (i.e. the X position in an unrotated coordinate system)  */
        private val distParallelStart: Float
        /** distance of the end of the chunk parallel to the orientation unit vector (i.e. the X position in an unrotated coordinate system)  */
        private val distParallelEnd: Float

        init {

            var oVector = endLocation.subtract(startLocation)
            if (oVector.length() == 0f) {
                oVector = Vector(1f, 0f, 0f)
            }
            orientationVector = oVector.normalize()
            orientationMagnitude = (Math.atan2(orientationVector.get(Vector.I2).toDouble(), orientationVector.get(Vector.I1).toDouble()) * 1000).toInt()

            // see http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
            // the two vectors we are crossing are in the same plane, so the result will be purely
            // in the z-axis (out of plane) direction, so we just take the I3 component of the result
            val origin = Vector(0f, 0f, 1f)
            distPerpendicular = startLocation.subtract(origin).cross(orientationVector).get(Vector.I3).toInt()

            distParallelStart = orientationVector.dot(startLocation)
            distParallelEnd = orientationVector.dot(endLocation)
        }


        override fun orientationMagnitude(): Int {
            return orientationMagnitude
        }

        override fun distPerpendicular(): Int {
            return distPerpendicular
        }

        override fun distParallelStart(): Float {
            return distParallelStart
        }

        override fun distParallelEnd(): Float {
            return distParallelEnd
        }


        /**
         * @param as the location to compare to
         * *
         * @return true is this location is on the the same line as the other
         */
        override fun sameLine(`as`: TextChunkLocation): Boolean {
            return orientationMagnitude() == `as`.orientationMagnitude() && distPerpendicular() == `as`.distPerpendicular()
        }

        /**
         * Computes the distance between the end of 'other' and the beginning of this chunk
         * in the direction of this chunk's orientation vector.  Note that it's a bad idea
         * to call this for chunks that aren't on the same line and orientation, but we don't
         * explicitly check for that condition for performance reasons.
         * @param other
         * *
         * @return the number of spaces between the end of 'other' and the beginning of this chunk
         */
        override fun distanceFromEndOf(other: TextChunkLocation): Float {
            val distance = distParallelStart() - other.distParallelEnd()
            return distance
        }

        override fun isAtWordBoundary(previous: TextChunkLocation): Boolean {
            /**
             * Here we handle a very specific case which in PDF may look like:
             * -.232 Tc [( P)-226.2(r)-231.8(e)-230.8(f)-238(a)-238.9(c)-228.9(e)]TJ
             * The font's charSpace width is 0.232 and it's compensated with charSpacing of 0.232.
             * And a resultant TextChunk.charSpaceWidth comes to TextChunk constructor as 0.
             * In this case every chunk is considered as a word boundary and space is added.
             * We should consider charSpaceWidth equal (or close) to zero as a no-space.
             */
            if (charSpaceWidth < 0.1f)
                return false

            val dist = distanceFromEndOf(previous)

            return dist < -charSpaceWidth || dist > charSpaceWidth / 2.0f
        }

        override fun compareTo(other: TextChunkLocation): Int {
            if (this === other) return 0 // not really needed, but just in case

            var rslt: Int
            rslt = compareInts(orientationMagnitude(), other.orientationMagnitude())
            if (rslt != 0) return rslt

            rslt = compareInts(distPerpendicular(), other.distPerpendicular())
            if (rslt != 0) return rslt

            return java.lang.Float.compare(distParallelStart(), other.distParallelStart())
        }
    }

    /**
     * Represents a chunk of text, it's orientation, and location relative to the orientation vector
     */
    class TextChunk(
            /** the text of the chunk  */
            /**
             * @return the text captured by this chunk
             */
            val text: String,
            /**
             * @return an object holding location data about this TextChunk
             */
            val location: TextChunkLocation) : Comparable<TextChunk> {

        constructor(string: String, startLocation: Vector, endLocation: Vector, charSpaceWidth: Float) : this(string, TextChunkLocationDefaultImp(startLocation, endLocation, charSpaceWidth)) {
        }

        /**
         * @return the start location of the text
         */
        val startLocation: Vector
            get() = location.startLocation
        /**
         * @return the end location of the text
         */
        val endLocation: Vector
            get() = location.endLocation

        /**
         * @return the width of a single space character as rendered by this chunk
         */
        val charSpaceWidth: Float
            get() = location.charSpaceWidth

        /**
         * Computes the distance between the end of 'other' and the beginning of this chunk
         * in the direction of this chunk's orientation vector.  Note that it's a bad idea
         * to call this for chunks that aren't on the same line and orientation, but we don't
         * explicitly check for that condition for performance reasons.
         * @param other the other [TextChunk]
         * *
         * @return the number of spaces between the end of 'other' and the beginning of this chunk
         */
        fun distanceFromEndOf(other: TextChunk): Float {
            return location.distanceFromEndOf(other.location)
        }

        private fun printDiagnostics() {
            println("Text (@" + location.startLocation + " -> " + location.endLocation + "): " + text)
            println("orientationMagnitude: " + location.orientationMagnitude())
            println("distPerpendicular: " + location.distPerpendicular())
            println("distParallel: " + location.distParallelStart())
        }

        /**
         * Compares based on orientation, perpendicular distance, then parallel distance
         * @param rhs the other object
         * *
         * @see java.lang.Comparable.compareTo
         */
        override fun compareTo(rhs: TextChunk): Int {
            return location.compareTo(rhs.location)
        }

        private fun sameLine(lastChunk: TextChunk): Boolean {
            return location.sameLine(lastChunk.location)
        }
    }

    /**
     * no-op method - this renderer isn't interested in image events
     * @see com.itextpdf.text.pdf.parser.RenderListener.renderImage
     * @since 5.0.1
     */
    override fun renderImage(renderInfo: ImageRenderInfo) {
        // do nothing
    }

    /**
     * Specifies a filter for filtering [TextChunk] objects during text extraction
     * @see LocationTextExtractionStrategy.getResultantText
     * @since 5.3.3
     */
    interface TextChunkFilter {
        /**
         * @param textChunk the chunk to check
         * *
         * @return true if the chunk should be allowed
         */
        fun accept(textChunk: TextChunk): Boolean
    }

    companion object {

        /** set to true for debugging  */
        internal var DUMP_STATE = false


        /**

         * @param int1
         * *
         * @param int2
         * *
         * @return comparison of the two integers
         */
        private fun compareInts(int1: Int, int2: Int): Int {
            return if (int1 == int2) 0 else if (int1 < int2) -1 else 1
        }
    }
}
/**
 * Creates a new text extraction renderer.
 */

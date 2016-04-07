/*
 * $Id: 4b98fbfd79f49548a4a1a002f5110bce7a368901 $
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
package com.itextpdf.text.pdf.internal

import com.itextpdf.text.pdf.PdfArray
import com.itextpdf.text.pdf.PdfBoolean
import com.itextpdf.text.pdf.PdfDictionary
import com.itextpdf.text.pdf.PdfName
import com.itextpdf.text.pdf.PdfNumber
import com.itextpdf.text.pdf.PdfObject
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.interfaces.PdfViewerPreferences

/**
 * Stores the information concerning viewer preferences,
 * and contains the business logic that allows you to set viewer preferences.
 */

class PdfViewerPreferencesImp : PdfViewerPreferences {

    /** This value will hold the viewer preferences for the page layout and page mode.  */
    /**
     * Returns the page layout and page mode value.
     */
    var pageLayoutAndMode = 0
        private set

    /** This dictionary holds the viewer preferences (other than page layout and page mode).  */
    /**
     * Returns the viewer preferences.
     */
    val viewerPreferences = PdfDictionary()

    /**
     * Sets the viewer preferences as the sum of several constants.

     * @param preferences
     * *            the viewer preferences
     * *
     * @see PdfViewerPreferences.setViewerPreferences
     */
    override fun setViewerPreferences(preferences: Int) {
        this.pageLayoutAndMode = this.pageLayoutAndMode or preferences
        // for backwards compatibility, it is also possible
        // to set the following viewer preferences with this method:
        if (preferences and viewerPreferencesMask != 0) {
            pageLayoutAndMode = viewerPreferencesMask.inv() and pageLayoutAndMode
            if (preferences and PdfWriter.HideToolbar != 0)
                viewerPreferences.put(PdfName.HIDETOOLBAR, PdfBoolean.PDFTRUE)
            if (preferences and PdfWriter.HideMenubar != 0)
                viewerPreferences.put(PdfName.HIDEMENUBAR, PdfBoolean.PDFTRUE)
            if (preferences and PdfWriter.HideWindowUI != 0)
                viewerPreferences.put(PdfName.HIDEWINDOWUI, PdfBoolean.PDFTRUE)
            if (preferences and PdfWriter.FitWindow != 0)
                viewerPreferences.put(PdfName.FITWINDOW, PdfBoolean.PDFTRUE)
            if (preferences and PdfWriter.CenterWindow != 0)
                viewerPreferences.put(PdfName.CENTERWINDOW, PdfBoolean.PDFTRUE)
            if (preferences and PdfWriter.DisplayDocTitle != 0)
                viewerPreferences.put(PdfName.DISPLAYDOCTITLE, PdfBoolean.PDFTRUE)

            if (preferences and PdfWriter.NonFullScreenPageModeUseNone != 0)
                viewerPreferences.put(PdfName.NONFULLSCREENPAGEMODE, PdfName.USENONE)
            else if (preferences and PdfWriter.NonFullScreenPageModeUseOutlines != 0)
                viewerPreferences.put(PdfName.NONFULLSCREENPAGEMODE, PdfName.USEOUTLINES)
            else if (preferences and PdfWriter.NonFullScreenPageModeUseThumbs != 0)
                viewerPreferences.put(PdfName.NONFULLSCREENPAGEMODE, PdfName.USETHUMBS)
            else if (preferences and PdfWriter.NonFullScreenPageModeUseOC != 0)
                viewerPreferences.put(PdfName.NONFULLSCREENPAGEMODE, PdfName.USEOC)

            if (preferences and PdfWriter.DirectionL2R != 0)
                viewerPreferences.put(PdfName.DIRECTION, PdfName.L2R)
            else if (preferences and PdfWriter.DirectionR2L != 0)
                viewerPreferences.put(PdfName.DIRECTION, PdfName.R2L)

            if (preferences and PdfWriter.PrintScalingNone != 0)
                viewerPreferences.put(PdfName.PRINTSCALING, PdfName.NONE)
        }
    }

    /**
     * Given a key for a viewer preference (a PdfName object),
     * this method returns the index in the VIEWER_PREFERENCES array.
     * @param key    a PdfName referring to a viewer preference
     * *
     * @return    an index in the VIEWER_PREFERENCES array
     */
    private fun getIndex(key: PdfName): Int {
        for (i in VIEWER_PREFERENCES.indices) {
            if (VIEWER_PREFERENCES[i] == key)
                return i
        }
        return -1
    }

    /**
     * Checks if some value is valid for a certain key.
     */
    private fun isPossibleValue(value: PdfName, accepted: Array<PdfName>): Boolean {
        for (i in accepted.indices) {
            if (accepted[i] == value) {
                return true
            }
        }
        return false
    }

    /**
     * Sets the viewer preferences for printing.
     */
    fun addViewerPreference(key: PdfName, value: PdfObject) {
        when (getIndex(key)) {
            0 // HIDETOOLBAR
                , 1 // HIDEMENUBAR
                , 2 // HIDEWINDOWUI
                , 3 // FITWINDOW
                , 4 // CENTERWINDOW
                , 5 // DISPLAYDOCTITLE
                , 14 // PICKTRAYBYPDFSIZE
            -> if (value is PdfBoolean) {
                viewerPreferences.put(key, value)
            }
            6 // NONFULLSCREENPAGEMODE
            -> if (value is PdfName && isPossibleValue(value, NONFULLSCREENPAGEMODE_PREFERENCES)) {
                viewerPreferences.put(key, value)
            }
            7 // DIRECTION
            -> if (value is PdfName && isPossibleValue(value, DIRECTION_PREFERENCES)) {
                viewerPreferences.put(key, value)
            }
            8  // VIEWAREA
                , 9  // VIEWCLIP
                , 10 // PRINTAREA
                , 11 // PRINTCLIP
            -> if (value is PdfName && isPossibleValue(value, PAGE_BOUNDARIES)) {
                viewerPreferences.put(key, value)
            }
            12 // PRINTSCALING
            -> if (value is PdfName && isPossibleValue(value, PRINTSCALING_PREFERENCES)) {
                viewerPreferences.put(key, value)
            }
            13 // DUPLEX
            -> if (value is PdfName && isPossibleValue(value, DUPLEX_PREFERENCES)) {
                viewerPreferences.put(key, value)
            }
            15 // PRINTPAGERANGE
            -> if (value is PdfArray) {
                viewerPreferences.put(key, value)
            }
            16 // NUMCOPIES
            -> if (value is PdfNumber) {
                viewerPreferences.put(key, value)
            }
        }
    }

    /**
     * Adds the viewer preferences defined in the preferences parameter to a
     * PdfDictionary (more specifically the root or catalog of a PDF file).

     * @param catalog
     */
    fun addToCatalog(catalog: PdfDictionary) {
        // Page Layout
        catalog.remove(PdfName.PAGELAYOUT)
        if (pageLayoutAndMode and PdfWriter.PageLayoutSinglePage != 0)
            catalog.put(PdfName.PAGELAYOUT, PdfName.SINGLEPAGE)
        else if (pageLayoutAndMode and PdfWriter.PageLayoutOneColumn != 0)
            catalog.put(PdfName.PAGELAYOUT, PdfName.ONECOLUMN)
        else if (pageLayoutAndMode and PdfWriter.PageLayoutTwoColumnLeft != 0)
            catalog.put(PdfName.PAGELAYOUT, PdfName.TWOCOLUMNLEFT)
        else if (pageLayoutAndMode and PdfWriter.PageLayoutTwoColumnRight != 0)
            catalog.put(PdfName.PAGELAYOUT, PdfName.TWOCOLUMNRIGHT)
        else if (pageLayoutAndMode and PdfWriter.PageLayoutTwoPageLeft != 0)
            catalog.put(PdfName.PAGELAYOUT, PdfName.TWOPAGELEFT)
        else if (pageLayoutAndMode and PdfWriter.PageLayoutTwoPageRight != 0)
            catalog.put(PdfName.PAGELAYOUT, PdfName.TWOPAGERIGHT)

        // Page Mode
        catalog.remove(PdfName.PAGEMODE)
        if (pageLayoutAndMode and PdfWriter.PageModeUseNone != 0)
            catalog.put(PdfName.PAGEMODE, PdfName.USENONE)
        else if (pageLayoutAndMode and PdfWriter.PageModeUseOutlines != 0)
            catalog.put(PdfName.PAGEMODE, PdfName.USEOUTLINES)
        else if (pageLayoutAndMode and PdfWriter.PageModeUseThumbs != 0)
            catalog.put(PdfName.PAGEMODE, PdfName.USETHUMBS)
        else if (pageLayoutAndMode and PdfWriter.PageModeFullScreen != 0)
            catalog.put(PdfName.PAGEMODE, PdfName.FULLSCREEN)
        else if (pageLayoutAndMode and PdfWriter.PageModeUseOC != 0)
            catalog.put(PdfName.PAGEMODE, PdfName.USEOC)
        else if (pageLayoutAndMode and PdfWriter.PageModeUseAttachments != 0)
            catalog.put(PdfName.PAGEMODE, PdfName.USEATTACHMENTS)

        // viewer preferences (Table 8.1 of the PDF Reference)
        catalog.remove(PdfName.VIEWERPREFERENCES)
        if (viewerPreferences.size() > 0) {
            catalog.put(PdfName.VIEWERPREFERENCES, viewerPreferences)
        }
    }

    companion object {
        val VIEWER_PREFERENCES = arrayOf(PdfName.HIDETOOLBAR, // 0
                PdfName.HIDEMENUBAR, // 1
                PdfName.HIDEWINDOWUI, // 2
                PdfName.FITWINDOW, // 3
                PdfName.CENTERWINDOW, // 4
                PdfName.DISPLAYDOCTITLE, // 5
                PdfName.NONFULLSCREENPAGEMODE, // 6
                PdfName.DIRECTION, // 7
                PdfName.VIEWAREA, // 8
                PdfName.VIEWCLIP, // 9
                PdfName.PRINTAREA, // 10
                PdfName.PRINTCLIP, // 11
                PdfName.PRINTSCALING, // 12
                PdfName.DUPLEX, // 13
                PdfName.PICKTRAYBYPDFSIZE, // 14
                PdfName.PRINTPAGERANGE, // 15
                PdfName.NUMCOPIES                // 16
        )


        /** A series of viewer preferences.  */
        val NONFULLSCREENPAGEMODE_PREFERENCES = arrayOf(PdfName.USENONE, PdfName.USEOUTLINES, PdfName.USETHUMBS, PdfName.USEOC)
        /** A series of viewer preferences.  */
        val DIRECTION_PREFERENCES = arrayOf(PdfName.L2R, PdfName.R2L)
        /** A series of viewer preferences.  */
        val PAGE_BOUNDARIES = arrayOf(PdfName.MEDIABOX, PdfName.CROPBOX, PdfName.BLEEDBOX, PdfName.TRIMBOX, PdfName.ARTBOX)
        /** A series of viewer preferences  */
        val PRINTSCALING_PREFERENCES = arrayOf(PdfName.APPDEFAULT, PdfName.NONE)
        /** A series of viewer preferences.  */
        val DUPLEX_PREFERENCES = arrayOf(PdfName.SIMPLEX, PdfName.DUPLEXFLIPSHORTEDGE, PdfName.DUPLEXFLIPLONGEDGE)

        /** The mask to decide if a ViewerPreferences dictionary is needed  */
        private val viewerPreferencesMask = 0xfff000

        fun getViewerPreferences(catalog: PdfDictionary): PdfViewerPreferencesImp {
            val preferences = PdfViewerPreferencesImp()
            var prefs = 0
            var name: PdfName? = null
            // page layout
            var obj: PdfObject? = PdfReader.getPdfObjectRelease(catalog.get(PdfName.PAGELAYOUT))
            if (obj != null && obj.isName) {
                name = obj as PdfName?
                if (name == PdfName.SINGLEPAGE)
                    prefs = prefs or PdfWriter.PageLayoutSinglePage
                else if (name == PdfName.ONECOLUMN)
                    prefs = prefs or PdfWriter.PageLayoutOneColumn
                else if (name == PdfName.TWOCOLUMNLEFT)
                    prefs = prefs or PdfWriter.PageLayoutTwoColumnLeft
                else if (name == PdfName.TWOCOLUMNRIGHT)
                    prefs = prefs or PdfWriter.PageLayoutTwoColumnRight
                else if (name == PdfName.TWOPAGELEFT)
                    prefs = prefs or PdfWriter.PageLayoutTwoPageLeft
                else if (name == PdfName.TWOPAGERIGHT)
                    prefs = prefs or PdfWriter.PageLayoutTwoPageRight
            }
            // page mode
            obj = PdfReader.getPdfObjectRelease(catalog.get(PdfName.PAGEMODE))
            if (obj != null && obj.isName) {
                name = obj as PdfName?
                if (name == PdfName.USENONE)
                    prefs = prefs or PdfWriter.PageModeUseNone
                else if (name == PdfName.USEOUTLINES)
                    prefs = prefs or PdfWriter.PageModeUseOutlines
                else if (name == PdfName.USETHUMBS)
                    prefs = prefs or PdfWriter.PageModeUseThumbs
                else if (name == PdfName.FULLSCREEN)
                    prefs = prefs or PdfWriter.PageModeFullScreen
                else if (name == PdfName.USEOC)
                    prefs = prefs or PdfWriter.PageModeUseOC
                else if (name == PdfName.USEATTACHMENTS)
                    prefs = prefs or PdfWriter.PageModeUseAttachments
            }
            // set page layout and page mode preferences
            preferences.setViewerPreferences(prefs)
            // other preferences
            obj = PdfReader.getPdfObjectRelease(catalog.get(PdfName.VIEWERPREFERENCES))
            if (obj != null && obj.isDictionary) {
                val vp = obj as PdfDictionary?
                for (i in VIEWER_PREFERENCES.indices) {
                    obj = PdfReader.getPdfObjectRelease(vp.get(VIEWER_PREFERENCES[i]))
                    preferences.addViewerPreference(VIEWER_PREFERENCES[i], obj)
                }
            }
            return preferences
        }
    }
}

/*
 * $Id: 54a80a448e991a2413daa3586e8f138cff723baf $
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

import com.itextpdf.text.DocumentException
import com.itextpdf.text.Element
import com.itextpdf.text.Paragraph
import com.itextpdf.text.WritableDirectElement
import com.itextpdf.text.api.Spaceable

import java.util.ArrayList

/**
 * Helper class for PdfDiv to put a collection of Element objects
 * at an absolute position.
 */
class FloatLayout(protected val content: List<Element>, protected val useAscender: Boolean) {
    protected var maxY: Float = 0.toFloat()

    protected var minY: Float = 0.toFloat()

    protected var leftX: Float = 0.toFloat()

    protected var rightX: Float = 0.toFloat()

    var yLine: Float = 0.toFloat()

    protected var floatLeftX: Float = 0.toFloat()

    protected var floatRightX: Float = 0.toFloat()

    var filledWidth: Float = 0.toFloat()

    protected val compositeColumn: ColumnText

    var runDirection: Int
        get() = compositeColumn.runDirection
        set(runDirection) {
            compositeColumn.runDirection = runDirection
        }

    init {
        compositeColumn = ColumnText(null)
        compositeColumn.isUseAscender = useAscender
    }

    fun setSimpleColumn(llx: Float, lly: Float, urx: Float, ury: Float) {
        leftX = Math.min(llx, urx)
        maxY = Math.max(lly, ury)
        minY = Math.min(lly, ury)
        rightX = Math.max(llx, urx)
        floatLeftX = leftX
        floatRightX = rightX
        yLine = maxY
        filledWidth = 0f
    }

    @Throws(DocumentException::class)
    fun layout(canvas: PdfContentByte, simulate: Boolean): Int {
        compositeColumn.canvas = canvas
        var status = ColumnText.NO_MORE_TEXT

        val floatingElements = ArrayList<Element>()
        val content = if (simulate) ArrayList(this.content) else this.content

        while (!content.isEmpty()) {
            if (content[0] is PdfDiv) {
                val floatingElement = content[0] as PdfDiv
                if (floatingElement.floatType == PdfDiv.FloatType.LEFT || floatingElement.floatType == PdfDiv.FloatType.RIGHT) {
                    floatingElements.add(floatingElement)
                    content.removeAt(0)
                } else {
                    if (!floatingElements.isEmpty()) {
                        status = floatingLayout(floatingElements, simulate)
                        if (status and ColumnText.NO_MORE_TEXT == 0) {
                            break
                        }
                    }

                    content.removeAt(0)

                    status = floatingElement.layout(canvas, useAscender, true, floatLeftX, minY, floatRightX, yLine)

                    if (floatingElement.keepTogether && status and ColumnText.NO_MORE_TEXT == 0) {
                        //check for empty page
                        if (compositeColumn.canvas.pdfDocument.currentHeight > 0 || yLine != maxY) {
                            content.add(0, floatingElement)
                            break
                        }
                    }

                    if (!simulate) {
                        canvas.openMCBlock(floatingElement)
                        status = floatingElement.layout(canvas, useAscender, simulate, floatLeftX, minY, floatRightX, yLine)
                        canvas.closeMCBlock(floatingElement)
                    }

                    if (floatingElement.actualWidth > filledWidth) {
                        filledWidth = floatingElement.actualWidth
                    }
                    if (status and ColumnText.NO_MORE_TEXT == 0) {
                        content.add(0, floatingElement)
                        yLine = floatingElement.yLine
                        break
                    } else {
                        yLine -= floatingElement.actualHeight
                    }
                }
            } else {
                floatingElements.add(content[0])
                content.removeAt(0)
            }
        }

        if (status and ColumnText.NO_MORE_TEXT != 0 && !floatingElements.isEmpty()) {
            status = floatingLayout(floatingElements, simulate)
        }

        content.addAll(0, floatingElements)

        return status
    }

    @Throws(DocumentException::class)
    private fun floatingLayout(floatingElements: MutableList<Element>, simulate: Boolean): Int {
        var status = ColumnText.NO_MORE_TEXT
        var minYLine = yLine
        var leftWidth = 0f
        var rightWidth = 0f

        var currentCompositeColumn: ColumnText? = compositeColumn
        if (simulate) {
            currentCompositeColumn = ColumnText.duplicate(compositeColumn)
        }

        var ignoreSpacingBefore = maxY == yLine

        while (!floatingElements.isEmpty()) {
            val nextElement = floatingElements[0]
            floatingElements.removeAt(0)
            if (nextElement is PdfDiv) {
                status = nextElement.layout(compositeColumn.canvas, useAscender, true, floatLeftX, minY, floatRightX, yLine)
                if (status and ColumnText.NO_MORE_TEXT == 0) {
                    yLine = minYLine
                    floatLeftX = leftX
                    floatRightX = rightX
                    status = nextElement.layout(compositeColumn.canvas, useAscender, true, floatLeftX, minY, floatRightX, yLine)
                    if (status and ColumnText.NO_MORE_TEXT == 0) {
                        floatingElements.add(0, nextElement)
                        break
                    }
                }
                if (nextElement.floatType == PdfDiv.FloatType.LEFT) {
                    status = nextElement.layout(compositeColumn.canvas, useAscender, simulate, floatLeftX, minY, floatRightX, yLine)
                    floatLeftX += nextElement.actualWidth
                    leftWidth += nextElement.actualWidth
                } else if (nextElement.floatType == PdfDiv.FloatType.RIGHT) {
                    status = nextElement.layout(compositeColumn.canvas, useAscender, simulate, floatRightX - nextElement.actualWidth - 0.01f, minY, floatRightX, yLine)
                    floatRightX -= nextElement.actualWidth
                    rightWidth += nextElement.actualWidth
                }
                minYLine = Math.min(minYLine, yLine - nextElement.actualHeight)
            } else {
                if (minY > minYLine) {
                    status = ColumnText.NO_MORE_COLUMN
                    floatingElements.add(0, nextElement)
                    if (currentCompositeColumn != null)
                        currentCompositeColumn.setText(null)
                    break
                } else {
                    if (nextElement is Spaceable && (!ignoreSpacingBefore || !currentCompositeColumn!!.isIgnoreSpacingBefore || nextElement.paddingTop != 0f)) {
                        yLine -= nextElement.spacingBefore
                    }
                    if (simulate) {
                        if (nextElement is PdfPTable)
                            currentCompositeColumn!!.addElement(PdfPTable(nextElement))
                        else
                            currentCompositeColumn!!.addElement(nextElement)
                    } else {
                        currentCompositeColumn!!.addElement(nextElement)
                    }

                    if (yLine > minYLine)
                        currentCompositeColumn.setSimpleColumn(floatLeftX, yLine, floatRightX, minYLine)
                    else
                        currentCompositeColumn.setSimpleColumn(floatLeftX, yLine, floatRightX, minY)

                    currentCompositeColumn.filledWidth = 0

                    status = currentCompositeColumn.go(simulate)
                    if (yLine > minYLine && (floatLeftX > leftX || floatRightX < rightX) && status and ColumnText.NO_MORE_TEXT == 0) {
                        yLine = minYLine
                        floatLeftX = leftX
                        floatRightX = rightX
                        if (leftWidth != 0f && rightWidth != 0f) {
                            filledWidth = rightX - leftX
                        } else {
                            if (leftWidth > filledWidth) {
                                filledWidth = leftWidth
                            }
                            if (rightWidth > filledWidth) {
                                filledWidth = rightWidth
                            }
                        }

                        leftWidth = 0f
                        rightWidth = 0f
                        if (simulate && nextElement is PdfPTable) {
                            currentCompositeColumn.addElement(PdfPTable(nextElement))
                        }

                        currentCompositeColumn.setSimpleColumn(floatLeftX, yLine, floatRightX, minY)
                        status = currentCompositeColumn.go(simulate)
                        minYLine = currentCompositeColumn.yLine + currentCompositeColumn.descender
                        yLine = minYLine
                        if (currentCompositeColumn.filledWidth > filledWidth) {
                            filledWidth = currentCompositeColumn.filledWidth
                        }
                    } else {
                        if (rightWidth > 0) {
                            rightWidth += currentCompositeColumn.filledWidth
                        } else if (leftWidth > 0) {
                            leftWidth += currentCompositeColumn.filledWidth
                        } else if (currentCompositeColumn.filledWidth > filledWidth) {
                            filledWidth = currentCompositeColumn.filledWidth
                        }
                        minYLine = Math.min(currentCompositeColumn.yLine + currentCompositeColumn.descender, minYLine)
                        yLine = currentCompositeColumn.yLine + currentCompositeColumn.descender
                    }

                    if (status and ColumnText.NO_MORE_TEXT == 0) {
                        if (!simulate) {
                            floatingElements.addAll(0, currentCompositeColumn.getCompositeElements())
                            currentCompositeColumn.getCompositeElements().clear()
                        } else {
                            floatingElements.add(0, nextElement)
                            currentCompositeColumn.setText(null)
                        }
                        break
                    } else {
                        currentCompositeColumn.setText(null)
                    }
                }
            }

            if (nextElement is Paragraph) {
                for (e in nextElement) {
                    if (e is WritableDirectElement) {
                        if (e.directElementType == WritableDirectElement.DIRECT_ELEMENT_TYPE_HEADER && !simulate) {
                            val writer = compositeColumn.canvas.pdfWriter
                            val doc = compositeColumn.canvas.pdfDocument

                            // here is used a little hack:
                            // writableElement.write() method implementation uses PdfWriter.getVerticalPosition() to create PdfDestination (see com.itextpdf.tool.xml.html.Header),
                            // so here we are adjusting document's currentHeight in order to make getVerticalPosition() return value corresponding to real current position
                            val savedHeight = doc.currentHeight
                            doc.currentHeight = doc.top() - yLine - doc.indentation.indentTop
                            e.write(writer, doc)
                            doc.currentHeight = savedHeight
                        }
                    }
                }
            }

            if (ignoreSpacingBefore && nextElement.chunks.size == 0) {
                if (nextElement is Paragraph) {
                    val e = nextElement[0]
                    if (e is WritableDirectElement) {
                        if (e.directElementType != WritableDirectElement.DIRECT_ELEMENT_TYPE_HEADER) {
                            ignoreSpacingBefore = false
                        }
                    }
                } else if (nextElement is Spaceable) {
                    ignoreSpacingBefore = false
                }

            } else {
                ignoreSpacingBefore = false
            }
        }

        if (leftWidth != 0f && rightWidth != 0f) {
            filledWidth = rightX - leftX
        } else {
            if (leftWidth > filledWidth) {
                filledWidth = leftWidth
            }
            if (rightWidth > filledWidth) {
                filledWidth = rightWidth
            }
        }

        yLine = minYLine
        floatLeftX = leftX
        floatRightX = rightX

        return status
    }
}

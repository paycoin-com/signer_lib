/*
 * Copyright 2003-2012 by Paulo Soares.
 *
 * This list of constants was originally released with libtiff
 * under the following license:
 *
 * Copyright (c) 1988-1997 Sam Leffler
 * Copyright (c) 1991-1997 Silicon Graphics, Inc.
 *
 * Permission to use, copy, modify, distribute, and sell this software and 
 * its documentation for any purpose is hereby granted without fee, provided
 * that (i) the above copyright notices and this permission notice appear in
 * all copies of the software and related documentation, and (ii) the names of
 * Sam Leffler and Silicon Graphics may not be used in any advertising or
 * publicity relating to the software without the specific, prior written
 * permission of Sam Leffler and Silicon Graphics.
 * 
 * THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY 
 * WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  
 * 
 * IN NO EVENT SHALL SAM LEFFLER OR SILICON GRAPHICS BE LIABLE FOR
 * ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES OF ANY KIND,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON ANY THEORY OF 
 * LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 
 * OF THIS SOFTWARE.
 */
package com.itextpdf.text.pdf.codec

/**
 * A list of constants used in class TIFFImage.
 */
object TIFFConstants {

    /*
 * TIFF Tag Definitions (from tifflib).
 */
    /** subfile data descriptor  */
    val TIFFTAG_SUBFILETYPE = 254
    /** reduced resolution version  */
    val FILETYPE_REDUCEDIMAGE = 0x1
    /** one page of many  */
    val FILETYPE_PAGE = 0x2
    /** transparency mask  */
    val FILETYPE_MASK = 0x4
    /** +kind of data in subfile  */
    val TIFFTAG_OSUBFILETYPE = 255
    /** full resolution image data  */
    val OFILETYPE_IMAGE = 1
    /** reduced size image data  */
    val OFILETYPE_REDUCEDIMAGE = 2
    /** one page of many  */
    val OFILETYPE_PAGE = 3
    /** image width in pixels  */
    val TIFFTAG_IMAGEWIDTH = 256
    /** image height in pixels  */
    val TIFFTAG_IMAGELENGTH = 257
    /** bits per channel (sample)  */
    val TIFFTAG_BITSPERSAMPLE = 258
    /** data compression technique  */
    val TIFFTAG_COMPRESSION = 259
    /** dump mode  */
    val COMPRESSION_NONE = 1
    /** CCITT modified Huffman RLE  */
    val COMPRESSION_CCITTRLE = 2
    /** CCITT Group 3 fax encoding  */
    val COMPRESSION_CCITTFAX3 = 3
    /** CCITT Group 4 fax encoding  */
    val COMPRESSION_CCITTFAX4 = 4
    /** Lempel-Ziv & Welch  */
    val COMPRESSION_LZW = 5
    /** !6.0 JPEG  */
    val COMPRESSION_OJPEG = 6
    /** %JPEG DCT compression  */
    val COMPRESSION_JPEG = 7
    /** NeXT 2-bit RLE  */
    val COMPRESSION_NEXT = 32766
    /** #1 w/ word alignment  */
    val COMPRESSION_CCITTRLEW = 32771
    /** Macintosh RLE  */
    val COMPRESSION_PACKBITS = 32773
    /** ThunderScan RLE  */
    val COMPRESSION_THUNDERSCAN = 32809
    /* codes 32895-32898 are reserved for ANSI IT8 TIFF/IT <dkelly@etsinc.com) */
    /** IT8 CT w/padding  */
    val COMPRESSION_IT8CTPAD = 32895
    /** IT8 Linework RLE  */
    val COMPRESSION_IT8LW = 32896
    /** IT8 Monochrome picture  */
    val COMPRESSION_IT8MP = 32897
    /** IT8 Binary line art  */
    val COMPRESSION_IT8BL = 32898
    /* compression codes 32908-32911 are reserved for Pixar */
    /** Pixar companded 10bit LZW  */
    val COMPRESSION_PIXARFILM = 32908
    /** Pixar companded 11bit ZIP  */
    val COMPRESSION_PIXARLOG = 32909
    /** Deflate compression  */
    val COMPRESSION_DEFLATE = 32946
    /** Deflate compression, as recognized by Adobe  */
    val COMPRESSION_ADOBE_DEFLATE = 8
    /* compression code 32947 is reserved for Oceana Matrix <dev@oceana.com> */
    /** Kodak DCS encoding  */
    val COMPRESSION_DCS = 32947
    /** ISO JBIG  */
    val COMPRESSION_JBIG = 34661
    /** SGI Log Luminance RLE  */
    val COMPRESSION_SGILOG = 34676
    /** SGI Log 24-bit packed  */
    val COMPRESSION_SGILOG24 = 34677
    /** photometric interpretation  */
    val TIFFTAG_PHOTOMETRIC = 262
    /** min value is white  */
    val PHOTOMETRIC_MINISWHITE = 0
    /** min value is black  */
    val PHOTOMETRIC_MINISBLACK = 1
    /** RGB color model  */
    val PHOTOMETRIC_RGB = 2
    /** color map indexed  */
    val PHOTOMETRIC_PALETTE = 3
    /** $holdout mask  */
    val PHOTOMETRIC_MASK = 4
    /** !color separations  */
    val PHOTOMETRIC_SEPARATED = 5
    /** !CCIR 601  */
    val PHOTOMETRIC_YCBCR = 6
    /** !1976 CIE L*a*b*  */
    val PHOTOMETRIC_CIELAB = 8
    /** CIE Log2(L)  */
    val PHOTOMETRIC_LOGL = 32844
    /** CIE Log2(L) (u',v')  */
    val PHOTOMETRIC_LOGLUV = 32845
    /** +thresholding used on data  */
    val TIFFTAG_THRESHHOLDING = 263
    /** b&w art scan  */
    val THRESHHOLD_BILEVEL = 1
    /** or dithered scan  */
    val THRESHHOLD_HALFTONE = 2
    /** usually floyd-steinberg  */
    val THRESHHOLD_ERRORDIFFUSE = 3
    /** +dithering matrix width  */
    val TIFFTAG_CELLWIDTH = 264
    /** +dithering matrix height  */
    val TIFFTAG_CELLLENGTH = 265
    /** data order within a byte  */
    val TIFFTAG_FILLORDER = 266
    /** most significant -> least  */
    val FILLORDER_MSB2LSB = 1
    /** least significant -> most  */
    val FILLORDER_LSB2MSB = 2
    /** name of doc. image is from  */
    val TIFFTAG_DOCUMENTNAME = 269
    /** info about image  */
    val TIFFTAG_IMAGEDESCRIPTION = 270
    /** scanner manufacturer name  */
    val TIFFTAG_MAKE = 271
    /** scanner model name/number  */
    val TIFFTAG_MODEL = 272
    /** offsets to data strips  */
    val TIFFTAG_STRIPOFFSETS = 273
    /** +image orientation  */
    val TIFFTAG_ORIENTATION = 274
    /** row 0 top, col 0 lhs  */
    val ORIENTATION_TOPLEFT = 1
    /** row 0 top, col 0 rhs  */
    val ORIENTATION_TOPRIGHT = 2
    /** row 0 bottom, col 0 rhs  */
    val ORIENTATION_BOTRIGHT = 3
    /** row 0 bottom, col 0 lhs  */
    val ORIENTATION_BOTLEFT = 4
    /** row 0 lhs, col 0 top  */
    val ORIENTATION_LEFTTOP = 5
    /** row 0 rhs, col 0 top  */
    val ORIENTATION_RIGHTTOP = 6
    /** row 0 rhs, col 0 bottom  */
    val ORIENTATION_RIGHTBOT = 7
    /** row 0 lhs, col 0 bottom  */
    val ORIENTATION_LEFTBOT = 8
    /** samples per pixel  */
    val TIFFTAG_SAMPLESPERPIXEL = 277
    /** rows per strip of data  */
    val TIFFTAG_ROWSPERSTRIP = 278
    /** bytes counts for strips  */
    val TIFFTAG_STRIPBYTECOUNTS = 279
    /** +minimum sample value  */
    val TIFFTAG_MINSAMPLEVALUE = 280
    /** +maximum sample value  */
    val TIFFTAG_MAXSAMPLEVALUE = 281
    /** pixels/resolution in x  */
    val TIFFTAG_XRESOLUTION = 282
    /** pixels/resolution in y  */
    val TIFFTAG_YRESOLUTION = 283
    /** storage organization  */
    val TIFFTAG_PLANARCONFIG = 284
    /** single image plane  */
    val PLANARCONFIG_CONTIG = 1
    /** separate planes of data  */
    val PLANARCONFIG_SEPARATE = 2
    /** page name image is from  */
    val TIFFTAG_PAGENAME = 285
    /** x page offset of image lhs  */
    val TIFFTAG_XPOSITION = 286
    /** y page offset of image lhs  */
    val TIFFTAG_YPOSITION = 287
    /** +byte offset to free block  */
    val TIFFTAG_FREEOFFSETS = 288
    /** +sizes of free blocks  */
    val TIFFTAG_FREEBYTECOUNTS = 289
    /** $gray scale curve accuracy  */
    val TIFFTAG_GRAYRESPONSEUNIT = 290
    /** tenths of a unit  */
    val GRAYRESPONSEUNIT_10S = 1
    /** hundredths of a unit  */
    val GRAYRESPONSEUNIT_100S = 2
    /** thousandths of a unit  */
    val GRAYRESPONSEUNIT_1000S = 3
    /** ten-thousandths of a unit  */
    val GRAYRESPONSEUNIT_10000S = 4
    /** hundred-thousandths  */
    val GRAYRESPONSEUNIT_100000S = 5
    /** $gray scale response curve  */
    val TIFFTAG_GRAYRESPONSECURVE = 291
    /** 32 flag bits  */
    val TIFFTAG_GROUP3OPTIONS = 292
    /** 2-dimensional coding  */
    val GROUP3OPT_2DENCODING = 0x1
    /** data not compressed  */
    val GROUP3OPT_UNCOMPRESSED = 0x2
    /** fill to byte boundary  */
    val GROUP3OPT_FILLBITS = 0x4
    /** 32 flag bits  */
    val TIFFTAG_GROUP4OPTIONS = 293
    /** data not compressed  */
    val GROUP4OPT_UNCOMPRESSED = 0x2
    /** units of resolutions  */
    val TIFFTAG_RESOLUTIONUNIT = 296
    /** no meaningful units  */
    val RESUNIT_NONE = 1
    /** english  */
    val RESUNIT_INCH = 2
    /** metric  */
    val RESUNIT_CENTIMETER = 3
    /** page numbers of multi-page  */
    val TIFFTAG_PAGENUMBER = 297
    /** $color curve accuracy  */
    val TIFFTAG_COLORRESPONSEUNIT = 300
    /** tenths of a unit  */
    val COLORRESPONSEUNIT_10S = 1
    /** hundredths of a unit  */
    val COLORRESPONSEUNIT_100S = 2
    /** thousandths of a unit  */
    val COLORRESPONSEUNIT_1000S = 3
    /** ten-thousandths of a unit  */
    val COLORRESPONSEUNIT_10000S = 4
    /** hundred-thousandths  */
    val COLORRESPONSEUNIT_100000S = 5
    /** !colorimetry info  */
    val TIFFTAG_TRANSFERFUNCTION = 301
    /** name & release  */
    val TIFFTAG_SOFTWARE = 305
    /** creation date and time  */
    val TIFFTAG_DATETIME = 306
    /** creator of image  */
    val TIFFTAG_ARTIST = 315
    /** machine where created  */
    val TIFFTAG_HOSTCOMPUTER = 316
    /** prediction scheme w/ LZW  */
    val TIFFTAG_PREDICTOR = 317
    /**
     * no predictor
     * @since 5.0.3
     */
    val PREDICTOR_NONE = 1
    /**
     * horizontal differencing
     * @since 5.0.3
     */
    val PREDICTOR_HORIZONTAL_DIFFERENCING = 2
    /** image white point  */
    val TIFFTAG_WHITEPOINT = 318
    /** !primary chromaticities  */
    val TIFFTAG_PRIMARYCHROMATICITIES = 319
    /** RGB map for pallette image  */
    val TIFFTAG_COLORMAP = 320
    /** !highlight+shadow info  */
    val TIFFTAG_HALFTONEHINTS = 321
    /** !rows/data tile  */
    val TIFFTAG_TILEWIDTH = 322
    /** !cols/data tile  */
    val TIFFTAG_TILELENGTH = 323
    /** !offsets to data tiles  */
    val TIFFTAG_TILEOFFSETS = 324
    /** !byte counts for tiles  */
    val TIFFTAG_TILEBYTECOUNTS = 325
    /** lines w/ wrong pixel count  */
    val TIFFTAG_BADFAXLINES = 326
    /** regenerated line info  */
    val TIFFTAG_CLEANFAXDATA = 327
    /** no errors detected  */
    val CLEANFAXDATA_CLEAN = 0
    /** receiver regenerated lines  */
    val CLEANFAXDATA_REGENERATED = 1
    /** uncorrected errors exist  */
    val CLEANFAXDATA_UNCLEAN = 2
    /** max consecutive bad lines  */
    val TIFFTAG_CONSECUTIVEBADFAXLINES = 328
    /** subimage descriptors  */
    val TIFFTAG_SUBIFD = 330
    /** !inks in separated image  */
    val TIFFTAG_INKSET = 332
    /** !cyan-magenta-yellow-black  */
    val INKSET_CMYK = 1
    /** !ascii names of inks  */
    val TIFFTAG_INKNAMES = 333
    /** !number of inks  */
    val TIFFTAG_NUMBEROFINKS = 334
    /** !0% and 100% dot codes  */
    val TIFFTAG_DOTRANGE = 336
    /** !separation target  */
    val TIFFTAG_TARGETPRINTER = 337
    /** !info about extra samples  */
    val TIFFTAG_EXTRASAMPLES = 338
    /** !unspecified data  */
    val EXTRASAMPLE_UNSPECIFIED = 0
    /** !associated alpha data  */
    val EXTRASAMPLE_ASSOCALPHA = 1
    /** !unassociated alpha data  */
    val EXTRASAMPLE_UNASSALPHA = 2
    /** !data sample format  */
    val TIFFTAG_SAMPLEFORMAT = 339
    /** !unsigned integer data  */
    val SAMPLEFORMAT_UINT = 1
    /** !signed integer data  */
    val SAMPLEFORMAT_INT = 2
    /** !IEEE floating point data  */
    val SAMPLEFORMAT_IEEEFP = 3
    /** !untyped data  */
    val SAMPLEFORMAT_VOID = 4
    /** !complex signed int  */
    val SAMPLEFORMAT_COMPLEXINT = 5
    /** !complex ieee floating  */
    val SAMPLEFORMAT_COMPLEXIEEEFP = 6
    /** !variable MinSampleValue  */
    val TIFFTAG_SMINSAMPLEVALUE = 340
    /** !variable MaxSampleValue  */
    val TIFFTAG_SMAXSAMPLEVALUE = 341
    /** %JPEG table stream  */
    val TIFFTAG_JPEGTABLES = 347
    /*
     * Tags 512-521 are obsoleted by Technical Note #2
     * which specifies a revised JPEG-in-TIFF scheme.
     */
    /** !JPEG processing algorithm  */
    val TIFFTAG_JPEGPROC = 512
    /** !baseline sequential  */
    val JPEGPROC_BASELINE = 1
    /** !Huffman coded lossless  */
    val JPEGPROC_LOSSLESS = 14
    /** !pointer to SOI marker  */
    val TIFFTAG_JPEGIFOFFSET = 513
    /** !JFIF stream length  */
    val TIFFTAG_JPEGIFBYTECOUNT = 514
    /** !restart interval length  */
    val TIFFTAG_JPEGRESTARTINTERVAL = 515
    /** !lossless proc predictor  */
    val TIFFTAG_JPEGLOSSLESSPREDICTORS = 517
    /** !lossless point transform  */
    val TIFFTAG_JPEGPOINTTRANSFORM = 518
    /** !Q matrice offsets  */
    val TIFFTAG_JPEGQTABLES = 519
    /** !DCT table offsets  */
    val TIFFTAG_JPEGDCTABLES = 520
    /** !AC coefficient offsets  */
    val TIFFTAG_JPEGACTABLES = 521
    /** !RGB -> YCbCr transform  */
    val TIFFTAG_YCBCRCOEFFICIENTS = 529
    /** !YCbCr subsampling factors  */
    val TIFFTAG_YCBCRSUBSAMPLING = 530
    /** !subsample positioning  */
    val TIFFTAG_YCBCRPOSITIONING = 531
    /** !as in PostScript Level 2  */
    val YCBCRPOSITION_CENTERED = 1
    /** !as in CCIR 601-1  */
    val YCBCRPOSITION_COSITED = 2
    /** !colorimetry info  */
    val TIFFTAG_REFERENCEBLACKWHITE = 532
    /* tags 32952-32956 are private tags registered to Island Graphics */
    /** image reference points  */
    val TIFFTAG_REFPTS = 32953
    /** region-xform tack point  */
    val TIFFTAG_REGIONTACKPOINT = 32954
    /** warp quadrilateral  */
    val TIFFTAG_REGIONWARPCORNERS = 32955
    /** affine transformation mat  */
    val TIFFTAG_REGIONAFFINE = 32956
    /* tags 32995-32999 are private tags registered to SGI */
    /** $use ExtraSamples  */
    val TIFFTAG_MATTEING = 32995
    /** $use SampleFormat  */
    val TIFFTAG_DATATYPE = 32996
    /** z depth of image  */
    val TIFFTAG_IMAGEDEPTH = 32997
    /** z depth/data tile  */
    val TIFFTAG_TILEDEPTH = 32998
    /* tags 33300-33309 are private tags registered to Pixar
     * TIFFTAG_PIXAR_IMAGEFULLWIDTH and TIFFTAG_PIXAR_IMAGEFULLLENGTH
     * are set when an image has been cropped out of a larger image.
     * They reflect the size of the original uncropped image.
     * The TIFFTAG_XPOSITION and TIFFTAG_YPOSITION can be used
     * to determine the position of the smaller image in the larger one.
     */
    /** full image size in x  */
    val TIFFTAG_PIXAR_IMAGEFULLWIDTH = 33300
    /** full image size in y  */
    val TIFFTAG_PIXAR_IMAGEFULLLENGTH = 33301
    /* Tags 33302-33306 are used to identify special image modes and data used by Pixar's texture formats. */
    /** texture map format  */
    val TIFFTAG_PIXAR_TEXTUREFORMAT = 33302
    /** s & t wrap modes  */
    val TIFFTAG_PIXAR_WRAPMODES = 33303
    /** cotan(fov) for env. maps  */
    val TIFFTAG_PIXAR_FOVCOT = 33304
    /** W2S  */
    val TIFFTAG_PIXAR_MATRIX_WORLDTOSCREEN = 33305
    /** W2C  */
    val TIFFTAG_PIXAR_MATRIX_WORLDTOCAMERA = 33306
    /**
     * device serial number
     * tag 33405 is a private tag registered to Eastman Kodak
     */
    val TIFFTAG_WRITERSERIALNUMBER = 33405
    /** tag 33432 is listed in the 6.0 spec w/ unknown ownership  */
    val TIFFTAG_COPYRIGHT = 33432    /* copyright string */
    /** IPTC TAG from RichTIFF specifications  */
    val TIFFTAG_RICHTIFFIPTC = 33723
    /* 34016-34029 are reserved for ANSI IT8 TIFF/IT <dkelly@etsinc.com) */
    /** site name  */
    val TIFFTAG_IT8SITE = 34016
    /** color seq. [RGB,CMYK,etc]  */
    val TIFFTAG_IT8COLORSEQUENCE = 34017
    /** DDES Header  */
    val TIFFTAG_IT8HEADER = 34018
    /** raster scanline padding  */
    val TIFFTAG_IT8RASTERPADDING = 34019
    /** # of bits in short run  */
    val TIFFTAG_IT8BITSPERRUNLENGTH = 34020
    /** # of bits in long run  */
    val TIFFTAG_IT8BITSPEREXTENDEDRUNLENGTH = 34021
    /** LW colortable  */
    val TIFFTAG_IT8COLORTABLE = 34022
    /** BP/BL image color switch  */
    val TIFFTAG_IT8IMAGECOLORINDICATOR = 34023
    /** BP/BL bg color switch  */
    val TIFFTAG_IT8BKGCOLORINDICATOR = 34024
    /** BP/BL image color value  */
    val TIFFTAG_IT8IMAGECOLORVALUE = 34025
    /** BP/BL bg color value  */
    val TIFFTAG_IT8BKGCOLORVALUE = 34026
    /** MP pixel intensity value  */
    val TIFFTAG_IT8PIXELINTENSITYRANGE = 34027
    /** HC transparency switch  */
    val TIFFTAG_IT8TRANSPARENCYINDICATOR = 34028
    /** color character. table  */
    val TIFFTAG_IT8COLORCHARACTERIZATION = 34029
    /* tags 34232-34236 are private tags registered to Texas Instruments */
    /** Sequence Frame Count  */
    val TIFFTAG_FRAMECOUNT = 34232
    /**
     * ICC profile data
     * tag 34750 is a private tag registered to Adobe?
     */
    val TIFFTAG_ICCPROFILE = 34675
    /** tag 34377 is private tag registered to Adobe for PhotoShop  */
    val TIFFTAG_PHOTOSHOP = 34377
    /**
     * JBIG options
     * tag 34750 is a private tag registered to Pixel Magic
     */
    val TIFFTAG_JBIGOPTIONS = 34750
    /* tags 34908-34914 are private tags registered to SGI */
    /** encoded Class 2 ses. parms  */
    val TIFFTAG_FAXRECVPARAMS = 34908
    /** received SubAddr string  */
    val TIFFTAG_FAXSUBADDRESS = 34909
    /** receive time (secs)  */
    val TIFFTAG_FAXRECVTIME = 34910
    /* tags 37439-37443 are registered to SGI <gregl@sgi.com> */
    /**
     * Sample value to Nits
     */
    val TIFFTAG_STONITS = 37439
    /**
     * unknown use
     * tag 34929 is a private tag registered to FedEx
     */
    val TIFFTAG_FEDEX_EDR = 34929
    /**
     * hue shift correction data
     * tag 65535 is an undefined tag used by Eastman Kodak
     */
    val TIFFTAG_DCSHUESHIFTVALUES = 65535

}

package org.bouncycastle.asn1

interface BERTags {
    companion object {
        val BOOLEAN = 0x01
        val INTEGER = 0x02
        val BIT_STRING = 0x03
        val OCTET_STRING = 0x04
        val NULL = 0x05
        val OBJECT_IDENTIFIER = 0x06
        val EXTERNAL = 0x08
        val ENUMERATED = 0x0a
        val SEQUENCE = 0x10
        val SEQUENCE_OF = 0x10 // for completeness - used to model a SEQUENCE of the same type.
        val SET = 0x11
        val SET_OF = 0x11 // for completeness - used to model a SET of the same type.


        val NUMERIC_STRING = 0x12
        val PRINTABLE_STRING = 0x13
        val T61_STRING = 0x14
        val VIDEOTEX_STRING = 0x15
        val IA5_STRING = 0x16
        val UTC_TIME = 0x17
        val GENERALIZED_TIME = 0x18
        val GRAPHIC_STRING = 0x19
        val VISIBLE_STRING = 0x1a
        val GENERAL_STRING = 0x1b
        val UNIVERSAL_STRING = 0x1c
        val BMP_STRING = 0x1e
        val UTF8_STRING = 0x0c

        val CONSTRUCTED = 0x20
        val APPLICATION = 0x40
        val TAGGED = 0x80
    }
}

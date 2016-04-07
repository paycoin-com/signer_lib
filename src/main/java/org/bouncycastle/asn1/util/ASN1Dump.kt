package org.bouncycastle.asn1.util

import java.io.IOException
import java.util.Enumeration

import org.bouncycastle.asn1.ASN1ApplicationSpecific
import org.bouncycastle.asn1.ASN1Boolean
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1Enumerated
import org.bouncycastle.asn1.ASN1GeneralizedTime
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1Set
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.ASN1UTCTime
import org.bouncycastle.asn1.BERApplicationSpecific
import org.bouncycastle.asn1.BEROctetString
import org.bouncycastle.asn1.BERSequence
import org.bouncycastle.asn1.BERSet
import org.bouncycastle.asn1.BERTaggedObject
import org.bouncycastle.asn1.BERTags
import org.bouncycastle.asn1.DERApplicationSpecific
import org.bouncycastle.asn1.DERBMPString
import org.bouncycastle.asn1.DERBitString
import org.bouncycastle.asn1.DERExternal
import org.bouncycastle.asn1.DERGraphicString
import org.bouncycastle.asn1.DERIA5String
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.DERPrintableString
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.DERT61String
import org.bouncycastle.asn1.DERUTF8String
import org.bouncycastle.asn1.DERVideotexString
import org.bouncycastle.asn1.DERVisibleString
import org.bouncycastle.util.Strings
import org.bouncycastle.util.encoders.Hex

open class ASN1Dump {
    companion object {
        private val TAB = "    "
        private val SAMPLE_SIZE = 32

        /**
         * dump a DER object as a formatted string with indentation

         * @param obj the ASN1Primitive to be dumped out.
         */
        internal fun _dumpAsString(
                indent: String,
                verbose: Boolean,
                obj: ASN1Primitive,
                buf: StringBuffer) {
            val nl = Strings.lineSeparator()
            if (obj is ASN1Sequence) {
                val e = obj.objects
                val tab = indent + TAB

                buf.append(indent)
                if (obj is BERSequence) {
                    buf.append("BER Sequence")
                } else if (obj is DERSequence) {
                    buf.append("DER Sequence")
                } else {
                    buf.append("Sequence")
                }

                buf.append(nl)

                while (e.hasMoreElements()) {
                    val o = e.nextElement()

                    if (o == null || o == DERNull.INSTANCE) {
                        buf.append(tab)
                        buf.append("NULL")
                        buf.append(nl)
                    } else if (o is ASN1Primitive) {
                        _dumpAsString(tab, verbose, o as ASN1Primitive?, buf)
                    } else {
                        _dumpAsString(tab, verbose, (o as ASN1Encodable).toASN1Primitive(), buf)
                    }
                }
            } else if (obj is ASN1TaggedObject) {
                val tab = indent + TAB

                buf.append(indent)
                if (obj is BERTaggedObject) {
                    buf.append("BER Tagged [")
                } else {
                    buf.append("Tagged [")
                }

                buf.append(Integer.toString(obj.tagNo))
                buf.append(']')

                if (!obj.isExplicit) {
                    buf.append(" IMPLICIT ")
                }

                buf.append(nl)

                if (obj.isEmpty) {
                    buf.append(tab)
                    buf.append("EMPTY")
                    buf.append(nl)
                } else {
                    _dumpAsString(tab, verbose, obj.`object`, buf)
                }
            } else if (obj is ASN1Set) {
                val e = obj.objects
                val tab = indent + TAB

                buf.append(indent)

                if (obj is BERSet) {
                    buf.append("BER Set")
                } else {
                    buf.append("DER Set")
                }

                buf.append(nl)

                while (e.hasMoreElements()) {
                    val o = e.nextElement()

                    if (o == null) {
                        buf.append(tab)
                        buf.append("NULL")
                        buf.append(nl)
                    } else if (o is ASN1Primitive) {
                        _dumpAsString(tab, verbose, o as ASN1Primitive?, buf)
                    } else {
                        _dumpAsString(tab, verbose, (o as ASN1Encodable).toASN1Primitive(), buf)
                    }
                }
            } else if (obj is ASN1OctetString) {

                if (obj is BEROctetString) {
                    buf.append(indent + "BER Constructed Octet String" + "[" + obj.octets.size + "] ")
                } else {
                    buf.append(indent + "DER Octet String" + "[" + obj.octets.size + "] ")
                }
                if (verbose) {
                    buf.append(dumpBinaryDataAsString(indent, obj.octets))
                } else {
                    buf.append(nl)
                }
            } else if (obj is ASN1ObjectIdentifier) {
                buf.append(indent + "ObjectIdentifier(" + obj.id + ")" + nl)
            } else if (obj is ASN1Boolean) {
                buf.append(indent + "Boolean(" + obj.isTrue + ")" + nl)
            } else if (obj is ASN1Integer) {
                buf.append(indent + "Integer(" + obj.value + ")" + nl)
            } else if (obj is DERBitString) {
                buf.append(indent + "DER Bit String" + "[" + obj.bytes.size + ", " + obj.padBits + "] ")
                if (verbose) {
                    buf.append(dumpBinaryDataAsString(indent, obj.bytes))
                } else {
                    buf.append(nl)
                }
            } else if (obj is DERIA5String) {
                buf.append(indent + "IA5String(" + obj.string + ") " + nl)
            } else if (obj is DERUTF8String) {
                buf.append(indent + "UTF8String(" + obj.string + ") " + nl)
            } else if (obj is DERPrintableString) {
                buf.append(indent + "PrintableString(" + obj.string + ") " + nl)
            } else if (obj is DERVisibleString) {
                buf.append(indent + "VisibleString(" + obj.string + ") " + nl)
            } else if (obj is DERBMPString) {
                buf.append(indent + "BMPString(" + obj.string + ") " + nl)
            } else if (obj is DERT61String) {
                buf.append(indent + "T61String(" + obj.string + ") " + nl)
            } else if (obj is DERGraphicString) {
                buf.append(indent + "GraphicString(" + obj.string + ") " + nl)
            } else if (obj is DERVideotexString) {
                buf.append(indent + "VideotexString(" + obj.string + ") " + nl)
            } else if (obj is ASN1UTCTime) {
                buf.append(indent + "UTCTime(" + obj.time + ") " + nl)
            } else if (obj is ASN1GeneralizedTime) {
                buf.append(indent + "GeneralizedTime(" + obj.time + ") " + nl)
            } else if (obj is BERApplicationSpecific) {
                buf.append(outputApplicationSpecific("BER", indent, verbose, obj, nl))
            } else if (obj is DERApplicationSpecific) {
                buf.append(outputApplicationSpecific("DER", indent, verbose, obj, nl))
            } else if (obj is ASN1Enumerated) {
                buf.append(indent + "DER Enumerated(" + obj.value + ")" + nl)
            } else if (obj is DERExternal) {
                buf.append(indent + "External " + nl)
                val tab = indent + TAB
                if (obj.directReference != null) {
                    buf.append(tab + "Direct Reference: " + obj.directReference.id + nl)
                }
                if (obj.indirectReference != null) {
                    buf.append(tab + "Indirect Reference: " + obj.indirectReference.toString() + nl)
                }
                if (obj.dataValueDescriptor != null) {
                    _dumpAsString(tab, verbose, obj.dataValueDescriptor, buf)
                }
                buf.append(tab + "Encoding: " + obj.encoding + nl)
                _dumpAsString(tab, verbose, obj.externalContent, buf)
            } else {
                buf.append(indent + obj.toString() + nl)
            }
        }

        private fun outputApplicationSpecific(type: String, indent: String, verbose: Boolean, obj: ASN1Primitive, nl: String): String {
            val app = ASN1ApplicationSpecific.getInstance(obj)
            val buf = StringBuffer()

            if (app.isConstructed) {
                try {
                    val s = ASN1Sequence.getInstance(app.getObject(BERTags.SEQUENCE))
                    buf.append(indent + type + " ApplicationSpecific[" + app.applicationTag + "]" + nl)
                    val e = s.objects
                    while (e.hasMoreElements()) {
                        _dumpAsString(indent + TAB, verbose, e.nextElement() as ASN1Primitive, buf)
                    }
                } catch (e: IOException) {
                    buf.append(e)
                }

                return buf.toString()
            }

            return indent + type + " ApplicationSpecific[" + app.applicationTag + "] (" + String(Hex.encode(app.contents)) + ")" + nl
        }

        /**
         * Dump out the object as a string.

         * @param obj  the object to be dumped
         * *
         * @param verbose  if true, dump out the contents of octet and bit strings.
         * *
         * @return  the resulting string.
         */
        @JvmOverloads fun dumpAsString(
                obj: Any,
                verbose: Boolean = false): String {
            val buf = StringBuffer()

            if (obj is ASN1Primitive) {
                _dumpAsString("", verbose, obj, buf)
            } else if (obj is ASN1Encodable) {
                _dumpAsString("", verbose, obj.toASN1Primitive(), buf)
            } else {
                return "unknown object type " + obj.toString()
            }

            return buf.toString()
        }

        private fun dumpBinaryDataAsString(indent: String, bytes: ByteArray): String {
            var indent = indent
            val nl = Strings.lineSeparator()
            val buf = StringBuffer()

            indent += TAB

            buf.append(nl)
            var i = 0
            while (i < bytes.size) {
                if (bytes.size - i > SAMPLE_SIZE) {
                    buf.append(indent)
                    buf.append(String(Hex.encode(bytes, i, SAMPLE_SIZE)))
                    buf.append(TAB)
                    buf.append(calculateAscString(bytes, i, SAMPLE_SIZE))
                    buf.append(nl)
                } else {
                    buf.append(indent)
                    buf.append(String(Hex.encode(bytes, i, bytes.size - i)))
                    for (j in bytes.size - i..SAMPLE_SIZE - 1) {
                        buf.append("  ")
                    }
                    buf.append(TAB)
                    buf.append(calculateAscString(bytes, i, bytes.size - i))
                    buf.append(nl)
                }
                i += SAMPLE_SIZE
            }

            return buf.toString()
        }

        private fun calculateAscString(bytes: ByteArray, off: Int, len: Int): String {
            val buf = StringBuffer()

            for (i in off..off + len - 1) {
                if (bytes[i] >= ' ' && bytes[i] <= '~') {
                    buf.append(bytes[i].toChar())
                }
            }

            return buf.toString()
        }
    }
}
/**
 * dump out a DER object as a formatted string, in non-verbose mode.

 * @param obj the ASN1Primitive to be dumped out.
 * *
 * @return  the resulting string.
 */

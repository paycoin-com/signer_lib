package org.bouncycastle.asn1.x509

/**
 * class for breaking up an X500 Name into it's component tokens, ala
 * java.util.StringTokenizer. We need this class as some of the
 * lightweight Java environment don't support classes like
 * StringTokenizer.
 */
@Deprecated("use X500NameTokenizer")
class X509NameTokenizer @JvmOverloads constructor(
        private val value: String,
        private val separator: Char = ',') {
    private var index: Int = 0
    private val buf = StringBuffer()

    init {
        this.index = -1
    }

    fun hasMoreTokens(): Boolean {
        return index != value.length
    }

    fun nextToken(): String? {
        if (index == value.length) {
            return null
        }

        var end = index + 1
        var quoted = false
        var escaped = false

        buf.setLength(0)

        while (end != value.length) {
            val c = value[end]

            if (c == '"') {
                if (!escaped) {
                    quoted = !quoted
                }
                buf.append(c)
                escaped = false
            } else {
                if (escaped || quoted) {
                    buf.append(c)
                    escaped = false
                } else if (c == '\\') {
                    buf.append(c)
                    escaped = true
                } else if (c == separator) {
                    break
                } else {
                    buf.append(c)
                }
            }
            end++
        }

        index = end

        return buf.toString()
    }
}

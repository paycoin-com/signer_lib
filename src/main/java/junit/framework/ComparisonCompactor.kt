package junit.framework

class ComparisonCompactor(private val fContextLength: Int, private val fExpected: String?, private val fActual: String?) {
    private var fPrefix: Int = 0
    private var fSuffix: Int = 0

    fun compact(message: String): String {
        if (fExpected == null || fActual == null || areStringsEqual()) {
            return Assert.format(message, fExpected, fActual)
        }

        findCommonPrefix()
        findCommonSuffix()
        val expected = compactString(fExpected)
        val actual = compactString(fActual)
        return Assert.format(message, expected, actual)
    }

    private fun compactString(source: String): String {
        var result = DELTA_START + source.substring(fPrefix, source.length - fSuffix + 1) + DELTA_END
        if (fPrefix > 0) {
            result = computeCommonPrefix() + result
        }
        if (fSuffix > 0) {
            result = result + computeCommonSuffix()
        }
        return result
    }

    private fun findCommonPrefix() {
        fPrefix = 0
        val end = Math.min(fExpected!!.length, fActual!!.length)
        while (fPrefix < end) {
            if (fExpected[fPrefix] != fActual[fPrefix]) {
                break
            }
            fPrefix++
        }
    }

    private fun findCommonSuffix() {
        var expectedSuffix = fExpected!!.length - 1
        var actualSuffix = fActual!!.length - 1
        while (actualSuffix >= fPrefix && expectedSuffix >= fPrefix) {
            if (fExpected[expectedSuffix] != fActual[actualSuffix]) {
                break
            }
            actualSuffix--
            expectedSuffix--
        }
        fSuffix = fExpected.length - expectedSuffix
    }

    private fun computeCommonPrefix(): String {
        return if (fPrefix > fContextLength) ELLIPSIS else "" + fExpected!!.substring(Math.max(0, fPrefix - fContextLength), fPrefix)
    }

    private fun computeCommonSuffix(): String {
        val end = Math.min(fExpected!!.length - fSuffix + 1 + fContextLength, fExpected.length)
        return fExpected.substring(fExpected.length - fSuffix + 1, end) + if (fExpected.length - fSuffix + 1 < fExpected.length - fContextLength) ELLIPSIS else ""
    }

    private fun areStringsEqual(): Boolean {
        return fExpected == fActual
    }

    companion object {

        private val ELLIPSIS = "..."
        private val DELTA_END = "]"
        private val DELTA_START = "["
    }
}

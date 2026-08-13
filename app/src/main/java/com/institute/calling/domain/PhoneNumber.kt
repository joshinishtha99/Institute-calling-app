package com.institute.calling.domain

/**
 * Indian mobile number validation.
 *
 * Rules: a valid Indian mobile is 10 digits starting 6–9. We accept common ways
 * people write it — spaces, dashes, a leading +91 / 91 / 0 — and normalise to the
 * bare 10 digits. Anything else is rejected.
 */
object PhoneNumber {

    /** Returns the normalised 10-digit number, or null if the input isn't a valid Indian mobile. */
    fun normalizeIndianMobile(raw: String): String? {
        var digits = raw.filter { it.isDigit() }

        // Strip country/trunk prefixes.
        digits = when {
            digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
            digits.length == 11 && digits.startsWith("0") -> digits.substring(1)
            else -> digits
        }

        val validFirst = digits.firstOrNull()?.let { it in '6'..'9' } ?: false
        return if (digits.length == 10 && validFirst) digits else null
    }

    fun isValid(raw: String): Boolean = normalizeIndianMobile(raw) != null

    /** For display: "+91 98765 43210". Falls back to the raw string if not normalisable. */
    fun formatForDisplay(raw: String): String {
        val n = normalizeIndianMobile(raw) ?: return raw
        return "+91 ${n.substring(0, 5)} ${n.substring(5)}"
    }
}

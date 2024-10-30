package me.tomasan7.plenr.security

class PasswordValidator(
    private val lowerCaseLetter: Boolean,
    private val upperCaseLetter: Boolean,
    private val number: Boolean,
    private val specialSymbol: Boolean,
    private val minLength: Int,
)
{
    /**
     * Checks if the password meets the requirements.
     * @return A set of rules that the password does not meet, or an empty set if the password is valid.
     */
    fun validate(password: String): Set<Rule>
    {
        val failedRules = mutableSetOf<Rule>()

        if (lowerCaseLetter && !password.any { it.isLowerCase() })
            failedRules.add(Rule.LOWERCASE_LETTER)
        if (upperCaseLetter && !password.any { it.isUpperCase() })
            failedRules.add(Rule.UPPERCASE_LETTER)
        if (number && !password.any { it.isDigit() })
            failedRules.add(Rule.NUMBER)
        if (specialSymbol && !password.any { !it.isLetterOrDigit() })
            failedRules.add(Rule.SPECIAL_SYMBOL)
        if (password.length < minLength)
            failedRules.add(Rule.MIN_LENGTH)

        return failedRules
    }

    enum class Rule
    {
        LOWERCASE_LETTER,
        UPPERCASE_LETTER,
        NUMBER,
        SPECIAL_SYMBOL,
        MIN_LENGTH
    }
}
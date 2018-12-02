package pl.jjp.statsscraper.common

import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.UrlValidator
import pl.jjp.statsscraper.utils.StatusLogger
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.reflect.KProperty

internal class DataValidator(private val language: String) {
    private val groupingSeparator: String

    init {
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        this.groupingSeparator = symbols.groupingSeparator.toString()
    }

    private fun validateNumber(field: KProperty<String>) {
        val value = field.getter.call()
        val actual = Objects.requireNonNull<String>(value).replace(groupingSeparator, "")

        if (!StringUtils.isNumeric(actual.replace(groupingSeparator, ""))) {
            StatusLogger.logErrorFor(language, "${field.name} is not a number.  - $actual")
        }
    }

    fun validateNumber(field: KProperty<String>, min: Int, max: Int) {
        val value = field.getter.call()
        val actual = Objects.requireNonNull<String>(value).replace(groupingSeparator, "")

        validateNumber(field)

        val actualNumber = Integer.parseInt(actual)
        if (actualNumber < min || actualNumber > max) {
            StatusLogger.logErrorFor(language, "${field.name} is not in range $min-$max - $actual")
        }
    }

    fun validateNumber(field: KProperty<String>, min: Int) {
        val value = field.getter.call()
        val actual = value.replace(groupingSeparator, "")

        validateNumber(field)

        val actualNumber = Integer.parseInt(actual)
        if (actualNumber < min) {
            StatusLogger.logErrorFor(language, "${field.name} is not grater or equal than $min - $actual")
        }
    }

    fun validateNotBlank(field: KProperty<String>) {

        val actual = field.getter.call()
        if (!StringUtils.isNotBlank(actual)) {
            StatusLogger.logErrorFor(language, "${field.name} is empty.")
        }
    }

    fun validateUrl(field: KProperty<String>) {
        val actual = field.getter.call()
        if (!UrlValidator.getInstance().isValid(actual)) {
            StatusLogger.logErrorFor(language, "${field.name} is not valid URL. - $actual")
        }
    }
}

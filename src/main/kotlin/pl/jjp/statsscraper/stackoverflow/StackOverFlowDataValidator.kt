package pl.jjp.statsscraper.stackoverflow

import pl.jjp.statsscraper.common.DataValidator
import pl.jjp.statsscraper.utils.StatusLogger

object StackOverFlowDataValidator {

    fun validate(language: String, languageData: StackOverflowData) {

        try {
            val validator = DataValidator(language)
            validator.validateNumber(languageData::questions, 10000)

            StatusLogger.logSuccessFor(language)
        } catch (e: Exception) {
            StatusLogger.logException(language, e)
        }

    }
}
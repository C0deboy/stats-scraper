package pl.jjp.statsscraper.tiobeindex

import pl.jjp.statsscraper.common.DataValidator
import pl.jjp.statsscraper.utils.StatusLogger

object TiobeIndexDataValidator {

    fun validate(language: String, languageData: TiobeIndexData) {

        try {
            val validator = DataValidator(language)
            validator.validateNumber(languageData::currentPosition, 1, 25)
            validator.validateNumber(languageData::lastYearPosition, 1, 25)

            StatusLogger.logSuccessFor(language)
        } catch (e: Exception) {
            StatusLogger.logException(language, e)
        }

    }
}
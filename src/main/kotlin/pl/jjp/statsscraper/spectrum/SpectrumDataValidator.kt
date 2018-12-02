package pl.jjp.statsscraper.spectrum

import pl.jjp.statsscraper.common.DataValidator
import pl.jjp.statsscraper.utils.StatusLogger

object SpectrumDataValidator {

    fun validate(language: String, languageData: SpectrumData) {

        try {
            val validator = DataValidator(language)
            validator.validateNumber(languageData::currentPosition, 1, 20)
            validator.validateNumber(languageData::lastYearPosition, 1, 20)

            StatusLogger.logSuccessFor(language)
        } catch (e: Exception) {
            StatusLogger.logException(language, e)
        }

    }
}
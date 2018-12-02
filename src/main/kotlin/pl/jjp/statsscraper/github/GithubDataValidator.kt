package pl.jjp.statsscraper.github

import pl.jjp.statsscraper.common.DataValidator
import pl.jjp.statsscraper.utils.StatusLogger

object GithubDataValidator {

    fun validate(language: String, languageData: GithubData) {
        try {
            validateTop10Projects(language, languageData)

            val validator = DataValidator(language)
            validator.validateNumber(languageData::projects, 30000)
            validator.validateNumber(languageData::moreThen1000Stars, 15)

            StatusLogger.logSuccessFor(language)
        } catch (e: Exception) {
            StatusLogger.logException(language, e)
        }

    }

    private fun validateTop10Projects(language: String, languageData: GithubData) {

        for (projectData in languageData.top10) {
            val validator = DataValidator(language)

            validator.validateUrl(projectData::url)
            validator.validateNotBlank(projectData::name)
            validator.validateNumber(projectData::stars, 1500)
        }

        if (languageData.top10.size != 10) {
            StatusLogger.logErrorFor(language, "Top10 data does not contain 10 projects")
        }
    }
}
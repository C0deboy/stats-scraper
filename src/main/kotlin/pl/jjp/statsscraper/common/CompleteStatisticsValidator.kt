package pl.jjp.statsscraper.common

import com.beust.klaxon.JsonObject
import pl.jjp.statsscraper.github.GithubData
import pl.jjp.statsscraper.github.GithubDataScraper
import pl.jjp.statsscraper.github.GithubDataValidator
import pl.jjp.statsscraper.meetup.MeetupData
import pl.jjp.statsscraper.meetup.MeetupDataScraper
import pl.jjp.statsscraper.meetup.MeetupDataValidator
import pl.jjp.statsscraper.spectrum.SpectrumData
import pl.jjp.statsscraper.spectrum.SpectrumDataScraper
import pl.jjp.statsscraper.spectrum.SpectrumDataValidator
import pl.jjp.statsscraper.stackoverflow.StackOverFlowDataValidator
import pl.jjp.statsscraper.stackoverflow.StackOverflowData
import pl.jjp.statsscraper.stackoverflow.StackOverflowDataScraper
import pl.jjp.statsscraper.tiobeindex.TiobeIndexData
import pl.jjp.statsscraper.tiobeindex.TiobeIndexDataScraper
import pl.jjp.statsscraper.tiobeindex.TiobeIndexDataValidator
import pl.jjp.statsscraper.utils.StatusLogger

object CompleteStatisticsValidator {

    fun validate(mergedStatistics: JsonObject, languages: List<String>, scrapers: Set<DataScraper>) {
        if (mergedStatistics.size != languages.size + 1) {
            StatusLogger.logError("Missing language or data.")
        }
        for (language in mergedStatistics.keys) {

            if (language == "date") {
                continue
            }

            if (!languages.contains(language.replace("pp", "++"))) {
                StatusLogger.logErrorFor(language, "$language is not in given languages.")
            }

            val languageData = mergedStatistics[language] as JsonObject

            if (languageData.size != scrapers.size) {
                StatusLogger.logErrorFor(language, "$language doesn't have complete data.")
            }

            val validator = DataValidator(language)

            try {

                val maxRank = languages.size;

                when {
                    scrapers.any { it is GithubDataScraper } -> {
                        val githubData = languageData[GithubDataScraper.NAME] as GithubData

                        validator.validateNumber(githubData::ranking, 1, maxRank)

                        GithubDataValidator.validate(language, githubData)
                    }

                    scrapers.any { it is StackOverflowDataScraper } -> {
                        val stackOverflowData = languageData[StackOverflowDataScraper.NAME] as StackOverflowData

                        validator.validateNumber(stackOverflowData::ranking, 1, maxRank)

                        StackOverFlowDataValidator.validate(language, stackOverflowData)
                    }

                    scrapers.any { it is MeetupDataScraper } && !MeetupDataScraper.excluded.contains(language) -> {
                        val meetupData = languageData[MeetupDataScraper.NAME] as MeetupData

                        validator.validateNumber(meetupData.local::ranking, 1, maxRank)
                        validator.validateNumber(meetupData.global::ranking, 1, maxRank)

                        MeetupDataValidator.validate(language, meetupData);
                    }

                    scrapers.any { it is SpectrumDataScraper } -> {
                        val spectrumData = languageData[MeetupDataScraper.NAME] as SpectrumData

                        SpectrumDataValidator.validate(language, spectrumData);
                    }

                    scrapers.any { it is TiobeIndexDataScraper } -> {
                        val tiobeIndexData = languageData[MeetupDataScraper.NAME] as TiobeIndexData

                        TiobeIndexDataValidator.validate(language, tiobeIndexData);
                    }
                }

            } catch (e: Exception) {
                StatusLogger.logException("Not complete data.", e)
            }
        }

        StatusLogger.gap()
        StatusLogger.logSuccessFor("Complete validation")
        StatusLogger.gap()
    }
}

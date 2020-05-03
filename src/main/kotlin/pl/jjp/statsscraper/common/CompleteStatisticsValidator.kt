package pl.jjp.statsscraper.common

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.beust.klaxon.JsonObject
import org.slf4j.LoggerFactory
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
        if (mergedStatistics.size != languages.size + 1 && scrapers.size == 4) {
            StatusLogger.logError("Missing language or data.")
        }

        val lc = LoggerFactory.getILoggerFactory() as LoggerContext
        val logger = lc.getLogger(StatusLogger.LOG.name)
        logger.level = Level.WARN

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

                val maxRank = languages.size

                scrapers.forEach {
                    when (it) {

                        is GithubDataScraper -> {
                            if (it.onlyForLanguages.contains(language)) {
                                return;
                            }
                            val githubData = languageData[GithubDataScraper.name] as GithubData

                            validator.validateNumber(githubData::ranking, 1, maxRank)

                            GithubDataValidator.validate(language, githubData)
                        }

                        is StackOverflowDataScraper -> {
                            val stackOverflowData = languageData[StackOverflowDataScraper.name] as StackOverflowData

                            validator.validateNumber(stackOverflowData::ranking, 1, maxRank)

                            StackOverFlowDataValidator.validate(language, stackOverflowData)
                        }

                        is MeetupDataScraper -> {
                            val meetupData = languageData[MeetupDataScraper.name] as MeetupData

                            validator.validateNumber(meetupData.local::ranking, 1, maxRank)
                            validator.validateNumber(meetupData.global::ranking, 1, maxRank)

                            MeetupDataValidator.validate(language, meetupData)
                        }

                        is SpectrumDataScraper -> {
                            val spectrumData = languageData[SpectrumDataScraper.name] as SpectrumData

                            SpectrumDataValidator.validate(language, spectrumData)
                        }

                        is TiobeIndexDataScraper -> {
                            val tiobeIndexData = languageData[TiobeIndexDataScraper.name] as TiobeIndexData

                            TiobeIndexDataValidator.validate(language, tiobeIndexData)
                        }
                    }
                }

            } catch (e: Exception) {
                StatusLogger.logException("Not complete data.", e)
            }
        }

        StatusLogger.gap()
        StatusLogger.logSuccessFor("Complete validation")
        StatusLogger.gap()

        logger.level = Level.INFO
    }
}

package pl.jjp.statsscraper

import com.beust.klaxon.Klaxon
import org.apache.commons.lang3.ArrayUtils
import org.fusesource.jansi.AnsiConsole
import pl.jjp.statsscraper.common.CompleteStatisticsValidator
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.github.GithubDataScraper
import pl.jjp.statsscraper.meetup.MeetupDataScraper
import pl.jjp.statsscraper.spectrum.SpectrumDataScraper
import pl.jjp.statsscraper.stackoverflow.StackOverflowDataScraper
import pl.jjp.statsscraper.tiobeindex.TiobeIndexData
import pl.jjp.statsscraper.tiobeindex.TiobeIndexDataScraper
import pl.jjp.statsscraper.utils.FilePersister
import pl.jjp.statsscraper.utils.StatisticsBuilder
import pl.jjp.statsscraper.utils.StatusLogger
import pl.jjp.statsscraper.versions.LanguageVersionDataScraper
import java.util.HashSet
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

private val languages =
    listOf("C", "C++", "Java", "JavaScript", "Python", "Swift", "R", "Csharp", "Ruby", "PHP")

private val scrapers = HashSet<DataScraper>()

fun main(args: Array<String>) {

    enableAnsiColors(args)

    scrapers.add(TiobeIndexDataScraper(languages))
    scrapers.add(StackOverflowDataScraper(languages))
    scrapers.add(SpectrumDataScraper(languages))
    scrapers.add(GithubDataScraper(languages))
    scrapers.add(MeetupDataScraper(languages))

    val elapsedTime = measureNanoTime {
        val statisticsBuilder = StatisticsBuilder(languages, scrapers)

        val completeStatistics = statisticsBuilder.buildStatisticsForEachLanguage()
        CompleteStatisticsValidator.validate(completeStatistics, languages, scrapers)

        FilePersister.saveStatisticsAndKeepOld(Klaxon().toJsonString(completeStatistics), "statistics.json")

        val languagesVersions = LanguageVersionDataScraper(languages).scrapData()

        FilePersister.saveToFile(Klaxon().toJsonString(languagesVersions), "languagesVersions.json")
    }


    val elapsedTimeInSeconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS)
    StatusLogger.logInfo("Done in $elapsedTimeInSeconds seconds.")
}

private fun enableAnsiColors(args: Array<String>) {
    if (!ArrayUtils.contains(args, "--no-jansi")) {
        AnsiConsole.systemInstall()
    }
}
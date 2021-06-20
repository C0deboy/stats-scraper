package pl.jjp.statsscraper

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.apache.commons.lang3.ArrayUtils
import org.fusesource.jansi.AnsiConsole
import pl.jjp.statsscraper.common.CompleteStatisticsValidator
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.github.GithubDataScraper
import pl.jjp.statsscraper.meetup.MeetupDataScraper
import pl.jjp.statsscraper.spectrum.SpectrumDataScraper
import pl.jjp.statsscraper.stackoverflow.StackOverflowDataScraper
import pl.jjp.statsscraper.tiobeindex.TiobeIndexDataScraper
import pl.jjp.statsscraper.utils.FilePersister
import pl.jjp.statsscraper.utils.StatisticsBuilder
import pl.jjp.statsscraper.utils.StatusLogger
import pl.jjp.statsscraper.versions.LanguageVersionDataScraper
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

private val languages = listOf(
    "C", "C++", "Java", "JavaScript", "Python", "Swift", "R", "Csharp", "Ruby", "PHP",
    "Kotlin", "Scala", "Go", "Groovy", "Rust"
)

private val scrapers = HashSet<DataScraper>()

fun main(args: Array<String>) {

    enableAnsiColors(args)

    if (args.contains("--tiobe")) {
        scrapers.add(TiobeIndexDataScraper)
    }
    if (args.contains("--stack")) {
        scrapers.add(StackOverflowDataScraper)
    }
    if (args.contains("--spectrum")) {
        scrapers.add(SpectrumDataScraper)
    }
    if (args.contains("--github")) {
        scrapers.add(GithubDataScraper)
    }
    if (args.contains("--meetup")) {
        scrapers.add(MeetupDataScraper)
    }

    val elapsedTime = measureNanoTime {
        if(scrapers.isNotEmpty()) {

            val completeStatistics = scrapAndValidateStatistics()

            FilePersister.saveStatisticsAndKeepOld(Klaxon().toJsonString(completeStatistics), "statistics.json")
        }

        if (args.contains("--versions")) {
            val languagesVersions = LanguageVersionDataScraper.scrapData(languages)

            FilePersister.saveToFile(Klaxon().toJsonString(languagesVersions), "languagesVersions.json")
        }
    }

    val elapsedTimeInSeconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS)
    StatusLogger.logInfo("Done in $elapsedTimeInSeconds seconds.")
}

private fun scrapAndValidateStatistics(): JsonObject {

    val completeStatistics = StatisticsBuilder(scrapers).buildStatisticsForEachLanguage(languages)

    try {
        CompleteStatisticsValidator.validate(completeStatistics, languages, scrapers)
    } catch (e: Exception) {
        StatusLogger.logException("Error: ${e.message}", e)
    }

    return completeStatistics
}

private fun enableAnsiColors(args: Array<String>) {
    if (!ArrayUtils.contains(args, "--no-jansi")) {
        AnsiConsole.systemInstall()
    }
}
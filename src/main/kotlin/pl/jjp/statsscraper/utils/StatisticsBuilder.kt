package pl.jjp.statsscraper.utils

import com.beust.klaxon.JsonObject
import pl.jjp.statsscraper.common.Data
import pl.jjp.statsscraper.common.DataScraper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class StatisticsBuilder(private val languages: List<String>, private val scrapers: Set<DataScraper>) {
    private val statsForEachLanguage = JsonObject()

    fun buildStatisticsForEachLanguage(): JsonObject {
        appendDateToStatistics()

        val statisticsSet = HashSet<HashMap<String, Map<String, Data>>>()

        for (scraper in scrapers) {

            val statistics = HashMap<String, Map<String, Data>>()
            statistics[scraper.name] = scraper.scrapData()

            statisticsSet.add(statistics)
        }

        for (language in languages) {

            val languageData = JsonObject()

            for (statistics in statisticsSet) {

                statistics.entries.forEach{ entry ->
                    languageData[entry.key] = entry.value[language]
                    statsForEachLanguage[language.replace("++", "pp")] = languageData
                }
            }
        }

        return statsForEachLanguage
    }

    private fun appendDateToStatistics() {
        val localDate = LocalDate.now()
        statsForEachLanguage["date"] = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}



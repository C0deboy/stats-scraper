package scrapers

import com.beust.klaxon.Klaxon
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pl.jjp.statsscraper.common.Data
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.utils.FilePersister
import pl.jjp.statsscraper.utils.StatusLogger
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.TimeUnit

val languages = listOf("C", "C++", "Java", "JavaScript", "Python", "Swift", "R", "Csharp", "Ruby", "PHP")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal abstract class BaseScraperTest(var scraper: DataScraper) {
    lateinit var groupingSeparator: String
    val scraperData: Map<String, Data> = scraper.scrapData()

    @BeforeAll
    fun init() {
        val startTime = System.nanoTime()


        FilePersister.saveToFile(Klaxon().toJsonString(scraperData), "src/test/statistics/${scraper.name}.json")

        val symbols = DecimalFormatSymbols(Locale.getDefault())
        groupingSeparator = symbols.groupingSeparator.toString()

        val elapsedTime = TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
        StatusLogger.logInfo("Done in $elapsedTime seconds.")
    }

    @Test
    fun scrapedDataSizeIsEqualToNumberOfLanguages() {
        assertThat(scraperData).hasSize(languages.size)
    }

    @Test
    fun scrapedDataHasAllLanguages() {
        assertThat(scraperData.keys).allSatisfy { langauge -> languages.contains(langauge) }
    }

    fun shouldBeNumeric(actual: String) {
        assertThat(actual).satisfies { StringUtils.isNumeric(it) }
    }

    protected fun cleanNumber(number: String): String {
        return number.replace(groupingSeparator, "")
    }
}

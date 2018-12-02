package scrapers

import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pl.jjp.statsscraper.spectrum.SpectrumData
import pl.jjp.statsscraper.spectrum.SpectrumDataScraper

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SpectrumDataScraperTest :
    BaseScraperTest(SpectrumDataScraper(languages)) {

    @Test
    fun currentPositionShouldBeValidNumber() {
        assertThat(scraperData).allSatisfy { language, languageStats ->
            val stats = languageStats as SpectrumData

            val currentPosition = stats.currentPosition

            assertThat(currentPosition).`as`("$language currentPosition")
                .satisfies { StringUtils.isNumeric(it) }

            assertThat(Integer.parseInt(currentPosition)).`as`("$language currentPosition").isBetween(1, 20)
        }
    }

    @Test
    fun lastYearPositionShouldBeValidNumber() {
        scraperData.forEach { language, languageStats ->
            val stats = languageStats as SpectrumData

            val lastYearPosition = stats.lastYearPosition

            assertThat(lastYearPosition).`as`("$language lastYearPosition")
                .satisfies { StringUtils.isNumeric(it) }

            assertThat(Integer.parseInt(lastYearPosition)).`as`("$language lastYearPosition").isBetween(1, 20)
        }
    }

}
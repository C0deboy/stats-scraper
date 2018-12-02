package scrapers

import io.mockk.every
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.stackoverflow.StackOverflowData
import pl.jjp.statsscraper.stackoverflow.StackOverflowDataScraper
import java.io.File

internal class StackOverflowDataScraperTest : BaseScraperTest(mockedStackOverflowDataScraper()) {

    @Test
    fun getData() {

        for (languageStats in scraperData.values) {
            val stackOverflowData = languageStats as StackOverflowData

            val questionCount = cleanNumber(stackOverflowData.questions)
            val ranking = cleanNumber(stackOverflowData.ranking)

            shouldBeNumeric(questionCount)
            shouldBeNumeric(ranking)
            assertThat(Integer.parseInt(ranking)).isBetween(1, 20)
        }
    }
}

fun mockedStackOverflowDataScraper(): DataScraper {

    return spyk(StackOverflowDataScraper(languages)) {
        every { fetchData(any())} answers  { call ->
            val language = call.invocation.args.first() as String
            val file = File("src/test/resources/stackOverflow/" + language + "_data.json")
            Jsoup.parse(file, "UTF-8").text()
        }
    }
}


package scrapers

import com.beust.klaxon.Klaxon
import io.mockk.every
import io.mockk.spyk
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.UrlValidator
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.github.GithubData
import pl.jjp.statsscraper.github.GithubDataScraper
import java.io.File
import java.io.StringReader


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class GithubDataScraperTest : BaseScraperTest(mockedGithubDataScraper()) {

    @Test
    fun validGithubCountsData() {

        assertThat(scraperData).allSatisfy { language, languageStats ->
            val stats = languageStats as GithubData
            val projectsCount = stats.projects.replace(groupingSeparator, "")
            val moreThan1000StarsCount = cleanNumber(stats.moreThen1000Stars)
            val ranking = stats.ranking

            assertThat(projectsCount).satisfies { StringUtils.isNumeric(it) }
            assertThat(moreThan1000StarsCount).satisfies { StringUtils.isNumeric(it) }
            assertThat(ranking).satisfies { StringUtils.isNumeric(it) }
            assertThat(Integer.parseInt(ranking)).`as`("$language ranking").isBetween(1, 20)
        }
    }

    @Test
    fun validTop10Data() {

        scraperData.values.stream()
            .map { v -> (v as GithubData).top10 }
            .forEach { top10 ->

                for (githubProject in top10) {
                    assertThat(githubProject.url).satisfies { UrlValidator.getInstance().isValid(it) }
                    assertThat(githubProject.name).satisfies { StringUtils.isNotBlank(it) }
                    assertThat(githubProject.stars).satisfies { StringUtils.isNumeric(it) }
                }

                assertThat(top10).hasSize(10)
            }
    }
}

private fun mockedGithubDataScraper(): DataScraper {

    return spyk(GithubDataScraper(languages)) {
        every { fetchLanguageStats(any())} answers  { call ->
            val language = call.invocation.args.first() as String
            val file = File("src/test/resources/github/" + language + "_data.json")
            val doc = Jsoup.parse(file, "UTF-8").text()
            Klaxon().parseJsonObject(StringReader(doc))
        }

        every { fetchMoreThan1000StarsData(any())} answers { call ->
            val language = call.invocation.args.first() as String
            val file = File("src/test/resources/github/" + language + "_starsData.json")
            Jsoup.parse(file, "UTF-8").text()
        }
    }
}
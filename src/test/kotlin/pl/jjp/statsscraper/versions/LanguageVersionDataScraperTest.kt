package pl.jjp.statsscraper.versions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LanguageVersionDataScraperTest {

    private val versionsScraper = LanguageVersionDataScraper;

    @Test
    fun getDateFromReleaseInfo() {
        versionsScraper.currentLanguage = "TEST"
        val date = versionsScraper.getDateFromReleaseInfo("3.0.1[1] (5 April 2021; 2 days ago (5 April 2021)) [±]")
        assertThat(date).isEqualTo("5 kwietnia 2021")

    }
}
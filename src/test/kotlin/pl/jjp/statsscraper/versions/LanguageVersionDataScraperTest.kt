package pl.jjp.statsscraper.versions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class LanguageVersionDataScraperTest {

    private val versionsScraper = LanguageVersionDataScraper

    @ParameterizedTest
    @CsvSource(value = [
            "8.2.1 / 2023-01-05[3]=5 stycznia 2023",
            "3.0.1[1] (5 April 2021; 2 days ago (5 April 2021)) [±]=5 kwietnia 2021",
            "ECMAScript 2021[2] / June 2021; 20 months ago (June 2021)=w czerwcu 2021",
            "Java SE 19[2] / 20 September 2022; 4 months ago (20 September 2022)=20 września 2022",
            "C++20 (ISO/IEC 14882:2020) / 15 December 2020; 2 years ago (2020-12-15)=15 grudnia 2020",
            "1.67.0[1] / January 26, 2023; 11 days ago (January 26, 2023)=26 stycznia 2023",
            "C17 / June 2018; 4 years ago (2018-06)=w czerwcu 2018",
            "2.13.10[1] / 13 October 2022; 3 months ago (13 October 2022)=13 października 2022",
            "1.20[4] / 1 February 2023; 3 days ago (1 February 2023)=1 lutego 2023",
            "4.0.5 (September 6, 2022; 4 months ago (2022-09-06)[1]) [±]=6 września 2022",
            "1.8.0[1] / 28 December 2022; 38 days ago (28 December 2022)=28 grudnia 2022",
            "4.2.2[2] / 31 October 2022; 3 months ago (31 October 2022)=31 października 2022",
            "5.7.3[3] / 19 January 2023; 17 days ago (19 January 2023)=19 stycznia 2023",
            "3.2.0 [1] / 25 December 2022; 42 days ago (25 December 2022)=25 grudnia 2022",
            "11[2] / 8 November 2022; 2 months ago (8 November 2022)=8 listopada 2022",
            "3.11.1[3] / 6 December 2022; 1 month ago (6 December 2022)=6 grudnia 2022"
        ], delimiter = '='
    )
    fun getDateFromReleaseInfo(input: String, expected: String) {
        versionsScraper.currentLanguage = "TEST"
        val date = versionsScraper.getDateFromReleaseInfo(input)
        assertThat(date).isEqualTo(expected)
    }
}
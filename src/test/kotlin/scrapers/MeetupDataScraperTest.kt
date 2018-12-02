package scrapers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pl.jjp.statsscraper.meetup.MeetupData
import pl.jjp.statsscraper.meetup.MeetupDataScraper
import pl.jjp.statsscraper.meetup.Ranking

internal class MeetupDataScraperTest : BaseScraperTest(MeetupDataScraper(languages)) {

    @Test
    fun getData() {

        for (stats in scraperData.entries) {
            if (MeetupDataScraper.excluded.contains(stats.key)) {
                continue
            }

            val languageData = stats.value as MeetupData

            validateRanking(languageData.local)
            validateRanking(languageData.global)
        }
    }

    private fun validateRanking(localRanking: Ranking) {
        val localMeetupsCount = cleanNumber(localRanking.meetups)
        val localMembersCount = cleanNumber(localRanking.meetups)
        val localPosition = localRanking.ranking

        assertThat(localRanking).isNotNull
        shouldBeNumeric(localMeetupsCount)
        shouldBeNumeric(localMembersCount)
        shouldBeNumeric(localPosition)
    }
}
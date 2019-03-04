package pl.jjp.statsscraper.spectrum

import org.jsoup.Jsoup
import pl.jjp.statsscraper.common.Data
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.utils.StatusLogger
import pl.jjp.statsscraper.utils.escapeLanguage

private const val LAST_YEAR_RANKING_FILE = "/spectrumRanking2017.html"
private const val CURRENT_RANKING_FILE = "/spectrumRanking2018.html"
private const val RANK_DATA_LANGUAGE = ".language"

class SpectrumDataScraper(private val languages: List<String>) : DataScraper {

    companion object {
        const val NAME = "SpectrumRanking"
    }

    override val name get() = NAME

    val data = HashMap<String, SpectrumData>()

    override fun scrapData(): Map<String, Data> {
        StatusLogger.logCollecting("Spectrum ranking data")

        var language = "NONE"
        try {

            val currentSpectrumRanking = getSpectrumRanking(CURRENT_RANKING_FILE)
            val lastYearSpectrumRanking = getSpectrumRanking(LAST_YEAR_RANKING_FILE)


            currentSpectrumRanking.forEachIndexed { index, lang ->
                language = lang

                if (languages.contains(language)) {

                    val currentPosition = index + 1
                    val lastYearPosition = lastYearSpectrumRanking.indexOf(language) + 1

                    val languageData = SpectrumData(
                        currentPosition = currentPosition.toString(),
                        lastYearPosition = lastYearPosition.toString()
                    )

                    SpectrumDataValidator.validate(language, languageData)
                    data[language] = languageData
                }
            }

            data["Kotlin"] = SpectrumData("N/A", "N/A");
            data["Groovy"] = SpectrumData("N/A", "N/A");

        } catch (e: Exception) {
            StatusLogger.logException(language, e)
        }

        return data

    }

    private fun getSpectrumRanking(rankingDataPath: String): MutableList<String> {
        val spectrumRankingFile = this.javaClass.getResourceAsStream(rankingDataPath)
        val doc = Jsoup.parse(spectrumRankingFile, "UTF-8", "")
        val ranking = doc.select(RANK_DATA_LANGUAGE).eachText()
        ranking.replaceAll(::escapeLanguage)
        return ranking
    }
}

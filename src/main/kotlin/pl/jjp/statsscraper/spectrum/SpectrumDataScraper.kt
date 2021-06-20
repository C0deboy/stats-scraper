package pl.jjp.statsscraper.spectrum

import org.jsoup.Jsoup
import pl.jjp.statsscraper.common.Data
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.utils.StatusLogger
import pl.jjp.statsscraper.utils.escapeLanguage

private const val LAST_YEAR_RANKING_FILE = "/spectrumRanking2019.html"
private const val CURRENT_RANKING_FILE = "/spectrumRanking2020.html"
private const val RANK_DATA_LANGUAGE = ".language"

object SpectrumDataScraper : DataScraper {

    override val name = "SpectrumRanking"
    val data = HashMap<String, SpectrumData>()

    override fun scrapData(languages: List<String>): Map<String, Data> {
        StatusLogger.logCollecting("Spectrum ranking data")

        var language = "NONE"
        try {
            val currentSpectrumRanking = getSpectrumRanking(CURRENT_RANKING_FILE)
            val lastYearSpectrumRanking = getSpectrumRanking(LAST_YEAR_RANKING_FILE)

            languages.forEachIndexed { index, lang ->
                language = lang

                if (currentSpectrumRanking.find { it.contains(language) } != null) {

                    val currentPosition = index + 1
                    val find = lastYearSpectrumRanking.find { it.contains(language) }
                    val lastYearPosition = if (find != null) lastYearSpectrumRanking.indexOf(find) + 1 else 0

                    val languageData = SpectrumData(
                        currentPosition = currentPosition.toString(),
                        lastYearPosition = if (lastYearPosition > 0) lastYearPosition.toString() else "N/A"
                    )

                    SpectrumDataValidator.validate(language, languageData)
                    data[language] = languageData
                }
            }

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

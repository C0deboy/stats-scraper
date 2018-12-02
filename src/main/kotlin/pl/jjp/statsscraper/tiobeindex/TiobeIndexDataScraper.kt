package pl.jjp.statsscraper.tiobeindex

import org.jsoup.Jsoup
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.utils.StatusLogger

private const val URL = "https://www.tiobe.com/tiobe-index/"
private const val TABLE_TOP20_ROWS = ".table-top20 tbody tr"
private const val LAST_YEAR_POSITION_TD = "td:nth-child(2)"
private const val CURRENT_YEAR_POSITION_TD = "td:nth-child(1)"
private const val LANGUAGE_TD = "td:nth-child(4)"

class TiobeIndexDataScraper(private val languages: List<String>) : DataScraper {

    companion object {
        const val NAME = "TiobeIndex"
    }

    override val name get() = NAME

    val data = HashMap<String, TiobeIndexData>()

    override fun scrapData(): HashMap<String, TiobeIndexData> {
        StatusLogger.logCollecting("Tiobe index data")

        var language = "NONE"

        try {
            val doc = Jsoup.connect(URL).get()

            val possibleRows = doc.select(TABLE_TOP20_ROWS)

            for (row in possibleRows) {
                language = row.select(LANGUAGE_TD).text().replace("#", "sharp")

                if (languages.contains(language)) {

                    val currentYearPosition = Integer.parseInt(row.select(CURRENT_YEAR_POSITION_TD).text())
                    val lastYearPosition = Integer.parseInt(row.select(LAST_YEAR_POSITION_TD).text())
                    val languageData = TiobeIndexData(
                        currentPosition = currentYearPosition.toString(),
                        lastYearPosition = lastYearPosition.toString()
                    )

                    TiobeIndexDataValidator.validate(language, languageData)

                    data[language] = languageData
                }
            }

        } catch (e: Exception) {
            StatusLogger.logException(language, e)
        }

        return data
    }
}

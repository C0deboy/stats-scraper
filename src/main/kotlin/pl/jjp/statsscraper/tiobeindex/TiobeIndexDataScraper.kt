package pl.jjp.statsscraper.tiobeindex

import org.jsoup.Jsoup
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.utils.StatusLogger

private const val URL = "https://www.tiobe.com/tiobe-index/"
private const val TABLE_TOP20_ROWS = ".table"
private const val LAST_YEAR_POSITION_TD = "td:nth-child(2)"
private const val CURRENT_YEAR_POSITION_TD = "td:nth-child(1)"
private const val LANGUAGE_TD = "td:nth-child(5)"
private const val REMAINING_LANGUAGE_TD = "td:nth-child(2)"

object TiobeIndexDataScraper : DataScraper {

    override val name = "TiobeIndex"
    val data = HashMap<String, TiobeIndexData>()

    override fun scrapData(languages: List<String>): HashMap<String, TiobeIndexData> {
        StatusLogger.logCollecting("Tiobe index data")

        var language = "NONE"

        try {
            val doc = Jsoup.connect(URL).get()
            val top20 = doc.select(TABLE_TOP20_ROWS)[0]
            val remainingTop = doc.select(TABLE_TOP20_ROWS)[1]

            val possibleRows = top20.select("tr")
            possibleRows.addAll(remainingTop.select("tr"))

            possibleRows.forEachIndexed { index, row ->
                language = row.select(LANGUAGE_TD).text().replace("#", "sharp")

                if (index > 20) language = row.select(REMAINING_LANGUAGE_TD).text()

                if (languages.contains(language)) {

                    val currentYearPosition = Integer.parseInt(row.select(CURRENT_YEAR_POSITION_TD).text())

                    val lastYearPosition: String = if (index > 20) "N/A"
                    else Integer.parseInt(row.select(LAST_YEAR_POSITION_TD).text()).toString()

                    val languageData = TiobeIndexData(
                        currentPosition = currentYearPosition.toString(),
                        lastYearPosition = lastYearPosition
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

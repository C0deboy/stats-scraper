package pl.jjp.statsscraper.stackoverflow

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.jsoup.Jsoup
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.utils.StatusLogger
import java.io.StringReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap

private const val URL = "https://api.stackexchange.com/2.2/tags/{language}/info?site=stackoverflow"

object StackOverflowDataScraper : DataScraper {

    override val name = "StackOverFlow"

    private var data = ConcurrentHashMap<String, StackOverflowData>()
    private val rankingData = ConcurrentSkipListMap<Int, String>()

    override fun scrapData(languages: List<String>): ConcurrentHashMap<String, StackOverflowData> {
        StatusLogger.logCollecting("Stack OverFlow data")

        languages.stream().parallel()
            .forEach { lang -> data[lang] = scrap(lang) }

        var ranking: Int = rankingData.size
        for (language in rankingData.values) {
            data[language]!!.ranking = ranking--.toString()
        }

        return data
    }

    private fun scrap(language: String): StackOverflowData {

        lateinit var languageData: StackOverflowData

        try {
            val doc = fetchData(language)

            val data = Klaxon().parseJsonObject(StringReader(doc))
            val items = data.array<JsonObject>("items")!![0]
            val count = items.int("count")!!
            val questions = String.format("%,d", count)

            languageData = StackOverflowData(questions)

            rankingData[count] = language

            StackOverFlowDataValidator.validate(language, languageData)

        } catch (e: Exception) {
            StatusLogger.logException(language, e)
        }

        return languageData
    }

    private fun fetchData(language: String): String {
        val escapedLanguage = language.replace("+", "%2B")
        val url = URL.replace("{language}", escapedLanguage)
        return Jsoup.connect(url).ignoreContentType(true).execute().body()
    }
}


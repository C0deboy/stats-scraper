package pl.jjp.statsscraper.meetup

import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.utils.StatusLogger
import pl.jjp.statsscraper.utils.afterDelay
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.concurrent.getOrSet

object MeetupDataScraper : DataScraper {

    const val url = "https://www.meetup.com/pl-PL/topics/"
    override val name = "Meetup"
    val excluded = listOf("C++")

    private val data = ConcurrentHashMap<String, MeetupData>()
    private var localRankingData = ConcurrentSkipListMap<Int, String>()
    private var globalRankingData = ConcurrentSkipListMap<Int, String>()

    private val retries = ThreadLocal<Int>()
    private val customTopics = HashMap<String, String>()

    init {
        customTopics["R"] = "programming-in-r"
        customTopics["Swift"] = "swift-language"
        customTopics["Go"] = "go-programming-language"
    }

    override fun scrapData(languages: List<String>): Map<String, MeetupData> {
        StatusLogger.logCollecting("Meetup data")

        languages.stream().parallel()
            .filter(this::filterExcluded)
            .forEach { lang -> data[lang] = scrap(lang) }

        resolveRankings(localRankingData) { language -> data[language]!!.local }
        resolveRankings(globalRankingData) { language -> data[language]!!.global }

        data["C++"] = data["C"]!!//C and C++ are the same

        return data
    }

    private fun filterExcluded(language: String): Boolean {
        return if (excluded.contains(language)) {
            StatusLogger.logSkipped(language, "excluded")
            false
        } else {
            true
        }
    }

    private fun scrap(language: String): MeetupData {

        val topic = customTopics.getOrDefault(language, language)

        val localUrl = url + topic.toLowerCase()
        val globalUrl = "$localUrl/global/"

        lateinit var languageData: MeetupData

        try {

            val localData = getMeetupData(localUrl, language, localRankingData)
            val globalData = getMeetupData(globalUrl, language, globalRankingData)
            languageData = MeetupData(localData, globalData)


            MeetupDataValidator.validate(language, languageData)

        } catch (e: HttpStatusException) {

            retries.set(retries.getOrSet { 0 } + 1)

            if (retries.get() > 3) {
                StatusLogger.logErrorFor(language, "${e.message}. Failed (${retries.get()} times.")
            }
            val delay: Long = 50
            StatusLogger.logErrorFor(language, "${e.message}. Retrying (${retries.get()}) in ${delay}ms", false)

            return afterDelay(delay) {
                scrap(language)
            }

        } catch (e: Exception) {
            StatusLogger.logException(language, e)
        }


        return languageData
    }

    private fun resolveRankings(ranking: Map<Int, String>, meetupdata: (String) -> Ranking) {
        var position = ranking.size
        for (language in ranking.values) {
            meetupdata.invoke(language).ranking = position--.toString()
        }
    }

    private fun getMeetupData(url: String, language: String, rankingData: MutableMap<Int, String>): Ranking {
//        val process = ProcessBuilder("curl", "-s", url).start()
//        val doc = Jsoup.parse(process.inputStream, "UTF-8", "")
        val doc = Jsoup.connect(url).get()

        val meetups = String.format("%,d", getMeetupsCount(doc))
        val membersCount = getMembersCount(doc)
        val members = String.format("%,d", membersCount)

        rankingData[membersCount] = language

        return Ranking(meetups, members)
    }

    private fun getMeetupsCount(doc: Document): Int {
        val meetups = doc.select(".bounds").first().select(".text--bold:matches([\\d,])")[1]
        return scrapNumber(meetups)
    }

    private fun getMembersCount(doc: Document): Int {
        val members = doc.select(".bounds").first().select(".text--bold:matches([\\d,])")[0]
        return scrapNumber(members)
    }

    private fun scrapNumber(doc: Element): Int {
        val number = doc.text()
        return Integer.parseInt(number.replace("[\\s,]".toRegex(), ""))
    }
}

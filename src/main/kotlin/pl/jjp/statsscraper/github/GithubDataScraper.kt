package pl.jjp.statsscraper.github

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.utils.StatusLogger
import java.io.IOException
import java.io.StringReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap

private const val URL = "https://api.github.com/search/repositories?q=language:{language}+stars:>0&s=stars&per_page=10"
private const val STARS_URL = "https://api.github.com/search/repositories?q=language:{language}+stars:>1000&per_page=1"

class GithubDataScraper(private val languages: List<String>) : DataScraper {

    companion object {
        const val NAME = "Github"
    }

    override val name get() = NAME

    val data = ConcurrentHashMap<String, GithubData>()

    private val rankingData = ConcurrentSkipListMap<Int, String>()

    private var authToken: String? = null

    init {
        try {
            val properties = Properties()
            properties.load(this.javaClass.getResourceAsStream("/config.properties")!!)
            authToken = properties.getProperty("GithubAuthToken")
        } catch (e: IOException) {
            StatusLogger.logException("Cannot load properties", e)
        }

    }

    override fun scrapData(): Map<String, GithubData> {
        StatusLogger.logCollecting("Github data")

        if (authToken == null || StringUtils.isBlank(authToken)) {
            StatusLogger.appendWarning("No auth token provided. Github data won't be scrapped.")
            return data
        }

        languages.stream().parallel()
            .forEach { lang -> data[lang] = scrap(lang) }

        var ranking = rankingData.size
        for (language in rankingData.values) {
            data[language]!!.ranking = ranking--.toString()
        }

        return data
    }

    private fun scrap(language: String): GithubData {

        lateinit var languageData: GithubData

        try {
            val json = fetchLanguageStats(language)

            languageData = GithubData(
                moreThen1000Stars = scrapMoreThan1000Stars(language),
                projects = scrapProjectsTotalCount(json, language),
                top10 = scrapTop10Data(json)
            )

            GithubDataValidator.validate(language, languageData)

        } catch (e: Exception) {
            checkGithubApiLimits()
            StatusLogger.logException(language, e)
        }

        return languageData
    }

    fun fetchLanguageStats(language: String): JsonObject {
        val escapedLanguage = language.replace("+", "%2B")
        val url = URL.replace("{language}", escapedLanguage)

        val doc = Jsoup.connect(url).header("Authorization", authToken).ignoreContentType(true).execute().body()

        return Klaxon().parseJsonObject(StringReader(doc))
    }

    private fun scrapProjectsTotalCount(json: JsonObject, language: String): String {
        val count = json.int("total_count")!!
        rankingData[count] = language
        return String.format("%,d", count)
    }

    private fun scrapTop10Data(json: JsonObject): List<GithubProject> {

        val top10List = json.array<JsonObject>("items")

        val top10 = ArrayList<GithubProject>()

        for (projectJSON in top10List!!) {

            val projectName = projectJSON.string("name")
            val projectStars = String.format("%,d", projectJSON.int("stargazers_count"))
            val projectUrl = projectJSON.string("html_url")

            val projectData = GithubProject(
                name = projectName!!,
                stars = projectStars,
                url = projectUrl!!
            )

            top10.add(projectData)
        }

        return top10
    }

    private fun scrapMoreThan1000Stars(language: String): String {
        val doc = fetchMoreThan1000StarsData(language)
        val data = Klaxon().parseJsonObject(StringReader(doc))
        return String.format("%,d", data.int("total_count"))
    }

    fun fetchMoreThan1000StarsData(language: String): String {
        val escapedLanguage = language.replace("+", "%2B")
        val urlStars = STARS_URL.replace("{language}", escapedLanguage)
        return Jsoup.connect(urlStars).header("Authorization", authToken).ignoreContentType(true).execute().body()
    }

    private fun checkGithubApiLimits() {
        val url = "https://api.github.com/rate_limit"

        try {
            val doc = Jsoup.connect(url).header("Authorization", authToken).ignoreContentType(true).execute().body()
            StatusLogger.logInfo(doc)
        } catch (e: IOException) {
            StatusLogger.logException("Checking api limits", e)
        }

    }
}

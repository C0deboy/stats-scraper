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
import kotlin.streams.toList


private const val URL = "https://api.github.com/search/repositories?q=language:{language}+stars:>0&s=stars&per_page=20"
private const val STARS_URL = "https://api.github.com/search/repositories?q=language:{language}+stars:>1000&per_page=1"

object GithubDataScraper : DataScraper {


    override val name = "Github"

    val data = ConcurrentHashMap<String, GithubData>()
    private val rankingData = ConcurrentSkipListMap<Int, String>()
    private var authToken: String? = null
    var onlyForLanguages: List<String> = emptyList()
    private var projectsToIgnore: List<String> = emptyList()

    init {
        try {
            val properties = Properties()
            properties.load(this.javaClass.getResourceAsStream("/config.properties")!!)
            authToken = properties.getProperty("GithubAuthToken")

            val whitespace = "\\s+".toRegex()
            val githubIgnoreProjectsProperty = System.getProperty("githubIgnoreProjects")
            if (githubIgnoreProjectsProperty != null) {
                projectsToIgnore = githubIgnoreProjectsProperty.replace(whitespace,"").split(",")
            }
            val githubSkipLangsProperty = System.getProperty("githubOnlyLangs")
            if (githubSkipLangsProperty != null) {
                onlyForLanguages = githubSkipLangsProperty.replace(whitespace,"").split(",")
            }
        } catch (e: IOException) {
            StatusLogger.logException("Cannot load properties", e)
        }
    }

    override fun scrapData(languages: List<String>): Map<String, GithubData> {

        val filteredLanguages = if (onlyForLanguages.isEmpty()) languages else languages
            .stream()
            .filter { lang -> onlyForLanguages.contains(lang) }.toList()

        StatusLogger.logCollecting("Github data")

        if (authToken == null || StringUtils.isBlank(authToken)) {
            StatusLogger.appendWarning("No auth token provided. Github data won't be scrapped.")
            return data
        }

        filteredLanguages.stream()
            .forEach { lang -> data[lang] = scrap(lang, true) }

        var ranking = rankingData.size
        for (language in rankingData.values) {
            data[language]!!.ranking = ranking--.toString()
        }

        return data
    }

    private fun scrap(language: String, firstTry: Boolean): GithubData {

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
            if (firstTry) {
                StatusLogger.logInfo("Github API limit. Waiting 1 min")
                Thread.sleep(60000)//1min
                return scrap(language, false)
            } else {
                checkGithubApiLimits()
                StatusLogger.logException(language, e)
            }
        }

        return languageData
    }

    private fun fetchLanguageStats(language: String): JsonObject {
        val escapedLanguage = language.replace("+", "%2B")
        val url = URL.replace("{language}", escapedLanguage)

        val doc = getPageBodyAsString(url)

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

            if (top10.size == 10) {
                break
            }

            val projectName = projectJSON.string("name")

            if (projectsToIgnore.contains(projectName)) {
                continue
            }

            val projectStars = String.format("%,d", projectJSON.int("stargazers_count"))
            val projectUrl = projectJSON.string("html_url")

            val projectData = GithubProject(
                name = projectName!!,
                stars = projectStars,
                url = projectUrl!!
            )

            top10.add(projectData)
        }
        top10.sortByDescending { project -> project.stars.replace(",","").toInt() }
        return top10
    }

    private fun scrapMoreThan1000Stars(language: String): String {
        val doc = fetchMoreThan1000StarsData(language)
        val data = Klaxon().parseJsonObject(StringReader(doc))
        return String.format("%,d", data.int("total_count"))
    }

    private fun fetchMoreThan1000StarsData(language: String): String {
        val escapedLanguage = language.replace("+", "%2B")
        val urlStars = STARS_URL.replace("{language}", escapedLanguage)
        return getPageBodyAsString(urlStars)
    }

    private fun checkGithubApiLimits() {
        val url = "https://api.github.com/rate_limit"

        try {
            val doc = getPageBodyAsString(url)
            StatusLogger.logInfo(doc)
        } catch (e: IOException) {
            StatusLogger.logException("Checking api limits", e)
        }
    }

    private fun getPageBodyAsString(url: String): String {
        return Jsoup.connect(url)
            .header("Authorization", authToken)
            .ignoreContentType(true)
            .execute()
            .body()
    }
}

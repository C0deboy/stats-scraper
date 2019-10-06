package pl.jjp.statsscraper.versions

import org.jsoup.Jsoup
import pl.jjp.statsscraper.common.DataScraper
import pl.jjp.statsscraper.utils.StatusLogger
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern


const val WIKI_INFOBOX = "table.infobox tr"
const val VERSION_HEADER = "Stable release"
const val INDEX_OF_JAVA_TD_AT_PL_WIKI = 7
const val INDEX_OF_R_TD = 7

class LanguageVersionDataScraper(val languages: List<String>) : DataScraper {

    companion object {
        const val NAME = "LanguagesVersion"
    }

    override val name get() = NAME

    private var data = ConcurrentHashMap<String, VersionData>()
    private lateinit var currentLanguage: String

    override fun scrapData(): ConcurrentHashMap<String, VersionData> {
        StatusLogger.logCollecting("Languages version data")

        languages.stream().parallel()
            .forEach { lang -> data[lang.replace("+", "p")] = scrap(lang) }

        return data
    }

    private fun scrap(language: String): VersionData {
        currentLanguage = language

        lateinit var languageData: VersionData
        var commonUrl = "https://en.wikipedia.org/wiki/" + language + "_(programming_language)"

        val specificUrls = HashMap<String, String>()
        specificUrls["Java"] = "https://pl.wikipedia.org/wiki/Java"
        specificUrls["JavaScript"] = "https://en.wikipedia.org/wiki/JavaScript"
        specificUrls["Csharp"] = "https://en.wikipedia.org/wiki/C_Sharp_(programming_language)"
        specificUrls["C++"] = "https://en.wikipedia.org/wiki/C%2B%2B"

        if (language in specificUrls) {
            commonUrl = specificUrls.getValue(language)
        }

        try {
            val doc = Jsoup.connect(commonUrl).get()
            var wikiTable = doc.select(WIKI_INFOBOX)
            wikiTable = wikiTable.next()
            val th = wikiTable.select("th").eachText()
            val td = wikiTable.select("td").eachText()

            var index = th.indexOf(VERSION_HEADER)

            when (language) {
                "Java" -> index = INDEX_OF_JAVA_TD_AT_PL_WIKI
                "R" -> index += 1 // additional image row
            }

            val latestReleaseInfo = td[index]
            val releaseDate = getDateFromReleaseInfo(latestReleaseInfo)
            val version = getVersionFromReleaseInfo(latestReleaseInfo)

            languageData = VersionData(
                releaseInfo = latestReleaseInfo,
                releaseDate = releaseDate,
                version = version
            )

            StatusLogger.logSuccessFor(language)

        } catch (e: Exception) {
            StatusLogger.logException(currentLanguage, e)
        }

        return languageData
    }

    private fun getVersionFromReleaseInfo(latestReleaseInfo: String): String {
        val versionPattern = Pattern.compile("\\d+\\.\\d+(\\.\\d+)?")
        val versionMatcher = versionPattern.matcher(latestReleaseInfo)
        var version = ""

        if (versionMatcher.find()) {
            version = versionMatcher.group()
        } else {
            try {
                version =
                    latestReleaseInfo.split(" /".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
                        .replace("\\[.*]".toRegex(), "")
                StatusLogger.appendWarning("Not plain version, using: $version")
            } catch (e: ArrayIndexOutOfBoundsException) {
                StatusLogger.logError("Cannot retrieve version from release info.")
            }

        }
        return version
    }

    private fun getDateFromReleaseInfo(latestReleaseInfo: String): String {
        val datePattern = Pattern.compile("\\(\\d+-\\d+-?\\d*\\)")
        val matcher = datePattern.matcher(latestReleaseInfo)
        var releaseDate = ""

        if (matcher.find()) {
            releaseDate = matcher.group().replace("[()]".toRegex(), "")
        } else {
            val fullDatePattern = Pattern.compile("\\d+ \\w+.+\\d{4}")
            val fullDateMatcher = fullDatePattern.matcher(latestReleaseInfo)
            if (fullDateMatcher.find()) {
                releaseDate = fullDateMatcher.group()
            } else {
                StatusLogger.logErrorFor(currentLanguage, "Cannot retrieve date from release info.")
            }
        }

        try {
            val fullDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))
            return try {
                LocalDate.parse(releaseDate).format(fullDateFormatter)
            } catch (e: DateTimeParseException) {
                LocalDate.parse(releaseDate, fullDateFormatter).format(fullDateFormatter)
            }

        } catch (e: DateTimeParseException) {
            val yearMonth = YearMonth.parse(releaseDate, DateTimeFormatter.ofPattern("yyyy-MM"))
            var month = yearMonth.month.getDisplayName(TextStyle.FULL, Locale("pl"))
            month = when (month) {
                "listopada" -> "listopadzie"
                "lutego" -> "lutym"
                else -> month.substring(0, month.length - 1) + "u "
            }
            val date = "w " + month + yearMonth.year
            StatusLogger.appendWarning("Not full date, using: $date")
            return date
        }

    }
}

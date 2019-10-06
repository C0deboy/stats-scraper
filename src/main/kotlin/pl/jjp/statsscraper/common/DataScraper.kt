package pl.jjp.statsscraper.common

interface DataScraper {

    val name: String

    fun scrapData(languages: List<String>): Map<String, Data>
}

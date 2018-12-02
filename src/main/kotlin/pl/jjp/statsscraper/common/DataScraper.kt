package pl.jjp.statsscraper.common

interface DataScraper {

    val name: String

    fun scrapData(): Map<String, Data>

}

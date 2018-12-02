package pl.jjp.statsscraper.github

import pl.jjp.statsscraper.common.Data

data class GithubData(
    var projects: String,
    var moreThen1000Stars: String,
    var ranking: String = "",
    val top10: List<GithubProject>
) : Data

data class GithubProject(
    val name: String,
    val stars: String,
    val url: String
)
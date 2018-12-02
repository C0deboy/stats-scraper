package pl.jjp.statsscraper.stackoverflow

import pl.jjp.statsscraper.common.Data

data class StackOverflowData(

    val questions: String,
    var ranking: String = ""

) : Data
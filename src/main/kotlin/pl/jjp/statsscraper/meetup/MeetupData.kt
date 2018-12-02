package pl.jjp.statsscraper.meetup

import pl.jjp.statsscraper.common.Data

data class MeetupData(
    var local: Ranking,
    var global: Ranking
) : Data

data class Ranking(
    var meetups: String,
    var members: String,
    var ranking: String = ""
)
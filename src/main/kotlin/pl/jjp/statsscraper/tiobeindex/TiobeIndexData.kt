package pl.jjp.statsscraper.tiobeindex

import pl.jjp.statsscraper.common.Data

data class TiobeIndexData(
    val currentPosition: String,
    val lastYearPosition: String
) : Data

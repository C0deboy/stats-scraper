package pl.jjp.statsscraper.versions

import pl.jjp.statsscraper.common.Data

data class VersionData (

    val releaseDate: String,
    val releaseInfo: String,
    val version: String

) : Data

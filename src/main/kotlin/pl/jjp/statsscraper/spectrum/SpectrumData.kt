package pl.jjp.statsscraper.spectrum

import pl.jjp.statsscraper.common.Data

data class SpectrumData(
    val currentPosition: String,
    val lastYearPosition: String
) : Data
package pl.jjp.statsscraper.utils

fun <R> afterDelay(time: Long, action: () -> R) : R {
    Thread.sleep(time)
    return action()
}

fun escapeLanguage(language: String): String {
    return language.replace("#", "sharp")
}

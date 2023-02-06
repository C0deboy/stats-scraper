package pl.jjp.statsscraper.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import kotlin.system.exitProcess

object StatusLogger {
    val LOG: Logger = LoggerFactory.getLogger("Progress logger")

    fun logInfo(message: String) {
        LOG.info(message)
    }

    fun gap() {
        println()
    }

    fun logCollecting(what: String) {
        println()
        MDC.put("collecting", what.toUpperCase())
        LOG.info("COLLECTING")
        MDC.clear()
    }

    fun logInfoTodo(language: String) {
        MDC.put("status", "TODO   ")
        LOG.info(language)
        MDC.clear()
    }

    fun logSuccessFor(language: String) {
        MDC.put("status", "SUCCESS")
        LOG.info(language)
        MDC.clear()
    }

    fun logSkipped(language: String) {
        MDC.put("status", "SKIPPED")
        LOG.info(language)
        MDC.clear()
    }

    fun logSkipped(language: String, cause: String) {
        MDC.put("status", "SKIPPED")
        MDC.put("cause", cause)
        LOG.info(language)
        MDC.clear()
    }

    fun logChecking(language: String) {
        println()
        MDC.put("collecting", language.toUpperCase())
        LOG.info("CHECKING")
        MDC.clear()
    }

    fun logSuccess(data: String) {
        MDC.put("status", "SUCCESS")
        LOG.info(data)
        MDC.clear()
    }

    fun logError(data: String) {
        MDC.put("error", " FAILURE")
        LOG.info(data)
        MDC.clear()
        exitProcess(1)
    }

    fun logErrorFor(data: String, cause: String, exit: Boolean = true) {
        MDC.put("error", " FAILURE")
        MDC.put("cause", cause)
        LOG.info(data)
        MDC.clear()

        if (exit)
            exitProcess(1)
    }

    fun logException(message: String, e: Exception) {
        LOG.error(message, e)
        exitProcess(1)
    }

    fun appendWarning(cause: String) {
        var causes = cause
        if (MDC.get("cause") != null) {
            causes = "${MDC.get("cause")} | $cause"
        }
        MDC.put("cause", causes)
    }
}

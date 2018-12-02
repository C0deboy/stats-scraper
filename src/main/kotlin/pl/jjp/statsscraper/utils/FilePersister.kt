package pl.jjp.statsscraper.utils

import com.beust.klaxon.Klaxon
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths

object FilePersister {

    fun saveToFile(text: String, filename: String) {
        try {
            BufferedWriter(
                OutputStreamWriter(
                    FileOutputStream(filename), "UTF-8"
                )
            ).use { out ->
                out.write(text)
                StatusLogger.gap()
                StatusLogger.logInfo("File $filename saved.")
            }
        } catch (e: IOException) {
            StatusLogger.logException("Saving file $filename", e)
        }

    }

    fun saveStatisticsAndKeepOld(completeStatistics: String, filename: String) {
        val fileToMovePath = Paths.get(filename)
        if (Files.notExists(fileToMovePath)) {
            saveToFile(completeStatistics, filename)
        } else {
            try {
                Files.newBufferedReader(fileToMovePath).use { reader ->
                    val oldStatistics = Klaxon().parseJsonObject(reader)
                    val oldStatisticDate = oldStatistics.string("date")

                    var newFileName = appendToFileName(filename, oldStatisticDate!!)
                    var targetPath = Paths.get(newFileName)
                    while (Files.exists(targetPath)) {
                        newFileName = appendToFileName(newFileName, "OLD")
                        targetPath = Paths.get(newFileName)
                    }
                    Files.move(fileToMovePath, targetPath)
                    StatusLogger.logInfo("Old statistics file renamed to: $newFileName")
                    saveToFile(completeStatistics, filename)
                }
            } catch (e: IOException) {
                StatusLogger.logException("Renaming file $filename", e)
            }

        }
    }

    private fun appendToFileName(filename: String, oldStatisticDate: String): String {
        val fileData = filename.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val name = fileData[0]
        val extension = fileData[1]
        return name + "_" + oldStatisticDate + "." + extension
    }
}

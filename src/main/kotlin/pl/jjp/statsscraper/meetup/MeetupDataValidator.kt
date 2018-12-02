package pl.jjp.statsscraper.meetup

import pl.jjp.statsscraper.common.DataValidator
import pl.jjp.statsscraper.utils.StatusLogger

object MeetupDataValidator {

    fun validate(language: String, languageData: MeetupData) {

        if (MeetupDataScraper.excluded.contains(language)) {
            return
        }

        val validator = DataValidator(language)

        try {
            val localRanking = languageData.local


            validator.validateNumber(localRanking::meetups, 4)
            validator.validateNumber(localRanking::members, 1400)
//            validator.validateNumber(localRanking::ranking, 1)

            val globalRanking = languageData.global

            validator.validateNumber(globalRanking::meetups, 150)
            validator.validateNumber(globalRanking::members, 60000)

            StatusLogger.logSuccessFor(language)
        } catch (e: Exception) {
            StatusLogger.logException(language, e)
        }

    }


}
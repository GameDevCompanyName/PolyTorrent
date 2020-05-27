package ru.gdcn.polytorrent

import kotlin.math.roundToInt

object ProgressManager {

    var startTimeMillis: Long? = null

    @JvmStatic
    fun progress(progress: Double) {
        val timePassedSeconds = (System.currentTimeMillis() - startTimeMillis!!) / 1000
        val totalTimeSeconds = ((timePassedSeconds / progress) * 100).toLong()
        val timeLeftTotalSeconds = totalTimeSeconds - timePassedSeconds
        val timeLeftHours = timeLeftTotalSeconds / 3600
        val timeLeftMinutes = (timeLeftTotalSeconds % 3600) / 60
        val timeLeftSeconds = timeLeftTotalSeconds - timeLeftHours * 3600 - timeLeftMinutes * 60
        println("Download progress: " + ((progress * 100).roundToInt() / 100.0) + "%")
        println("Time left: $timeLeftHours:$timeLeftMinutes:$timeLeftSeconds")
    }

}
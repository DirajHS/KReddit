package com.diraj.kreddit.network.models

import com.squareup.moshi.JsonClass
import org.joda.time.Period

@JsonClass(generateAdapter = true)
data class CommentsData(
    val author: String,
    val score: Int,
    val created_utc: Int,
    val body: String,
    val depth: Int
) {
    fun getAgePeriod(): Period {
        val nowInSecs = System.currentTimeMillis() / 1000
        return Period.seconds((nowInSecs - created_utc).toInt())
    }
}
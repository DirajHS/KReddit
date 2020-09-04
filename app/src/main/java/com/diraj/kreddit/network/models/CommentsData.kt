package com.diraj.kreddit.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentsData(
    val id : String?,
    val author: String?,
    val score: Int?,
    val created_utc: Long?,
    val body: String?,
    var children : List<CommentsData>?
): Parcelable
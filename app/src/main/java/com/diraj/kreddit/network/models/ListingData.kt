package com.diraj.kreddit.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListingData(
    val after: String?,
    val before: String?,
    val children: List<RedditObject>
) : Parcelable
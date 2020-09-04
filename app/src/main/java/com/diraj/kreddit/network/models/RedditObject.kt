package com.diraj.kreddit.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RedditObject(
    val kind: String,
    val data: RedditObjectData
) : Parcelable
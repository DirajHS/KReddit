package com.diraj.kreddit.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class RedditObject(
    val kind: String,
    val data: RedditObjectData
) : Parcelable
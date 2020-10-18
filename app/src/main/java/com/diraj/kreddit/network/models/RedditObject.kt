package com.diraj.kreddit.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class RedditObject(
    val kind: String,
    val data: RedditObjectData
) : Parcelable
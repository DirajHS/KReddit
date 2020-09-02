package com.diraj.kreddit.network.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class RedditObjectPreview(
    val images: List<PreviewImageObject>?
) : Parcelable
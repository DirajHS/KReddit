package com.diraj.kreddit.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
class RedditObjectPreview(
    val images: List<PreviewImageObject>? = null
) : Parcelable

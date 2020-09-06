package com.diraj.kreddit.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RedditObjectPreview(
    val images: List<PreviewImageObject>?
) : Parcelable

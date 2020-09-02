package com.diraj.kreddit.network.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PreviewImage(
    val url: String?,
    val width: Int,
    val height: Int
) : Parcelable
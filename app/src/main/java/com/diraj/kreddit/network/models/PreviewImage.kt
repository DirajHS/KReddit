package com.diraj.kreddit.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PreviewImage(
    val url: String?,
    val width: Int,
    val height: Int
) : Parcelable
package com.diraj.kreddit.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
class PreviewImageObject(
    val source: PreviewImage? = null,
    val resolutions: List<Resolutions>? = null
) : Parcelable
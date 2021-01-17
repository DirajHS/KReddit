package com.diraj.kreddit.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
class PreviewImageObject(
    val source: PreviewImage? = null,
    val resolutions: List<Resolutions>? = null
) : Parcelable
package com.diraj.kreddit.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BaseModel(
    val kind: String,
    val data: ListingData
) : Parcelable

package com.diraj.kreddit.network.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BaseModel(
    val kind: String,
    val data: ListingData
) : Parcelable
